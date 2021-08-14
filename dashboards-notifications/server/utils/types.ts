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

import { NOTIFICATION_SOURCE } from "../../public/utils/constants";

export type ConfigType = 
  | 'slack' 
  | 'chime' 
  | 'email' 
  | 'custom_webhook' 
  | 'ses' 
  | 'sns'
  | 'smtp_account'
  | 'email_group'

export type PluginAvailabilityType = keyof typeof NOTIFICATION_SOURCE

export type EntityType = 'config' | 'event' | 'feature' | 'send_test_message';

export type UsageActionType = 'send'

export type CountersNameType =
  | 'count'
  | 'system_error'
  | 'user_error'
  | 'total';

export type ActionType = 
  | 'create'
  | 'delete'
  | 'info'
  | 'list'
  | 'update';

export type CountersType = ActionCountersType & UsageCountersType & PluginAvailabilityCountersType;

type ActionCountersType = {
  [entity in EntityType]: {
    [action in ActionType]?: {
      [counter in CountersNameType]?: number;
    };
  };
};

type UsageCountersType = {
  [channel in ConfigType]: {
    [counter in CountersNameType]?: number;
  };
};

type PluginAvailabilityCountersType = {
  [plugin in PluginAvailabilityType]: {
    [counter in CountersNameType]?: number;
  };
};