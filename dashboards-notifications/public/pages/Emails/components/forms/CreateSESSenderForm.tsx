/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import {
  EuiFieldText,
  EuiFlexGroup,
  EuiFlexItem,
  EuiFormRow,
  EuiSpacer,
} from '@elastic/eui';
import React, { useContext } from 'react';
import { MainContext } from '../../../Main/Main';
import {
  validateAwsRegion,
  validateEmail,
  validateRoleArn,
  validateSenderName,
} from '../../utils/validationHelper';

interface CreateSESSenderFormProps {
  senderName: string;
  setSenderName: (name: string) => void;
  email: string;
  setEmail: (email: string) => void;
  roleArn: string;
  setRoleArn: (roleArn: string) => void;
  awsRegion: string;
  setAwsRegion: (awsRegion: string) => void;
  inputErrors: { [key: string]: string[] };
  setInputErrors: (errors: { [key: string]: string[] }) => void;
}

export function CreateSESSenderForm(props: CreateSESSenderFormProps) {
  const mainStateContext = useContext(MainContext)!;
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
          data-test-subj="create-ses-sender-form-name-input"
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
      <EuiFormRow
        label="Email address"
        style={{ maxWidth: '650px' }}
        error={props.inputErrors.email.join(' ')}
        isInvalid={props.inputErrors.email.length > 0}
      >
        <EuiFieldText
          fullWidth
          placeholder="name@example.com"
          data-test-subj="create-ses-sender-form-email-input"
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

      <EuiSpacer size="m" />
    </>
  );
}
