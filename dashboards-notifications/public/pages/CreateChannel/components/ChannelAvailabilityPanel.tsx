/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import {
  EuiCheckboxGroup,
  EuiCheckboxGroupOption,
  EuiFormRow,
  EuiSpacer,
  EuiText,
} from '@elastic/eui';
import React from 'react';
import { ContentPanel } from '../../../components/ContentPanel';
import { NOTIFICATION_SOURCE } from '../../../utils/constants';

interface ChannelAvailabilityPanelProps {
  sourceCheckboxIdToSelectedMap: { [x: string]: boolean };
  setSourceCheckboxIdToSelectedMap: (map: { [x: string]: boolean }) => void;
}

export function ChannelAvailabilityPanel(props: ChannelAvailabilityPanelProps) {
  const sourceOptions: EuiCheckboxGroupOption[] = Object.entries(
    NOTIFICATION_SOURCE
  ).map(([key, value]) => ({
    id: key,
    label: value,
  }));

  return (
    <ContentPanel
      bodyStyles={{ padding: 'initial' }}
      title="Availability"
      titleSize="s"
    >
      <EuiFormRow label="Notification source">
        <>
          <EuiText size="xs" color="subdued">
            Select the features that can use this channel.
          </EuiText>
          <EuiSpacer size="s" />
          <EuiCheckboxGroup
            options={sourceOptions}
            idToSelectedMap={props.sourceCheckboxIdToSelectedMap}
            onChange={(optionId: string) => {
              props.setSourceCheckboxIdToSelectedMap({
                ...props.sourceCheckboxIdToSelectedMap,
                ...{
                  [optionId]: !props.sourceCheckboxIdToSelectedMap[optionId],
                },
              });
            }}
          />
        </>
      </EuiFormRow>
    </ContentPanel>
  );
}
