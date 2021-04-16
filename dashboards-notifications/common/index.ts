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

export enum SORT_DIRECTION {
  ASC = 'asc',
  DESC = 'desc',
}

export const BASE_API_PATH = '/api/notifications';
export const NODE_API = Object.freeze({
  NOTIFICATIONS: `${BASE_API_PATH}/notifications`,
  _SEARCH: `${BASE_API_PATH}/_search`,
  _INDICES: `${BASE_API_PATH}/_indices`,
  _MAPPINGS: `${BASE_API_PATH}/_mappings`,
  APPLY_POLICY: `${BASE_API_PATH}/applyPolicy`,
  EDIT_ROLLOVER_ALIAS: `${BASE_API_PATH}/editRolloverAlias`,
  POLICIES: `${BASE_API_PATH}/policies`,
  ROLLUPS: `${BASE_API_PATH}/rollups`,
  MANAGED_INDICES: `${BASE_API_PATH}/managedIndices`,
  RETRY: `${BASE_API_PATH}/retry`,
  CHANGE_POLICY: `${BASE_API_PATH}/changePolicy`,
  REMOVE_POLICY: `${BASE_API_PATH}/removePolicy`,
});

export const REQUEST = Object.freeze({
  PUT: 'PUT',
  DELETE: 'DELETE',
  GET: 'GET',
  POST: 'POST',
  HEAD: 'HEAD',
});
