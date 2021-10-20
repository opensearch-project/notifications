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

import { EuiComboBoxOptionOption } from '@elastic/eui';
import _ from 'lodash';
import { ChannelItemType, SenderType } from '../../../../models/interfaces';
import { CUSTOM_WEBHOOK_ENDPOINT_TYPE } from '../../../utils/constants';
import {
  HeaderItemType,
  WebhookHttpType,
  WebhookMethodType,
} from '../../Channels/types';

export const constructWebhookObject = (
  webhookTypeIdSelected: keyof typeof CUSTOM_WEBHOOK_ENDPOINT_TYPE,
  webhookURL: string,
  customURLType: WebhookHttpType,
  customURLHost: string,
  customURLPort: string,
  customURLPath: string,
  webhookMethod: WebhookMethodType,
  webhookParams: HeaderItemType[],
  webhookHeaders: HeaderItemType[]
) => {
  let url: string;
  if (webhookTypeIdSelected === 'WEBHOOK_URL') {
    url = webhookURL;
  } else {
    url = `${customURLType.toLowerCase()}://${customURLHost.replace(
      /^https?:\/\//,
      ''
    )}`;
    if (customURLPort) url += `:${customURLPort}`;
    if (customURLPath) url += `/${customURLPath.replace(/^\//, '')}`;
    if (webhookParams.length > 0) {
      const params = new URLSearchParams(
        webhookParams
          .filter(({ key, value }) => key)
          .map(({ key, value }) => [key, value])
      );
      url += '?' + params.toString();
    }
  }
  const header_params = webhookHeaders
    .filter(({ key, value }) => key)
    .reduce((prev, curr) => ({ ...prev, [curr.key]: curr.value }), {});
  return { url, header_params, method: webhookMethod };
};

export const deconstructWebhookObject = (
  webhook: NonNullable<ChannelItemType['webhook']>
): {
  webhookURL: string;
  customURLType: WebhookHttpType;
  customURLHost: string;
  customURLPort: string;
  customURLPath: string;
  webhookMethod: WebhookMethodType;
  webhookParams: HeaderItemType[];
  webhookHeaders: HeaderItemType[];
} => {
  try {
    const url = new URL(webhook.url);
    const customURLType = url.protocol
      .replace(':', '')
      .toUpperCase() as WebhookHttpType;
    const customURLHost = url.hostname;
    const customURLPort = url.port;
    const customURLPath = url.pathname.replace(/^\//, '');
    const webhookParams: HeaderItemType[] = [];
    url.searchParams.forEach((value, key) =>
      webhookParams.push({ key, value })
    );
    const webhookHeaders = Object.entries(
      webhook.header_params
    ).map(([key, value]) => ({ key, value }));
    return {
      webhookURL: webhook.url,
      customURLType,
      customURLHost,
      customURLPort,
      customURLPath,
      webhookMethod: webhook.method as WebhookMethodType,
      webhookParams,
      webhookHeaders,
    };
  } catch (error) {
    console.error('Error parsing url:', error);
    return {
      webhookURL: webhook.url,
      customURLType: 'HTTPS',
      customURLHost: '',
      customURLPort: '',
      customURLPath: '',
      webhookMethod: 'POST',
      webhookParams: [],
      webhookHeaders: [],
    };
  }
};

export const constructEmailObject = (
  selectedSenderOptions: Array<EuiComboBoxOptionOption<string>>,
  selectedRecipientGroupOptions: Array<EuiComboBoxOptionOption<string>>
) => {
  const customEmailsList = [];
  const recipientGroupIds = [];
  for (let i = 0; i < selectedRecipientGroupOptions.length; i++) {
    const group = selectedRecipientGroupOptions[i];
    if (group.value) {
      recipientGroupIds.push(group.value);
    } else {
      customEmailsList.push(group.label);
    }
  }
  return {
    email_account_id: selectedSenderOptions[0].value,
    recipient_list: customEmailsList,
    email_group_id_list: recipientGroupIds,
  };
};

export const deconstructEmailObject = (
  email: NonNullable<ChannelItemType['email']>
): {
  senderType: SenderType;
  selectedSenderOptions: Array<EuiComboBoxOptionOption<string>>;
  selectedRecipientGroupOptions: Array<EuiComboBoxOptionOption<string>>;
} => {
  const selectedSenderOptions = [
    {
      label: email.email_account_name || '-',
      value: email.email_account_id,
    },
  ];
  const selectedRecipientGroupOptions: Array<EuiComboBoxOptionOption<
    string
  >> = [
    ...email.email_group_id_list.map((groupId) => ({
      label: _.get(email.email_group_id_map, groupId, '-'),
      value: groupId,
    })),
    ...email.recipient_list.map((address) => ({ label: address })),
  ];
  return {
    senderType: email.sender_type!,
    selectedSenderOptions,
    selectedRecipientGroupOptions,
  };
};
