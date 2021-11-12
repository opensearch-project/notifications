/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
import React, { useState } from 'react';
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
  const [query, setQuery] = useState(props.search);

  return (
    <>
      <EuiFlexGroup gutterSize="s" alignItems="center">
        <EuiFlexItem>
          <EuiFieldSearch
            data-test-subj="notifications-search-bar-input"
            fullWidth
            isClearable={false}
            placeholder="Search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onSearch={props.setSearch}
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
          <EuiButton
            iconType="refresh"
            onClick={() => {
              props.setSearch(query);
              props.refresh();
            }}
          >
            Refresh
          </EuiButton>
        </EuiFlexItem>
      </EuiFlexGroup>
      <EuiSpacer size="s" />
      <Filters filters={props.filters} setFilters={props.setFilters} />
    </>
  );
}
