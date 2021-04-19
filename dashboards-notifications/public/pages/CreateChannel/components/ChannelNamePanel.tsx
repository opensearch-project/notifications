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

import { EuiFieldText, EuiFormRow, EuiText, EuiTextArea } from '@elastic/eui';
import React, { useContext } from 'react';
import { ContentPanel } from '../../../components/ContentPanel';
import { CreateChannelContext } from '../CreateChannel';
import { validateChannelName } from '../utils/validationHelper';

interface ChannelNamePanelProps {
  name: string;
  setName: (name: string) => void;
  description: string;
  setDescription: (description: string) => void;
}

export function ChannelNamePanel(props: ChannelNamePanelProps) {
  const context = useContext(CreateChannelContext)!;
  return (
    <>
      <ContentPanel
        bodyStyles={{ padding: 'initial' }}
        title="Name and description"
        titleSize="s"
      >
        <EuiFormRow
          label="Name"
          error="Name is required."
          isInvalid={context.inputErrors.name}
        >
          <EuiFieldText
            placeholder="Enter channel name"
            value={props.name}
            onChange={(e) => props.setName(e.target.value)}
            onBlur={() => {
              const error = validateChannelName(props.name);
              if (error !== context.inputErrors.name) {
                context.setInputErrors({
                  ...context.inputErrors,
                  name: error,
                });
              }
            }}
          />
        </EuiFormRow>
        <EuiFormRow
          label={
            <span>
              Description - <i style={{ fontWeight: 'normal' }}>optional</i>
            </span>
          }
        >
          <>
            <EuiText size="xs" color="subdued">
              Describe the purpose of the channel.
            </EuiText>
            <EuiTextArea
              placeholder="Describe the channel"
              style={{ height: '2.8rem' }}
              value={props.description}
              onChange={(e) => props.setDescription(e.target.value)}
            />
          </>
        </EuiFormRow>
      </ContentPanel>
    </>
  );
}
