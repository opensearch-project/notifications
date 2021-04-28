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

import org.opensearch.common.io.stream.Writeable.Reader
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.commons.notifications.model.ConfigType

/**
 * Properties for ConfigTypes.
 * This interface is used to provide contract accross configTypes without reading into config data classes.
 */
interface ConfigDataProperties {

    /**
     * @return ChannelTag for concrete ConfigType
     */
    fun getChannelTag(): String

    /**
     * @return Reader for concrete ConfigType.
     */
    fun getConfigDataReader(): Reader<out BaseConfigData>

    /**
     * Create ConfigData for provided parser, by calling data class' parse.
     * @return Created ConfigData
     */
    fun createConfigData(parser: XContentParser): BaseConfigData

    /**
     * @return ConfigType for concrete implementation
     */
    fun getConfigType(): ConfigType
}
