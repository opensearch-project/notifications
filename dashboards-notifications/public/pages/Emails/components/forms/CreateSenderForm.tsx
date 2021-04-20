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
  EuiFieldNumber,
  EuiFieldText,
  EuiFlexGroup,
  EuiFlexItem,
  EuiFormRow,
  EuiLink,
  EuiSpacer,
  EuiSuperSelect,
  EuiSuperSelectOption,
} from '@elastic/eui';
import React from 'react';
import { ENCRYPTION_METHOD } from '../../../../../models/interfaces';
import { DOCUMENTATION_LINK } from '../../../../utils/constants';
import {
  validateEmail,
  validateHost,
  validatePort,
  validateSenderName,
} from '../../utils/validationHelper';

interface CreateSenderFormProps {
  senderName: string;
  setSenderName: (name: string) => void;
  email: string;
  setEmail: (email: string) => void;
  host: string;
  setHost: (host: string) => void;
  port: string;
  setPort: (port: string) => void;
  encryption: ENCRYPTION_METHOD;
  setEncryption: (encryption: ENCRYPTION_METHOD) => void;
  inputErrors: { [key: string]: string[] };
  setInputErrors: (errors: { [key: string]: string[] }) => void;
}

export function CreateSenderForm(props: CreateSenderFormProps) {
  const encryptionOptions: Array<EuiSuperSelectOption<ENCRYPTION_METHOD>> = [
    {
      value: 'SSL',
      inputDisplay: 'SSL',
    },
    {
      value: 'TSL',
      inputDisplay: 'TSL',
    },
  ];

  return (
    <>
      <EuiFormRow
        label="Sender name"
        style={{ maxWidth: '650px' }}
        helpText="Use a unique, descriptive name. The sender name must contain from m to n characters. Valid characters are lowercase a-z, 0-9, and - (hyphen)."
        error={props.inputErrors.senderName.join(' ')}
        isInvalid={props.inputErrors.senderName.length > 0}
      >
        <EuiFieldText
          fullWidth
          placeholder="Enter sender name"
          value={props.senderName}
          onChange={(e) => props.setSenderName(e.target.value)}
          isInvalid={props.inputErrors.senderName.length > 0}
          onBlur={() => {
            props.setInputErrors({
              ...props.inputErrors,
              senderName: validateSenderName(props.senderName),
            });
          }}
        />
      </EuiFormRow>

      <EuiSpacer size="m" />
      <EuiFlexGroup gutterSize="s" style={{ maxWidth: '658px' }}>
        <EuiFlexItem grow={4}>
          <EuiFormRow
            label="Email address"
            error={props.inputErrors.email.join(' ')}
            isInvalid={props.inputErrors.email.length > 0}
          >
            <EuiFieldText
              placeholder="Enter email address"
              value={props.email}
              onChange={(e) => props.setEmail(e.target.value)}
              isInvalid={props.inputErrors.email.length > 0}
              onBlur={() => {
                props.setInputErrors({
                  ...props.inputErrors,
                  email: validateEmail(props.email),
                });
              }}
            />
          </EuiFormRow>
        </EuiFlexItem>
        <EuiFlexItem grow={4}>
          <EuiFormRow
            label="Host"
            error={props.inputErrors.host.join(' ')}
            isInvalid={props.inputErrors.host.length > 0}
          >
            <EuiFieldText
              placeholder="Enter host"
              value={props.host}
              onChange={(e) => props.setHost(e.target.value)}
              isInvalid={props.inputErrors.host.length > 0}
              onBlur={() => {
                props.setInputErrors({
                  ...props.inputErrors,
                  host: validateHost(props.host),
                });
              }}
            />
          </EuiFormRow>
        </EuiFlexItem>
        <EuiFlexItem grow={2}>
          <EuiFormRow
            label="Port"
            error={props.inputErrors.port.join(' ')}
            isInvalid={props.inputErrors.port.length > 0}
          >
            <EuiFieldNumber
              placeholder="Enter port"
              value={props.port}
              onChange={(e) => props.setPort(e.target.value)}
              isInvalid={props.inputErrors.port.length > 0}
              onBlur={() => {
                props.setInputErrors({
                  ...props.inputErrors,
                  port: validatePort(props.port),
                });
              }}
            />
          </EuiFormRow>
        </EuiFlexItem>
      </EuiFlexGroup>

      <EuiSpacer size="m" />
      <EuiFormRow
        label="Encryption method"
        style={{ maxWidth: '650px' }}
        helpText={
          <div>
            SSL or TLS is recommended for security. To use either one, you must
            add the following fields to the Elasticsearch keystore on each node.{' '}
            <EuiLink href={DOCUMENTATION_LINK}>Learn more</EuiLink>
            <br />
            opendistro.alerting.destination.mail.adminTest.username: [username]
            <br />
            opendistro.alerting.destination.mail.adminTest.password: [password]
          </div>
        }
      >
        <EuiSuperSelect
          fullWidth
          options={encryptionOptions}
          valueOfSelected={props.encryption}
          onChange={props.setEncryption}
        />
      </EuiFormRow>

      <EuiSpacer size="m" />
    </>
  );
}
