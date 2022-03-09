/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

export interface ListItemType {
  title: NonNullable<React.ReactNode>;
  description: NonNullable<React.ReactNode>;
}

export interface HeaderItemType {
  key: string;
  value: string;
}

export type WebhookMethodType = 'POST' | 'PUT' | 'PATCH';

export type WebhookHttpType = 'HTTP' | 'HTTPS';

export interface ChannelFiltersType {
  state?: string;
  type?: string[];
}
