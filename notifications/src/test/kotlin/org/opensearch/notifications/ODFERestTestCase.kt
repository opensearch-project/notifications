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

package org.opensearch.notifications

import com.google.gson.JsonObject
import org.apache.http.Header
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.config.RequestConfig
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.ssl.SSLContextBuilder
import org.junit.After
import org.junit.Before
import org.opensearch.client.Request
import org.opensearch.client.RequestOptions
import org.opensearch.client.ResponseException
import org.opensearch.client.RestClient
import org.opensearch.client.RestClientBuilder
import org.opensearch.common.settings.Settings
import org.opensearch.common.unit.TimeValue
import org.opensearch.common.util.concurrent.ThreadContext
import org.opensearch.common.xcontent.DeprecationHandler
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.common.xcontent.XContentType
import org.opensearch.test.rest.OpenSearchRestTestCase
import java.io.IOException
import java.security.cert.X509Certificate

abstract class ODFERestTestCase : OpenSearchRestTestCase() {

    private fun isHttps(): Boolean {
        return System.getProperty("https", "false")!!.toBoolean()
    }

    override fun getProtocol(): String {
        return if (isHttps()) {
            "https"
        } else {
            "http"
        }
    }

    override fun preserveIndicesUponCompletion(): Boolean {
        return true
    }

    @Before
    @Throws(InterruptedException::class)
    fun setupClient() {
        if (client() == null) {
            initClient()
        }
    }

    @Throws(IOException::class)
    @After
    fun wipeAllODFEIndices() {
        val response = client().performRequest(Request("GET", "/_cat/indices?format=json&expand_wildcards=all"))

        val xContentType = XContentType.fromMediaTypeOrFormat(response.entity.contentType.value)
        xContentType.xContent().createParser(
            NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
            response.entity.content
        ).use { parser ->
            for (index in parser.list()) {
                val jsonObject: Map<*, *> = index as java.util.HashMap<*, *>
                val indexName: String = jsonObject["index"] as String
                // .opensearch_security isn't allowed to delete from cluster
                if (".opensearch_security" != indexName) {
                    client().performRequest(Request("DELETE", "/$indexName"))
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun buildClient(settings: Settings, hosts: Array<HttpHost>): RestClient {
        val builder = RestClient.builder(*hosts)
        if (isHttps()) {
            configureHttpsClient(builder, settings)
        } else {
            configureClient(builder, settings)
        }
        builder.setStrictDeprecationMode(true)
        return builder.build()
    }

    @Throws(IOException::class)
    protected fun configureHttpsClient(builder: RestClientBuilder, settings: Settings) {
        val headers = ThreadContext.buildDefaultHeaders(settings)
        val defaultHeaders = arrayOfNulls<Header>(headers.size)
        var i = 0
        for ((key, value) in headers) {
            defaultHeaders[i++] = BasicHeader(key, value)
        }
        builder.setDefaultHeaders(defaultHeaders)
        builder.setHttpClientConfigCallback { httpClientBuilder: HttpAsyncClientBuilder ->
            val userName = System.getProperty("user")
            val password = System.getProperty("password")
            val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
            credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(userName, password))
            try {
                return@setHttpClientConfigCallback httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                    // disable the certificate since our testing cluster just uses the default security configuration
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setSSLContext(SSLContextBuilder.create()
                        .loadTrustMaterial(null) { _: Array<X509Certificate?>?, _: String? -> true }
                        .build())
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
        val socketTimeoutString = settings[CLIENT_SOCKET_TIMEOUT]
        val socketTimeout = TimeValue.parseTimeValue(socketTimeoutString ?: "60s", CLIENT_SOCKET_TIMEOUT)
        builder.setRequestConfigCallback { conf: RequestConfig.Builder ->
            conf.setSocketTimeout(
                Math.toIntExact(
                    socketTimeout.millis
                )
            )
        }
        if (settings.hasValue(CLIENT_PATH_PREFIX)) {
            builder.setPathPrefix(settings[CLIENT_PATH_PREFIX])
        }
    }

    @Throws(IOException::class)
    protected fun updateClusterSettings(setting: ClusterSetting): JsonObject? {
        val request = Request("PUT", "/_cluster/settings")
        val persistentSetting = "{\"${setting.type}\": {\"${setting.name}\": ${setting.value}}}"
        request.setJsonEntity(persistentSetting)
        val restOptionsBuilder = RequestOptions.DEFAULT.toBuilder()
        restOptionsBuilder.addHeader("Content-Type", "application/json")
        request.setOptions(restOptionsBuilder)
        return executeRequest(request)
    }

    protected fun executeRequest(request: Request): JsonObject {
        val response = try {
            client().performRequest(request)
        } catch (exception: ResponseException) {
            exception.response
        }
        val responseBody = getResponseBody(response, true)
        return jsonify(responseBody)
    }

    @Throws(IOException::class)
    protected fun getAllClusterSettings(): JsonObject? {
        val request = Request("GET", "/_cluster/settings?flat_settings&include_defaults")
        val restOptionsBuilder = RequestOptions.DEFAULT.toBuilder()
        restOptionsBuilder.addHeader("Content-Type", "application/json")
        request.setOptions(restOptionsBuilder)
        return executeRequest(request)
    }

    @Throws(IOException::class)
    protected fun wipeAllClusterSettings() {
        updateClusterSettings(ClusterSetting("persistent", "*", null))
        updateClusterSettings(ClusterSetting("transient", "*", null))
    }

    protected class ClusterSetting(val type: String, val name: String, var value: Any?) {
        init {
            this.value = if (value == null) "null" else "\"" + value + "\""
        }
    }
}
