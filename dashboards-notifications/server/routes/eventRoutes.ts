/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { schema } from '@osd/config-schema';
import {
  ILegacyScopedClusterClient,
  IRouter,
} from '../../../../src/core/server';
import { NODE_API } from '../../common';

export function eventRoutes(router: IRouter) {
  router.get(
    {
      path: `${NODE_API.GET_EVENT}/{eventId}`,
      validate: {
        params: schema.object({
          eventId: schema.string(),
        }),
      },
    },
    async (context, request, response) => {
      // @ts-ignore
      const client: ILegacyScopedClusterClient = context.notificationsContext.notificationsClient.asScoped(
        request
      );
      try {
        const resp = await client.callAsCurrentUser(
          'notifications.getEventById',
          { eventId: request.params.eventId }
        );
        return response.ok({ body: resp });
      } catch (error) {
        return response.custom({
          statusCode: error.statusCode || 500,
          body: error.message,
        });
      }
    }
  );

  router.get(
    {
      path: `${NODE_API.SEND_TEST_MESSAGE}/{configId}`,
      validate: {
        params: schema.object({
          configId: schema.string(),
        }),
        query: schema.object({
          feature: schema.string(),
        }),
      },
    },
    async (context, request, response) => {
      // @ts-ignore
      const client: ILegacyScopedClusterClient = context.notificationsContext.notificationsClient.asScoped(
        request
      );
      try {
        const resp = await client.callAsCurrentUser(
          'notifications.sendTestMessage',
          {
            configId: request.params.configId,
            feature: request.query.feature,
          }
        );
        return response.ok({ body: resp });
      } catch (error) {
        return response.custom({
          statusCode: error.statusCode || 500,
          body: error.message,
        });
      }
    }
  );
}
