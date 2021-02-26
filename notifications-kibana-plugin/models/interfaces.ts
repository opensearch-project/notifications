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

export interface NotificationItem {
  title: string;
  channel: ChannelOverview; // those will be prepared by kibana server. UI model vs Backend Model
  referenceId: string; // TODO: this should probably be sourcelink, which is created by kibana server using source + ref_id and a dictionary of plugin url
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
