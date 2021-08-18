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
import { NODE_API } from '../../common';
import { joinRequestParams } from '../utils/helper';

export function configRoutes(router: IRouter) {
  router.get(
    {
      path: NODE_API.GET_CONFIGS,
      validate: {
        query: schema.object({
          from_index: schema.number(),
          max_items: schema.number(),
          query: schema.maybe(schema.string()),
          config_type: schema.oneOf([
            schema.arrayOf(schema.string()),
            schema.string(),
          ]),
          feature_list: schema.maybe(
            schema.oneOf([schema.arrayOf(schema.string()), schema.string()])
          ),
          is_enabled: schema.maybe(schema.boolean()),
          sort_field: schema.string(),
          sort_order: schema.string(),
          config_id_list: schema.maybe(
            schema.oneOf([schema.arrayOf(schema.string()), schema.string()])
          ),
        }),
      },
    },
    async (context, request, response) => {
      const config_type = joinRequestParams(request.query.config_type);
      const feature_list = joinRequestParams(request.query.feature_list);
      const config_id_list = joinRequestParams(request.query.config_id_list);
      const query = request.query.query;
      // @ts-ignore
      const client: ILegacyScopedClusterClient = context.notificationsContext.notificationsClient.asScoped(
        request
      );
      try {
        const resp = await client.callAsCurrentUser(
          'notifications.getConfigs',
          {
            from_index: request.query.from_index,
            max_items: request.query.max_items,
            is_enabled: request.query.is_enabled,
            sort_field: request.query.sort_field,
            sort_order: request.query.sort_order,
            config_type,
            ...(feature_list && { feature_list }),
            ...(query && { query }),
            ...(config_id_list && { config_id_list }),
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

  router.get(
    {
      path: `${NODE_API.GET_CONFIG}/{configId}`,
      validate: {
        params: schema.object({
          configId: schema.string(),
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
          'notifications.getConfigById',
          { configId: request.params.configId }
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

  router.post(
    {
      path: NODE_API.CREATE_CONFIG,
      validate: {
        body: schema.any(),
      },
    },
    async (context, request, response) => {
      // @ts-ignore
      const client: ILegacyScopedClusterClient = context.notificationsContext.notificationsClient.asScoped(
        request
      );
      try {
        const resp = await client.callAsCurrentUser(
          'notifications.createConfig',
          { body: request.body }
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

  router.put(
    {
      path: `${NODE_API.UPDATE_CONFIG}/{configId}`,
      validate: {
        body: schema.any(),
        params: schema.object({
          configId: schema.string(),
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
          'notifications.updateConfigById',
          {
            configId: request.params.configId,
            body: request.body,
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

  router.delete(
    {
      path: NODE_API.DELETE_CONFIGS,
      validate: {
        query: schema.object({
          config_id_list: schema.oneOf([
            schema.arrayOf(schema.string()),
            schema.string(),
          ]),
        }),
      },
    },
    async (context, request, response) => {
      // @ts-ignore
      const client: ILegacyScopedClusterClient = context.notificationsContext.notificationsClient.asScoped(
        request
      );
      const config_id_list = joinRequestParams(request.query.config_id_list);
      try {
        const resp = await client.callAsCurrentUser(
          'notifications.deleteConfigs',
          { config_id_list }
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
      path: NODE_API.GET_AVAILABLE_FEATURES,
      validate: false,
    },
    async (context, request, response) => {
      // @ts-ignore
      const client: ILegacyScopedClusterClient = context.notificationsContext.notificationsClient.asScoped(
        request
      );
      try {
        const resp = await client.callAsCurrentUser(
          'notifications.getServerFeatures'
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
