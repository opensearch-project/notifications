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

import { Direction } from '@elastic/eui';
import { WebhookMethodType } from '../public/pages/Channels/types';
import {
  CHANNEL_TYPE,
  ENCRYPTION_TYPE,
  NOTIFICATION_SOURCE,
  SEVERITY_TYPE,
} from '../public/utils/constants';

export interface NotificationItem {
  event_id: string;
  created_time_ms: number;
  last_updated_time_ms: number;
  tenant?: string;
  event_source: {
    title: string;
    reference_id: string;
    feature: keyof typeof NOTIFICATION_SOURCE;
    severity: keyof typeof SEVERITY_TYPE;
    tags?: string[];
  };
  status_list: ChannelStatus[]; // could be multiple channels in a notification item
  success: boolean; // calculated in the frontend based on status_list
}

export interface ChannelStatus {
  config_id: string;
  config_name: string;
  config_type: keyof typeof CHANNEL_TYPE;
  email_recipient_status?: {
    recipient: string;
    delivery_status: DeliveryStatus;
  }[];
  delivery_status: DeliveryStatus;
}

interface DeliveryStatus {
  status_code: string;
  status_text: string;
}

export type SenderType = 'smtp_account' | 'ses_account';

export interface ChannelItemType extends ConfigType {
  config_type: keyof typeof CHANNEL_TYPE;
  feature_list: Array<keyof typeof NOTIFICATION_SOURCE>;
  is_enabled: boolean; // active or muted
  slack?: {
    url: string;
  };
  chime?: {
    url: string;
  };
  webhook?: {
    url: string;
    header_params: object;
    method: WebhookMethodType;
  };
  email?: {
    email_account_id: string;
    recipient_list: string[]; // custom email addresses
    email_group_id_list: string[];
    // optional fields for displaying or editing email channel, needs more requests
    sender_type?: SenderType;
    email_account_name?: string;
    email_group_id_map?: {
      [id: string]: string;
    };
    invalid_ids?: string[]; // invalid sender and/or recipient group ids, possible deleted
  };
  sns?: {
    topic_arn: string;
    role_arn?: string;
  }
}

interface ConfigType {
  config_id: string;
  name: string;
  description?: string;
  created_time_ms: number;
  last_updated_time_ms: number;
}

export interface SenderItemType extends ConfigType {
  smtp_account: {
    from_address: string; // outbound email address
    host: string;
    port: string;
    method: keyof typeof ENCRYPTION_TYPE;
  };
}

export interface SESSenderItemType extends ConfigType {
  ses_account: {
    from_address: string; // outbound email address
    region: string;
    role_arn?: string;
  };
}

export interface RecipientGroupItemType extends ConfigType {
  email_group: {
    recipient_list: { [recipient: string]: string }[];
  };
}

export interface TableState<T> {
  total: number;
  from: number;
  size: number;
  search: string;
  sortField: any; // keyof T
  sortDirection: Direction;
  selectedItems: T[];
  items: T[];
  loading: boolean;
}
