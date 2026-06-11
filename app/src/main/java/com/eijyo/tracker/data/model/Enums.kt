package com.eijyo.tracker.data.model

/**
 * Domain enums. Each carries a Chinese [label] used directly in the UI.
 * Persistence stores the enum [name] (stable identifier), never the label,
 * so display text can change without migrating the database.
 */

/** Main application state — drives which Dashboard variant is shown. */
enum class ApplicationStatus(val label: String) {
    PREPARING("准备中"),
    REVIEWING("审查中"),
    COMPLETED("已结束"),
}

/** Final result for a completed application. */
enum class ResultType(val label: String) {
    APPROVED("许可"),
    REJECTED("不许可"),
    WITHDRAWN("撤回"),
    UNKNOWN("未知"),
}

/** Yes / No / Unknown answer used for tax, pension and health-insurance status. */
enum class TriState(val label: String) {
    YES("是"),
    NO("否"),
    UNKNOWN("不确定"),
}

/** Q2 — current residence status (在留资格). */
enum class VisaType(val label: String) {
    ENGINEER("技术・人文知识・国际业务"),
    HIGHLY_SKILLED("高度专业职"),
    SPOUSE_OF_JAPANESE("日本人配偶者"),
    SPOUSE_OF_PR("永住者配偶者"),
    LONG_TERM_RESIDENT("定住者"),
    DEPENDENT("家族滞在"),
}

/** Q6 — permanent-residency application path (申请路径). */
enum class ApplicationPath(val label: String) {
    TEN_YEARS("10年居住"),
    HSP_70("高度人才 70 分"),
    HSP_80("高度人才 80 分"),
    SPOUSE_OF_JAPANESE("日本人配偶者"),
    SPOUSE_OF_PR("永住者配偶者"),
    LONG_TERM_RESIDENT("定住者"),
    UNSURE("不确定"),
}

/** Q5 — immigration office the application was submitted to (提交入管). */
enum class ImmigrationOffice(val label: String) {
    TOKYO("东京入管"),
    OSAKA("大阪入管"),
    NAGOYA("名古屋入管"),
    YOKOHAMA("横滨支局"),
    KOBE("神户支局"),
    FUKUOKA("福冈入管"),
    OTHER("其他 / 不确定"),
}

/** Q10 — annual income range (年收范围). */
enum class IncomeRange(val label: String) {
    BELOW_300("300万日元以下"),
    R300_400("300万 - 400万日元"),
    R400_500("400万 - 500万日元"),
    R500_700("500万 - 700万日元"),
    ABOVE_700("700万日元以上"),
    UNDISCLOSED("不想填写"),
}

/** Precision of the submitted date, per Q4's day / month / unknown logic. */
enum class DatePrecision {
    DAY,
    MONTH,
    UNKNOWN,
}

/** Document grouping in the materials list. */
enum class DocumentCategory(val label: String) {
    IDENTITY("身份与在留"),
    TAX("纳税相关"),
    PENSION("年金相关"),
    HEALTH_INSURANCE("健康保险"),
    INCOME("收入与在职"),
    FAMILY("家族与配偶"),
    FORMS("申请表与照片"),
    SUPPLEMENT("补充资料"),
    OTHER("其他"),
}

/** How essential a document is. */
enum class RequiredLevel(val label: String) {
    REQUIRED("必备"),
    RECOMMENDED("建议"),
    CONDITIONAL("视情况"),
}

/** Per-document preparation status. */
enum class DocumentStatus(val label: String) {
    NOT_STARTED("未开始"),
    PREPARED("已准备"),
    SUBMITTED("已提交"),
    NEEDS_UPDATE("需更新"),
}

/** Status of an additional-documents (补资料) request. */
enum class SupplementStatus(val label: String) {
    RECEIVED("已收到"),
    SUBMITTED("已提交"),
    OVERDUE("已逾期"),
}

/** Overall risk level produced by the risk-assessment rules. */
enum class RiskLevel(val label: String) {
    LOW("低风险"),
    MEDIUM("中风险"),
    HIGH("高风险"),
}

/** How much trust to place in a prediction range; lowered when inputs are imprecise. */
enum class ConfidenceLevel(val label: String) {
    HIGH("较高"),
    MEDIUM("中等"),
    LOW("较低"),
}
