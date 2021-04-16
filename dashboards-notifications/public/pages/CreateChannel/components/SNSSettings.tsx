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
  snsArn: string;
  setSnsArn: (snsArn: string) => void;
  iamArn: string;
  setIamArn: (iamArn: string) => void;
}

export function SNSSettings(props: SNSSettingsProps) {
  const context = useContext(CreateChannelContext)!;
  return (
    <>
      <EuiSpacer />
      <EuiFormRow
        label="SNS topic ARN"
        error={context.inputErrors.snsArn.join(' ')}
        isInvalid={context.inputErrors.snsArn.length > 0}
      >
        <EuiFieldText
          placeholder="ARN key"
          value={props.snsArn}
          onChange={(e) => props.setSnsArn(e.target.value)}
          onBlur={() => {
            context.setInputErrors({
              ...context.inputErrors,
              snsArn: validateArn(props.snsArn),
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
                value={props.iamArn}
                onChange={(e) => props.setIamArn(e.target.value)}
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
          error={context.inputErrors.iamArn.join(' ')}
          isInvalid={context.inputErrors.iamArn.length > 0}
        >
          <EuiFieldText
            placeholder="ARN key"
            value={props.iamArn}
            onChange={(e) => props.setIamArn(e.target.value)}
            onBlur={() => {
              context.setInputErrors({
                ...context.inputErrors,
                iamArn: validateArn(props.iamArn),
              });
            }}
          />
        </EuiFormRow>
      )}
    </>
  );
}
