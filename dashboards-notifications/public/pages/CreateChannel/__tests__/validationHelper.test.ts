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
