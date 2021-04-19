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
