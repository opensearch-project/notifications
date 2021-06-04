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
  RecipientGroupItemType,
  SenderItemType,
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
    smtp_account: config.config.smtp_account,
  };
};

export const configListToSenders = (configs: any[]): SenderItemType[] => {
  return configs?.map((config) => configToSender(config)) || [];
};

export const configToRecipientGroup = (
  config: any
): RecipientGroupItemType => {
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
