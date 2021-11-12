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
  EuiButton,
  EuiEmptyPrompt,
  EuiFieldSearch,
  EuiHorizontalRule,
  EuiLink,
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
  RecipientGroupItemType,
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
import { DetailsListModal } from '../../../Channels/components/modals/DetailsListModal';
import { DEFAULT_PAGE_SIZE_OPTIONS } from '../../../Notifications/utils/constants';
import { DeleteRecipientGroupModal } from '../modals/DeleteRecipientGroupModal';

interface RecipientGroupsTableProps {
  coreContext: CoreStart;
}

interface RecipientGroupsTableState
  extends TableState<RecipientGroupItemType> {}

export class RecipientGroupsTable extends Component<
  RecipientGroupsTableProps,
  RecipientGroupsTableState
> {
  static contextType = ServicesContext;
  columns: EuiTableFieldDataColumnType<RecipientGroupItemType>[];

  constructor(props: RecipientGroupsTableProps) {
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
        width: '150px',
      },
      {
        field: 'email_group.recipient_list',
        name: 'Email addresses',
        sortable: true,
        truncateText: true,
        width: '450px',
        render: (
          recipient_list: RecipientGroupItemType['email_group']['recipient_list']
        ) => {
          const emails = recipient_list.map((recipient) => recipient.recipient);
          return (
            <div>
              {emails.slice(0, 5).join(', ')}
              {emails.length > 5 && (
                <span>
                  {' '}
                  <ModalConsumer>
                    {({ onShow }) => (
                      <EuiLink
                        onClick={() =>
                          onShow(DetailsListModal, {
                            header: `Email addresses (${emails.length})`,
                            title: 'Email addresses',
                            items: emails,
                          })
                        }
                      >
                        {emails.length - 5} more
                      </EuiLink>
                    )}
                  </ModalConsumer>
                </span>
              )}
            </div>
          );
        },
      },
      {
        field: 'description',
        name: 'Description',
        sortable: true,
        truncateText: true,
        width: '300px',
        render: (description: string) => description || '-',
      },
    ];
    this.refresh = this.refresh.bind(this);
  }

  async componentDidMount() {
    await this.refresh();
  }

  async componentDidUpdate(
    prevProps: RecipientGroupsTableProps,
    prevState: RecipientGroupsTableState
  ) {
    const prevQuery = RecipientGroupsTable.getQueryObjectFromState(prevState);
    const currQuery = RecipientGroupsTable.getQueryObjectFromState(this.state);
    if (!_.isEqual(prevQuery, currQuery)) {
      await this.refresh();
    }
  }

  static getQueryObjectFromState(state: RecipientGroupsTableState) {
    return {
      from_index: state.from,
      max_items: state.size,
      query: state.search,
      config_type: 'email_group',
      sort_field: state.sortField,
      sort_order: state.sortDirection,
    };
  }

  async refresh() {
    this.setState({ loading: true });
    try {
      const queryObject = RecipientGroupsTable.getQueryObjectFromState(
        this.state
      );
      const recipientGroups = await this.context.notificationService.getRecipientGroups(
        queryObject
      );
      this.setState({
        items: recipientGroups.items,
        total: recipientGroups.total,
      });
    } catch (error) {
      this.props.coreContext.notifications.toasts.addDanger(
        getErrorMessage(error, 'There was a problem loading recipient groups.')
      );
    }
    this.setState({ loading: false });
  }

  onTableChange = ({
    page: tablePage,
    sort,
  }: Criteria<RecipientGroupItemType>): void => {
    const { index: page, size } = tablePage!;
    const { field: sortField, direction: sortDirection } = sort!;
    this.setState({ from: page * size, size, sortField, sortDirection });
  };

  onSelectionChange = (selectedItems: RecipientGroupItemType[]): void => {
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

    const sorting: EuiTableSortingType<RecipientGroupItemType> = {
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
                          data-test-subj="recipient-groups-table-delete-button"
                          disabled={this.state.selectedItems.length === 0}
                          onClick={() =>
                            onShow(DeleteRecipientGroupModal, {
                              recipientGroups: this.state.selectedItems,
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
                      data-test-subj="recipient-groups-table-edit-button"
                      disabled={this.state.selectedItems.length !== 1}
                      onClick={() =>
                        location.assign(
                          `#${ROUTES.EDIT_RECIPIENT_GROUP}/${this.state.selectedItems[0]?.config_id}`
                        )
                      }
                    >
                      Edit
                    </EuiButton>
                  ),
                },
                {
                  component: (
                    <EuiButton fill href={`#${ROUTES.CREATE_RECIPIENT_GROUP}`}>
                      Create recipient group
                    </EuiButton>
                  ),
                },
              ]}
            />
          }
          bodyStyles={{ padding: 'initial' }}
          title="Recipient groups"
          titleSize="m"
          total={this.state.total}
        >
          <EuiFieldSearch
            data-test-subj="recipient-groups-table-search-input"
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
                title={<h2>No recipient groups to display</h2>}
                body="Use an email group to manage a list of email addresses you frequently send at a time. You can select recipient groups when configuring email channels."
                actions={
                  <EuiButton href={`#${ROUTES.CREATE_RECIPIENT_GROUP}`}>
                    Create recipient group
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
