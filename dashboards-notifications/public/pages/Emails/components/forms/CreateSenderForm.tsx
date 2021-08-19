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
  EuiRadioGroup,
  EuiSpacer,
  EuiSuperSelect,
  EuiSuperSelectOption,
} from '@elastic/eui';
import React, { useContext } from 'react';
import {
  ALERTING_DOCUMENTATION_LINK,
  ENCRYPTION_TYPE,
} from '../../../../utils/constants';
import { MainContext } from '../../../Main/Main';
import {
  validateAwsRegion,
  validateEmail,
  validateHost,
  validatePort,
  validateRoleArn,
  validateSenderName,
} from '../../utils/validationHelper';

interface CreateSenderFormProps {
  senderName: string;
  setSenderName: (name: string) => void;
  senderType: 'smtp' | 'ses';
  setSenderType: (senderType: 'smtp' | 'ses') => void;
  email: string;
  setEmail: (email: string) => void;
  host: string;
  setHost: (host: string) => void;
  port: string;
  setPort: (port: string) => void;
  encryption: keyof typeof ENCRYPTION_TYPE;
  setEncryption: (encryption: keyof typeof ENCRYPTION_TYPE) => void;
  roleArn: string;
  setRoleArn: (roleArn: string) => void;
  awsRegion: string;
  setAwsRegion: (awsRegion: string) => void;
  inputErrors: { [key: string]: string[] };
  setInputErrors: (errors: { [key: string]: string[] }) => void;
}

export function CreateSenderForm(props: CreateSenderFormProps) {
  const mainStateContext = useContext(MainContext)!;
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
        helpText="Use a unique, descriptive name. The sender name must contain from 2 to 50 characters. Valid characters are lowercase a-z, 0-9, and - (hyphen)."
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
      <EuiFormRow label="Sender type">
        <EuiRadioGroup
          options={[
            {
              id: 'smtp',
              label: 'SMTP sender',
            },
            {
              id: 'ses',
              label: 'SES sender',
            },
          ]}
          idSelected={props.senderType}
          onChange={(id) => props.setSenderType(id as 'smtp' | 'ses')}
          name="sender type radio group"
        />
      </EuiFormRow>

      <EuiSpacer size="m" />
      {props.senderType === 'smtp' ? (
        <>
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
                SSL or TLS is recommended for security. To use either one, you
                must enter each sender account's credentials to the OpenSearch
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
        </>
      ) : (
        <>
          <EuiFormRow
            label="Email address"
            style={{ maxWidth: '650px' }}
            error={props.inputErrors.email.join(' ')}
            isInvalid={props.inputErrors.email.length > 0}
          >
            <EuiFieldText
              fullWidth
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

          <EuiSpacer size="m" />
          <EuiFlexGroup gutterSize="s" style={{ maxWidth: '658px' }}>
            <EuiFlexItem grow={6}>
              <EuiFormRow
                label={
                  mainStateContext.tooltipSupport ? (
                    <span>
                      IAM Role ARN -{' '}
                      <i style={{ fontWeight: 'normal' }}>optional</i>
                    </span>
                  ) : (
                    'IAM Role ARN'
                  )
                }
                error={props.inputErrors.roleArn.join(' ')}
                isInvalid={props.inputErrors.roleArn.length > 0}
              >
                <EuiFieldText
                  placeholder="ARN key"
                  data-test-subj="create-ses-sender-form-role-arn-input"
                  value={props.roleArn}
                  onChange={(e) => props.setRoleArn(e.target.value)}
                  isInvalid={props.inputErrors.roleArn.length > 0}
                  onBlur={() => {
                    if (!mainStateContext.tooltipSupport) {
                      props.setInputErrors({
                        ...props.inputErrors,
                        roleArn: validateRoleArn(props.roleArn),
                      });
                    }
                  }}
                />
              </EuiFormRow>
            </EuiFlexItem>
            <EuiFlexItem grow={4}>
              <EuiFormRow
                label="AWS region"
                error={props.inputErrors.awsRegion.join(' ')}
                isInvalid={props.inputErrors.awsRegion.length > 0}
              >
                <EuiFieldText
                  placeholder="us-east-1"
                  data-test-subj="create-ses-sender-form-aws-region-input"
                  value={props.awsRegion}
                  onChange={(e) => props.setAwsRegion(e.target.value)}
                  isInvalid={props.inputErrors.awsRegion.length > 0}
                  onBlur={() => {
                    props.setInputErrors({
                      ...props.inputErrors,
                      awsRegion: validateAwsRegion(props.awsRegion),
                    });
                  }}
                />
              </EuiFormRow>
            </EuiFlexItem>
          </EuiFlexGroup>
        </>
      )}

      <EuiSpacer size="m" />
    </>
  );
}
