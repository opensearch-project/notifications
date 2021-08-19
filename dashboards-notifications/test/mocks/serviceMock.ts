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

import { CoreStart } from 'opensearch-dashboards/public';
import { MainState } from '../../public/pages/Main/Main';
import { EventService, NotificationService } from '../../public/services';
import { CHANNEL_TYPE } from '../../public/utils/constants';
import httpClientMock from './httpClientMock';

const coreServicesMock = ({
  uiSettings: {
    get: jest.fn(),
  },
  chrome: {
    setBreadcrumbs: jest.fn(),
  },
  notifications: {
    toasts: {
      addDanger: jest.fn().mockName('addDanger'),
      addSuccess: jest.fn().mockName('addSuccess'),
      addError: jest.fn().mockName('addError'),
    },
  },
} as unknown) as CoreStart;

const browserServicesMock = new NotificationService(httpClientMock);
const eventServiceMock = new EventService(httpClientMock);
const notificationServiceMock = {
  notificationService: browserServicesMock,
  eventService: eventServiceMock,
};

const mainStateMock: MainState = {
  availableChannels: CHANNEL_TYPE,
  tooltipSupport: true,
};

export { notificationServiceMock, coreServicesMock, mainStateMock };
