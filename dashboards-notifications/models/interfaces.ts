/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { Direction } from '@elastic/eui';
import { WebhookMethodType } from '../public/pages/Channels/types';
import {
  CHANNEL_TYPE,
  ENCRYPTION_TYPE,
} from '../public/utils/constants';

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
