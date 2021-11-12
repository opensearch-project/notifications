/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { DEFAULT_QUERY_PARAMS } from '../constants';
import { getURLQueryParams } from '../helpers';

describe('test url params', () => {
  it('parses empty url query params', () => {
    const emptyParams = getURLQueryParams('');
    expect(emptyParams).toEqual(DEFAULT_QUERY_PARAMS);
  });

  it('parses url query params', () => {
    const params = getURLQueryParams(
      [
        'from_index=100',
        'max_items=200',
        'query=test%20query',
        'sort_field=last_updated_time_ms',
        'sort_order=asc',
        'startTime=now-1w',
        'endTime=now',
        'filters=',
      ].join('&')
    );
    expect(params).toEqual({
      from: 100,
      search: 'test query',
      size: 200,
      sortDirection: 'asc',
      sortField: 'last_updated_time_ms',
      startTime: 'now-1w',
      endTime: 'now',
      filters: [],
      histogramType: 'Channel type',
    });
  });

  it('parses invalid url query params', () => {
    const params = getURLQueryParams(
      [
        'from_index=test',
        'max_items=test',
        'query=',
        'sort_field=last_updated_time_ms',
        'sort_order=asc',
        'startTime=',
        'endTime=',
        'filters=invalidFilters',
      ].join('&')
    );
    expect(params).toEqual({
      from: 0,
      search: '',
      size: 10,
      sortDirection: 'asc',
      sortField: 'last_updated_time_ms',
      startTime: 'now-7d',
      endTime: 'now',
      filters: [],
      histogramType: 'Channel type',
    });
  });
});
