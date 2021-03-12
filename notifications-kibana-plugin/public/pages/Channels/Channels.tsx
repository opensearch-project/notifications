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
import React, { Component } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { SORT_DIRECTION } from '../../../common';
import { ChannelItemType, TableState } from '../../../models/interfaces';
import {
  ContentPanel,
  ContentPanelActions,
} from '../../components/ContentPanel';
import { CoreServicesContext } from '../../components/coreServices';
import { BREADCRUMBS, ROUTES } from '../../utils/constants';
import { DEFAULT_PAGE_SIZE_OPTIONS } from '../Notifications/utils/constants';
import { ChannelControls } from './components/ChannelControls';
import { ChannelActions } from './components/ChannelActions';
import _ from 'lodash';

interface ChannelsProps extends RouteComponentProps {}

interface ChannelsState extends TableState<ChannelItemType> {}

export class Channels extends Component<ChannelsProps, ChannelsState> {
  static contextType = CoreServicesContext;
  columns: EuiTableFieldDataColumnType<ChannelItemType>[];

  constructor(props: ChannelsProps) {
    super(props);

    this.state = {
      total: 1,
      from: 0,
      size: 5,
      search: '',
      sortField: 'name',
      sortDirection: SORT_DIRECTION.ASC,
      items: Array.from({ length: 5 }, (v, i) => ({
        id: `${i}`,
        name: 'Channel ' + (i + 1),
        enabled: [true, false][Math.round(Math.random())],
        type: ['email', 'slack', 'chime'][Math.round(Math.random() * 2)],
        allowedFeatures: _.sampleSize(['Alerting', 'ISM', 'Reporting'], Math.round(Math.random() * 2 + 1)),
        description: 'a sample description',
        lastUpdatedTime: 0,
        destination: {
          slack: {
            url:
              'https://hooks.slack.com/services/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',
          },
        },
      })),
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
        width: '150px',
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
        width: '150px',
      },
      {
        field: 'allowedFeatures',
        name: 'Notification source',
        sortable: true,
        truncateText: true,
        width: '150px',
        render: (features: string[]) => features.join(', '),
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
    this.context.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.CHANNELS,
    ]);
    window.scrollTo(0, 0);
    // await this.getNotifications();
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
                    <EuiButton size="s" fill href={`#${ROUTES.CREATE_CHANNEL}`}>
                      Create channel
                    </EuiButton>
                  ),
                },
              ]}
            />
          }
          bodyStyles={{ padding: 'initial' }}
          title={`Channels (${this.state.total})`}
          titleSize="m"
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
          />
        </ContentPanel>
      </>
    );
  }
}
