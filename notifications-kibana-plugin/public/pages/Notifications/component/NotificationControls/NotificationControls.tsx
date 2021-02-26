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

import React from 'react';
import {
  EuiFieldSearch,
  EuiFlexGroup,
  EuiFlexItem,
  EuiPagination,
  EuiSelect,
} from '@elastic/eui';
import {
  NOTIFICATION_STATUS,
  NOTIFICATION_SOURCE,
} from '../../../../utils/constants';

//TODO: add NotificationControlsProps

const severityOptions = [
  { value: 'ALL', text: 'Severity' },
  { value: '1', text: '1' },
  { value: '2', text: '2' },
  { value: '3', text: '3' },
  { value: '4', text: '4' },
  { value: '5', text: '5' },
];

const statusOptions = [
  { value: 'ALL', text: 'Status' },
  { value: NOTIFICATION_STATUS.SENT, text: 'Sent' },
  { value: NOTIFICATION_STATUS.ERROR, text: 'Error' },
];

const sourceOptions = [
  { value: 'ALL', text: 'Source' },
  { value: NOTIFICATION_SOURCE.REPORTING, text: 'Reporting' },
  { value: NOTIFICATION_SOURCE.ALERTING, text: 'Alerting' },
];

const NotificationControls = ({
  activePage,
  pageCount,
  search,
  severity = severityOptions[0],
  status = statusOptions[0],
  source = sourceOptions[0],
  onSearchChange,
  onSeverityChange,
  onStatusChange,
  onPageChange,
  onSourceChange,
}) => (
  <EuiFlexGroup style={{ padding: '0px 5px' }}>
    <EuiFlexItem>
      <EuiFieldSearch
        fullWidth={true}
        placeholder="Search"
        onChange={onSearchChange}
        value={search}
      />
    </EuiFlexItem>
    <EuiFlexItem grow={false}>
      <EuiSelect
        options={severityOptions}
        value={severity}
        onChange={onSeverityChange}
      />
    </EuiFlexItem>
    <EuiFlexItem grow={false}>
      <EuiSelect
        options={statusOptions}
        value={status}
        onChange={onStatusChange}
      />
    </EuiFlexItem>
    <EuiFlexItem grow={false}>
      <EuiSelect
        options={sourceOptions}
        value={source}
        onChange={onSourceChange}
      />
    </EuiFlexItem>
    {pageCount > 1 && (
      <EuiFlexItem grow={false} style={{ justifyContent: 'center' }}>
        <EuiPagination
          pageCount={pageCount}
          activePage={activePage}
          onPageClick={onPageChange}
        />
      </EuiFlexItem>
    )}
  </EuiFlexGroup>
);

export default NotificationControls;
