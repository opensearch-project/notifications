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

import { schema } from '@osd/config-schema';
import {
  ILegacyScopedClusterClient,
  IRouter,
} from '../../../../src/core/server';
import { NODE_API } from '../../../dashboards-notifications/common';
import { joinRequestParams } from '../utils/helper';

export function eventRoutes(router: IRouter) {
  router.get(
    {
      path: NODE_API.GET_EVENTS,
      validate: {
        query: schema.object({
          from_index: schema.number(),
          max_items: schema.number(),
          query: schema.maybe(schema.string()),
          sort_field: schema.string(),
          sort_order: schema.string(),
          last_updated_time_ms: schema.maybe(schema.string()),
          'status_list.config_name': schema.maybe(
            schema.oneOf([schema.arrayOf(schema.string()), schema.string()])
          ),
          'status_list.config_type': schema.maybe(
            schema.oneOf([schema.arrayOf(schema.string()), schema.string()])
          ),
          'event_source.feature': schema.maybe(
            schema.oneOf([schema.arrayOf(schema.string()), schema.string()])
          ),
          'event_source.severity': schema.maybe(
            schema.oneOf([schema.arrayOf(schema.string()), schema.string()])
          ),
          'status_list.delivery_status.status_code': schema.maybe(
            schema.oneOf([schema.arrayOf(schema.string()), schema.string()])
          ),
        }),
      },
    },
    async (context, request, response) => {
      const query = request.query.query;
      const last_updated_time_ms = request.query.last_updated_time_ms;
      const config_name = joinRequestParams(
        request.query['status_list.config_name']
      );
      const config_type = joinRequestParams(
        request.query['status_list.config_type']
      );
      const feature = joinRequestParams(request.query['event_source.feature']);
      const severity = joinRequestParams(
        request.query['event_source.severity']
      );
      const status_code = joinRequestParams(
        request.query['status_list.delivery_status.status_code']
      );

      // @ts-ignore
      const client: ILegacyScopedClusterClient = context.notificationsContext.notificationsClient.asScoped(
        request
      );
      try {
        const resp = await client.callAsCurrentUser('notifications.getEvents', {
          from_index: request.query.from_index,
          max_items: request.query.max_items,
          sort_field: request.query.sort_field,
          sort_order: request.query.sort_order,
          ...(query && { query }),
          ...(last_updated_time_ms && { last_updated_time_ms }),
          ...(config_name && { 'status_list.config_name': config_name }),
          ...(config_type && { 'status_list.config_type': config_type }),
          ...(feature && { 'event_source.feature': feature }),
          ...(severity && { 'event_source.severity': severity }),
          ...(status_code && {
            'status_list.delivery_status.status_code': status_code,
          }),
        });
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
