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

import { HttpSetup } from '../../../../src/core/public';
import { NODE_API } from '../../common';
import {
  ChannelItemType,
  NotificationItem,
  RecipientGroupItemType,
  SenderItemType,
} from '../../models/interfaces';
import { CHANNEL_TYPE } from '../utils/constants';
import { MOCK_GET_HISTOGRAM, MOCK_NOTIFICATIONS } from './mockData';
import {
  configListToChannels,
  configListToRecipientGroups,
  configListToSenders,
  configToChannel,
  configToRecipientGroup,
  configToSender,
} from './utils/helper';

export interface GetNotificationsResponse {
  totalNotifications: number;
  notifications: NotificationItem[];
}

export default class NotificationService {
  httpClient: HttpSetup;

  constructor(httpClient: HttpSetup) {
    this.httpClient = httpClient;
  }

  getNotifications = async (queryObject: object): Promise<any> => {
    return MOCK_NOTIFICATIONS;
  };

  getHistogram = async (queryObject: object) => {
    return MOCK_GET_HISTOGRAM();
  };

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

  getChannels = async (
    queryObject: object = { config_type: Object.keys(CHANNEL_TYPE) }
  ): Promise<{ items: ChannelItemType[]; total: number }> => {
    const response = await this.httpClient.get(NODE_API.GET_CONFIGS, {
      query: queryObject,
    });
    return {
      items: configListToChannels(response.config_list),
      total: response.total_hits || 0,
    };
  };

  getChannel = async (id: string): Promise<ChannelItemType> => {
    const response = await this.httpClient.get(`${NODE_API.GET_CONFIG}/${id}`);
    return configToChannel(response.config_list[0]);
  };

  getSenders = async (
    queryObject: object = { config_type: 'smtp_account' }
  ): Promise<{ items: SenderItemType[]; total: number }> => {
    const response = await this.httpClient.get(NODE_API.GET_CONFIGS, {
      query: queryObject,
    });
    return {
      items: configListToSenders(response.config_list),
      total: response.total_hits || 0,
    };
  };

  getSender = async (id: string): Promise<SenderItemType> => {
    const response = await this.httpClient.get(`${NODE_API.GET_CONFIG}/${id}`);
    return configToSender(response.config_list[0]);
  };

  getRecipientGroups = async (
    queryObject: object = { config_type: 'email_group' }
  ): Promise<{ items: RecipientGroupItemType[]; total: number }> => {
    const response = await this.httpClient.get(NODE_API.GET_CONFIGS, {
      query: queryObject,
    });
    return {
      items: configListToRecipientGroups(response.config_list),
      total: response.total_hits || 0,
    };
  };

  getRecipientGroup = async (id: string): Promise<RecipientGroupItemType> => {
    const response = await this.httpClient.get(`${NODE_API.GET_CONFIG}/${id}`);
    return configToRecipientGroup(response.config_list[0]);
  };
}
