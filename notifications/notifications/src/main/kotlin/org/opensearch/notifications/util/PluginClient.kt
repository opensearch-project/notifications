/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.util

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionType
import org.opensearch.commons.utils.logger
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.identity.Subject
import org.opensearch.transport.client.Client
import org.opensearch.transport.client.FilterClient

class PluginClient(delegate: Client) : FilterClient(delegate) {

    private var subject: Subject? = null

    companion object {
        private val log by logger(PluginClient::class.java)
    }

    fun setSubject(subject: Subject) {
        this.subject = subject
    }

    @Suppress("UNCHECKED_CAST")
    override fun <Request : ActionRequest, Response : ActionResponse> doExecute(
        action: ActionType<Response>,
        request: Request,
        listener: ActionListener<Response>
    ) {
        val currentSubject = subject
            ?: error("PluginClient is not initialized.")

        val storedContext = threadPool().threadContext.newStoredContext(false)

        try {
            currentSubject.runAs<Exception> {
                super.doExecute(
                    action,
                    request,
                    object : ActionListener<Response> {
                        override fun onResponse(response: Response) {
                            storedContext.restore()
                            listener.onResponse(response)
                        }

                        override fun onFailure(e: Exception) {
                            storedContext.restore()
                            listener.onFailure(e)
                        }
                    }
                )
            }
        } catch (e: Exception) {
            storedContext.restore()
            listener.onFailure(e)
        }
    }
}
