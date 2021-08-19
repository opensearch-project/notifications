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
 *
 */

package org.opensearch.notifications.resthandler

import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.notifications.metrics.Metrics
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestChannel
import org.opensearch.rest.RestResponse
import org.opensearch.rest.RestStatus
import org.opensearch.rest.action.RestToXContentListener

/**
 * Overrides RestToXContentListener REST based action listener that assumes the response is of type
 * {@link ToXContent} and automatically builds an XContent based response
 * (wrapping the toXContent in startObject/endObject).
 */
internal class RestResponseToXContentListener<Response : BaseResponse>(channel: RestChannel) :
    RestToXContentListener<Response>(channel) {
    override fun buildResponse(response: Response, builder: XContentBuilder?): RestResponse {
        super.buildResponse(response, builder)

        Metrics.REQUEST_TOTAL.counter.increment()
        Metrics.REQUEST_INTERVAL_COUNT.counter.increment()

        when (response.getStatus()) {
            in RestStatus.OK..RestStatus.MULTI_STATUS -> Metrics.REQUEST_SUCCESS.counter.increment()
            RestStatus.FORBIDDEN -> Metrics.NOTIFICATIONS_SECURITY_USER_ERROR.counter.increment()
            in RestStatus.UNAUTHORIZED..RestStatus.TOO_MANY_REQUESTS -> Metrics.REQUEST_USER_ERROR.counter.increment()
            else -> Metrics.REQUEST_SYSTEM_ERROR.counter.increment()
        }
        return BytesRestResponse(getStatus(response), builder)
    }
    /**
     * {@inheritDoc}
     */
    override fun getStatus(response: Response): RestStatus {
        return response.getStatus()
    }
}
