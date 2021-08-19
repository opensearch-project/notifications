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

import { IRouter } from '../../../../src/core/server';
import { configRoutes } from './configRoutes';
import { eventRoutes } from './eventRoutes';

export function defineRoutes(router: IRouter) {
  configRoutes(router);
  eventRoutes(router);
}
