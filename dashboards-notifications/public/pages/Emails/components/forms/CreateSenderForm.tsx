/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
import {
  ALERTING_DOCUMENTATION_LINK,
  ENCRYPTION_TYPE,
} from '../../../../utils/constants';
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
  encryption: keyof typeof ENCRYPTION_TYPE;
  setEncryption: (encryption: keyof typeof ENCRYPTION_TYPE) => void;
  inputErrors: { [key: string]: string[] };
  setInputErrors: (errors: { [key: string]: string[] }) => void;
}

export function CreateSenderForm(props: CreateSenderFormProps) {
  const encryptionOptions: Array<EuiSuperSelectOption<
    keyof typeof ENCRYPTION_TYPE
  >> = Object.entries(ENCRYPTION_TYPE).map(([key, value]) => ({
    value: key as keyof typeof ENCRYPTION_TYPE,
    inputDisplay: value,
  }));

  return (
    <>
      <EuiFormRow
        label="Sender name"
        style={{ maxWidth: '650px' }}
        helpText="Use a unique, descriptive name. The sender name must contain from 2 to 50 characters. Valid characters are lowercase a-z, 0-9, - (hyphen) and _ (underscore)."
        error={props.inputErrors.senderName.join(' ')}
        isInvalid={props.inputErrors.senderName.length > 0}
      >
        <EuiFieldText
          fullWidth
          placeholder="Enter sender name"
          data-test-subj="create-sender-form-name-input"
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
              placeholder="name@example.com"
              data-test-subj="create-sender-form-email-input"
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
              placeholder="smtp.example.com"
              data-test-subj="create-sender-form-host-input"
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
              placeholder="465"
              data-test-subj="create-sender-form-port-input"
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
            enter each sender account's credentials to the OpenSearch
            keystore using the CLI.{' '}
            <EuiLink
              href={ALERTING_DOCUMENTATION_LINK}
              target="_blank"
              external
            >
              Learn more
            </EuiLink>
          </div>
        }
      >
        <EuiSuperSelect
          fullWidth
          data-test-subj="create-sender-form-encryption-input"
          options={encryptionOptions}
          valueOfSelected={props.encryption}
          onChange={props.setEncryption}
        />
      </EuiFormRow>

      <EuiSpacer size="m" />
    </>
  );
}
