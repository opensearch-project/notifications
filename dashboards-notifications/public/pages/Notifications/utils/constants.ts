/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { SortDirection } from '@elastic/eui';
import { HISTOGRAM_TYPE } from '../../../utils/constants';

export const DEFAULT_PAGE_SIZE_OPTIONS = [5, 10, 20, 50];

export const DEFAULT_QUERY_PARAMS = {
  from: 0,
  search: '',
  size: 10,
  sortDirection: SortDirection.DESC,
  sortField: 'last_updated_time_ms',
  startTime: 'now-7d',
  endTime: 'now',
  filters: [],
  histogramType: Object.values(HISTOGRAM_TYPE)[0],
};
