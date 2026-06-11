package com.eijyo.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * One item in the generated materials checklist. Items are produced from the
 * onboarding answers (visa type + application path), not hand-picked by the user;
 * the user later updates [status] on the materials screen.
 */
@Entity(tableName = "document_item")
data class DocumentItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val applicationId: String = ApplicationProfile.SINGLETON_ID,
    val category: DocumentCategory,
    val title: String,
    val description: String = "",
    val requiredLevel: RequiredLevel = RequiredLevel.REQUIRED,
    val status: DocumentStatus = DocumentStatus.NOT_STARTED,
    val source: String = "",
    val expiresAt: String? = null,
    val notes: String = "",
    val sortOrder: Int = 0,
)
