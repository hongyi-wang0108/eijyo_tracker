package com.eijyo.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * An anonymized real-world case used to refine predictions and populate the data
 * screen. Seeded from bundled samples for the MVP; later sourced from opted-in
 * anonymous submissions. Holds no personally identifying information.
 */
@Entity(tableName = "case_record")
data class CaseRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val office: ImmigrationOffice,
    val visaType: VisaType?,
    val applicationPath: ApplicationPath?,
    val submittedDate: String?,
    val resultDate: String?,
    val waitDays: Int,
    val hadSupplementRequest: Boolean,
    val resultType: ResultType,
    val anonymousNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
