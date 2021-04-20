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

import { EuiComboBoxOptionOption } from '@elastic/eui';

export const validateChannelName = (name: string) => {
  const errors = [];
  if (name.length === 0) errors.push('Channel name cannot be empty.');
  return errors;
};

export const validateWebhookURL = (url: string) => {
  const errors = [];
  if (url.length === 0 || !url.match(/https?:\/\/.+/)) errors.push('Invalid webhook URL.');
  return errors;
};

export const validateWebhookKey = (key: string) => {
  const errors = [];
  if (key.length === 0) errors.push('Key cannot be empty.');
  return errors;
};

export const validateWebhookValue = (value: string) => {
  const errors = [];
  if (value.length === 0) errors.push('Value cannot be empty.');
  return errors;
};

export const validateCustomURLHost = (host: string) => {
  const errors = [];
  if (host.length === 0) errors.push('Invalid host.');
  return errors;
};

export const validateCustomURLPort = (port: string) => {
  const errors = [];
  const portNum = parseInt(port);
  if (isNaN(portNum) || portNum < 0 || portNum > 65535)
    errors.push('Invalid port.');
  return errors;
};

export const validateEmailSender = (sender: string) => {
  const errors = [];
  if (sender.length === 0) errors.push('Sender cannot be empty.');
  return errors;
};

export const validateRecipients = (
  group: Array<EuiComboBoxOptionOption<string>>
) => {
  const errors = [];
  if (group.length === 0) errors.push('Default recipients cannot be empty.');
  return errors;
};

export const validateArn = (arn: string) => {
  const errors = [];
  if (arn.length === 0) errors.push('Invalid ARN format.');
  return errors;
};
