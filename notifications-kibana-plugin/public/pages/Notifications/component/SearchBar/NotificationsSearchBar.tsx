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
  EuiButton,
  EuiFieldSearch,
  EuiFlexGroup,
  EuiFlexItem,
  EuiSpacer,
  EuiSuperDatePicker,
  ShortDate,
} from '@elastic/eui';
import React from 'react';
import { Filters, FilterType } from './Filter/Filters';

interface NotificationsSearchBarProps {
  startTime: ShortDate;
  setStartTime: (startTime: ShortDate) => void;
  endTime: ShortDate;
  setEndTime: (endTime: ShortDate) => void;
  search: string;
  setSearch: (search: string) => void;
  filters: FilterType[];
  setFilters: (filters: FilterType[]) => void;
  refresh: () => void;
}

export function NotificationsSearchBar(props: NotificationsSearchBarProps) {
  return (
    <>
      <EuiFlexGroup gutterSize="s" alignItems="center">
        <EuiFlexItem>
          <EuiFieldSearch
            fullWidth
            isClearable={false}
            placeholder="Search"
            value={props.search}
            onChange={(e) => props.setSearch(e.target.value)}
            onSearch={props.refresh}
          />
        </EuiFlexItem>
        <EuiFlexItem grow={false}>
          <EuiSuperDatePicker
            start={props.startTime}
            end={props.endTime}
            showUpdateButton={false}
            onTimeChange={(e) => {
              props.setStartTime(e.start);
              props.setEndTime(e.end);
            }}
          />
        </EuiFlexItem>
        <EuiFlexItem grow={false}>
          <EuiButton size="s" onClick={props.refresh}>
            Refresh
          </EuiButton>
        </EuiFlexItem>
      </EuiFlexGroup>
      <EuiSpacer size="s" />
      <Filters filters={props.filters} setFilters={props.setFilters} />
    </>
  );
}
