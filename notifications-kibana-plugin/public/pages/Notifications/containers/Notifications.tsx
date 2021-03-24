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
  EuiButton,
  EuiFlexGroup,
  EuiFlexItem,
  EuiSpacer,
  EuiTableSortingType,
  EuiTitle,
  ShortDate,
} from '@elastic/eui';
import { Criteria } from '@elastic/eui/src/components/basic_table/basic_table';
import { Pagination } from '@elastic/eui/src/components/basic_table/pagination_bar';
import _ from 'lodash';
import queryString from 'querystring';
import React, { Component } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { NotificationItem, TableState } from '../../../../models/interfaces';
import { CoreServicesContext } from '../../../components/coreServices';
import { NotificationService } from '../../../services';
import { BREADCRUMBS, HISTOGRAM_TYPE, ROUTES } from '../../../utils/constants';
import { getErrorMessage } from '../../../utils/helpers';
import { EmptyState } from '../components/EmptyState/EmptyState';
import { NotificationsHistogram } from '../components/NotificationsHistogram/NotificationsHistogram';
import { NotificationsTable } from '../components/NotificationsTable/NotificationsTable';
import { FilterType } from '../components/SearchBar/Filter/Filters';
import { NotificationsSearchBar } from '../components/SearchBar/NotificationsSearchBar';
import {
  DEFAULT_PAGE_SIZE_OPTIONS,
  DEFAULT_QUERY_PARAMS,
} from '../utils/constants';
import { getURLQueryParams } from '../utils/helpers';

interface NotificationsProps extends RouteComponentProps {
  notificationService: NotificationService;
}

interface NotificationsState extends TableState<NotificationItem> {
  startTime: ShortDate;
  endTime: ShortDate;
  filters: FilterType[];
  histogramType: keyof typeof HISTOGRAM_TYPE;
}

export default class Notifications extends Component<
  NotificationsProps,
  NotificationsState
> {
  static contextType = CoreServicesContext;

  constructor(props: NotificationsProps) {
    super(props);

    const {
      from,
      size,
      search,
      sortField,
      sortDirection,
      startTime,
      endTime,
      filters,
      histogramType,
    } = getURLQueryParams(this.props.location);

    this.state = {
      total: 0,
      from,
      size,
      search,
      sortField,
      sortDirection,
      items: [],
      selectedItems: [],
      loading: true,
      startTime,
      endTime,
      filters,
      histogramType,
    };

    this.getNotifications = _.debounce(this.getNotifications, 500, {
      leading: true,
    });
  }

  async componentDidMount() {
    this.context.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.DASHBOARD,
    ]);
    window.scrollTo(0, 0);
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

  static getQueryObjectFromState(state: NotificationsState) {
    return {
      from: state.from,
      size: state.size,
      search: state.search,
      sortField: state.sortField,
      sortDirection: state.sortDirection,
      startTime: state.startTime,
      endTime: state.endTime,
      filters: JSON.stringify(state.filters),
      histogramType: state.histogramType,
    };
  }

  getNotifications = async () => {
    this.setState({ loading: true });
    try {
      const { notificationService, history } = this.props;
      const queryObject = Notifications.getQueryObjectFromState(this.state);
      const queryParamsString = queryString.stringify(queryObject);
      history.replace({ ...this.props.location, search: queryParamsString });
      const getNotificationsResponse = await notificationService.getNotifications(
        queryObject
      );
      const { notifications, totalNotifications } = getNotificationsResponse;
      this.setState({ items: notifications, total: totalNotifications });
    } catch (err) {
      this.context.notifications.toasts.addDanger(
        getErrorMessage(err, 'There was a problem loading notifications.')
      );
    }
    this.setState({ loading: false });
  };

  onTableChange = ({
    page: tablePage,
    sort,
  }: Criteria<NotificationItem>): void => {
    const { index: page, size } = tablePage!;
    const { field: sortField, direction: sortDirection } = sort!;
    this.setState({ from: page * size, size, sortField, sortDirection });
  };

  // onSelectionChange = (selectedItems: NotificationItem[]): void => {
  //   this.setState({ selectedItems });
  // };

  onSearchChange = (search: string): void => {
    this.setState({ from: 0, search });
  };

  onPageChange = (page: number): void => {
    const { size } = this.state;
    this.setState({ from: page * size });
  };

  setStartTime = (startTime: ShortDate) => {
    this.setState({ from: 0, startTime });
  };
  setEndTime = (endTime: ShortDate) => {
    this.setState({ from: 0, endTime });
  };
  setFilters = (filters: FilterType[]) => {
    this.setState({ from: 0, filters });
  };
  setHistogramType = (histogramType: keyof typeof HISTOGRAM_TYPE) => {
    this.setState({ histogramType });
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

    const sorting: EuiTableSortingType<NotificationItem> = {
      sort: {
        direction: sortDirection,
        field: sortField,
      },
    };

    if (this.state.items.length === 0) {
      // TODO check if no channels, return <EmptyState channels={false} />
      return <EmptyState channels />;
    }

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
              href={`#${ROUTES.CHANNELS}`}
              data-test-subj="createChannelButton"
              fill
              size="s"
            >
              Manage channels
            </EuiButton>
          </EuiFlexItem>
        </EuiFlexGroup>

        <EuiSpacer size="m" />
        <NotificationsSearchBar
          startTime={this.state.startTime}
          setStartTime={this.setStartTime}
          endTime={this.state.endTime}
          setEndTime={this.setEndTime}
          search={search}
          setSearch={this.onSearchChange}
          filters={this.state.filters}
          setFilters={this.setFilters}
          refresh={this.getNotifications}
        />

        <EuiSpacer />
        <NotificationsHistogram
          histogramType={this.state.histogramType}
          setHistogramType={this.setHistogramType}
        />

        <EuiSpacer />
        <NotificationsTable
          items={items}
          onTableChange={this.onTableChange}
          pagination={pagination}
          sorting={sorting}
        />
      </div>
    );
  }
}
