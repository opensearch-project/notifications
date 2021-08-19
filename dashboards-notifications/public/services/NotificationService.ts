/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import { SortDirection } from '@elastic/eui';
import _ from 'lodash';
import { HttpFetchQuery, HttpSetup } from '../../../../src/core/public';
import { NODE_API } from '../../common';
import {
  ChannelItemType,
  RecipientGroupItemType,
  SenderItemType,
  SESSenderItemType,
} from '../../models/interfaces';
import { CHANNEL_TYPE } from '../utils/constants';
import {
  configListToChannels,
  configListToRecipientGroups,
  configListToSenders,
  configListToSESSenders,
  configToChannel,
  configToRecipientGroup,
  configToSender,
  configToSESSender,
} from './utils/helper';

interface ConfigsResponse {
  total_hits: number;
  config_list: any[];
}

export default class NotificationService {
  httpClient: HttpSetup;

  constructor(httpClient: HttpSetup) {
    this.httpClient = httpClient;
  }

  createConfig = async (config: any) => {
    const response = await this.httpClient.post(NODE_API.CREATE_CONFIG, {
      body: JSON.stringify({ config: config }),
    });
    return response;
  };

  updateConfig = async (id: string, config: any) => {
    const response = await this.httpClient.put(
      `${NODE_API.UPDATE_CONFIG}/${id}`,
      {
        body: JSON.stringify({ config }),
      }
    );
    return response;
  };

  deleteConfigs = async (ids: string[]) => {
    const response = await this.httpClient.delete(NODE_API.DELETE_CONFIGS, {
      query: {
        config_id_list: ids,
      },
    });
    return response;
  };

  getConfigs = async (queryObject: HttpFetchQuery) => {
    return this.httpClient.get<ConfigsResponse>(NODE_API.GET_CONFIGS, {
      query: queryObject,
    });
  };

  getConfig = async (id: string) => {
    return this.httpClient.get<ConfigsResponse>(`${NODE_API.GET_CONFIG}/${id}`);
  };

  getChannels = async (
    queryObject: HttpFetchQuery // config_type: Object.keys(CHANNEL_TYPE)
  ): Promise<{ items: ChannelItemType[]; total: number }> => {
    const response = await this.getConfigs(queryObject);
    return {
      items: configListToChannels(response.config_list),
      total: response.total_hits || 0,
    };
  };

  getChannel = async (id: string): Promise<ChannelItemType> => {
    const response = await this.getConfig(id);
    return configToChannel(response.config_list[0]);
  };

  getEmailConfigDetails = async (
    channel: ChannelItemType
  ): Promise<ChannelItemType> => {
    if (!channel.email) return channel;

    const idMap: { [id: string]: string } = {};
    const ids = [
      channel.email.email_account_id,
      ...channel.email.email_group_id_list,
    ];
    let senderType: 'smtp' | 'ses';
    await this.getConfigs({
      from_index: 0,
      max_items: ids.length,
      config_id_list: ids,
      sort_order: SortDirection.ASC,
      sort_field: 'name',
      config_type: ['smtp_account', 'ses_account', 'email_group'],
    }).then((response) => {
      response.config_list.map((config) => {
        if (config.config_id === channel.email?.email_account_id)
          senderType = config.config.config_type === 'ses_account' ? 'ses' : 'smtp';
        idMap[config.config_id] = config.config.name;
      });
    });

    channel.email.sender_type = senderType!;
    channel.email.email_account_name = idMap[channel.email.email_account_id];
    channel.email.email_group_id_map = {};
    channel.email.email_group_id_list.map(
      (id) => (channel.email!.email_group_id_map![id] = idMap[id])
    );

    return channel;
  };

  getSenders = async (
    queryObject: HttpFetchQuery // config_type: 'smtp_account'
  ): Promise<{ items: SenderItemType[]; total: number }> => {
    const response = await this.getConfigs(queryObject);
    return {
      items: configListToSenders(response.config_list),
      total: response.total_hits || 0,
    };
  };

  getSender = async (id: string): Promise<SenderItemType> => {
    const response = await this.getConfig(id);
    return configToSender(response.config_list[0]);
  };

  getSESSenders = async (
    queryObject: HttpFetchQuery // config_type: 'ses_account'
  ): Promise<{ items: SESSenderItemType[]; total: number }> => {
    const response = await this.getConfigs(queryObject);
    return {
      items: configListToSESSenders(response.config_list),
      total: response.total_hits || 0,
    };
  };

  getSESSender = async (id: string): Promise<SESSenderItemType> => {
    const response = await this.getConfig(id);
    return configToSESSender(response.config_list[0]);
  };

  getRecipientGroups = async (
    queryObject: HttpFetchQuery // config_type: 'email_group'
  ): Promise<{ items: RecipientGroupItemType[]; total: number }> => {
    const response = await this.getConfigs(queryObject);
    return {
      items: configListToRecipientGroups(response.config_list),
      total: response.total_hits || 0,
    };
  };

  getRecipientGroup = async (id: string): Promise<RecipientGroupItemType> => {
    const response = await this.getConfig(id);
    return configToRecipientGroup(response.config_list[0]);
  };

  getServerFeatures = async () => {
    try {
      const response = await this.httpClient.get(
        NODE_API.GET_AVAILABLE_FEATURES
      );
      const config_type_list = response.config_type_list as Array<
        keyof typeof CHANNEL_TYPE
      >;
      const channelTypes: Partial<typeof CHANNEL_TYPE> = {};
      for (let i = 0; i < config_type_list.length; i++) {
        const channel = config_type_list[i];
        if (!CHANNEL_TYPE[channel]) continue;
        channelTypes[channel] = CHANNEL_TYPE[channel];
      }
      return {
        availableChannels: channelTypes,
        availableConfigTypes: config_type_list as string[],
        tooltipSupport:
          _.get(response, [
            'plugin_features',
            'opensearch.notifications.spi.tooltip_support',
          ]) === 'true',
      };
    } catch (error) {
      console.error('error fetching available features', error);
      return null;
    }
  };
}
