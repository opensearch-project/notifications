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
  EuiSpacer,
  EuiSuperSelect,
  EuiSuperSelectOption,
} from '@elastic/eui';
import React from 'react';
import { ENCRYPTION_METHOD } from '../../../../../models/interfaces';

interface CreateSenderModalProps {
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
}

export function CreateSenderForm(props: CreateSenderModalProps) {
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
        helpText="Use a unique and descriptive name that's easy to search. The sender name must contain from m to n characters. Valid characters are lowercase a-z, 0-9, and - (hyphen)."
      >
        <EuiFieldText
          fullWidth
          placeholder=""
          value={props.senderName}
          onChange={(e) => props.setSenderName(e.target.value)}
        />
      </EuiFormRow>

      <EuiSpacer size="m" />
      <EuiFlexGroup gutterSize="s" style={{maxWidth: '658px'}}>
        <EuiFlexItem grow={4}>
          <EuiFormRow label="Email address">
            <EuiFieldText
              placeholder=""
              value={props.email}
              onChange={(e) => props.setEmail(e.target.value)}
            />
          </EuiFormRow>
        </EuiFlexItem>
        <EuiFlexItem grow={4}>
          <EuiFormRow label="Host">
            <EuiFieldText
              placeholder=""
              value={props.host}
              onChange={(e) => props.setHost(e.target.value)}
            />
          </EuiFormRow>
        </EuiFlexItem>
        <EuiFlexItem grow={2}>
          <EuiFormRow label="Port">
            <EuiFieldNumber
              placeholder=""
              value={props.port}
              onChange={(e) => props.setPort(e.target.value)}
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
            SSL or TSL is recommended for security. SSL and TSL requires
            validation by adding the following two fields to OpenSearch
            keystore:
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
