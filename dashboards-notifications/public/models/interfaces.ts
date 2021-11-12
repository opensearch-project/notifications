/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { NotificationService } from '../services';
import EventService from '../services/EventService';

export interface BrowserServices {
  notificationService: NotificationService;
  eventService: EventService;
}
