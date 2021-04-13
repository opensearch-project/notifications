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

export interface NotificationItem {
  id: string;
  title: string;
  referenceId: string; // TODO: this should probably be sourcelink, which is created by OpenSearch Dashboards server using source + ref_id and a dictionary of plugin url
  source: string;
  severity: string;
  tags?: string[];
  lastUpdatedTime: number;
  status: string;
  statusList: ChannelStatus[]; // could be multiple channels in a notification item
}

export interface ChannelStatus {
  configId: string;
  configName: string;
  configType: string;
  emailRecipientStatus?: {
    recipient: string;
    deliveryStatus: DeliveryStatus;
  }[];
  deliveryStatus: DeliveryStatus;
}

export interface DeliveryStatus {
  statusCode: string;
  statusText: string;
}

export interface ChannelItemType {
  id: string;
  name: string;
  enabled: boolean; // active or muted
  type: string;
  allowedFeatures: string[];
  lastUpdatedTime: number;
  destination: {
    [type: string]: object;
  };
  description?: string;
}

export interface SenderItemType {
  id: string;
  name: string;
  from: string; // outbound email address
  host: string;
  port: string;
  method: ENCRYPTION_METHOD;
}

export interface RecipientGroupItemType {
  id: string;
  name: string;
  email: Array<{ email: string }>;
  description?: string;
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

export type ENCRYPTION_METHOD = 'SSL' | 'TSL';
