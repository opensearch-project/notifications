/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
