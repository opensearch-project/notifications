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

import { Datum } from '@elastic/charts';
import dateMath from '@elastic/datemath';
import {
  EuiButton,
  EuiFlexGroup,
  EuiFlexItem,
  EuiSpacer,
  EuiTableSortingType,
  EuiTitle,
  ShortDate,
  SortDirection,
} from '@elastic/eui';
import { Criteria } from '@elastic/eui/src/components/basic_table/basic_table';
import { Pagination } from '@elastic/eui/src/components/basic_table/pagination_bar';
import _ from 'lodash';
import queryString from 'querystring';
import React, { Component } from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { NotificationItem, TableState } from '../../../../models/interfaces';
import { CoreServicesContext } from '../../../components/coreServices';
import { BrowserServices } from '../../../models/interfaces';
import {
  BREADCRUMBS,
  CHANNEL_TYPE,
  HISTOGRAM_TYPE,
  ROUTES,
} from '../../../utils/constants';
import { getErrorMessage } from '../../../utils/helpers';
import { MainState } from '../../Main/Main';
import { EmptyState } from '../components/EmptyState/EmptyState';
import { NotificationsTable } from '../components/NotificationsTable/NotificationsTable';
import { FilterType } from '../components/SearchBar/Filter/Filters';
import { NotificationsSearchBar } from '../components/SearchBar/NotificationsSearchBar';
import { filtersToQueryParams } from '../components/SearchBar/utils/filterHelpers';
import { DEFAULT_PAGE_SIZE_OPTIONS } from '../utils/constants';
import { getURLQueryParams } from '../utils/helpers';

interface NotificationsProps extends RouteComponentProps {
  services: BrowserServices;
  mainProps: MainState;
}

interface NotificationsState extends TableState<NotificationItem> {
  startTime: ShortDate;
  endTime: ShortDate;
  filters: Array<FilterType>;
  histogramType: keyof typeof HISTOGRAM_TYPE;
  histogramData: Array<Datum>;
  isChannelConfigured: boolean;
}

export default class Notifications extends Component<
  NotificationsProps,
  NotificationsState
> {
  static contextType = CoreServicesContext;

  constructor(props: NotificationsProps) {
    super(props);

    const urlParams =
      this.props.location.search ||
      sessionStorage.getItem('NotificationsQueryParams') ||
      '';
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
    } = getURLQueryParams(urlParams);

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
      histogramData: [],
      isChannelConfigured: true,
    };

    this.getNotifications = _.debounce(this.getNotifications, 200);
  }

  async componentDidMount() {
    this.context.chrome.setBreadcrumbs([
      BREADCRUMBS.NOTIFICATIONS,
      BREADCRUMBS.DASHBOARD,
    ]);
    window.scrollTo(0, 0);
    await this.getNotifications();
    try {
      const isChannelConfigured =
        await this.props.services.notificationService.getChannels({
          config_type: Object.keys(CHANNEL_TYPE),
          from_index: 0,
          max_items: 1,
          sort_field: 'name',
          sort_order: SortDirection.ASC,
        });
      if (!isChannelConfigured?.total) {
        this.setState({ isChannelConfigured: false });
      }
    } catch (error) {
      this.setState({ isChannelConfigured: false });
    }
  }

  async componentDidUpdate(
    prevProps: NotificationsProps,
    prevState: NotificationsState
  ) {
    const prevQuery = Notifications.serializeSearchParams(prevState);
    const currQuery = Notifications.serializeSearchParams(this.state);
    if (!_.isEqual(prevQuery, currQuery)) {
      await this.getNotifications();
    }
  }

  static serializeSearchParams(state: NotificationsState) {
    return {
      from_index: state.from,
      max_items: state.size,
      query: state.search,
      sort_field: state.sortField,
      sort_order: state.sortDirection,
      startTime: state.startTime,
      endTime: state.endTime,
      filters: JSON.stringify(state.filters),
      // histogramType: state.histogramType,
    };
  }

  static getQueryObjectFromState(state: NotificationsState) {
    const filterParams = filtersToQueryParams(state.filters);
    return {
      from_index: state.from,
      max_items: state.size,
      query: state.search,
      sort_field: state.sortField,
      sort_order: state.sortDirection,
      last_updated_time_ms: `${dateMath
        .parse(state.startTime)
        ?.valueOf()}..${dateMath.parse(state.endTime)?.valueOf()}`,
      ...filterParams,
    };
  }

  getNotifications = async () => {
    this.setState({ loading: true });
    try {
      const { services, history } = this.props;
      const searchParams = Notifications.serializeSearchParams(this.state);
      const queryParamsString = queryString.stringify(searchParams);
      history.replace({ ...this.props.location, search: queryParamsString });
      sessionStorage.setItem('NotificationsQueryParams', queryParamsString);

      const queryObject = Notifications.getQueryObjectFromState(this.state);
      const getNotificationsResponse = await services.eventService.getNotifications(
        queryObject
      );
      // const getHistogramResponse = await services.eventService.getHistogram(
      //   queryObject
      // );
      this.setState({
        items: getNotificationsResponse.items,
        total: getNotificationsResponse.total,
        // histogramData: getHistogramResponse,
      });
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

  onSearchChange = (search: string): void => {
    this.setState({ from: 0, search });
  };

  setStartTime = (startTime: ShortDate) => {
    this.setState({ from: 0, startTime });
  };
  setEndTime = (endTime: ShortDate) => {
    this.setState({ from: 0, endTime });
  };
  setFilters = (filters: FilterType[]) => {
    this.setState({ from: 0, filters });
    this.getNotifications();
  };
  setHistogramType = (histogramType: keyof typeof HISTOGRAM_TYPE) => {
    this.setState({ histogramType });
  };

  render() {
    const page = Math.floor(this.state.from / this.state.size);

    const pagination: Pagination = {
      pageIndex: page,
      pageSize: this.state.size,
      pageSizeOptions: DEFAULT_PAGE_SIZE_OPTIONS,
      totalItemCount: this.state.total,
    };

    const sorting: EuiTableSortingType<NotificationItem> = {
      sort: {
        direction: this.state.sortDirection,
        field: this.state.sortField,
      },
    };

    if (
      !this.state.loading &&
      this.state.total === 0 &&
      !this.state.isChannelConfigured
    ) {
      return <EmptyState channels={false} />;
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
          search={this.state.search}
          setSearch={this.onSearchChange}
          filters={this.state.filters}
          setFilters={this.setFilters}
          refresh={this.getNotifications}
        />

        {this.state.loading || this.state.total > 0 ? (
          <>
            {/* <EuiSpacer />
            <NotificationsHistogram
              histogramType={this.state.histogramType}
              setHistogramType={this.setHistogramType}
              histogramData={this.state.histogramData}
            /> */}

            <EuiSpacer />
            <NotificationsTable
              items={this.state.items}
              onTableChange={this.onTableChange}
              pagination={pagination}
              sorting={sorting}
              loading={this.state.loading}
            />
          </>
        ) : (
          <EmptyState channels={true} />
        )}
      </div>
    );
  }
}
