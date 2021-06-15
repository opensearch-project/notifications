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
        'from=100',
        'size=200',
        'search=test%20query',
        'sortField=last_updated_time_ms',
        'sortDirection=asc',
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
        'from=test',
        'size=test',
        'search=',
        'sortField=last_updated_time_ms',
        'sortDirection=asc',
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
