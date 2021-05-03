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
  EuiHealth,
  EuiHorizontalRule,
  EuiLink,
  EuiTableFieldDataColumnType,
  EuiTableSortingType,
} from '@elastic/eui';
import { Criteria } from '@elastic/eui/src/components/basic_table/basic_table';
import { Pagination } from '@elastic/eui/src/components/basic_table/pagination_bar';
import _ from 'lodash';
import React, { Component } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { SORT_DIRECTION } from '../../../common';
import { ChannelItemType, TableState } from '../../../models/interfaces';
import {
  ContentPanel,
  ContentPanelActions,
} from '../../components/ContentPanel';
import { CoreServicesContext } from '../../components/coreServices';
import { NotificationService } from '../../services';
import {
  BREADCRUMBS,
  CHANNEL_TYPE,
  NOTIFICATION_SOURCE,
  ROUTES,
} from '../../utils/constants';
import { getErrorMessage } from '../../utils/helpers';
import { DEFAULT_PAGE_SIZE_OPTIONS } from '../Notifications/utils/constants';
import { ChannelActions } from './components/ChannelActions';
import { ChannelControls } from './components/ChannelControls';

interface ChannelsProps extends RouteComponentProps {
  notificationService: NotificationService;
}

interface ChannelsState extends TableState<ChannelItemType> {}

export class Channels extends Component<ChannelsProps, ChannelsState> {
  static contextType = CoreServicesContext;
  columns: EuiTableFieldDataColumnType<ChannelItemType>[];

  constructor(props: ChannelsProps) {
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
        render: (name: string, item: ChannelItemType) => (
          <EuiLink href={`#${ROUTES.CHANNEL_DETAILS}/${item.id}`}>
            {name}
          </EuiLink>
        ),
      },
      {
        field: 'enabled',
        name: 'Notification status',
        sortable: true,
        render: (enabled: boolean) => {
          const color = enabled ? 'success' : 'subdued';
          const label = enabled ? 'Active' : 'Muted';
          return <EuiHealth color={color}>{label}</EuiHealth>;
        },
      },
      {
        field: 'type',
        name: 'Type',
        sortable: true,
        truncateText: false,
        render: (type: string) => _.get(CHANNEL_TYPE, type, '-'),
      },
      {
        field: 'allowedFeatures',
        name: 'Notification source',
        sortable: true,
        truncateText: true,
        render: (features: string[]) =>
          features
            .map((feature) => _.get(NOTIFICATION_SOURCE, feature, '-'))
            .join(', '),
      },
      {
        field: 'description',
        name: 'Description',
        sortable: true,
        truncateText: true,
      },
    ];
  }

  async componentDidMount() {
    this.context.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.CHANNELS,
    ]);
    window.scrollTo(0, 0);
    await this.getChannels();
  }

  async getChannels() {
    this.setState({ loading: true });
    try {
      const queryObject = {
        from: this.state.from,
        size: this.state.size,
        search: this.state.search,
        sortField: this.state.sortField,
        sortDirection: this.state.sortDirection,
      };
      const channels = await this.props.notificationService.getChannels(
        queryObject
      );
      this.setState({ items: channels, total: channels.length });
    } catch (error) {
      this.context.notifications.toasts.addDanger(
        getErrorMessage(error, 'There was a problem loading channels.')
      );
    }
    this.setState({ loading: false });
  }

  onTableChange = ({
    page: tablePage,
    sort,
  }: Criteria<ChannelItemType>): void => {
    const { index: page, size } = tablePage!;
    const { field: sortField, direction: sortDirection } = sort!;
    this.setState({ from: page * size, size, sortField, sortDirection });
  };

  onSelectionChange = (selectedItems: ChannelItemType[]): void => {
    this.setState({ selectedItems });
  };

  onSearchChange = (search: string): void => {
    this.setState({ from: 0, search });
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

    const sorting: EuiTableSortingType<ChannelItemType> = {
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
                  component: <ChannelActions selectedItems={selectedItems} />,
                },
                {
                  component: (
                    <EuiButton fill href={`#${ROUTES.CREATE_CHANNEL}`}>
                      Create channel
                    </EuiButton>
                  ),
                },
              ]}
            />
          }
          bodyStyles={{ padding: 'initial' }}
          title="Channels"
          titleSize="m"
          total={this.state.total}
        >
          <ChannelControls
            search={search}
            onSearchChange={this.onSearchChange}
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
                title={<h2>No channels to display</h2>}
                body="To send or receive notifications, you will need to create a notification channel."
                actions={
                  <EuiButton href={`#${ROUTES.CREATE_CHANNEL}`}>
                    Create channel
                  </EuiButton>
                }
              />
            }
            onChange={this.onTableChange}
            pagination={pagination}
            sorting={sorting}
            tableLayout="auto"
          />
        </ContentPanel>
      </>
    );
  }
}
