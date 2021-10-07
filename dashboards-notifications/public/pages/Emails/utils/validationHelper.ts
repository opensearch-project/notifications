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
import _ from 'lodash';

export const validateSenderName = (name: string) => {
  const errors = [];
  if (_.trim(name).length === 0) {
    errors.push('Sender name cannot be empty.');
    return errors;
  }
  if (name.length > 50 || name.length < 2)
    errors.push('Sender name must contain 2 to 50 characters.');
  if (!/^[a-z0-9-]+$/.test(name))
    errors.push('Sender name contains invalid characters.');
  return errors;
};

export const validateEmail = (email: string) => {
  const errors = [];
  if (email.length === 0) errors.push('Email address cannot be empty.');
  return errors;
};

export const validateHost = (host: string) => {
  const errors = [];
  if (host.length === 0) errors.push('Host cannot be empty.');
  return errors;
};

export const validatePort = (port: string) => {
  const errors = [];
  const portNum = parseInt(port);
  if (port.length === 0) errors.push('Port cannot be empty.');
  else if (isNaN(portNum) || portNum < 0 || portNum > 65535)
    errors.push('Invalid port.');
  return errors;
};

export const validateRoleArn = (roleArn: string) => {
  const errors = [];
  if (roleArn.length === 0) errors.push('IAM role ARN cannot be empty.');
  return errors;
};

export const validateAwsRegion = (region: string) => {
  const errors = [];
  if (region.length === 0) errors.push('AWS region cannot be empty.');
  return errors;
};

export const validateRecipientGroupName = (name: string) => {
  const errors = [];
  if (_.trim(name).length === 0) {
    errors.push('Recipient group name cannot be empty.');
    return errors;
  }
  if (name.length > 50 || name.length < 2)
    errors.push('Recipient group name must contain 2 to 50 characters.');
  return errors;
};

export const validateRecipientGroupEmails = (
  emails: Array<EuiComboBoxOptionOption<string>>
) => {
  const errors = [];
  if (emails.length === 0) errors.push('Email addresses cannot be empty.');
  return errors;
};
