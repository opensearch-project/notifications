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

export interface NotificationItem {
  title: string;
  channel: ChannelOverview; // those will be prepared by OpenSearch Dashboards server. UI model vs Backend Model
  referenceId: string; // TODO: this should probably be sourcelink, which is created by OpenSearch Dashboards server using source + ref_id and a dictionary of plugin url
  source: string;
  severity: string;
  status: { overview: string; detail: NotificationStatusDetail[] };
  sentTime: number;
  lastUpdatedTime: number;
}

export interface NotificationStatusDetail {
  recipient: string; // if email, this will be email address.
  statusCode: number;
  statusText: string;
}

export interface ChannelOverview {
  id: string;
  name: string;
  type: string;
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
