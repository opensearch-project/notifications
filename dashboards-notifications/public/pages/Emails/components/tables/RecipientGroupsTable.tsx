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
import { DEFAULT_PAGE_SIZE_OPTIONS } from '../../../Notifications/utils/constants';
import { DeleteRecipientGroupModal } from '../modals/DeleteRecipientGroupModal';

interface RecipientGroupsTableProps {}

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
        width: '150px',
      },
      {
        field: 'email',
        name: 'Email addresses',
        sortable: true,
        truncateText: true,
        width: '150px',
        render: (emails: string[]) => emails.length,
      },
      {
        field: 'description',
        name: 'Description',
        sortable: true,
        truncateText: true,
        width: '150px',
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
      const recipientGroups = await this.context.notificationService.getRecipientGroups(
        queryObject
      );
      this.setState({ items: recipientGroups, total: recipientGroups.length });
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
  }: Criteria<RecipientGroupItemType>): void => {
    const { index: page, size } = tablePage!;
    const { field: sortField, direction: sortDirection } = sort!;
    this.setState({ from: page * size, size, sortField, sortDirection });
  };

  onSelectionChange = (selectedItems: RecipientGroupItemType[]): void => {
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

    const sorting: EuiTableSortingType<RecipientGroupItemType> = {
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
                          size="s"
                          disabled={this.state.selectedItems.length === 0}
                          onClick={() =>
                            onShow(DeleteRecipientGroupModal, {
                              recipientGroups: this.state.selectedItems,
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
                      size="s"
                      disabled={this.state.selectedItems.length !== 1}
                      onClick={() =>
                        location.assign(
                          `#${ROUTES.EDIT_RECIPIENT_GROUP}/${this.state.selectedItems[0]?.id}`
                        )
                      }
                    >
                      Edit
                    </EuiButton>
                  ),
                },
                {
                  component: (
                    <EuiButton
                      size="s"
                      fill
                      href={`#${ROUTES.CREATE_RECIPIENT_GROUP}`}
                    >
                      Create recipient group
                    </EuiButton>
                  ),
                },
              ]}
            />
          }
          bodyStyles={{ padding: 'initial' }}
          title={`Recipient groups (${this.state.total})`}
          titleSize="m"
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
