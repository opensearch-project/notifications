/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.notifications.security

import org.opensearch.commons.authuser.User

interface UserAccess {
    /**
     * Validate User if eligible to do operation
     * If filter by BackendRoles is enabled then backend roles should be present
     */
    fun validateUser(user: User?)

    /**
     * Get all user access info from user object.
     */
    fun getAllAccessInfo(user: User?): List<String>

    /**
     * Get access info for search filtering
     */
    fun getSearchAccessInfo(user: User?): List<String>

    /**
     * validate if user has access based on given access list
     */
    fun doesUserHaveAccess(user: User?, access: List<String>): Boolean

    /**
     * validate if user has send-notification access based on given access list
     */
    fun doesUserHaveSendAccess(user: User?, access: List<String>): Boolean
}
