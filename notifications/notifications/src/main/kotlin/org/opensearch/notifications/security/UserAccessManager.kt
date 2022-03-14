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
        if (user == null || !PluginSettings.isRbacEnabled()) { // Filtering is disabled
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
        return user.backendRoles.any { it in access }
    }

    /**
     * {@inheritDoc}
     */
    override fun doesUserHaveSendAccess(user: User?, access: List<String>): Boolean {
        return !PluginSettings.filterSendByBackendRoles || doesUserHaveAccess(user, access)
    }
}
