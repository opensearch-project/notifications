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

import { EuiFieldText, EuiFormRow } from '@elastic/eui';
import React, { useContext } from 'react';
import { CreateChannelContext } from '../CreateChannel';
import { validateSlackWebhook } from '../utils/validationHelper';

interface SlackSettingsProps {
  slackWebhook: string;
  setSlackWebhook: (url: string) => void;
}

export function SlackSettings(props: SlackSettingsProps) {
  const context = useContext(CreateChannelContext)!;

  return (
    <EuiFormRow
      label="Slack webhook URL"
      style={{ maxWidth: '700px' }}
      error="Slack webhook URL is required."
      isInvalid={context.inputErrors.slackWebhook}
    >
      <EuiFieldText
        fullWidth
        placeholder="https://hook.slack.com/services/T0000000000/B0000000/XXXXXXXXXXXXXXX"
        value={props.slackWebhook}
        onChange={(e) => props.setSlackWebhook(e.target.value)}
        onBlur={() => {
          const error = validateSlackWebhook(props.slackWebhook);
          if (error !== context.inputErrors.slackWebhook) {
            context.setInputErrors({
              ...context.inputErrors,
              slackWebhook: error,
            });
          }
        }}
      />
    </EuiFormRow>
  );
}
