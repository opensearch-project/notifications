/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.security

import kotlinx.coroutines.suspendCancellableCoroutine
import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.core.action.ActionListener
import org.opensearch.core.rest.RestStatus
import org.opensearch.notifications.NotificationsResourceSharingExtension.Companion.RESOURCE_TYPE
import org.opensearch.notifications.ResourceSharingClientAccessor
import org.opensearch.notifications.settings.FilterByBackendRolesAccessStrategy
import org.opensearch.notifications.settings.PluginSettings
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Class for checking/filtering user access.
 */
internal object UserAccessManager : UserAccess {
    const val ADMIN_ROLE = "all_access"

    private fun isResourceSharingEnabled(): Boolean {
        val client = ResourceSharingClientAccessor.getResourceSharingClient()
        return client != null && client.isFeatureEnabledForType(RESOURCE_TYPE)
    }

    /**
     * {@inheritDoc}
     */
    override fun validateUser(user: User?) {
        if (isResourceSharingEnabled()) return
        if (PluginSettings.isRbacEnabled() && user?.backendRoles.isNullOrEmpty()) {
            throw OpenSearchStatusException(
                "User doesn't have backend roles configured. Contact administrator.",
                RestStatus.FORBIDDEN
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun getAllAccessInfo(user: User?): List<String> {
        if (isResourceSharingEnabled() || user == null) {
            return listOf()
        }
        return user.backendRoles
    }

    /**
     * {@inheritDoc}
     */
    override fun getSearchAccessInfo(user: User?): List<String> {
        if (isResourceSharingEnabled() || user == null || !PluginSettings.isRbacEnabled() || user.roles.contains(ADMIN_ROLE)) {
            return listOf()
        }
        return user.backendRoles
    }

    fun checkUserBackendRolesAccess(userBackendRoles: List<String>, objectAccess: List<String>): Boolean {
        val filterByAccessStrategy = PluginSettings.getFilterByBackendAccessStrategy()
        if (filterByAccessStrategy == FilterByBackendRolesAccessStrategy.ALL.strategy) {
            return userBackendRoles.containsAll(objectAccess)
        } else if (filterByAccessStrategy == FilterByBackendRolesAccessStrategy.INTERSECT.strategy) {
            return userBackendRoles.any { it in objectAccess }
        } else if (filterByAccessStrategy == FilterByBackendRolesAccessStrategy.EXACT.strategy) {
            return userBackendRoles.toSet().equals(objectAccess.toSet())
        }
        throw IllegalArgumentException(
            "Invalid filter by access strategy: $filterByAccessStrategy"
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun doesUserHaveAccess(user: User?, access: List<String>): Boolean {
        if (isResourceSharingEnabled() || user == null || !PluginSettings.isRbacEnabled()) {
            return true
        }
        return access.isEmpty() || user.roles.contains(ADMIN_ROLE) || checkUserBackendRolesAccess(user.backendRoles, access)
    }

    /**
     * Verify resource access via the security plugin's ResourceSharingClient.
     * Used for multi-ID requests where DocRequest.id() returns null and the
     * transport-level ResourceAccessEvaluator is skipped.
     */
    suspend fun verifyResourceAccess(resourceId: String, action: String) {
        val client = ResourceSharingClientAccessor.getResourceSharingClient() ?: return
        if (!client.isFeatureEnabledForType(RESOURCE_TYPE)) return
        val hasAccess = suspendCancellableCoroutine { cont ->
            client.verifyAccess(
                resourceId,
                RESOURCE_TYPE,
                action,
                object : ActionListener<Boolean> {
                    override fun onResponse(response: Boolean) = cont.resume(response)
                    override fun onFailure(e: Exception) = cont.resumeWithException(e)
                }
            )
        }
        if (!hasAccess) {
            throw OpenSearchStatusException(
                "no permissions for [$action] on resource [$resourceId]",
                RestStatus.FORBIDDEN
            )
        }
    }
}
