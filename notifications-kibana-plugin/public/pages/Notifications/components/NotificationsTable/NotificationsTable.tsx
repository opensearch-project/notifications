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
import React from 'react';
import {
  ChannelStatus,
  NotificationItem,
} from '../../../../../models/interfaces';
import { ContentPanel } from '../../../../components/ContentPanel';
import { ModalConsumer } from '../../../../components/Modal';
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
}

export function NotificationsTable(props: NotificationsTableProps) {
  const columns: EuiTableFieldDataColumnType<NotificationItem>[] = [
    {
      field: 'title',
      name: 'Notification title',
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
      field: 'source',
      name: 'Notification source',
      sortable: true,
      truncateText: true,
    },
    {
      field: 'severity', // we don't care about the field as we're using the whole item in render
      name: 'Severity',
      sortable: true,
      truncateText: false,
      textOnly: true,
    },
    {
      field: 'lastUpdatedTime',
      name: 'Time sent',
      sortable: true,
      truncateText: false,
      render: renderTime,
      dataType: 'date',
    },
    {
      field: 'status',
      name: 'Sent status',
      sortable: true,
      // TODO: render the errors detail with a modal
      render: (status, item: NotificationItem) => {
        const color = status == 'Success' ? 'success' : 'danger';
        const label = status == 'Success' ? 'Sent' : 'Errors';
        return (
          <EuiHealth color={color}>
            {status === 'Success' ? (
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
      field: 'statusList',
      name: 'Channels',
      sortable: true,
      truncateText: true,
      render: (status: ChannelStatus[]) =>
        status.length === 1
          ? status[0].configName
          : `${status.length} channels`,
    },
    {
      field: 'statusList', // we don't care about the field as we're using the whole item in render
      name: 'Channel types',
      sortable: true,
      truncateText: false,
      render: (status: ChannelStatus[]) =>
        status.map((channel) => channel.configType).join(', '),
    },
  ];

  return (
    <>
      <ContentPanel
        bodyStyles={{ padding: 'initial' }}
        title="Notification History"
        titleSize="m"
      >
        <EuiBasicTable
          columns={columns}
          tableLayout="auto"
          itemId="id"
          isSelectable={true}
          items={props.items}
          noItemsMessage={
            // TODO: add empty prompt component, pending UXDR
            <div>no item</div>
          }
          onChange={props.onTableChange}
          pagination={props.pagination}
          sorting={props.sorting}
        />
      </ContentPanel>
    </>
  );
}
