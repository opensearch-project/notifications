/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
  GET_AVAILABLE_FEATURES: `${NODE_API_BASE_PATH}/features`,
  SEND_TEST_MESSAGE: `${NODE_API_BASE_PATH}/test_message`,
});

const OPENSEARCH_API_BASE_PATH = '/_plugins/_notifications';
export const OPENSEARCH_API = Object.freeze({
  CONFIGS: `${OPENSEARCH_API_BASE_PATH}/configs`,
  EVENTS: `${OPENSEARCH_API_BASE_PATH}/events`,
  TEST_MESSAGE: `${OPENSEARCH_API_BASE_PATH}/feature/test`,
  FEATURES: `${OPENSEARCH_API_BASE_PATH}/features`,
});

export const REQUEST = Object.freeze({
  PUT: 'PUT',
  DELETE: 'DELETE',
  GET: 'GET',
  POST: 'POST',
  HEAD: 'HEAD',
});
