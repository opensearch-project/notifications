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
  constructEmailObject,
  constructWebhookObject,
  deconstructEmailObject,
  deconstructWebhookObject,
} from '../utils/helper';

describe('constructs and deconstructs webhook objects', () => {
  const args = [
    'https://test-webhook.com:1234/subdirectory?param1=value1&param2=&param3=value3',
    'test-webhook.com',
    '1234',
    'subdirectory',
    [
      { key: 'param1', value: 'value1' },
      { key: 'param2', value: '' },
      { key: '', value: 'value2' },
      { key: '', value: '' },
      { key: 'param3', value: 'value3' },
    ],
    [
      { key: 'header1', value: 'value1' },
      { key: 'header2', value: '' },
      { key: '', value: 'value2' },
      { key: '', value: '' },
      { key: 'header3', value: 'value3' },
    ],
  ];
  const webhookItem = {
    url:
      'https://test-webhook.com:1234/subdirectory?param1=value1&param2=&param3=value3',
    header_params: { header1: 'value1', header2: '', header3: 'value3' },
  };

  it('constructs webhook objects', () => {
    // @ts-ignore
    const resultFromWebhookURL = constructWebhookObject('WEBHOOK_URL', ...args);
    expect(resultFromWebhookURL).toEqual(webhookItem);

    // @ts-ignore
    const resultFromCustomURL = constructWebhookObject('CUSTOM_URL', ...args);
    expect(resultFromCustomURL).toEqual(webhookItem);
  });

  it('constructs webhook objects with no optional fields', () => {
    const resultFromCustomURL = constructWebhookObject(
      'CUSTOM_URL',
      '',
      'test-webhook.com',
      '',
      '',
      [],
      []
    );
    expect(resultFromCustomURL).toEqual({
      url: 'https://test-webhook.com',
      header_params: {},
    });
  });

  it('deconstructs webhook objects', () => {
    const {
      webhookURL,
      customURLHost,
      customURLPort,
      customURLPath,
      webhookParams,
      webhookHeaders,
    } = deconstructWebhookObject(webhookItem);
    expect(webhookURL).toEqual(args[0]);
    expect(customURLHost).toEqual(args[1]);
    expect(customURLPort).toEqual(args[2]);
    expect(customURLPath).toEqual(args[3]);
    expect(webhookParams).toEqual([
      { key: 'param1', value: 'value1' },
      { key: 'param2', value: '' },
      { key: 'param3', value: 'value3' },
    ]);
    expect(webhookHeaders).toEqual([
      { key: 'header1', value: 'value1' },
      { key: 'header2', value: '' },
      { key: 'header3', value: 'value3' },
    ]);
  });

  it('handles failures', () => {
    console.error = jest.fn();

    const {
      webhookURL,
      customURLHost,
      customURLPort,
      customURLPath,
      webhookParams,
      webhookHeaders,
    } = deconstructWebhookObject({ url: 'invalid url', header_params: {} });
    expect(webhookURL).toEqual('invalid url');
    expect(customURLHost).toEqual('');
    expect(customURLPort).toEqual('');
    expect(customURLPath).toEqual('');
    expect(webhookParams).toEqual([]);
    expect(webhookHeaders).toEqual([]);

    expect(console.error).toBeCalled();
  });
});

describe('constructs and deconstructs email objects', () => {
  const selectedSenderOptions = [
    { label: 'test sender name', value: 'test-sender-id' },
  ];
  const selectedRecipientGroupOptions = [
    {
      label: 'recipient group 1',
      value: 'recipient-group-id-1',
    },
    {
      label: 'recipient group 2',
      value: 'recipient-group-id-2',
    },
    {
      label: 'recipient group 3',
      value: 'recipient-group-id-3',
    },
    {
      label: 'custom.address1@email.com',
    },
    {
      label: 'custom.address2@email.com',
    },
  ];
  const emailItem = {
    email_account_id: 'test-sender-id',
    email_group_id_list: [
      'recipient-group-id-1',
      'recipient-group-id-2',
      'recipient-group-id-3',
    ],
    recipient_list: ['custom.address1@email.com', 'custom.address2@email.com'],
  };

  it('constructs email objects', () => {
    const object = constructEmailObject(
      selectedSenderOptions,
      selectedRecipientGroupOptions
    );

    expect(object).toEqual(emailItem);
  });

  it('deconstructs email objects', () => {
    const {
      selectedSenderOptions: senders,
      selectedRecipientGroupOptions: recipients,
    } = deconstructEmailObject({
      ...emailItem,
      email_account_name: selectedSenderOptions[0].label,
      email_group_id_map: {
        'recipient-group-id-1': 'recipient group 1',
        'recipient-group-id-2': 'recipient group 2',
        'recipient-group-id-3': 'recipient group 3',
      },
    });

    expect(senders).toEqual(selectedSenderOptions);
    expect(recipients).toEqual(selectedRecipientGroupOptions);
  });
});
