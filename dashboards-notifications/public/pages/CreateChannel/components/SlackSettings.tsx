/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
