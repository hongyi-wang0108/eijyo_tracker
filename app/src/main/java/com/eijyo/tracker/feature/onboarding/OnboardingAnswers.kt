package com.eijyo.tracker.feature.onboarding

import com.eijyo.tracker.data.model.ApplicationPath
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.DatePrecision
import com.eijyo.tracker.data.model.ImmigrationOffice
import com.eijyo.tracker.data.model.IncomeRange
import com.eijyo.tracker.data.model.ResultType
import com.eijyo.tracker.data.model.TriState
import com.eijyo.tracker.data.model.UserProfile
import com.eijyo.tracker.data.model.VisaType
import kotlinx.serialization.Serializable

/**
 * All answers collected during onboarding. Serializable so it can be persisted as a
 * draft and restored if the user leaves mid-questionnaire (PM doc 6.1).
 */
@Serializable
data class OnboardingAnswers(
    val nickname: String = "",
    val visaType: VisaType? = null,
    val submittedStatus: ApplicationStatus? = null,
    val dateMode: DatePrecision = DatePrecision.DAY,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val office: ImmigrationOffice? = null,
    val path: ApplicationPath? = null,
    val tax: TriState? = null,
    val pension: TriState? = null,
    val health: TriState? = null,
    val income: IncomeRange? = null,
    val dependents: Int = 0,
    val supplement: TriState? = null,
) {
    /** True when the user reports the application is already submitted or decided. */
    val hasSubmitted: Boolean
        get() = submittedStatus == ApplicationStatus.REVIEWING ||
            submittedStatus == ApplicationStatus.COMPLETED

    fun toUserProfile(): UserProfile = UserProfile(nickname = nickname.trim())

    fun toApplicationProfile(): ApplicationProfile {
        val status = submittedStatus ?: ApplicationStatus.PREPARING
        val (submittedDate, precision) = when {
            !hasSubmitted -> null to DatePrecision.UNKNOWN
            dateMode == DatePrecision.DAY && year != null && month != null && day != null ->
                "%04d-%02d-%02d".format(year, month, day) to DatePrecision.DAY
            dateMode == DatePrecision.MONTH && year != null && month != null ->
                "%04d-%02d".format(year, month) to DatePrecision.MONTH
            else -> null to DatePrecision.UNKNOWN
        }
        return ApplicationProfile(
            status = status,
            visaType = visaType,
            applicationPath = path,
            submittedOffice = if (hasSubmitted) office else null,
            submittedDate = submittedDate,
            submittedDatePrecision = precision,
            resultType = if (status == ApplicationStatus.COMPLETED) ResultType.UNKNOWN else ResultType.UNKNOWN,
            annualIncomeRange = income,
            dependentsCount = dependents,
            taxPaidStatus = tax ?: TriState.UNKNOWN,
            pensionPaidStatus = pension ?: TriState.UNKNOWN,
            healthInsuranceStatus = health ?: TriState.UNKNOWN,
            hasSupplementRequest = supplement ?: TriState.UNKNOWN,
        )
    }
}

/** Ordered onboarding steps. APP_DATE/OFFICE/SUPPLEMENT presence depends on answers. */
enum class OnboardingStep {
    NICKNAME,
    VISA,
    SUBMITTED_STATUS,
    APP_DATE,
    OFFICE,
    PATH,
    TAX,
    PENSION,
    HEALTH,
    INCOME,
    DEPENDENTS,
    SUPPLEMENT,
}
