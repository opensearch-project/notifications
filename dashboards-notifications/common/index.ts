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
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

export const PLUGIN_ID = 'notificationsDashboards';
export const PLUGIN_NAME = 'notifications-dashboards';

// after delete operation returns, a delay is needed before the change reflects in another request
export const SERVER_DELAY = 1000;

const NODE_API_BASE_PATH = '/api/notifications';
export const NODE_API = Object.freeze({
  GET_CONFIGS: `${NODE_API_BASE_PATH}/get_configs`,
  GET_CONFIG: `${NODE_API_BASE_PATH}/get_config`,
  CREATE_CONFIG: `${NODE_API_BASE_PATH}/create_config`,
  DELETE_CONFIGS: `${NODE_API_BASE_PATH}/delete_configs`,
  UPDATE_CONFIG: `${NODE_API_BASE_PATH}/update_config`,
  GET_EVENTS: `${NODE_API_BASE_PATH}/get_events`,
  GET_EVENT: `${NODE_API_BASE_PATH}/get_event`,
});

// TODO change to _plugins when backend updates
const OPENSEARCH_API_BASE_PATH = '/_opensearch/_notifications';
export const OPENSEARCH_API = Object.freeze({
  CONFIGS: `${OPENSEARCH_API_BASE_PATH}/configs`,
  EVENTS: `${OPENSEARCH_API_BASE_PATH}/events`,
});

export const REQUEST = Object.freeze({
  PUT: 'PUT',
  DELETE: 'DELETE',
  GET: 'GET',
  POST: 'POST',
  HEAD: 'HEAD',
});
