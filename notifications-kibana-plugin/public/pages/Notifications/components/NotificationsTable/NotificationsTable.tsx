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
import React, { useState } from 'react';
import { NotificationItem } from '../../../../../models/interfaces';
import { ContentPanel } from '../../../../components/ContentPanel';
import { ModalConsumer } from '../../../../components/Modal';
import { renderTime } from '../../../../utils/helpers';
import { navigateToChannelDetail } from '../../utils/helpers';
import ErrorDetailModal from '../ErrorDetailModal/ErrorDetailModel';
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
  const [flyoutOpen, setFlyoutOpen] = useState(false);

  const columns: EuiTableFieldDataColumnType<NotificationItem>[] = [
    {
      field: 'title',
      name: 'Notification title',
      sortable: true,
      truncateText: true,
      width: '150px',
      render: (title) => (
        //TODO: pending UX
        <EuiLink href="#" target="_blank">
          {title}
        </EuiLink>
      ),
    },
    {
      field: 'source',
      name: 'Notification source',
      sortable: true,
      truncateText: true,
      width: '150px',
    },
    {
      field: 'severity', // we don't care about the field as we're using the whole item in render
      name: 'Severity',
      sortable: true,
      truncateText: false,
      textOnly: true,
      width: '150px',
    },
    {
      field: 'sentTime',
      name: 'Time sent',
      sortable: true,
      truncateText: false,
      render: renderTime,
      dataType: 'date',
      width: '150px',
    },
    {
      field: 'status.overview',
      name: 'Sent status',
      sortable: true,
      width: '150px',
      // TODO: render the errors detail with a modal
      render: (status, item: NotificationItem) => {
        const color = status == 'Sent' ? 'success' : 'danger';
        const label = status == 'Sent' ? 'Sent' : 'Errors';
        const {
          status: { detail },
        } = item;
        return (
          <EuiHealth color={color}>
            {status === 'Sent' ? (
              label
            ) : (
              <ModalConsumer>
                {({ onShow }) => (
                  <EuiLink onClick={() => onShow(ErrorDetailModal, { detail })}>
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
      field: 'channel.name',
      name: 'Channels',
      sortable: true,
      truncateText: true,
      width: '150px',
      render: (name: string, item: NotificationItem) => (
        <EuiLink onClick={() => navigateToChannelDetail(item)}>{name}</EuiLink>
      ),
    },
    {
      field: 'channel.type', // we don't care about the field as we're using the whole item in render
      name: 'Channel types',
      sortable: true,
      truncateText: false,
      textOnly: true,
      width: '150px',
    },
  ];

  return (
    <>
      <button
        style={{ border: 'solid red', padding: 3 }}
        onClick={() => {
          setFlyoutOpen(true);
        }}
      >
        TEST
      </button>
      <ContentPanel
        bodyStyles={{ padding: 'initial' }}
        title="Notification History"
        titleSize="m"
      >
        <EuiBasicTable
          columns={columns}
          itemId="title"
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
      <TableFlyout flyoutOpen={flyoutOpen} setFlyoutOpen={setFlyoutOpen} />
    </>
  );
}
