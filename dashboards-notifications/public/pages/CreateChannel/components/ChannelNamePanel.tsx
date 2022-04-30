/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { EuiFieldText, EuiFormRow, EuiTextArea } from '@elastic/eui';
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
          error={context.inputErrors.name.join(' ')}
          isInvalid={context.inputErrors.name.length > 0}
        >
          <EuiFieldText
            data-test-subj="create-channel-name-input"
            placeholder="Enter channel name"
            value={props.name}
            onChange={(e) => props.setName(e.target.value)}
            isInvalid={context.inputErrors.name.length > 0}
            onBlur={() => {
              context.setInputErrors({
                ...context.inputErrors,
                name: validateChannelName(props.name),
              });
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
            <EuiTextArea
              data-test-subj="create-channel-description-input"
              placeholder="What is the purpose of this channel?"
              style={{ height: '4.1rem' }}
              value={props.description}
              onChange={(e) => props.setDescription(e.target.value)}
            />
          </>
        </EuiFormRow>
      </ContentPanel>
    </>
  );
}
