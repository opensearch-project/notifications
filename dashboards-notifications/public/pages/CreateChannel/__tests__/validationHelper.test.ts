/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import {
  validateArn,
  validateChannelName,
  validateCustomURLHost,
  validateCustomURLPort,
  validateEmailSender,
  validateRecipients,
  validateWebhookKey,
  validateWebhookURL,
  validateWebhookValue,
} from '../utils/validationHelper';

describe('test create channel validation helpers', () => {
  it('validates channel name', () => {
    const pass = validateChannelName('test channel');
    const fail = validateChannelName('');
    expect(pass).toHaveLength(0);
    expect(fail).toHaveLength(1);
  });

  it('validates webhook', () => {
    const pass = validateWebhookURL('https://test-webhook');
    const httpTest = validateWebhookURL('http://test-webhook');
    const emptyInput = validateWebhookURL('');
    const invalidURL = validateWebhookURL('hxxp://test-webhook');
    expect(pass).toHaveLength(0);
    expect(httpTest).toHaveLength(0);
    expect(emptyInput).toHaveLength(1);
    expect(invalidURL).toHaveLength(1);
  });

  it('validates webhook key', () => {
    const pass = validateWebhookKey('test-key');
    const fail = validateWebhookKey('');
    expect(pass).toHaveLength(0);
    expect(fail).toHaveLength(1);
  });

  it('validates webhook value', () => {
    const pass = validateWebhookValue('test-value');
    const fail = validateWebhookValue('');
    expect(pass).toHaveLength(0);
    expect(fail).toHaveLength(1);
  });

  it('validates custom url host', () => {
    const pass = validateCustomURLHost('test-webhook');
    const httpTest = validateCustomURLHost('http://test-webhook');
    const httpsTest = validateCustomURLHost('https://test-webhook');
    const emptyInput = validateCustomURLHost('');
    expect(pass).toHaveLength(0);
    expect(httpTest).toHaveLength(0);
    expect(httpsTest).toHaveLength(0);
    expect(emptyInput).toHaveLength(1);
  });

  it('validates custom url port', () => {
    const pass = validateCustomURLPort('23');
    const emptyPort = validateCustomURLPort('');
    const invalidPort = validateCustomURLPort('abc');
    expect(pass).toHaveLength(0)
    expect(emptyPort).toHaveLength(0); // port is optional
    expect(invalidPort).toHaveLength(1);
  });

  it('validates email sender', () => {
    const pass = validateEmailSender([{ label: 'test sender' }]);
    const fail = validateEmailSender([]);
    expect(pass).toHaveLength(0);
    expect(fail).toHaveLength(1);
  });

  it('validates recipients', () => {
    const pass = validateRecipients([{ label: 'test recipient group' }]);
    const fail = validateRecipients([]);
    expect(pass).toHaveLength(0);
    expect(fail).toHaveLength(1);
  });

  it('validates arn', () => {
    const pass = validateArn('test-key');
    const fail = validateArn('');
    expect(pass).toHaveLength(0);
    expect(fail).toHaveLength(1);
  });
});
