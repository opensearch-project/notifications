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
  EuiCallOut,
  EuiFieldText,
  EuiFormRow,
  EuiLink,
  EuiSpacer,
  EuiText,
} from '@elastic/eui';
import React, { useContext } from 'react';
import { ALERTING_DOCUMENTATION_LINK } from '../../../utils/constants';
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
            title="Using Amazon SNS outside of AWS"
            style={{ maxWidth: 720 }}
          >
            <div>
              If your cluster is not running on AWS, you must add your access
              key, secret key, and optional session token to the OpenSearch
              keystore.{' '}
              <EuiLink href={ALERTING_DOCUMENTATION_LINK} target="_blank" external>
                Learn more
              </EuiLink>
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
