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
    feature_list: [],
    is_enabled: true,
    smtp_account: {
      host,
      port,
      method: encryption,
      from_address: email,
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
    feature_list: [],
    is_enabled: true,
    email_group: {
      recipient_list: selectedEmailOptions.map((email) => email.label),
    },
  };
};
