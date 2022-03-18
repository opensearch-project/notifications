/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { EuiComboBoxOptionOption } from '@elastic/eui';
import { ENCRYPTION_TYPE } from '../../../utils/constants';

export const createSenderConfigObject = (
  senderName: string,
  host: string,
  port: string,
  encryption: keyof typeof ENCRYPTION_TYPE,
  email: string
) => {
  return {
    name: senderName,
    config_type: 'smtp_account',
    is_enabled: true,
    smtp_account: {
      host,
      port,
      method: encryption,
      from_address: email,
    },
  };
};

export const createSesSenderConfigObject = (
  senderName: string,
  email: string,
  awsRegion: string,
  roleArn?: string
) => {
  return {
    name: senderName,
    config_type: 'ses_account',
    is_enabled: true,
    ses_account: {
      from_address: email,
      region: awsRegion,
      ...(roleArn && { role_arn: roleArn }),
    },
  };
};

export const createRecipientGroupConfigObject = (
  name: string,
  description: string,
  selectedEmailOptions: Array<EuiComboBoxOptionOption<string>>
) => {
  return {
    name,
    description,
    config_type: 'email_group',
    is_enabled: true,
    email_group: {
      recipient_list: selectedEmailOptions.map((email) => ({
        recipient: email.label,
      })),
    },
  };
};

export const onComboBoxCreateOption = (
  searchValue: string,
  flattenedOptions: Array<EuiComboBoxOptionOption<string>> = [],
  options: Array<EuiComboBoxOptionOption<string>>,
  setOptions: (options: Array<EuiComboBoxOptionOption<string>>) => void,
  selectedOptions: Array<EuiComboBoxOptionOption<string>>,
  setSelectedOptions: (options: Array<EuiComboBoxOptionOption<string>>) => void,
  setInputError: (newOptions: Array<EuiComboBoxOptionOption<string>>) => void
) => {
  const normalizedSearchValue = searchValue.trim().toLowerCase();
  if (!normalizedSearchValue) return;

  const newOption = { label: searchValue };
  if (
    flattenedOptions.findIndex(
      (option) => option.label.trim().toLowerCase() === normalizedSearchValue
    ) === -1
  ) {
    setOptions([...options, newOption]);
  }
  const newOptions = [...selectedOptions, newOption];
  setSelectedOptions(newOptions);
  setInputError(newOptions);
};
