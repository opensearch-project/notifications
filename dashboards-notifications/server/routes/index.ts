/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { IRouter } from '../../../../src/core/server';
import { configRoutes } from './configRoutes';
import { eventRoutes } from './eventRoutes';

export function defineRoutes(router: IRouter) {
  configRoutes(router);
  eventRoutes(router);
}
