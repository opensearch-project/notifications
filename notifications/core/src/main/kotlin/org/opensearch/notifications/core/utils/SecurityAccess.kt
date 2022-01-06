/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.core.utils

import org.opensearch.SpecialPermission
import java.security.AccessController
import java.security.PrivilegedActionException
import java.security.PrivilegedExceptionAction

/**
 * Class for providing the elevated permission for the function call.
 */
object SecurityAccess {
    /**
     * Execute the operation in privileged mode.
     */
    @Throws(Exception::class)
    @Suppress("SwallowedException")
    fun <T> doPrivileged(operation: PrivilegedExceptionAction<T>?): T {
        SpecialPermission.check()
        return try {
            AccessController.doPrivileged(operation)
        } catch (e: PrivilegedActionException) {
            throw (e.cause as Exception?)!!
        }
    }
}
