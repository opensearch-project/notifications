/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integtest

import com.google.gson.JsonObject
import org.apache.hc.core5.http.HttpHost
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.opensearch.client.Request
import org.opensearch.client.RequestOptions
import org.opensearch.client.ResponseException
import org.opensearch.client.RestClient
import org.opensearch.client.WarningsHandler
import org.opensearch.common.io.PathUtils
import org.opensearch.common.settings.Settings
import org.opensearch.commons.ConfigConstants
import org.opensearch.commons.notifications.model.ConfigType
import org.opensearch.commons.rest.SecureRestClientBuilder
import org.opensearch.core.rest.RestStatus
import org.opensearch.core.xcontent.DeprecationHandler
import org.opensearch.core.xcontent.MediaType
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.notifications.NotificationPlugin
import org.opensearch.notifications.index.NotificationConfigIndex
import org.opensearch.rest.RestRequest
import org.opensearch.test.rest.OpenSearchRestTestCase
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.management.MBeanServerInvocationHandler
import javax.management.ObjectName
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

abstract class PluginRestTestCase : OpenSearchRestTestCase() {

    protected fun isHttps(): Boolean {
        return System.getProperty("https", "false")!!.toBoolean()
    }

    protected fun isLocalHost(): Boolean {
        val host = System.getProperty("tests.cluster", "dummyHost")!!.toString()
        return host.startsWith("localhost:")
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

    open fun preservePluginIndicesAfterTest(): Boolean = false

    @Throws(IOException::class)
    @After
    open fun wipeAllPluginIndices() {
        if (preservePluginIndicesAfterTest()) return

        val pluginIndices = listOf(".opensearch-notifications-config")
        val response = client().performRequest(Request("GET", "/_cat/indices?format=json&expand_wildcards=all"))
        val xContentType = MediaType.fromMediaType(response.entity.contentType)
        xContentType.xContent().createParser(
            NamedXContentRegistry.EMPTY,
            DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
            response.entity.content
        ).use { parser ->
            for (index in parser.list()) {
                val jsonObject: Map<*, *> = index as java.util.HashMap<*, *>
                val indexName: String = jsonObject["index"] as String
                if (pluginIndices.contains(indexName)) {
                    // TODO: remove PERMISSIVE option after moving system index access to REST API call
                    val request = Request("DELETE", "/$indexName")
                    val options = RequestOptions.DEFAULT.toBuilder()
                    options.setWarningsHandler(WarningsHandler.PERMISSIVE)
                    request.options = options.build()
                    adminClient().performRequest(request)
                }
            }
        }
    }

    /**
     * Returns the REST client settings used for super-admin actions like cleaning up after the test has completed.
     */
    override fun restAdminSettings(): Settings {
        return Settings
            .builder()
            .put("http.port", 9200)
            .put(ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_ENABLED, isHttps())
            .put(ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_PEMCERT_FILEPATH, "sample.pem")
            .put(ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_FILEPATH, "test-kirk.jks")
            .put(ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_PASSWORD, "changeit")
            .put(ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_KEYPASSWORD, "changeit")
            .build()
    }

    @Throws(IOException::class)
    override fun buildClient(settings: Settings, hosts: Array<HttpHost>): RestClient {
        if (isHttps()) {
            val keystore = settings.get(ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_FILEPATH)
            return when (keystore != null) {
                true -> {
                    // create adminDN (super-admin) client
                    val uri = javaClass.classLoader.getResource("sample.pem").toURI()
                    val configPath = PathUtils.get(uri).parent.toAbsolutePath()
                    SecureRestClientBuilder(settings, configPath, hosts)
                        .setSocketTimeout(60000)
                        .setConnectionRequestTimeout(180000)
                        .build()
                }
                false -> {
                    // create client with passed user
                    val userName = System.getProperty("user")
                    val password = System.getProperty("password")
                    SecureRestClientBuilder(hosts, isHttps(), userName, password)
                        .setSocketTimeout(60000)
                        .setConnectionRequestTimeout(180000)
                        .build()
                }
            }
        } else {
            val builder = RestClient.builder(*hosts)
            configureClient(builder, settings)
            builder.setStrictDeprecationMode(true)
            return builder.build()
        }
    }

    fun executeRequest(
        method: String,
        url: String,
        jsonString: String,
        expectedRestStatus: Int? = null,
        client: RestClient = client()
    ): JsonObject {
        val request = Request(method, url)
        request.setJsonEntity(jsonString)
        val restOptionsBuilder = RequestOptions.DEFAULT.toBuilder()
        restOptionsBuilder.addHeader("Content-Type", "application/json")
        request.setOptions(restOptionsBuilder)
        return executeRequest(request, expectedRestStatus, client)
    }

    fun executeRequest(request: Request, expectedRestStatus: Int? = null, client: RestClient = client()): JsonObject {
        val response = try {
            client.performRequest(request)
        } catch (exception: ResponseException) {
            exception.response
        }
        if (expectedRestStatus != null) {
            assertEquals(expectedRestStatus, response.statusLine.statusCode)
        }
        val responseBody = getResponseBody(response)
        return jsonify(responseBody)
    }

    fun createUser(name: String, passwd: String, backendRoles: Array<String>) {
        val request = Request("PUT", "/_plugins/_security/api/internalusers/$name")
        val broles = backendRoles.filter { it.isNotBlank() }.joinToString { it -> "\"$it\"" }
        val entity = """
            {
                "password": "$passwd",
                "backend_roles": [$broles],
                "attributes": {}
            }
        """.trimIndent()
        request.setJsonEntity(entity)
        client().performRequest(request)
    }

    fun deleteUser(name: String) {
        val request = Request(RestRequest.Method.DELETE.name, "/_plugins/_security/api/internalusers/$name")
        executeRequest(request, RestStatus.OK.status)
    }

    fun createUserRolesMapping(role: String, users: Array<String>) {
        val request = Request("PUT", "/_plugins/_security/api/rolesmapping/$role")
        val usersStr = users.joinToString { it -> "\"$it\"" }
        val entity = """
            {
                "backend_roles": [],
                "hosts": [],
                "users": [$usersStr]
            }
        """.trimIndent()
        request.setJsonEntity(entity)
        client().performRequest(request)
    }

    fun addPatchUserRolesMapping(role: String, users: Array<String>) {
        val request = Request("PATCH", "/_plugins/_security/api/rolesmapping/$role")
        val usersStr = users.joinToString { it -> "\"$it\"" }
        val entity = """
            [{
                "op": "add",
                "path": "users",
                "value": [$usersStr]
            }]
        """.trimIndent()
        request.setJsonEntity(entity)
        client().performRequest(request)
    }

    fun removePatchUserRolesMapping(role: String, users: Array<String>) {
        val request = Request("PATCH", "/_plugins/_security/api/rolesmapping/$role")
        val usersStr = users.joinToString { it -> "\"$it\"" }
        val entity = """
            [{
                "op": "remove",
                "path": "users",
                "value": [$usersStr]
            }]
        """.trimIndent()
        request.setJsonEntity(entity)
        client().performRequest(request)
    }

    fun deleteUserRolesMapping(role: String) {
        val request = Request("DELETE", "/_plugins/_security/api/rolesmapping/$role")
        client().performRequest(request)
    }

    fun createCustomRole(name: String, clusterPermissions: String?) {
        val request = Request("PUT", "/_plugins/_security/api/roles/$name")
        val permissions = if (clusterPermissions.isNullOrBlank()) "" else "\"$clusterPermissions\""
        val entity = """
            {
                "cluster_permissions": [$permissions],
                "tenant_permissions": []
            }
        """.trimIndent()
        request.setJsonEntity(entity)
        client().performRequest(request)
    }

    fun deleteCustomRole(name: String) {
        val request = Request("DELETE", "/_plugins/_security/api/roles/$name")
        client().performRequest(request)
    }

    fun createUserWithRoles(user: String, password: String, role: String, backendRole: String) {
        createUser(user, password, arrayOf(backendRole))
        addPatchUserRolesMapping(role, arrayOf(user))
    }

    fun deleteUserWithRoles(user: String, role: String) {
        removePatchUserRolesMapping(role, arrayOf(user))
        deleteUser(user)
    }

    fun createUserWithCustomRole(
        user: String,
        password: String,
        role: String,
        backendRole: String,
        clusterPermissions: String?
    ) {
        createUser(user, password, arrayOf(backendRole))
        createCustomRole(role, clusterPermissions)
        createUserRolesMapping(role, arrayOf(user))
    }

    fun deleteUserWithCustomRole(
        user: String,
        role: String
    ) {
        deleteUserRolesMapping(role)
        deleteCustomRole(role)
        deleteUser(user)
    }

    fun createConfig(
        nameSubstring: String = "",
        configType: ConfigType = ConfigType.SLACK,
        isEnabled: Boolean = true,
        smtpAccountId: String = "",
        emailGroupId: Set<String> = setOf(),
        client: RestClient = client()
    ): String {
        val createRequestJsonString = getCreateNotificationRequestJsonString(
            nameSubstring,
            configType,
            isEnabled,
            smtpAccountId,
            emailGroupId
        )
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs",
            createRequestJsonString,
            RestStatus.OK.status,
            client
        )
        refreshAllIndices()
        val configId = createResponse.get("config_id").asString
        Assert.assertNotNull(configId)
        Thread.sleep(100)
        return configId
    }

    fun createConfigWithRequestJsonString(
        createRequestJsonString: String,
        client: RestClient = client()
    ): String {
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs",
            createRequestJsonString,
            RestStatus.OK.status,
            client
        )
        refreshAllIndices()
        Thread.sleep(100)
        return createResponse.get("config_id").asString
    }

    fun deleteConfig(
        configId: String,
        client: RestClient = client()
    ): JsonObject {
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs/$configId",
            "",
            RestStatus.OK.status,
            client
        )
        refreshAllIndices()
        return deleteResponse
    }

    fun deleteConfigs(
        configIds: Set<String>,
        client: RestClient = client()
    ): JsonObject {
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "${NotificationPlugin.PLUGIN_BASE_URI}/configs?config_id_list=${configIds.joinToString(separator = ",")}",
            "",
            RestStatus.OK.status,
            client
        )
        refreshAllIndices()
        return deleteResponse
    }

    @After
    open fun wipeAllSettings() {
        wipeAllClusterSettings()
    }

    @Throws(IOException::class)
    protected fun updateClusterSettings(setting: ClusterSetting, client: RestClient = client()): JsonObject {
        val request = Request("PUT", "/_cluster/settings")
        val persistentSetting = "{\"${setting.type}\": {\"${setting.name}\": ${setting.value}}}"
        request.setJsonEntity(persistentSetting)
        val restOptionsBuilder = RequestOptions.DEFAULT.toBuilder()
        restOptionsBuilder.addHeader("Content-Type", "application/json")
        request.setOptions(restOptionsBuilder)
        return executeRequest(request, client = client)
    }

    @Throws(IOException::class)
    protected open fun wipeAllClusterSettings() {
        updateClusterSettings(ClusterSetting("persistent", "*", null))
        updateClusterSettings(ClusterSetting("transient", "*", null))
    }

    protected fun getCurrentMappingsSchemaVersion(): Int {
        val getMappingRequest = Request(RestRequest.Method.GET.name, "${NotificationConfigIndex.INDEX_NAME}/_mappings")
        val requestOptions = RequestOptions.DEFAULT.toBuilder()
        // Allow direct access to system index warning
        requestOptions.setWarningsHandler { warnings: List<String> ->
            if (warnings.isEmpty()) {
                return@setWarningsHandler false
            } else if (warnings.size > 1) {
                return@setWarningsHandler true
            } else {
                return@setWarningsHandler !warnings[0].startsWith("this request accesses system indices:")
            }
        }
        getMappingRequest.setOptions(requestOptions)
        val response = executeRequest(getMappingRequest, RestStatus.OK.status, client())
        val mappingsObject = response.get(NotificationConfigIndex.INDEX_NAME).asJsonObject.get("mappings").asJsonObject
        return mappingsObject.get(NotificationConfigIndex._META)?.asJsonObject?.get(NotificationConfigIndex.SCHEMA_VERSION)?.asInt
            ?: NotificationConfigIndex.DEFAULT_SCHEMA_VERSION
    }

    // only refresh the notification config index to avoid too many warnings
    @Throws(IOException::class)
    override fun refreshAllIndices() {
        val refreshRequest = Request("POST", "${NotificationConfigIndex.INDEX_NAME}/_refresh")
        val requestOptions = RequestOptions.DEFAULT.toBuilder()
        // Allow direct access to system index warning
        requestOptions.setWarningsHandler { warnings: List<String> ->
            if (warnings.isEmpty()) {
                return@setWarningsHandler false
            } else if (warnings.size > 1) {
                return@setWarningsHandler true
            } else {
                return@setWarningsHandler !warnings[0].startsWith("this request accesses system indices:")
            }
        }
        refreshRequest.setOptions(requestOptions)
        client().performRequest(refreshRequest)
    }

    protected class ClusterSetting(val type: String, val name: String, var value: Any?) {
        init {
            this.value = if (value == null) "null" else "\"" + value + "\""
        }
    }

    companion object {
        internal interface IProxy {
            val version: String?
            var sessionId: String?

            fun getExecutionData(reset: Boolean): ByteArray?
            fun dump(reset: Boolean)
            fun reset()
        }

        /*
        * We need to be able to dump the jacoco coverage before the cluster shuts down.
        * The new internal testing framework removed some gradle tasks we were listening to,
        * to choose a good time to do it. This will dump the executionData to file after each test.
        * TODO: This is also currently just overwriting integTest.exec with the updated execData without
        *   resetting after writing each time. This can be improved to either write an exec file per test
        *   or by letting jacoco append to the file.
        * */
        @JvmStatic
        @AfterClass
        fun dumpCoverage() {
            // jacoco.dir set in esplugin-coverage.gradle, if it doesn't exist we don't
            // want to collect coverage, so we can return early
            val jacocoBuildPath = System.getProperty("jacoco.dir") ?: return
            val serverUrl = "service:jmx:rmi:///jndi/rmi://127.0.0.1:7777/jmxrmi"
            JMXConnectorFactory.connect(JMXServiceURL(serverUrl)).use { connector ->
                val proxy = MBeanServerInvocationHandler.newProxyInstance(
                    connector.mBeanServerConnection,
                    ObjectName("org.jacoco:type=Runtime"),
                    IProxy::class.java,
                    false
                )
                proxy.getExecutionData(false)?.let {
                    val path = Path.of("$jacocoBuildPath/integTest.exec")
                    Files.write(path, it)
                }
            }
        }
    }
}
