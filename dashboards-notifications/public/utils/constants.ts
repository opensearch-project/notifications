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
 
export const DOCUMENTATION_LINK = '';

export const ROUTES = Object.freeze({
  // notification
  NOTIFICATIONS: '/notifications',
  CHANNELS: '/channels',
  CHANNEL_DETAILS: '/channel-details',
  CREATE_CHANNEL: '/create-channel',
  EDIT_CHANNEL: '/edit-channel',
  EMAIL_GROUPS: '/email-groups',
  CREATE_SENDER: '/create-sender',
  EDIT_SENDER: '/edit-sender',
  CREATE_RECIPIENT_GROUP: '/create-recipient-group',
  EDIT_RECIPIENT_GROUP: '/edit-recipient-group',
});

export const BREADCRUMBS = Object.freeze({
  NOTIFICATIONS: { text: 'Notifications', href: '#/' },
  DASHBOARD: { text: 'Dashboard', href: `#${ROUTES.NOTIFICATIONS}` },
  CHANNELS: { text: 'Channels', href: `#${ROUTES.CHANNELS}` },
  CHANNEL_DETAILS: { text: 'Channels', href: `#${ROUTES.CHANNEL_DETAILS}` },
  CREATE_CHANNEL: { text: 'Create channel', href: `#${ROUTES.CREATE_CHANNEL}` },
  EDIT_CHANNEL: { text: 'Edit channel' },
  EMAIL_GROUPS: { text: 'Email groups', href: `#${ROUTES.EMAIL_GROUPS}` },
  CREATE_SENDER: { text: 'Create sender', href: `#${ROUTES.CREATE_SENDER}` },
  EDIT_SENDER: { text: 'Edit sender' },
  CREATE_RECIPIENT_GROUP: {
    text: 'Create recipient group',
    href: `#${ROUTES.CREATE_RECIPIENT_GROUP}`,
  },
  EDIT_RECIPIENT_GROUP: { text: 'Edit recipient group' },
});

export const NOTIFICATION_STATUS = Object.freeze({
  SENT: 'Sent',
  ERROR: 'Error',
});

export const NOTIFICATION_SOURCE = Object.freeze({
  ALERTING: 'Alerting',
  ISM: 'ISM',
  REPORTING: 'Reporting',
});

export const CHANNEL_TYPE = Object.freeze({
  SLACK: 'Slack',
  EMAIL: 'Email',
  CHIME: 'Chime',
  CUSTOM_WEBHOOK: 'Custom webhook',
  SES: 'Amazon SES',
  SNS: 'Amazon SNS',
});

export const CUSTOM_WEBHOOK_ENDPOINT_TYPE = Object.freeze({
  WEBHOOK_URL: 'Webhook URL',
  CUSTOM_URL: 'Custom attributes URL with HTTPS',
});

export const HISTOGRAM_TYPE = Object.freeze({
  CHANNEL_TYPE: 'Channel type',
  SOURCE: 'Notification source',
  STATUS: 'Notification status',
  SEVERITY: 'Severity',
  TOP_10_CHANNELS: 'Top 10 channels',
});
