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
