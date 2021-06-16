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
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

  notifications.getEvents = clientAction({
    url: {
      fmt: OPENSEARCH_API.EVENTS,
    },
    method: 'GET',
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

  notifications.getAvailableFeatures = clientAction({
    url: {
      fmt: OPENSEARCH_API.FEATURES,
    },
    method: 'GET',
  });

}
