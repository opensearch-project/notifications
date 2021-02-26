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
  EuiFlexGroup,
  EuiFlexItem,
  EuiTitle,
  EuiButton,
  EuiSpacer,
  EuiHorizontalRule,
  EuiBasicTable,
  EuiHealth,
  EuiLink,
  Direction,
  EuiTableFieldDataColumnType,
  EuiTableSortingType,
  EuiTableSelectionType,
  //@ts-ignore
  Criteria,
  //@ts-ignore
  Pagination,
} from '@elastic/eui';
import { PLUGIN_NAME, SORT_DIRECTION } from '../../../../common';
import {
  ContentPanel,
  ContentPanelActions,
} from '../../../components/ContentPanel';
import _ from 'lodash';
import queryString from 'querystring';
import React, { Component, useEffect, useState } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { getURLQueryParams, navigateToChannelDetail } from '../utils/helpers';
import { CoreServicesContext } from '../../../components/coreServices';
import { CoreStart, HttpStart } from '../../../../../../src/core/public';
import { NotificationItem } from '../../../../models/interfaces';

import { NotificationService } from '../../../services';
import { BREADCRUMBS } from '../../../utils/constants';
import { getErrorMessage, renderTime } from '../../../utils/helpers';
import {
  DEFAULT_PAGE_SIZE_OPTIONS,
  DEFAULT_QUERY_PARAMS,
} from '../utils/constants';
import NotificationControls from '../component/NotificationControls/NotificationControls';
import { ModalConsumer } from '../../../components/Modal';
import ErrorDetailModal from '../component/ErrorDetailModal/ErrorDetailModel';

interface NotificationsProps extends RouteComponentProps {
  notificationService: NotificationService;
}

interface NotificationsState {
  totalNotifications: number;
  from: number;
  size: number;
  search: string;
  sortField: any; //keyof NotificationItem;
  sortDirection: Direction;
  selectedItems: []; // NotificationItem[];
  notifications: NotificationItem[]; // NotificationItem[];
  loadingNotifications: boolean;
  status: string;
  severity: string;
  source: string;
}

export default class Notifications extends Component<
  NotificationsProps,
  NotificationsState
> {
  static contextType = CoreServicesContext;
  columns: EuiTableFieldDataColumnType<NotificationItem>[];

  constructor(props: NotificationsProps) {
    super(props);

    const {
      from,
      size,
      search,
      sortField,
      sortDirection,
      status,
      severity,
      source,
    } = getURLQueryParams(this.props.location);

    this.state = {
      totalNotifications: 0,
      from,
      size,
      search,
      sortField,
      sortDirection,
      notifications: [],
      selectedItems: [],
      loadingNotifications: true,
      status,
      severity,
      source,
    };

    this.getNotifications = _.debounce(this.getNotifications, 500, {
      leading: true,
    });

    this.columns = [
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
                    <EuiLink
                      onClick={() => onShow(ErrorDetailModal, { detail })}
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
          <EuiLink onClick={() => navigateToChannelDetail(item)}>
            {name}
          </EuiLink>
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
    ];
  }

  async componentDidMount() {
    this.context.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.DASHBOARD,
    ]);
    await this.getNotifications();
  }

  async componentDidUpdate(
    prevProps: NotificationsProps,
    prevState: NotificationsState
  ) {
    const prevQuery = Notifications.getQueryObjectFromState(prevState);
    const currQuery = Notifications.getQueryObjectFromState(this.state);
    if (!_.isEqual(prevQuery, currQuery)) {
      await this.getNotifications();
    }
  }

  static getQueryObjectFromState({
    from,
    size,
    search,
    sortField,
    sortDirection,
    severity,
    status,
    source,
  }: NotificationsState) {
    return {
      from,
      size,
      search,
      sortField,
      sortDirection,
      severity,
      status,
      source,
    };
  }

  getNotifications = async () => {
    this.setState({ loadingNotifications: true });
    try {
      const { notificationService, history } = this.props;
      const queryObject = Notifications.getQueryObjectFromState(this.state);
      const queryParamsString = queryString.stringify(queryObject);
      history.replace({ ...this.props.location, search: queryParamsString });
      const getNotificationsResponse = await notificationService.getNotifications(
        queryObject
      );
      const { notifications, totalNotifications } = getNotificationsResponse;
      this.setState({ notifications, totalNotifications });
    } catch (err) {
      this.context.notifications.toasts.addDanger(
        getErrorMessage(err, 'There was a problem loading the managed indices')
      );
    }
    this.setState({ loadingNotifications: false });
  };

  onTableChange = ({
    page: tablePage,
    sort,
  }: Criteria<NotificationItem>): void => {
    const { index: page, size } = tablePage;
    const { field: sortField, direction: sortDirection } = sort;
    this.setState({ from: page * size, size, sortField, sortDirection });
  };

  // onSelectionChange = (selectedItems: NotificationItem[]): void => {
  //   this.setState({ selectedItems });
  // };

  onSearchChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    this.setState({ from: 0, search: e.target.value });
  };

  onNotificationStatusChange = (
    e: React.ChangeEvent<HTMLInputElement>
  ): void => {
    this.setState({ from: 0, status: e.target.value });
  };
  onSeverityChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    this.setState({ from: 0, severity: e.target.value });
  };
  onSourceChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    this.setState({ from: 0, source: e.target.value });
  };

  onPageChange = (page: number): void => {
    const { size } = this.state;
    this.setState({ from: page * size });
  };

  // onClickModalEdit = (item: NotificationItem, onClose: () => void): void => {
  //   onClose();
  //   if (!item || !item.policyId) return;
  //   this.props.history.push(`${ROUTES.EDIT_POLICY}?id=${item.policyId}`);
  // };

  resetFilters = (): void => {
    this.setState({ search: DEFAULT_QUERY_PARAMS.search });
  };

  render() {
    const {
      totalNotifications,
      from,
      size,
      search,
      sortField,
      sortDirection,
      selectedItems,
      notifications,
      loadingNotifications,
    } = this.state;

    const filterIsApplied = !!search;
    const page = Math.floor(from / size);

    const pagination: Pagination = {
      pageIndex: page,
      pageSize: size,
      pageSizeOptions: DEFAULT_PAGE_SIZE_OPTIONS,
      totalItemCount: totalNotifications,
    };

    const sorting: EuiTableSortingType<NotificationItem> = {
      sort: {
        direction: sortDirection,
        field: sortField,
      },
    };

    return (
      <div style={{ padding: '0px 25px' }}>
        <EuiFlexGroup alignItems="center">
          <EuiFlexItem>
            <EuiTitle size="l">
              <h1>Dashboard</h1>
            </EuiTitle>
          </EuiFlexItem>
          <EuiFlexItem grow={false}>
            <EuiButton
              href={`${PLUGIN_NAME}#/create-channel`}
              data-test-subj="createChannelButton"
            >
              Create Channel
            </EuiButton>
          </EuiFlexItem>
        </EuiFlexGroup>

        <EuiSpacer />

        <ContentPanel
          // actions={<ContentPanelActions actions={actions} />}
          bodyStyles={{ padding: 'initial' }}
          title="Notification History"
        >
          <NotificationControls
            activePage={page}
            pageCount={Math.ceil(totalNotifications / size) || 1}
            search={search}
            onSearchChange={this.onSearchChange}
            onPageChange={this.onPageChange}
            onSeverityChange={this.onSeverityChange}
            onStatusChange={this.onNotificationStatusChange}
            onSourceChange={this.onSourceChange}
            // onRefresh={this.getNotifications}
          />

          <EuiHorizontalRule margin="xs" />

          <EuiBasicTable
            columns={this.columns}
            itemId="title"
            isSelectable={true}
            items={notifications}
            noItemsMessage={
              // TODO: add empty prompt component, pending UXDR
              <div>no item</div>
            }
            onChange={this.onTableChange}
            pagination={pagination}
            sorting={sorting}
          />
        </ContentPanel>
      </div>
    );
  }
}
