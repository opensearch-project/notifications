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
import {
  SESSenderItemType,
  TableState,
} from '../../../../../models/interfaces';
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

interface SESSendersTableProps {
  coreContext: CoreStart;
}

interface SESSendersTableState extends TableState<SESSenderItemType> {}

export class SESSendersTable extends Component<
  SESSendersTableProps,
  SESSendersTableState
> {
  static contextType = ServicesContext;
  columns: EuiTableFieldDataColumnType<SESSenderItemType>[];

  constructor(props: SESSendersTableProps) {
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
        width: "200px"
      },
      {
        field: 'ses_account.from_address',
        name: 'Outbound email address',
        sortable: true,
        truncateText: true,
      },
      {
        field: 'ses_account.region',
        name: 'AWS region',
        sortable: true,
        truncateText: true,
      },
      {
        field: 'ses_account.role_arn',
        name: 'Role ARN',
        sortable: false,
        truncateText: true,
      },
    ];
    this.refresh = this.refresh.bind(this);
  }

  async componentDidMount() {
    await this.refresh();
  }

  async componentDidUpdate(
    prevProps: SESSendersTableProps,
    prevState: SESSendersTableState
  ) {
    const prevQuery = SESSendersTable.getQueryObjectFromState(prevState);
    const currQuery = SESSendersTable.getQueryObjectFromState(this.state);
    if (!_.isEqual(prevQuery, currQuery)) {
      await this.refresh();
    }
  }

  static getQueryObjectFromState(state: SESSendersTableState) {
    return {
      from_index: state.from,
      max_items: state.size,
      query: state.search,
      config_type: 'ses_account',
      sort_field: state.sortField,
      sort_order: state.sortDirection,
    };
  }

  async refresh() {
    this.setState({ loading: true });
    try {
      const queryObject = SESSendersTable.getQueryObjectFromState(this.state);
      const senders = await this.context.notificationService.getSESSenders(
        queryObject
      );
      this.setState({ items: senders.items, total: senders.total });
    } catch (error) {
      this.props.coreContext.notifications.toasts.addDanger(
        getErrorMessage(error, 'There was a problem loading SES senders.')
      );
    }
    this.setState({ loading: false });
  }

  onTableChange = ({
    page: tablePage,
    sort,
  }: Criteria<SESSenderItemType>): void => {
    const { index: page, size } = tablePage!;
    const { field: sortField, direction: sortDirection } = sort!;
    this.setState({ from: page * size, size, sortField, sortDirection });
  };

  onSelectionChange = (selectedItems: SESSenderItemType[]): void => {
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

    const sorting: EuiTableSortingType<SESSenderItemType> = {
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
                          data-test-subj="ses-senders-table-delete-button"
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
                      data-test-subj="ses-senders-table-edit-button"
                      disabled={this.state.selectedItems.length !== 1}
                      onClick={() =>
                        location.assign(
                          `#${ROUTES.EDIT_SES_SENDER}/${this.state.selectedItems[0]?.config_id}`
                        )
                      }
                    >
                      Edit
                    </EuiButton>
                  ),
                },
                {
                  component: (
                    <EuiButton fill href={`#${ROUTES.CREATE_SES_SENDER}`}>
                      Create SES sender
                    </EuiButton>
                  ),
                },
              ]}
            />
          }
          bodyStyles={{ padding: 'initial' }}
          title="SES senders"
          titleSize="m"
          total={this.state.total}
        >
          <EuiFieldSearch
            data-test-subj="ses-senders-table-search-input"
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
                title={<h2>No SES senders to display</h2>}
                body="Set up an outbound email server by creating a sender. You will select a sender when configuring email channels."
                actions={
                  <EuiButton href={`#${ROUTES.CREATE_SES_SENDER}`}>
                    Create SES sender
                  </EuiButton>
                }
              />
            }
            onChange={this.onTableChange}
            pagination={pagination}
            sorting={sorting}
            loading={this.state.loading}
            tableLayout="auto"
          />
        </ContentPanel>
      </>
    );
  }
}
