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
  SortDirection,
} from '@elastic/eui';
import { Criteria } from '@elastic/eui/src/components/basic_table/basic_table';
import { Pagination } from '@elastic/eui/src/components/basic_table/pagination_bar';
import _ from 'lodash';
import React, { Component } from 'react';
import { CoreStart } from '../../../../../../../src/core/public';
import { SenderItemType, TableState } from '../../../../../models/interfaces';
import {
  ContentPanel,
  ContentPanelActions,
} from '../../../../components/ContentPanel';
import { ModalConsumer } from '../../../../components/Modal';
import { ServicesContext } from '../../../../services';
import { ENCRYPTION_TYPE, ROUTES } from '../../../../utils/constants';
import { getErrorMessage } from '../../../../utils/helpers';
import { DEFAULT_PAGE_SIZE_OPTIONS } from '../../../Notifications/utils/constants';
import { DeleteSenderModal } from '../modals/DeleteSenderModal';

interface SendersTableProps {
  coreContext: CoreStart;
}

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
      sortDirection: SortDirection.ASC,
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
        field: 'from_address',
        name: 'Outbound email address',
        sortable: false,
        truncateText: true,
        width: '200px',
      },
      {
        field: 'smtp_account.host',
        name: 'Host',
        sortable: true,
        truncateText: true,
        render: (value: string) => value || '-',
      },
      {
        field: 'smtp_account.port',
        name: 'Port',
        sortable: false,
        truncateText: true,
        render: (value: string) => value || '-',
      },
      {
        field: 'smtp_account.method',
        name: 'Encryption method',
        sortable: true,
        truncateText: true,
        render: (method: string) => _.get(ENCRYPTION_TYPE, method, '-'),
      },
      {
        field: 'ses_account.region',
        name: 'AWS region',
        sortable: true,
        truncateText: true,
        render: (value: string) => value || '-',
      },
      {
        field: 'ses_account.role_arn',
        name: 'Role ARN',
        sortable: false,
        truncateText: true,
        render: (value: string) => value || '-',
      },
    ];
    this.refresh = this.refresh.bind(this);
  }

  async componentDidMount() {
    await this.refresh();
  }

  async componentDidUpdate(
    prevProps: SendersTableProps,
    prevState: SendersTableState
  ) {
    const prevQuery = SendersTable.getQueryObjectFromState(prevState);
    const currQuery = SendersTable.getQueryObjectFromState(this.state);
    if (!_.isEqual(prevQuery, currQuery)) {
      await this.refresh();
    }
  }

  static getQueryObjectFromState(state: SendersTableState) {
    return {
      from_index: state.from,
      max_items: state.size,
      query: state.search,
      config_type: ['smtp_account', 'ses_account'],
      sort_field: state.sortField,
      sort_order: state.sortDirection,
    };
  }

  async refresh() {
    this.setState({ loading: true });
    try {
      const queryObject = SendersTable.getQueryObjectFromState(this.state);
      const senders = await this.context.notificationService.getSenders(
        queryObject
      );
      this.setState({ items: senders.items, total: senders.total });
    } catch (error) {
      this.props.coreContext.notifications.toasts.addDanger(
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

  onSearchChange = (search: string): void => {
    this.setState({ from: 0, search });
  };

  render() {
    const page = Math.floor(this.state.from / this.state.size);

    const pagination: Pagination = {
      pageIndex: page,
      pageSize: this.state.size,
      pageSizeOptions: DEFAULT_PAGE_SIZE_OPTIONS,
      totalItemCount: this.state.total,
    };

    const sorting: EuiTableSortingType<SenderItemType> = {
      sort: {
        direction: this.state.sortDirection,
        field: this.state.sortField,
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
                          data-test-subj="senders-table-delete-button"
                          disabled={this.state.selectedItems.length === 0}
                          onClick={() =>
                            onShow(DeleteSenderModal, {
                              senders: this.state.selectedItems,
                              refresh: this.refresh,
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
                      data-test-subj="senders-table-edit-button"
                      disabled={this.state.selectedItems.length !== 1}
                      onClick={() =>
                        location.assign(
                          `#${ROUTES.EDIT_SENDER}/${this.state.selectedItems[0]?.config_id}`
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
            data-test-subj="senders-table-search-input"
            fullWidth={true}
            placeholder="Search"
            onSearch={this.onSearchChange}
          />
          <EuiHorizontalRule margin="s" />

          <EuiBasicTable
            columns={this.columns}
            items={this.state.items}
            itemId="config_id"
            isSelectable={true}
            selection={selection}
            noItemsMessage={
              <EuiEmptyPrompt
                title={<h2>No senders to display</h2>}
                body="Set up an outbound email server by creating a sender. You will select a sender when configuring email channels."
                actions={
                  <EuiButton href={`#${ROUTES.CREATE_SENDER}`}>
                    Create sender
                  </EuiButton>
                }
              />
            }
            onChange={this.onTableChange}
            pagination={pagination}
            sorting={sorting}
            loading={this.state.loading}
          />
        </ContentPanel>
      </>
    );
  }
}
