package com.eijyo.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * An additional-documents request (补资料) from immigration. Created when the user
 * reports having received one (Q12); affects the timeline and prediction range.
 * Attachments are deferred to a later milestone.
 */
@Entity(tableName = "supplement_request")
data class SupplementRequest(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val applicationId: String = ApplicationProfile.SINGLETON_ID,
    val receivedDate: String? = null,
    val deadlineDate: String? = null,
    val submittedDate: String? = null,
    val type: String = "",
    val status: SupplementStatus = SupplementStatus.RECEIVED,
    val notes: String = "",
)
