/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { EuiFieldText, EuiFormRow } from '@elastic/eui';
import React, { useContext } from 'react';
import { CreateChannelContext } from '../CreateChannel';
import { validateWebhookURL } from '../utils/validationHelper';

interface ChimeSettingsProps {
  chimeWebhook: string;
  setChimeWebhook: (url: string) => void;
}

export function ChimeSettings(props: ChimeSettingsProps) {
  const context = useContext(CreateChannelContext)!;

  return (
    <EuiFormRow
      label="Webhook URL"
      style={{ maxWidth: '700px' }}
      error={context.inputErrors.chimeWebhook.join(' ')}
      isInvalid={context.inputErrors.chimeWebhook.length > 0}
    >
      <EuiFieldText
        fullWidth
        data-test-subj="create-channel-chime-webhook-input"
        placeholder="https://hooks.chime.aws/incomingwebhooks/XXXXX..."
        value={props.chimeWebhook}
        onChange={(e) => props.setChimeWebhook(e.target.value)}
        isInvalid={context.inputErrors.chimeWebhook.length > 0}
        onBlur={() => {
          context.setInputErrors({
            ...context.inputErrors,
            chimeWebhook: validateWebhookURL(props.chimeWebhook),
          });
        }}
      />
    </EuiFormRow>
  );
}
