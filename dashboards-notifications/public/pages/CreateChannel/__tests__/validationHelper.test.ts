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
  validateChannelName,
  validateEmailSender,
  validateRecipients,
  validateWebhookURL,
} from '../utils/validationHelper';

// TODO add more detailed test cases when validations are finalized by UX
describe('test create channel validation helpers', () => {
  it('validates channel name', () => {
    const pass = validateChannelName('test');
    const fail = validateChannelName('');
    expect(pass).toBeTruthy;
    expect(fail).toBeFalsy;
  });

  it('validates slack webhook', () => {
    const pass = validateWebhookURL('test');
    const fail = validateWebhookURL('');
    expect(pass).toBeTruthy;
    expect(fail).toBeFalsy;
  });

  it('validates email sender', () => {
    const pass = validateEmailSender('test');
    const fail = validateEmailSender('');
    expect(pass).toBeTruthy;
    expect(fail).toBeFalsy;
  });

  it('validates recipients', () => {
    const pass = validateRecipients([{ label: 'test' }]);
    const fail = validateRecipients([]);
    expect(pass).toBeTruthy;
    expect(fail).toBeFalsy;
  });
});
