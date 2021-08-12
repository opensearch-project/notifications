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
  IOpenSearchDashboardsResponse,
  IRouter,
  ResponseError,
} from '../../../../src/core/server';
import { NODE_API } from '../../../dashboards-notifications/common';
import { getMetrics } from '../utils/metricsHelper';

export function metricRoutes(router: IRouter) {
  router.get(
    {
      path: NODE_API.STATS,
      validate: false,
    },
    async (
      context, 
      request, 
      response
    ): Promise<IOpenSearchDashboardsResponse<any | ResponseError>> => {
      try {
        // get Metrics
        const metrics = getMetrics();
        return response.ok({
          body: metrics
        })
      } catch (error) {
        console.log('error: could not get stats:', error);
        return response.custom({
          statusCode: error.statusCode || 500,
          body: error.message,
        });
      }
    }
  );
}