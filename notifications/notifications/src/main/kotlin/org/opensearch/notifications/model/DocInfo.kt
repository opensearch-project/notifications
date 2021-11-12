/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.notifications.model

import org.opensearch.index.seqno.SequenceNumbers.UNASSIGNED_PRIMARY_TERM
import org.opensearch.index.seqno.SequenceNumbers.UNASSIGNED_SEQ_NO

/**
 * Class for storing the document properties.
 */
data class DocInfo(
    val id: String? = null,
    val version: Long = -1L,
    val seqNo: Long = UNASSIGNED_SEQ_NO,
    val primaryTerm: Long = UNASSIGNED_PRIMARY_TERM
)
