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
 *
 */

package org.opensearch.commons.notifications.model.config

import org.opensearch.commons.notifications.model.ConfigType

/**
 * Create ConfigData using parser.
 * @param tag tag corresponding to the parser
 * @param parser to be passed to data class' parse method
 * @return generated ConfigData
 */
internal fun createConfigData(configType: ConfigType, map: Map<String, Any>): BaseConfigData? {
    return CONFIG_TYPE_VS_PROPERTIES[configType]?.createConfigData(map)
}
