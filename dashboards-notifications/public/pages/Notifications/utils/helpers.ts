/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { Direction, ShortDate } from '@elastic/eui';
import queryString from 'query-string';
import { NotificationItem } from '../../../../models/interfaces';
import { HISTOGRAM_TYPE } from '../../../utils/constants';
import { FilterType } from '../components/SearchBar/Filter/Filters';
import { DEFAULT_QUERY_PARAMS } from './constants';

export type NotificationsQueryParams = {
  from: number;
  size: number;
  search: string;
  sortDirection: Direction;
  sortField: string;
  startTime: ShortDate;
  endTime: ShortDate;
  filters: FilterType[];
  histogramType: keyof typeof HISTOGRAM_TYPE;
};

export const getURLQueryParams = (
  queryParams: string
): NotificationsQueryParams => {
  const {
    from_index: from,
    max_items: size,
    query: search,
    sort_field: sortField,
    sort_order: sortDirection,
    startTime,
    endTime,
    filters,
    histogramType,
  } = queryString.parse(queryParams);

  let parsedFilters = DEFAULT_QUERY_PARAMS.filters;
  if (typeof filters === 'string' && filters.trim().length > 0)
    try {
      parsedFilters = JSON.parse(filters);
    } catch (error) {}

  return <NotificationsQueryParams>{
    // @ts-ignore
    from: isNaN(parseInt(from, 10))
      ? DEFAULT_QUERY_PARAMS.from
      : parseInt(from as string, 10),
    // @ts-ignore
    size: isNaN(parseInt(size, 10))
      ? DEFAULT_QUERY_PARAMS.size
      : parseInt(size as string, 10),
    search: typeof search !== 'string' ? DEFAULT_QUERY_PARAMS.search : search,
    sortField:
      typeof sortField !== 'string'
        ? DEFAULT_QUERY_PARAMS.sortField
        : sortField,
    sortDirection:
      typeof sortDirection !== 'string'
        ? DEFAULT_QUERY_PARAMS.sortDirection
        : sortDirection,
    startTime:
      typeof startTime !== 'string' || startTime.length === 0
        ? DEFAULT_QUERY_PARAMS.startTime
        : startTime,
    endTime:
      typeof endTime !== 'string' || endTime.length === 0
        ? DEFAULT_QUERY_PARAMS.endTime
        : endTime,
    filters: parsedFilters,
    histogramType:
      typeof histogramType !== 'string'
        ? DEFAULT_QUERY_PARAMS.histogramType
        : histogramType,
  };
};

export const getReferenceURL = (item: NotificationItem) => {
  const id = item.event_source.reference_id;
  switch (item.event_source.feature) {
    case 'alerting':
      return `alerting#/monitors/${id}`;
    case 'index_management':
      return `opensearch_index_management_dashboards#/index-policies?search=${id}`;
    case 'reports':
      return `reports-dashboards#/report_details/${id}`;
    default:
      return '#';
  }
};

export const getReferenceText = (item: NotificationItem) => {
  switch (item.event_source.feature) {
    case 'alerting':
      return 'Monitor';
    case 'index_management':
      return 'Index Management';
    case 'reports':
      return 'Report';
    default:
      return '-';
  }
};
