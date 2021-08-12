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

export const joinRequestParams = (
  queryParams: string | string[] | undefined
) => {
  if (Array.isArray(queryParams)) return queryParams.join(',');
  if (typeof queryParams === 'string') return queryParams;
  return '';
};

export const checkErrorType = (error: any) => {
  if (error.statusCode && Math.floor(error.statusCode / 100) === 4) {
    return 'user_error';
  } else {
    return 'system_error';
  }
};