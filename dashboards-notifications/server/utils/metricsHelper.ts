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

import {
  EntityType,
  CountersNameType,
  CountersType,
  ActionType,
} from './types';
import _ from 'lodash';
import {
  CAPACITY,
  DEFAULT_ROLLING_COUNTER,
  GLOBAL_BASIC_COUNTER,
  INTERVAL,
  WINDOW,
} from './constants';

export const time2CountWin: Map<number, CountersType> = new Map();

export const addToMetric = (
  entity: EntityType,
  action: ActionType,
  counter: CountersNameType,
  notificationsMetadata?: any
) => {
  const count = 1;
  // remove outdated key-value pairs
  trim();

  const timeKey = getKey(Date.now());
  const rollingCounters = time2CountWin.get(timeKey);

  time2CountWin.set(
    timeKey,
    updateCounters(
      entity,
      action,
      counter,
      rollingCounters || _.cloneDeep(DEFAULT_ROLLING_COUNTER),
      count,
      notificationsMetadata
    )
  );
}

export const getMetrics = () => {
  const preTimeKey = getPreKey(Date.now());
  const rollingCounters = time2CountWin.get(preTimeKey);
  const metrics = buildMetrics(rollingCounters);
  return metrics;
};

const getPreKey = (milliseconds: number) => {
  return getKey(milliseconds) - 1;
};

const trim = () => {
  if (time2CountWin.size > CAPACITY) {
    const currentKey = getKey(Date.now() - WINDOW * 1000);
    time2CountWin.forEach((_value, key, map) => {
      if (key < currentKey) {
        map.delete(key);
      }
    });
  }
};

const getKey = (milliseconds: number) => {
  return Math.floor(milliseconds / 1000 / INTERVAL);
};

const updateCounters = (
  entity: EntityType,
  action: ActionType,
  counter: CountersNameType,
  rollingCounter: CountersType,
  count: number,
  notificationsMetadata?: any
) => {
  if (notificationsMetadata) {
    // @ts-ignore
    rollingCounter[notificationsMetadata.config.config_type][action][counter] += count;
    // update basic counter
    if (counter === 'count') {
      // @ts-ignore
      ++GLOBAL_BASIC_COUNTER[notificationsMetadata.config.config_type][action]['total'];
    }
  } else {
    // update action metric
    // @ts-ignore
    rollingCounter[entity][action][counter] += count;
    if (counter === 'count') {
      // @ts-ignore
      ++GLOBAL_BASIC_COUNTER[entity][action]['total'];
    }
  }
  return rollingCounter;
}

const buildMetrics = (rollingCounters: CountersType | undefined) => {
  if (!rollingCounters) {
    rollingCounters = DEFAULT_ROLLING_COUNTER;
  }
  const basicMetrics = _.merge(rollingCounters, GLOBAL_BASIC_COUNTER);
  const overallActionMetrics = {
    request_total: 0,
    request_count: 0,
    success_count: 0,
    failed_request_count_system_error: 0,
    failed_request_count_user_error: 0,
  };
  Object.keys(basicMetrics).forEach((keys) => {
    if (isEntity(keys)) {
      for (const [action, counters] of Object.entries(basicMetrics[keys])) {
        overallActionMetrics.request_count += counters?.count || 0;
        overallActionMetrics.request_total += counters?.total || 0;
        overallActionMetrics.failed_request_count_system_error +=
          counters?.system_error || 0;
        overallActionMetrics.failed_request_count_user_error +=
          counters?.user_error || 0;
      }
    }
  });
  overallActionMetrics.success_count =
    overallActionMetrics.request_count -
    (overallActionMetrics.failed_request_count_system_error +
      overallActionMetrics.failed_request_count_user_error);

  return { ...basicMetrics, ...overallActionMetrics };
};

const isEntity = (arg: string): arg is EntityType => {
  return (
    arg === 'config' || 
    arg === 'event' || 
    arg === 'feature' ||
    arg === 'send_test_message'
  );
};