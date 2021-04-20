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
  EuiCallOut,
  EuiFieldText,
  EuiFormRow,
  EuiSpacer,
  EuiText,
} from '@elastic/eui';
import React, { useContext } from 'react';
import { CreateChannelContext } from '../CreateChannel';
import { validateArn } from '../utils/validationHelper';

interface SNSSettingsProps {
  isOdfe: boolean;
  topicArn: string;
  setTopicArn: (topicArn: string) => void;
  roleArn: string;
  setRoleArn: (roleArn: string) => void;
}

export function SNSSettings(props: SNSSettingsProps) {
  const context = useContext(CreateChannelContext)!;
  return (
    <>
      <EuiSpacer />
      <EuiFormRow
        label="SNS topic ARN"
        error={context.inputErrors.topicArn.join(' ')}
        isInvalid={context.inputErrors.topicArn.length > 0}
      >
        <EuiFieldText
          placeholder="ARN key"
          value={props.topicArn}
          onChange={(e) => props.setTopicArn(e.target.value)}
          isInvalid={context.inputErrors.topicArn.length > 0}
          onBlur={() => {
            context.setInputErrors({
              ...context.inputErrors,
              topicArn: validateArn(props.topicArn),
            });
          }}
        />
      </EuiFormRow>

      {props.isOdfe ? (
        <>
          <EuiFormRow
            label={
              <span>
                IAM role ARN - <i style={{ fontWeight: 'normal' }}>optional</i>
              </span>
            }
          >
            <>
              <EuiText size="xs" color="subdued">
                IAM role ARN can only be used for clusters running on AWS
                network.
              </EuiText>
              <EuiSpacer size="s" />
              <EuiFieldText
                placeholder="ARN key"
                value={props.roleArn}
                onChange={(e) => props.setRoleArn(e.target.value)}
              />
            </>
          </EuiFormRow>
          <EuiSpacer />
          <EuiCallOut
            title="Using Amazon SNS outside of AWS network"
            style={{ maxWidth: 720 }}
          >
            <div>
              If your cluster is not running on AWS network, IAM user access and
              secret key credentials are required.
              <br />
              Add the following fields to Elastic Keystore:
              <br />
              <br />
              opendistro.alerting.destination.sns.access.key: [accessKey]
              <br />
              opendistro.alerting.destination.sns.secret.key: [secretKey]
            </div>
          </EuiCallOut>
        </>
      ) : (
        <EuiFormRow
          label="IAM role ARN"
          error={context.inputErrors.roleArn.join(' ')}
          isInvalid={context.inputErrors.roleArn.length > 0}
        >
          <EuiFieldText
            placeholder="ARN key"
            value={props.roleArn}
            onChange={(e) => props.setRoleArn(e.target.value)}
            isInvalid={context.inputErrors.roleArn.length > 0}
            onBlur={() => {
              context.setInputErrors({
                ...context.inputErrors,
                roleArn: validateArn(props.roleArn),
              });
            }}
          />
        </EuiFormRow>
      )}
    </>
  );
}
