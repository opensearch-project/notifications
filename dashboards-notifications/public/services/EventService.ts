/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { HttpFetchQuery, HttpSetup } from '../../../../src/core/public';
import { NODE_API } from '../../common';
import { NOTIFICATION_SOURCE } from '../utils/constants';
import { eventListToNotifications, eventToNotification } from './utils/helper';

interface EventsResponse {
  total_hits: number;
  event_list: any[];
}

export default class EventService {
  httpClient: HttpSetup;

  constructor(httpClient: HttpSetup) {
    this.httpClient = httpClient;
  }

  getHistogram = async (queryObject: object) => {
    // TODO needs backend support
    // return MOCK_GET_HISTOGRAM();
  };

  getNotifications = async (queryObject: HttpFetchQuery) => {
    const response = await this.httpClient.get<EventsResponse>(
      NODE_API.GET_EVENTS,
      {
        query: queryObject,
      }
    );
    return {
      items: eventListToNotifications(response.event_list),
      total: response.total_hits || 0,
    };
  };

  getNotification = async (id: string) => {
    const response = await this.httpClient.get<EventsResponse>(
      `${NODE_API.GET_EVENT}/${id}`
    );
    return eventToNotification(response.event_list[0]);
  };

  sendTestMessage = async (
    configId: string,
    feature: keyof typeof NOTIFICATION_SOURCE
  ) => {
    const response = await this.httpClient.get(
      `${NODE_API.SEND_TEST_MESSAGE}/${configId}`,
      {
        query: {
          feature,
        },
      }
    );
    if (response.event_id != null) {
      await this.getNotification(response.event_id).then((response) => {
        if (!response.success) {
          const error = new Error('Failed to send the test message.');
          error.stack = JSON.stringify(response.status_list, null, 2);
          throw error;
        }
      });
    } else {
      console.error(response);
      const error = new Error('Failed to send the test message.');
      error.stack = JSON.stringify(response, null, 2);
      throw error;
    }
    return response;
  };
}
