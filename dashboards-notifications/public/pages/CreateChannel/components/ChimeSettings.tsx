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
import { validateWebhook } from '../utils/validationHelper';

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
      error="Chime webhook URL is required."
      isInvalid={context.inputErrors.chimeWebhook}
    >
      <EuiFieldText
        fullWidth
        placeholder="Enter Chime webhook URL"
        value={props.chimeWebhook}
        onChange={(e) => props.setChimeWebhook(e.target.value)}
        onBlur={() => {
          const error = validateWebhook(props.chimeWebhook);
          if (error !== context.inputErrors.chimeWebhook) {
            context.setInputErrors({
              ...context.inputErrors,
              chimeWebhook: error,
            });
          }
        }}
      />
    </EuiFormRow>
  );
}
