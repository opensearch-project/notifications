/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.security

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.notifications.settings.PluginSettings
import org.opensearch.rest.RestStatus

/**
 * Class for checking/filtering user access.
 */
internal object UserAccessManager : UserAccess {
    const val ADMIN_ROLE = "all_access"

    /**
     * {@inheritDoc}
     */
    override fun validateUser(user: User?) {
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
        if (user == null) { // Filtering is disabled
            return listOf()
        }
        return user.backendRoles
    }

    /**
     * {@inheritDoc}
     */
    override fun getSearchAccessInfo(user: User?): List<String> {
        if (user == null || !PluginSettings.isRbacEnabled() || user.roles.contains(ADMIN_ROLE)) { // Filtering is disabled
            return listOf()
        }
        return user.backendRoles
    }

    /**
     * {@inheritDoc}
     */
    override fun doesUserHaveAccess(user: User?, access: List<String>): Boolean {
        if (user == null || !PluginSettings.isRbacEnabled()) { // Filtering is disabled
            return true
        }
        // User has access to resource if resource is public i.e. no access roles attached, user is an admin user or there is any intersection
        // between user backend roles and access roles
        return access.isEmpty() || user.roles.contains(ADMIN_ROLE) || user.backendRoles.any { it in access }
    }

    /**
     * {@inheritDoc}
     */
    override fun doesUserHaveSendAccess(user: User?, access: List<String>): Boolean {
        return !PluginSettings.filterSendByBackendRoles || doesUserHaveAccess(user, access)
    }
}
