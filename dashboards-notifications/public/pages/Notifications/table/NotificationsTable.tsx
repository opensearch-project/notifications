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

import { NotificationItem } from '../../../../models/interfaces';
import { ContentPanel } from '../../../components/ContentPanel';
import {
  EuiBasicTable,
  EuiButtonIcon,
  EuiDescriptionList,
  EuiFlexGroup,
  EuiFlexItem,
  EuiHealth,
  EuiLink,
  EuiTableFieldDataColumnType,
  EuiTableRow,
  EuiTableRowCell,
  EuiTableSortingType,
  RIGHT_ALIGNMENT,
} from '@elastic/eui';
import React, { useState } from 'react';
import { Criteria } from '@elastic/eui/src/components/basic_table/basic_table';
import { Pagination } from '@elastic/eui/src/components/basic_table/pagination_bar';
import { renderTime } from '../../../utils/helpers';
import { ModalConsumer } from '../../../components/Modal';
import ErrorDetailModal from '../component/ErrorDetailModal/ErrorDetailModel';
import { navigateToChannelDetail } from '../utils/helpers';

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
  const [itemIdToExpandedRowMap, setItemIdToExpandedRowMap] = useState<{
    [id: string]: React.ReactNode;
  }>({});

  const toggleDetails = (item: NotificationItem) => {
    const itemIdToExpandedRowMapValues = { ...itemIdToExpandedRowMap };
    if (itemIdToExpandedRowMapValues[item.title]) {
      delete itemIdToExpandedRowMapValues[item.title];
    } else {
      itemIdToExpandedRowMapValues[item.title] = (
      <div>h</div>
      );
    }
    setItemIdToExpandedRowMap(itemIdToExpandedRowMapValues);
  };

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
      field: 'status.overview',
      name: 'Notification status',
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
      field: 'sentTime',
      name: 'Time sent',
      sortable: true,
      truncateText: false,
      render: renderTime,
      dataType: 'date',
      width: '150px',
    },
    {
      field: 'source',
      name: 'Notification source',
      sortable: true,
      truncateText: true,
      width: '150px',
    },
    // TODO: the following 3 columns
    {
      field: 'channel.name',
      name: 'Channel',
      sortable: true,
      truncateText: true,
      width: '150px',
      render: (name: string, item: NotificationItem) => (
        <EuiLink onClick={() => navigateToChannelDetail(item)}>{name}</EuiLink>
      ),
    },
    {
      field: 'channel.type', // we don't care about the field as we're using the whole item in render
      name: 'Channel type',
      sortable: true,
      truncateText: false,
      textOnly: true,
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
      field: 'title',
      name: '',
      width: '40px',
      isExpander: true,
      render: (title, item) => (
        <EuiButtonIcon
          onClick={() => toggleDetails(item)}
          aria-label={
            itemIdToExpandedRowMap[item.title] ? 'Collapse' : 'Expand'
          }
          iconType={
            itemIdToExpandedRowMap[item.title] ? 'arrowUp' : 'arrowDown'
          }
        />
      ),
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
          itemIdToExpandedRowMap={itemIdToExpandedRowMap}
          isExpandable={true}
        />
      </ContentPanel>
    </>
  );
}
