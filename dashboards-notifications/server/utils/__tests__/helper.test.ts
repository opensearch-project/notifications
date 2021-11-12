/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { joinRequestParams } from '../helper';

describe('test joining request parameters', () => {
  it('joins array with comma', () => {
    const query = ['alerting', 'reports'];
    const result = joinRequestParams(query);
    expect(result).toEqual('alerting,reports');
  });

  it('returns string if array of 1', () => {
    const query = ['alerting'];
    const result = joinRequestParams(query);
    expect(result).toEqual('alerting');
  });

  it('does not change string', () => {
    const query = 'alerting';
    const result = joinRequestParams(query);
    expect(result).toEqual('alerting');
  });

  it('returns empty string for null or undefined', () => {
    const undefinedResult = joinRequestParams(undefined);
    expect(undefinedResult).toEqual('');
    const nullResult = joinRequestParams(null);
    expect(nullResult).toEqual('');
  });
});
