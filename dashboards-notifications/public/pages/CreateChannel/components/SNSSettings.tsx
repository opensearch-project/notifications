/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
import { DOCUMENTATION_LINK } from '../../../utils/constants';
import { MainContext } from '../../Main/Main';
import { CreateChannelContext } from '../CreateChannel';
import { validateArn } from '../utils/validationHelper';

interface SNSSettingsProps {
  topicArn: string;
  setTopicArn: (topicArn: string) => void;
  roleArn: string;
  setRoleArn: (roleArn: string) => void;
}

export function SNSSettings(props: SNSSettingsProps) {
  const context = useContext(CreateChannelContext)!;
  const mainStateContext = useContext(MainContext)!;
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
          data-test-subj="sns-settings-topic-arn-input"
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

      {mainStateContext.tooltipSupport ? (
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
                data-test-subj="sns-settings-role-arn-input"
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
              If your cluster is not running on AWS, you must configure aws
              credentials on your OpenSearch cluster.{' '}
              <EuiLink href={DOCUMENTATION_LINK} target="_blank" external>
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
            data-test-subj="sns-settings-role-arn-input"
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
