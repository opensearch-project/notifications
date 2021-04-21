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
  EuiBasicTable,
  EuiButton,
  EuiEmptyPrompt,
  EuiFieldSearch,
  EuiHorizontalRule,
  EuiTableFieldDataColumnType,
  EuiTableSortingType,
} from '@elastic/eui';
import { Criteria } from '@elastic/eui/src/components/basic_table/basic_table';
import { Pagination } from '@elastic/eui/src/components/basic_table/pagination_bar';
import React, { Component } from 'react';
import { SORT_DIRECTION } from '../../../../../common';
import { SenderItemType, TableState } from '../../../../../models/interfaces';
import {
  ContentPanel,
  ContentPanelActions,
} from '../../../../components/ContentPanel';
import { ModalConsumer } from '../../../../components/Modal';
import { ServicesContext } from '../../../../services';
import { ROUTES } from '../../../../utils/constants';
import { getErrorMessage } from '../../../../utils/helpers';
import { DEFAULT_PAGE_SIZE_OPTIONS } from '../../../Notifications/utils/constants';
import { DeleteSenderModal } from '../modals/DeleteSenderModal';

interface SendersTableProps {}

interface SendersTableState extends TableState<SenderItemType> {}

export class SendersTable extends Component<
  SendersTableProps,
  SendersTableState
> {
  static contextType = ServicesContext;
  columns: EuiTableFieldDataColumnType<SenderItemType>[];

  constructor(props: SendersTableProps) {
    super(props);

    this.state = {
      total: 0,
      from: 0,
      size: 5,
      search: '',
      sortField: 'name',
      sortDirection: SORT_DIRECTION.ASC,
      items: [],
      selectedItems: [],
      loading: true,
    };

    this.columns = [
      {
        field: 'name',
        name: 'Name',
        sortable: true,
        truncateText: true,
        width: '200px',
      },
      {
        field: 'from',
        name: 'Outbound email address',
        sortable: true,
        truncateText: true,
        width: '200px',
      },
      {
        field: 'host',
        name: 'Host',
        sortable: true,
        truncateText: true,
        width: '200px',
      },
      {
        field: 'port',
        name: 'Port',
        sortable: true,
        truncateText: true,
        width: '200px',
      },
      {
        field: 'method',
        name: 'Encryption method',
        sortable: true,
        truncateText: true,
        width: '200px',
      },
    ];
  }

  async componentDidMount() {
    this.setState({ loading: true });
    try {
      const queryObject = {
        from: this.state.from,
        size: this.state.size,
        search: this.state.search,
        sortField: this.state.sortField,
        sortDirection: this.state.sortDirection,
      };
      const senders = await this.context.notificationService.getSenders(
        queryObject
      );
      this.setState({ items: senders, total: senders.length });
    } catch (error) {
      this.context.notifications.toasts.addDanger(
        getErrorMessage(error, 'There was a problem loading senders.')
      );
    }
    this.setState({ loading: false });
  }

  onTableChange = ({
    page: tablePage,
    sort,
  }: Criteria<SenderItemType>): void => {
    const { index: page, size } = tablePage!;
    const { field: sortField, direction: sortDirection } = sort!;
    this.setState({ from: page * size, size, sortField, sortDirection });
  };

  onSelectionChange = (selectedItems: SenderItemType[]): void => {
    this.setState({ selectedItems });
  };

  onSearchChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    this.setState({ from: 0, search: e.target.value });
  };

  onPageChange = (page: number): void => {
    const { size } = this.state;
    this.setState({ from: page * size });
  };

  render() {
    const {
      total,
      from,
      size,
      search,
      sortField,
      sortDirection,
      selectedItems,
      items,
      loading,
    } = this.state;

    const filterIsApplied = !!search;
    const page = Math.floor(from / size);

    const pagination: Pagination = {
      pageIndex: page,
      pageSize: size,
      pageSizeOptions: DEFAULT_PAGE_SIZE_OPTIONS,
      totalItemCount: total,
    };

    const sorting: EuiTableSortingType<SenderItemType> = {
      sort: {
        direction: sortDirection,
        field: sortField,
      },
    };

    const selection = {
      selectable: () => true,
      onSelectionChange: this.onSelectionChange,
    };

    return (
      <>
        <ContentPanel
          actions={
            <ContentPanelActions
              actions={[
                {
                  component: (
                    <ModalConsumer>
                      {({ onShow }) => (
                        <EuiButton
                          disabled={this.state.selectedItems.length === 0}
                          onClick={() =>
                            onShow(DeleteSenderModal, {
                              senders: this.state.selectedItems,
                            })
                          }
                        >
                          Delete
                        </EuiButton>
                      )}
                    </ModalConsumer>
                  ),
                },
                {
                  component: (
                    <EuiButton
                      disabled={this.state.selectedItems.length !== 1}
                      onClick={() =>
                        location.assign(
                          `#${ROUTES.EDIT_SENDER}/${this.state.selectedItems[0]?.id}`
                        )
                      }
                    >
                      Edit
                    </EuiButton>
                  ),
                },
                {
                  component: (
                    <EuiButton fill href={`#${ROUTES.CREATE_SENDER}`}>
                      Create sender
                    </EuiButton>
                  ),
                },
              ]}
            />
          }
          bodyStyles={{ padding: 'initial' }}
          title="Senders"
          titleSize="m"
          total={this.state.total}
        >
          <EuiFieldSearch
            fullWidth={true}
            placeholder="Search"
            onChange={this.onSearchChange}
            value={search}
          />
          <EuiHorizontalRule margin="s" />

          <EuiBasicTable
            columns={this.columns}
            items={items}
            itemId="name"
            isSelectable={true}
            selection={selection}
            noItemsMessage={
              <EuiEmptyPrompt
                title={<h2>No senders to display</h2>}
                body="Set up outbound email server by creating a sender. You will select a sender when configuring email channels."
                actions={
                  <EuiButton href={`#${ROUTES.CREATE_CHANNEL}`}>
                    Create sender
                  </EuiButton>
                }
              />
            }
            onChange={this.onTableChange}
            pagination={pagination}
            sorting={sorting}
          />
        </ContentPanel>
      </>
    );
  }
}
