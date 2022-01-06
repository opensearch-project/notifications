/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

export const DOCUMENTATION_LINK = '';
export const ALERTING_DOCUMENTATION_LINK =
  'https://opensearch.org/docs/monitoring-plugins/alerting/monitors/#authenticate-sender-account';

export const ROUTES = Object.freeze({
  NOTIFICATIONS: '/notifications',
  CHANNELS: '/channels',
  CHANNEL_DETAILS: '/channel-details',
  CREATE_CHANNEL: '/create-channel',
  EDIT_CHANNEL: '/edit-channel',
  EMAIL_SENDERS: '/email-senders',
  EMAIL_GROUPS: '/email-recipient-groups',
  CREATE_SENDER: '/create-smtp-sender',
  EDIT_SENDER: '/edit-smtp-sender',
  CREATE_SES_SENDER: '/create-ses-sender',
  EDIT_SES_SENDER: '/edit-ses-sender',
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
  EMAIL_SENDERS: { text: 'Email senders', href: `#${ROUTES.EMAIL_SENDERS}` },
  EMAIL_GROUPS: { text: 'Email recipient groups', href: `#${ROUTES.EMAIL_GROUPS}` },
  CREATE_SENDER: {
    text: 'Create SMTP sender',
    href: `#${ROUTES.CREATE_SENDER}`,
  },
  EDIT_SENDER: { text: 'Edit SMTP sender' },
  CREATE_SES_SENDER: {
    text: 'Create SES sender',
    href: `#${ROUTES.CREATE_SENDER}`,
  },
  EDIT_SES_SENDER: { text: 'Edit SES sender' },
  CREATE_RECIPIENT_GROUP: {
    text: 'Create recipient group',
    href: `#${ROUTES.CREATE_RECIPIENT_GROUP}`,
  },
  EDIT_RECIPIENT_GROUP: { text: 'Edit recipient group' },
});

export const NOTIFICATION_SOURCE = Object.freeze({
  alerting: 'Alerting',
  index_management: 'ISM',
  reports: 'Reporting',
});

export const BACKEND_CHANNEL_TYPE = Object.freeze({
  SLACK: 'slack',
  EMAIL: 'email',
  CHIME: 'chime',
  CUSTOM_WEBHOOK: 'webhook',
  SNS: 'sns',
});

export const CHANNEL_TYPE = Object.freeze({
  [BACKEND_CHANNEL_TYPE.SLACK]: 'Slack',
  [BACKEND_CHANNEL_TYPE.EMAIL]: 'Email',
  [BACKEND_CHANNEL_TYPE.CHIME]: 'Chime',
  [BACKEND_CHANNEL_TYPE.CUSTOM_WEBHOOK]: 'Custom webhook',
  [BACKEND_CHANNEL_TYPE.SNS]: 'Amazon SNS',
}) as {
  slack: string;
  email: string;
  chime: string;
  webhook: string;
  sns: string;
};

export const ENCRYPTION_TYPE = Object.freeze({
  ssl: 'SSL',
  start_tls: 'TLS',
  none: 'None',
});

export const SEVERITY_TYPE = Object.freeze({
  none: 'None',
  info: 'Info',
  high: 'High',
  critical: 'Critical',
});

export const CUSTOM_WEBHOOK_ENDPOINT_TYPE = Object.freeze({
  WEBHOOK_URL: 'Webhook URL',
  CUSTOM_URL: 'Custom attributes URL',
});

export const HISTOGRAM_TYPE = Object.freeze({
  CHANNEL_TYPE: 'Channel type',
  SOURCE: 'Notification source',
  STATUS: 'Notification status',
  SEVERITY: 'Severity',
  TOP_10_CHANNELS: 'Top 10 channels',
});
