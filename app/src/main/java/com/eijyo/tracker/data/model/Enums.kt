package com.eijyo.tracker.data.model

import androidx.annotation.StringRes
import com.eijyo.tracker.R

/** Main application state — drives which Dashboard variant is shown. */
enum class ApplicationStatus(@StringRes val labelRes: Int) {
    PREPARING(R.string.appstatus_preparing),
    REVIEWING(R.string.appstatus_reviewing),
    COMPLETED(R.string.appstatus_completed),
}

/** Final result for a completed application. */
enum class ResultType(@StringRes val labelRes: Int) {
    APPROVED(R.string.resulttype_approved),
    REJECTED(R.string.resulttype_rejected),
    WITHDRAWN(R.string.resulttype_withdrawn),
    UNKNOWN(R.string.resulttype_unknown),
}

/** Yes / No / Unknown answer used for tax, pension and health-insurance status. */
enum class TriState(@StringRes val labelRes: Int) {
    YES(R.string.tristate_yes),
    NO(R.string.tristate_no),
    UNKNOWN(R.string.tristate_unknown),
}

/** Q2 — current residence status (在留资格). */
enum class VisaType(@StringRes val labelRes: Int) {
    ENGINEER(R.string.visatype_engineer),
    HIGHLY_SKILLED(R.string.visatype_highly_skilled),
    SPOUSE_OF_JAPANESE(R.string.visatype_spouse_of_japanese),
    SPOUSE_OF_PR(R.string.visatype_spouse_of_pr),
    LONG_TERM_RESIDENT(R.string.visatype_long_term_resident),
    DEPENDENT(R.string.visatype_dependent),
}

/** Q6 — permanent-residency application path (申请路径). */
enum class ApplicationPath(@StringRes val labelRes: Int) {
    TEN_YEARS(R.string.apppath_ten_years),
    HSP_70(R.string.apppath_hsp_70),
    HSP_80(R.string.apppath_hsp_80),
    SPOUSE_OF_JAPANESE(R.string.apppath_spouse_of_japanese),
    SPOUSE_OF_PR(R.string.apppath_spouse_of_pr),
    LONG_TERM_RESIDENT(R.string.apppath_long_term_resident),
    UNSURE(R.string.apppath_unsure),
}

/** Q5 — immigration office the application was submitted to (提交入管). */
enum class ImmigrationOffice(@StringRes val labelRes: Int) {
    TOKYO(R.string.office_tokyo),
    OSAKA(R.string.office_osaka),
    NAGOYA(R.string.office_nagoya),
    YOKOHAMA(R.string.office_yokohama),
    KOBE(R.string.office_kobe),
    FUKUOKA(R.string.office_fukuoka),
    OTHER(R.string.office_other),
}

/** Q10 — annual income range (年收范围). */
enum class IncomeRange(@StringRes val labelRes: Int) {
    BELOW_300(R.string.income_below_300),
    R300_400(R.string.income_r300_400),
    R400_500(R.string.income_r400_500),
    R500_700(R.string.income_r500_700),
    ABOVE_700(R.string.income_above_700),
    UNDISCLOSED(R.string.income_undisclosed),
}

/** Precision of the submitted date, per Q4's day / month / unknown logic. */
enum class DatePrecision {
    DAY,
    MONTH,
    UNKNOWN,
}

/** Document grouping in the materials list. */
enum class DocumentCategory(@StringRes val labelRes: Int) {
    IDENTITY(R.string.doccat_identity),
    TAX(R.string.doccat_tax),
    PENSION(R.string.doccat_pension),
    HEALTH_INSURANCE(R.string.doccat_health_insurance),
    INCOME(R.string.doccat_income),
    FAMILY(R.string.doccat_family),
    FORMS(R.string.doccat_forms),
    SUPPLEMENT(R.string.doccat_supplement),
    OTHER(R.string.doccat_other),
}

/** How essential a document is. */
enum class RequiredLevel(@StringRes val labelRes: Int) {
    REQUIRED(R.string.reqlevel_required),
    RECOMMENDED(R.string.reqlevel_recommended),
    CONDITIONAL(R.string.reqlevel_conditional),
}

/** Per-document preparation status. */
enum class DocumentStatus(@StringRes val labelRes: Int) {
    NOT_STARTED(R.string.docstatus_not_started),
    PREPARED(R.string.docstatus_prepared),
    SUBMITTED(R.string.docstatus_submitted),
    NEEDS_UPDATE(R.string.docstatus_needs_update),
}

/** Status of an additional-documents (补资料) request. */
enum class SupplementStatus(@StringRes val labelRes: Int) {
    RECEIVED(R.string.suppstatus_received),
    SUBMITTED(R.string.suppstatus_submitted),
    OVERDUE(R.string.suppstatus_overdue),
}

/** Overall risk level produced by the risk-assessment rules. */
enum class RiskLevel(@StringRes val labelRes: Int) {
    LOW(R.string.risklevel_low),
    MEDIUM(R.string.risklevel_medium),
    HIGH(R.string.risklevel_high),
}

/** How much trust to place in a prediction range; lowered when inputs are imprecise. */
enum class ConfidenceLevel(@StringRes val labelRes: Int) {
    HIGH(R.string.conflevel_high),
    MEDIUM(R.string.conflevel_medium),
    LOW(R.string.conflevel_low),
}
