/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

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
