package com.eijyo.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The permanent-residency application file built from the onboarding answers.
 * [status] decides which Dashboard variant renders; per the PM doc, missing
 * fields never hide Dashboard modules — they switch copy instead.
 *
 * [submittedDate] stores an ISO-ish string whose meaning depends on
 * [submittedDatePrecision]: `YYYY-MM-DD` for DAY, `YYYY-MM` for MONTH, null for UNKNOWN.
 */
@Entity(tableName = "application_profile")
data class ApplicationProfile(
    @PrimaryKey val id: String = SINGLETON_ID,
    val userId: String = UserProfile.SINGLETON_ID,
    val status: ApplicationStatus = ApplicationStatus.PREPARING,
    val visaType: VisaType? = null,
    val applicationPath: ApplicationPath? = null,
    val submittedOffice: ImmigrationOffice? = null,
    val submittedDate: String? = null,
    val submittedDatePrecision: DatePrecision = DatePrecision.UNKNOWN,
    val resultDate: String? = null,
    val resultType: ResultType = ResultType.UNKNOWN,
    val annualIncomeRange: IncomeRange? = null,
    val dependentsCount: Int = 0,
    val taxPaidStatus: TriState = TriState.UNKNOWN,
    val pensionPaidStatus: TriState = TriState.UNKNOWN,
    val healthInsuranceStatus: TriState = TriState.UNKNOWN,
    val hasSupplementRequest: TriState = TriState.UNKNOWN,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val SINGLETON_ID = "local-application"
    }
}
