/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import {
  ChannelItemType,
  RecipientGroupItemType,
  SenderItemType,
  SESSenderItemType,
} from '../../models/interfaces';

const mockChime: ChannelItemType = {
  name: 'Chime test channel',
  description: 'test description',
  config_type: 'chime',
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
  is_enabled: true,
  email: {
    email_account_id: 'dj8etXkBCzVy9Vy-nsiL',
    recipient_list: [
      { recipient: 'custom@email.com' },
      { recipient: 'email@test.com' },
      { recipient: 'test@email.com' },
    ],
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
  is_enabled: true,
  email: {
    email_account_id: 'dj8etXkBCzVy9Vy-nsiL',
    recipient_list: [
      { recipient: 'custom@email.com' },
      { recipient: 'email@test.com' },
      { recipient: 'test@email.com' },
    ],
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
};
