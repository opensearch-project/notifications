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

export type ConfigType = 'slack' | 'chime' | 'email' | 'webhook' | 'ses' | 'sns'

export type PluginAvailabilityType = 'reporting' | 'alerting' | 'index_state_management'

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

export type CountersType = ActionCountersType & UsageCountersType;

type ActionCountersType = {
  [entity in EntityType]: {
    [action in ActionType]?: {
      [counter in CountersNameType]?: number;
    };
  };
};

type UsageCountersType = {
  [channel in ConfigType]: {
    [action in UsageActionType]: {
      [counter in CountersNameType]?: number;
    };
  };
};