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

import { DEFAULT_QUERY_PARAMS } from './constants';
import queryString from 'query-string';
import { SORT_DIRECTION } from '../../../../common';
import { NotificationItem } from '../../../../models/interfaces';

export type NotificationsQueryParams = {
  from: number;
  size: number;
  search: string;
  sortDirection: SORT_DIRECTION;
  sortField: string;
  status: string;
  severity: string;
  source: string;
};

export const getURLQueryParams = (location: {
  search: string;
}): NotificationsQueryParams => {
  const {
    from,
    size,
    search,
    sortField,
    sortDirection,
    source,
    severity,
    status,
  } = queryString.parse(location.search);

  return <NotificationsQueryParams>{
    // @ts-ignore
    from: isNaN(parseInt(from, 10))
      ? DEFAULT_QUERY_PARAMS.from
      : parseInt(from, 10),
    // @ts-ignore
    size: isNaN(parseInt(size, 10))
      ? DEFAULT_QUERY_PARAMS.size
      : parseInt(size, 10),
    search: typeof search !== 'string' ? DEFAULT_QUERY_PARAMS.search : search,
    sortField:
      typeof sortField !== 'string'
        ? DEFAULT_QUERY_PARAMS.sortField
        : sortField,
    sortDirection:
      typeof sortDirection !== 'string'
        ? DEFAULT_QUERY_PARAMS.sortDirection
        : sortDirection,
    source: typeof source !== 'string' ? DEFAULT_QUERY_PARAMS.source : source,
    status: typeof status !== 'string' ? DEFAULT_QUERY_PARAMS.status : status,
    severity:
      typeof severity !== 'string' ? DEFAULT_QUERY_PARAMS.severity : severity,
  };
};

export const navigateToChannelDetail = (item: NotificationItem) => {
  const {
    channel: { id, type },
  } = item;
  // TODO: need a dict here to map source to it's pages identifier. e.g. reporting -> report detail page
  window.location.assign(`opendistro_something#/type${type}/${id}`);
};
