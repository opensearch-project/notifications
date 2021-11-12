/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { getErrorMessage, renderTime } from '../helpers';

describe('test helper functions', () => {
  it('returns default message if error not valid', () => {
    const message = getErrorMessage({}, 'default message');
    expect(message).toEqual('default message');
  });

  it('returns - if time is not valid', () => {
    const time = renderTime(NaN);
    expect(time).toEqual('-');
  });
});
