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

import {
  ChannelItemType,
  NotificationItem,
  RecipientGroupItemType,
  SenderItemType,
  SESSenderItemType,
} from '../../models/interfaces';

const mockChime: ChannelItemType = {
  name: 'Chime test channel',
  description: 'test description',
  config_type: 'chime',
  feature_list: ['reports'],
  is_enabled: true,
  chime: {
    url: 'https://chimehook',
  },
  config_id: 'test-chime',
  created_time_ms: 1622670451891,
  last_updated_time_ms: 1622670451891,
};

const mockSlack: ChannelItemType = {
  name: 'Slack test channel',
  description: 'test description',
  config_type: 'slack',
  feature_list: ['reports'],
  is_enabled: false,
  slack: {
    url: 'https://chimehook',
  },
  config_id: 'test-slack',
  created_time_ms: 1622670451891,
  last_updated_time_ms: 1622670451891,
};

const mockEmail: ChannelItemType = {
  name: 'Email test channel',
  description: 'test description',
  config_type: 'email',
  feature_list: ['alerting'],
  is_enabled: true,
  email: {
    email_account_id: 'dj8etXkBCzVy9Vy-nsiL',
    recipient_list: ['custom@email.com', 'email@test.com', 'test@email.com'],
    email_group_id_list: [
      '1l8hq3kB0XwiBlEbanSo',
      'dz8etXkBCzVy9Vy-0sgh',
      'eD8ktXkBCzVy9Vy-A8j-',
      '1V8gq3kB0XwiBlEbAXSI',
      'dD_8tHkBCzVy9Vy-5si0',
      '52XGr3kBqFjWrmvL43mv',
    ],
    email_account_name: 'name1',
    email_group_id_map: {
      '1l8hq3kB0XwiBlEbanSo': 'name1',
      'dz8etXkBCzVy9Vy-0sgh': 'name2',
      'eD8ktXkBCzVy9Vy-A8j-': 'name3',
      '1V8gq3kB0XwiBlEbAXSI': 'name4',
      'dD_8tHkBCzVy9Vy-5si0': 'name5',
      '52XGr3kBqFjWrmvL43mv': 'name6',
    },
    invalid_ids: [],
    sender_type: 'smtp_account',
  },
  config_id: 'DMG3zXkBPa6YFxmVfNmm',
  created_time_ms: 1622654024861,
  last_updated_time_ms: 1622656665674,
};

const mockEmailWithSES: ChannelItemType = {
  name: 'Email test channel',
  description: 'test description',
  config_type: 'email',
  feature_list: ['alerting'],
  is_enabled: true,
  email: {
    email_account_id: 'dj8etXkBCzVy9Vy-nsiL',
    recipient_list: ['custom@email.com', 'email@test.com', 'test@email.com'],
    email_group_id_list: [
      '1y8ud3xO0KjvOyRonaFb',
      'qm8rgKxOPmIl9Il-0ftu',
      'rQ8xgKxOPmIl9Il-N8w-',
      '1I8td3xO0KjvOyRoNKFV',
      'qQ_8gUxOPmIl9Il-5fv0',
      '52KTe3xOdSwJeziY43zi',
    ],
    email_account_name: 'name1',
    email_group_id_map: {
      '1y8ud3xO0KjvOyRonaFb': 'name1',
      'qm8rgKxOPmIl9Il-0ftu': 'name2',
      'rQ8xgKxOPmIl9Il-N8w-': 'name3',
      '1I8td3xO0KjvOyRoNKFV': 'name4',
      'qQ_8gUxOPmIl9Il-5fv0': 'name5',
      '52KTe3xOdSwJeziY43zi': 'name6',
    },
    invalid_ids: ['52KTe3xOdSwJeziY43zi'],
    sender_type: 'ses_account',
  },
  config_id: 'QZT3mKxOCn6LSkzIsAzz',
  created_time_ms: 1622654024861,
  last_updated_time_ms: 1622656665674,
};

const mockWebhook: ChannelItemType = {
  name: 'Webhook test channel',
  description: 'test description',
  config_type: 'webhook',
  feature_list: ['alerting'],
  is_enabled: true,
  webhook: {
    url: 'https://host:23/path?key1=%23%404&key2=&key3=value3',
    header_params: {
      key1: 'value1',
      key2: 'value2',
      key3: '',
      key4: 'value4',
      key5: 'value5',
      key6: 'value6',
    },
    method: 'POST',
  },
  config_id: '7mUjsHkBqFjWrmvLc3nl',
  created_time_ms: 1622157784037,
  last_updated_time_ms: 1622581084050,
};

const mockSNS: ChannelItemType = {
  name: 'SNS test channel',
  description: 'test description',
  config_type: 'sns',
  feature_list: ['alerting'],
  is_enabled: true,
  sns: {
    topic_arn: 'arn:aws:sns:us-west-2:012345678912:notifications-test',
    role_arn: 'arn:aws:iam::012345678912:role/NotificationsSNSRole',
  },
  config_id: 'erCDYHsBUMRLC5PZXfPN',
  created_time_ms: 1622157784037,
  last_updated_time_ms: 1622581084050,
};

const mockSender: SenderItemType = {
  name: 'test-sender',
  description: 'test description',
  config_id: '72UysHkBqFjWrmvLFXkB',
  created_time_ms: 1622158742784,
  last_updated_time_ms: 1622158742784,
  smtp_account: {
    host: 'example.com',
    port: '23',
    method: 'start_tls',
    from_address: 'sample@email.com',
  },
};

const mockSESSender: SESSenderItemType = {
  name: 'test-ses-sender',
  description: 'test description',
  config_id: '72HlfUxOdSwJeziYSKxO',
  created_time_ms: 1622158742784,
  last_updated_time_ms: 1622158742784,
  ses_account: {
    from_address: 'sample@email.com',
    region: 'us-west-2',
    role_arn: 'arn:aws:iam::012345678912:role/TestNotificationsSESRole',
  },
};

const mockRecipientGroup: RecipientGroupItemType = {
  name: 'Test recipient group',
  description: 'test description',
  config_id: '72UysHkBqVjWrmvLFXkB',
  created_time_ms: 1622158742784,
  last_updated_time_ms: 1622158742784,
  email_group: {
    recipient_list: [
      { recipient: 'test1@email.com' },
      { recipient: 'test2@email.com' },
      { recipient: 'test3@email.com' },
      { recipient: 'test4@email.com' },
      { recipient: 'test5@email.com' },
      { recipient: 'test6@email.com' },
    ],
  },
};

const mockNotification: NotificationItem = {
  event_id: 'qcuW53kBSU9z3nsaFegV',
  last_updated_time_ms: 1623084806565,
  created_time_ms: 1623084806565,
  tenant: '',
  event_source: {
    title: 'test notification',
    reference_id: 'reference_id',
    feature: 'reports',
    severity: 'info',
    tags: ['tag1', 'tag2'],
  },
  success: false,
  status_list: [
    {
      config_id: '8KUO2HkBtN9Xw674azJS',
      config_type: 'slack',
      config_name: 'slack test',
      email_recipient_status: [],
      delivery_status: {
        status_code: '200',
        status_text: 'success',
      },
    },
    {
      config_id: 'oMvr2HkBSU9z3nsaFujT',
      config_type: 'email',
      config_name: 'emailtest',
      email_recipient_status: [
        {
          recipient: 'test@email.com',
          delivery_status: {
            status_code: '500',
            status_text: 'failed',
          },
        },
      ],
      delivery_status: {
        status_code: '500',
        status_text: 'failed',
      },
    },
  ],
};

export const MOCK_DATA = {
  channels: {
    items: [
      mockChime,
      mockSlack,
      mockEmail,
      mockEmailWithSES,
      mockWebhook,
      mockSNS,
    ],
    total: 6,
  },
  chime: mockChime,
  slack: mockSlack,
  email: mockEmail,
  emailWithSES: mockEmailWithSES,
  webhook: mockWebhook,
  sns: mockSNS,
  sender: mockSender,
  sesSender: mockSESSender,
  senders: {
    items: [mockSender],
    total: 1,
  },
  sesSenders: {
    items: [mockSESSender],
    total: 1,
  },
  recipientGroup: mockRecipientGroup,
  recipientGroups: {
    items: [mockRecipientGroup],
    total: 1,
  },
  notifications: {
    items: [mockNotification],
    total: 1,
  },
};
