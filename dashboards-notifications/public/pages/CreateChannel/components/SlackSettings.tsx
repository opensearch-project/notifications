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

import { EuiFieldText, EuiFormRow } from '@elastic/eui';
import React, { useContext } from 'react';
import { CreateChannelContext } from '../CreateChannel';
import { validateWebhookURL } from '../utils/validationHelper';

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
      error={context.inputErrors.slackWebhook.join(' ')}
      isInvalid={context.inputErrors.slackWebhook.length > 0}
    >
      <EuiFieldText
        fullWidth
        data-test-subj="create-channel-slack-webhook-input"
        placeholder="https://hooks.slack.com/services/XXXXX/XXXXX/XXXXX..."
        value={props.slackWebhook}
        onChange={(e) => props.setSlackWebhook(e.target.value)}
        isInvalid={context.inputErrors.slackWebhook.length > 0}
        onBlur={() => {
          context.setInputErrors({
            ...context.inputErrors,
            slackWebhook: validateWebhookURL(props.slackWebhook),
          });
        }}
      />
    </EuiFormRow>
  );
}
