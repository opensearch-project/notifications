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

import { EuiSearchBar } from '@elastic/eui';
import { FieldValueSelectionFilterConfigType } from '@elastic/eui/src/components/search_bar/filters/field_value_selection_filter';
import React from 'react';
import { CHANNEL_TYPE } from '../../../../public/utils/constants';

interface ChannelControlsProps {
  search: string;
  onSearchChange: (search: string) => void;
}

export const ChannelControls = (props: ChannelControlsProps) => {
  const filters: FieldValueSelectionFilterConfigType[] = [
    {
      type: 'field_value_selection',
      name: 'State',
      field: 'enabled',
      multiSelect: false,
      options: [
        { name: 'Active', value: true },
        { name: 'Muted', value: false },
      ],
    },
    {
      type: 'field_value_selection',
      name: 'Type',
      field: 'type',
      multiSelect: 'or',
      options: [
        { name: CHANNEL_TYPE.SLACK, value: 'slack' },
        { name: CHANNEL_TYPE.EMAIL, value: 'email' },
        { name: CHANNEL_TYPE.CHIME, value: 'chime' },
        { name: CHANNEL_TYPE.CUSTOM_WEBHOOK, value: 'custom_webhook' },
        { name: CHANNEL_TYPE.SES, value: 'ses' },
        { name: CHANNEL_TYPE.SNS, value: 'sns' },
      ],
    },
    {
      type: 'field_value_selection',
      name: 'Source',
      field: 'source',
      multiSelect: 'or',
      options: [
        { name: 'Alerting', value: 'alerting' },
        { name: 'Reporting', value: 'reporting' },
        { name: 'ISM', value: 'ISM' },
      ],
    },
  ];

  return (
    <EuiSearchBar
      defaultQuery={EuiSearchBar.Query.MATCH_ALL}
      box={{
        placeholder: 'Search',
        incremental: false,
      }}
      filters={filters}
      onChange={(args) => {
        if (args.query) {
          console.log(EuiSearchBar.Query.toESQuery(args.query));
          console.log(EuiSearchBar.Query.toESQueryString(args.query));
          props.onSearchChange(EuiSearchBar.Query.toESQueryString(args.query));
        }
      }}
    />
  );
};
