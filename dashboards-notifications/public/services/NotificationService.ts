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

import { NODE_API } from '../../common';
import { HttpSetup } from '../../../../src/core/public';
import { NotificationItem } from '../../models/interfaces';

export interface GetNotificationsResponse {
  totalNotifications: number;
  notifications: NotificationItem[];
}

export default class NotificationService {
  httpClient: HttpSetup;

  constructor(httpClient: HttpSetup) {
    this.httpClient = httpClient;
  }

  getNotifications = async (queryObject: object): Promise<any> => {
    //TODO: add it back
    // let url = `..${NODE_API.NOTIFICATIONS}`;
    // const response = await this.httpClient.get(url, {
    //   query: queryObject,
    // });
    // return response;

    const fakeRes = {
      notifications: [
        {
          title: 'title 1',
          channel: {
            id: 'c_id_1',
            name: 'c_name_1',
            type: 'slack',
          },
          source: 'reporting',
          severity: '1',
          status: {
            overview: 'Sent',
            detail: [
              {
                recipient: 'slack',
                statusText: 'Success',
                statusCode: '200',
              },
            ],
          },
          sentTime: 1614234267001,
          lastUpdatedTime: 1614230526,
        },
        {
          title: 'title 2',
          channel: {
            id: 'c_id_2',
            name: 'c_name_2',
            type: 'email',
          },
          source: 'alerting',
          severity: '2',
          status: {
            overview: 'Error',
            detail: [
              {
                recipient: 'sd@amazon.com',
                statusText: 'failed',
                statusCode: '500',
              },
              {
                recipient: 'david@amazon.com',
                statusText: 'no recipient',
                statusCode: '404',
              },
            ],
          },
          sentTime: 1614120516,
          lastUpdatedTime: 1614130526,
        },
        {
          title: 'title 3',
          channel: {
            id: 'c_id_3',
            name: 'c_name_3',
            type: 'chime',
          },
          source: 'reporting',
          severity: '1',
          status: {
            overview: 'Sent',
            detail: [
              {
                recipient: 'chime',
                statusText: 'Success',
                statusCode: '200',
              },
            ],
          },
          sentTime: 1614220006,
          lastUpdatedTime: 1614230116,
        },
        {
          title: 'title 4',
          channel: {
            id: 'c_id_1',
            name: 'c_name_1',
            type: 'slack',
          },
          source: 'reporting',
          severity: '1',
          status: {
            overview: 'Sent',
            detail: [
              {
                recipient: 'slack',
                statusText: 'Success',
                statusCode: '200',
              },
            ],
          },
          sentTime: 1614220516,
          lastUpdatedTime: 1614230526,
        },
        {
          title: 'title 5',
          channel: {
            id: 'c_id_2',
            name: 'c_name_2',
            type: 'email',
          },
          source: 'alerting',
          severity: '2',
          status: {
            overview: 'Error',
            detail: [
              {
                recipient: 'sd@amazon.com',
                statusText: 'failed',
                statusCode: '500',
              },
              {
                recipient: 'david@amazon.com',
                statusText: 'no recipient',
                statusCode: '404',
              },
            ],
          },
          sentTime: 1614120516,
          lastUpdatedTime: 1614130526,
        },
        {
          title: 'title 6',
          channel: {
            id: 'c_id_3',
            name: 'c_name_3',
            type: 'chime',
          },
          source: 'reporting',
          severity: '1',
          status: {
            overview: 'Sent',
            detail: [
              {
                recipient: 'chime',
                statusText: 'Success',
                statusCode: '200',
              },
            ],
          },
          sentTime: 1614220006,
          lastUpdatedTime: 1614230116,
        },
      ],
      totalNotifications: 6,
    };

    return fakeRes;
  };
}
