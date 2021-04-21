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

import { DataGenerator } from '@elastic/charts';

export const MOCK_GET_HISTOGRAM = () => {
  const dg = new DataGenerator();
  const data = dg.generateGroupedSeries(26, 2, 'Channel-');
  data[18].y = 18;
  for (let index = 0; index < data.length / 2; index++) {
    const element = data[index];
    element.y = Math.round(element.y);
    element.x = 1618951331 + index * 1000 * 60 * 60;
    element.g = 'Ops_channel';
    data[index + data.length / 2].x = element.x;
    data[index + data.length / 2].y = Math.round(element.y);
    data[index + data.length / 2].g = 'Oncall_channel';
  }
  return data;
};

export const MOCK_RECIPIENT_GROUPS = [
  {
    id: '0',
    name: 'admin_list',
    email: Array.from({ length: 8 }, (v, i) => ({
      email: 'no-reply@company.com',
    })),
    description: 'Description about this group',
  },
  {
    id: '1',
    name: 'on_call_list',
    email: Array.from({ length: 2 }, (v, i) => ({
      email: 'no-reply@company.com',
    })),
    description: 'Description about this group',
  },
  {
    id: '2',
    name: 'Team2',
    email: Array.from({ length: 10 }, (v, i) => ({
      email: 'no-reply@company.com',
    })),
    description: 'Description about this group',
  },
  {
    id: '3',
    name: 'Security_alerts',
    email: Array.from({ length: 5 }, (v, i) => ({
      email: 'no-reply@company.com',
    })),
    description: 'Description about this group',
  },
];

export const MOCK_SENDERS = [
  {
    id: '0',
    name: 'Main',
    from: 'no-reply@company.com',
    host: 'smtp.company.com',
    port: '80',
    method: 'SSL',
  },
  {
    id: '1',
    name: 'Reports',
    from: 'reports@company.com',
    host: 'smtp.company.com',
    port: '80',
    method: 'SSL',
  },
  {
    id: '2',
    name: 'Admin bot',
    from: 'admin_bot@company.com',
    host: 'smtp-internal.company.com',
    port: '80',
    method: 'SSL',
  },
  {
    id: '3',
    name: 'Alerting bot',
    from: 'alerts@company.com',
    host: 'smtp.company.com',
    port: '80',
    method: 'SSL',
  },
];

export const MOCK_CHANNELS = [
  {
    id: '0',
    name: 'Ops_channel',
    enabled: true,
    type: 'SLACK',
    allowedFeatures: ['ALERTING'],
    description: 'Notifies all full-time operational team members.',
    lastUpdatedTime: 1618951331,
    destination: {
      slack: {
        url:
          'https://hooks.slack.com/services/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',
      },
    },
  },
  {
    id: '1',
    name: 'Team2',
    enabled: true,
    type: 'CHIME',
    allowedFeatures: ['ALERTING', 'REPORTING'],
    description: 'Notifies all full-time operational team members.',
    lastUpdatedTime: 1618951331,
    destination: {
      chime: {
        url: 'https://hooks.chime.com/example/url',
      },
    },
  },
  {
    id: '2',
    name: 'Security_alerts',
    enabled: true,
    type: 'CUSTOM_WEBHOOK',
    allowedFeatures: ['ISM', 'REPORTING'],
    description: 'Notifies all full-time operational team members.',
    lastUpdatedTime: 1618951331,
    destination: {
      custom_webhook: {
        host: 'https://hooks.myhost.com',
        port: 21,
        path: 'custompath',
        parameters: {
          Parameter1: 'value1',
          Parameter2: 'value2',
          Parameter3: 'value3',
          Parameter4: 'value4',
          Parameter5: 'value5',
          Parameter6: 'value6',
          Parameter7: 'value7',
          Parameter8: 'value8',
        },
        headers: {
          'Content-Type': 'application/JSON',
          Header1: 'value1',
          Header2: 'value2',
          Header3: 'value3',
          Header4: 'value4',
          Header5: 'value5',
          Header6: 'value6',
          Header7: 'value7',
          Header8: 'value8',
        },
      },
    },
  },
  {
    id: '3',
    name: 'Reporting_bot',
    enabled: false,
    type: 'EMAIL',
    allowedFeatures: ['REPORTING'],
    description: 'Notifies all full-time operational team members.',
    lastUpdatedTime: 1618951331,
    destination: {
      email: {
        email_account_id: 'robot@gmail.com',
        header: '# sample header',
        recipients: [
          'Team 2',
          'cyberadmin@company.com',
          'Ops_team_weekly',
          'security_pos@company.com',
          'Team 5',
          'bot@company.com',
          'Team 7',
        ],
      },
    },
  },
  {
    id: '4',
    name: 'SNS channel test',
    enabled: false,
    type: 'SNS',
    allowedFeatures: ['ISM'],
    description: 'Notifies all full-time operational team members.',
    lastUpdatedTime: 1618951331,
    destination: {
      sns: {
        topic_arn: 'arn:aws:sns:us-east-1:24586493349034:es-alerting-test', // sns arn
        role_arn: 'arn:aws:sns:us-east-1:24586493349034:es-alerting-test', // iam arn
      },
    },
  },
  {
    id: '5',
    name: 'SES channel test',
    enabled: false,
    type: 'SES',
    allowedFeatures: ['ALERTING', 'REPORTING'],
    description: 'Notifies all full-time operational team members.',
    lastUpdatedTime: 1618951331,
    destination: {
      ses: {
        email_account_id: 'robot@gmail.com',
        header: '# sample header',
        recipients: [
          'Team 2',
          'cyberadmin@company.com',
          'Ops_team_weekly',
          'Team 5',
          'bot@company.com',
          'Team 7',
          'security_pos@company.com',
        ],
      },
    },
  },
];

export const MOCK_NOTIFICATIONS = {
  notifications: [
    {
      id: '1',
      title: 'Alert notification on high error rate',
      referenceId: 'alert_id_1',
      source: 'ALERTING',
      severity: 'High',
      lastUpdatedTime: 1612229000,
      tags: ['optional string list'],
      status: 'Error',
      statusList: [
        {
          configId: '1',
          configName: 'dev_email_channel',
          configType: 'Email',
          emailRecipientStatus: [
            {
              recipient: 'dd@amazon.com',
              deliveryStatus: {
                statusCode: '500',
                statusText: 'Some error',
              },
            },
            {
              recipient: 'cc@amazon.com',
              deliveryStatus: {
                statusCode: '404',
                statusText: 'invalid',
              },
            },
          ],
          deliveryStatus: {
            // check this on each channel is enough
            statusCode: '500',
            statusText:
              'Unavailable to send message. Invalid SMTP configuration.',
          },
        },
        {
          configId: '2',
          configName: 'manage_slack_channel',
          configType: 'Slack',
          deliveryStatus: {
            statusCode: '200',
            statusText: 'Success',
          },
        },
      ],
    },
    {
      id: '2',
      title: 'another notification',
      referenceId: 'alert_id_2',
      source: 'ALERTING',
      severity: 'High',
      lastUpdatedTime: 1612229000,
      tags: ['optional string list'],
      status: 'Success',
      statusList: [
        {
          configId: '1',
          configName: 'dev_email_channel',
          configType: 'Email',
          emailRecipientStatus: [
            {
              recipient: 'dd@amazon.com',
              deliveryStatus: {
                statusCode: '500',
                statusText: 'Some error',
              },
            },
            {
              recipient: 'zhongnan@amazon.com',
              deliveryStatus: {
                statusCode: '404',
                statusText: 'invalid',
              },
            },
          ],
          deliveryStatus: {
            statusCode: '500',
            statusText: 'Error',
          },
        },
        {
          configId: '2',
          configName: 'manage_slack_channel',
          configType: 'Slack',
          deliveryStatus: {
            statusCode: '200',
            statusText: 'Success',
          },
        },
      ],
    },
  ],
  totalNotifications: 6,
};
