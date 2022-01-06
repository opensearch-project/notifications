/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
  availableConfigTypes: [
    'slack',
    'chime',
    'webhook',
    'email',
    'sns',
    'smtp_account',
    'ses_account',
    'email_group',
  ],
  tooltipSupport: true,
};

export { notificationServiceMock, coreServicesMock, mainStateMock };
