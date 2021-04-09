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

package com.amazon.opensearch.notifications.resthandler

import org.junit.jupiter.api.Test
import org.opensearch.rest.RestHandler
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.test.OpenSearchTestCase

internal class SendMessageRestHandlerTests : OpenSearchTestCase() {

    @Test
    fun `SendRestHandler name should return send`() {
        val restHandler = SendMessageRestHandler()
        assertEquals("send_message", restHandler.name)
    }

    @Test
    fun `SendRestHandler routes should return send url`() {
        val restHandler = SendMessageRestHandler()
        val routes = restHandler.routes()
        val actualRouteSize = routes.size
        val actualRoute = routes[0]
        val expectedRoute = RestHandler.Route(POST, "/_opensearch/_notifications/send")
        assertEquals(1, actualRouteSize)
        assertEquals(expectedRoute.method, actualRoute.method)
        assertEquals(expectedRoute.path, actualRoute.path)
    }
}
