/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { OPENSEARCH_API } from '../../common';

export function NotificationsPlugin(Client: any, config: any, components: any) {
  const clientAction = components.clientAction.factory;

  Client.prototype.notifications = components.clientAction.namespaceFactory();
  const notifications = Client.prototype.notifications.prototype;

  notifications.getConfigs = clientAction({
    url: {
      fmt: OPENSEARCH_API.CONFIGS,
    },
    method: 'GET',
  });

  notifications.createConfig = clientAction({
    url: {
      fmt: OPENSEARCH_API.CONFIGS,
    },
    method: 'POST',
    needBody: true,
  });

  notifications.getConfigById = clientAction({
    url: {
      fmt: `${OPENSEARCH_API.CONFIGS}/<%=configId%>`,
      req: {
        configId: {
          type: 'string',
          required: true,
        },
      },
    },
    method: 'GET',
  });

  notifications.deleteConfigs = clientAction({
    url: {
      fmt: OPENSEARCH_API.CONFIGS,
    },
    params: {
      config_id_list: {
        type: 'list',
        required: true,
      },
    },
    method: 'DELETE',
  });

  notifications.updateConfigById = clientAction({
    url: {
      fmt: `${OPENSEARCH_API.CONFIGS}/<%=configId%>`,
      req: {
        configId: {
          type: 'string',
          required: true,
        },
      },
    },
    method: 'PUT',
    needBody: true,
  });

  notifications.getEventById = clientAction({
    url: {
      fmt: `${OPENSEARCH_API.EVENTS}/<%=eventId%>`,
      req: {
        eventId: {
          type: 'string',
          required: true,
        },
      },
    },
    method: 'GET',
  });

  notifications.sendTestMessage = clientAction({
    url: {
      fmt: `${OPENSEARCH_API.TEST_MESSAGE}/<%=configId%>`,
      req: {
        configId: {
          type: 'string',
          required: true,
        },
      },
    },
    method: 'POST',
  });

  notifications.getServerFeatures = clientAction({
    url: {
      fmt: OPENSEARCH_API.FEATURES,
    },
    method: 'GET',
  });

}
