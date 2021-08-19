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

import {
  ChannelItemType,
  NotificationItem,
  RecipientGroupItemType,
  SenderItemType,
  SESSenderItemType,
} from '../../../models/interfaces';

export const configToChannel = (config: any): ChannelItemType => {
  return {
    ...config.config,
    config_id: config.config_id,
    created_time_ms: config.created_time_ms,
    last_updated_time_ms: config.last_updated_time_ms,
  };
};

export const configListToChannels = (configs: any[]): ChannelItemType[] => {
  return configs?.map((config) => configToChannel(config)) || [];
};

export const configToSender = (config: any): SenderItemType => {
  return {
    name: config.config.name,
    description: config.config.description,
    config_id: config.config_id,
    created_time_ms: config.created_time_ms,
    last_updated_time_ms: config.last_updated_time_ms,
    ...(config.config.config_type === 'smtp_account'
      ? {
          from_address: config.config.smtp_account.from_address,
          smtp_account: config.config.smtp_account,
        }
      : {
          from_address: config.config.ses_account.from_address,
          ses_account: config.config.ses_account,
        }),
  };
};

export const configListToSenders = (configs: any[]): SenderItemType[] => {
  return configs?.map((config) => configToSender(config)) || [];
};

export const configToSESSender = (config: any): SESSenderItemType => {
  return {
    name: config.config.name,
    description: config.config.description,
    config_id: config.config_id,
    created_time_ms: config.created_time_ms,
    last_updated_time_ms: config.last_updated_time_ms,
    ses_account: config.config.ses_account,
  };
};

export const configListToSESSenders = (configs: any[]): SESSenderItemType[] => {
  return configs?.map((config) => configToSESSender(config)) || [];
};

export const configToRecipientGroup = (config: any): RecipientGroupItemType => {
  return {
    name: config.config.name,
    description: config.config.description,
    config_id: config.config_id,
    created_time_ms: config.created_time_ms,
    last_updated_time_ms: config.last_updated_time_ms,
    email_group: config.config.email_group,
  };
};

export const configListToRecipientGroups = (
  configs: any[]
): RecipientGroupItemType[] => {
  return configs?.map((config) => configToRecipientGroup(config)) || [];
};

export const isStatusCodeSuccess = (statusCode: string) => {
  if (!statusCode) return true;
  return /^2\d\d/.test(statusCode);
};

export const eventToNotification = (event: any): NotificationItem => {
  const success = event.event.status_list.every((status: any) =>
    isStatusCodeSuccess(status.delivery_status.status_code)
  );
  return {
    event_source: event.event.event_source,
    status_list: event.event.status_list,
    event_id: event.event_id,
    created_time_ms: event.created_time_ms,
    last_updated_time_ms: event.last_updated_time_ms,
    success,
  };
};

export const eventListToNotifications = (events: any[]): NotificationItem[] => {
  return events?.map((event) => eventToNotification(event)) || [];
};
