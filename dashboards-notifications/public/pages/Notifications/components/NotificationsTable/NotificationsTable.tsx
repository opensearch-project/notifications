/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
  EuiBasicTable,
  EuiHealth,
  EuiLink,
  EuiTableFieldDataColumnType,
  EuiTableSortingType,
} from '@elastic/eui';
import { Criteria } from '@elastic/eui/src/components/basic_table/basic_table';
import { Pagination } from '@elastic/eui/src/components/basic_table/pagination_bar';
import _ from 'lodash';
import React from 'react';
import {
  ChannelStatus,
  NotificationItem,
} from '../../../../../models/interfaces';
import { ContentPanel } from '../../../../components/ContentPanel';
import { ModalConsumer } from '../../../../components/Modal';
import {
  CHANNEL_TYPE,
  NOTIFICATION_SOURCE,
  SEVERITY_TYPE,
} from '../../../../utils/constants';
import { renderTime } from '../../../../utils/helpers';
import { TableFlyout } from './Flyout/TableFlyout';

interface NotificationsTableProps {
  items: NotificationItem[];
  onTableChange: ({
    page: tablePage,
    sort,
  }: Criteria<NotificationItem>) => void;
  pagination: Pagination;
  sorting: EuiTableSortingType<NotificationItem>;
  loading: boolean;
}

export function NotificationsTable(props: NotificationsTableProps) {
  const columns: EuiTableFieldDataColumnType<NotificationItem>[] = [
    {
      field: 'event_source.title',
      name: 'Notification',
      sortable: true,
      truncateText: true,
      render: (title, item) => (
        <ModalConsumer>
          {({ onShow }) => (
            <EuiLink
              onClick={() => onShow(TableFlyout, { notificationItem: item })}
            >
              {title}
            </EuiLink>
          )}
        </ModalConsumer>
      ),
    },
    {
      field: 'event_source.feature',
      name: 'Source type',
      sortable: true,
      truncateText: true,
      render: (source) => _.get(NOTIFICATION_SOURCE, source, '-'),
    },
    {
      field: 'event_source.severity',
      name: 'Severity',
      sortable: true,
      truncateText: false,
      render: (severity) => _.get(SEVERITY_TYPE, severity, '-'),
    },
    {
      field: 'last_updated_time_ms',
      name: 'Time sent',
      sortable: true,
      truncateText: false,
      render: renderTime,
      dataType: 'date',
    },
    {
      field: 'success',
      name: 'Sent status',
      sortable: false,
      render: (success, item: NotificationItem) => {
        const color = success ? 'success' : 'danger';
        const label = success ? 'Sent' : 'Error';
        return (
          <EuiHealth color={color}>
            {success ? (
              label
            ) : (
              <ModalConsumer>
                {({ onShow }) => (
                  <EuiLink
                    onClick={() =>
                      onShow(TableFlyout, { notificationItem: item })
                    }
                  >
                    {label}
                  </EuiLink>
                )}
              </ModalConsumer>
            )}
          </EuiHealth>
        );
      },
    },
    {
      field: 'status_list',
      name: 'Channels',
      sortable: false,
      truncateText: true,
      render: (status_list: ChannelStatus[]) =>
        status_list.length === 1
          ? status_list[0].config_name
          : `${status_list.length} channels`,
    },
    {
      field: 'status_list',
      name: 'Channel types',
      sortable: false,
      truncateText: false,
      render: (status_list: ChannelStatus[]) =>
        status_list
          .map((channel) => _.get(CHANNEL_TYPE, channel.config_type, '-'))
          .join(', '),
    },
  ];

  return (
    <>
      <ContentPanel
        bodyStyles={{ padding: 'initial' }}
        title="Notification history"
        titleSize="m"
      >
        <EuiBasicTable
          columns={columns}
          tableLayout="auto"
          itemId="id"
          isSelectable={true}
          items={props.items}
          onChange={props.onTableChange}
          pagination={props.pagination}
          sorting={props.sorting}
          loading={props.loading}
        />
      </ContentPanel>
    </>
  );
}
