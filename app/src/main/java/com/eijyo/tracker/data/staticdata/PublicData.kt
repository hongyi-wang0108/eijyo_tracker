package com.eijyo.tracker.data.staticdata

import com.eijyo.tracker.data.model.ImmigrationOffice

/**
 * Officially confirmable public reference data, bundled statically for the MVP.
 * Per the PM doc the app must NOT invent regional approval numbers — only show
 * what can be confirmed. Values reflect the published "standard processing period"
 * (4–6 months) from the Immigration Services Agency.
 */
data class PublicData(
    val standardProcessingRange: String = "4 - 6 个月",
    val standardProcessingMinMonths: Int = 4,
    val standardProcessingMaxMonths: Int = 6,
    val sourceName: String = "出入国在留管理庁",
    val sourceUpdatedAt: String = "2026年5月更新",
    val sourceUrl: String = "https://www.moj.go.jp/isa/",
    val regionalNote: String,
)

object PublicDataSource {

    private val regionalNotes: Map<ImmigrationOffice, String> = mapOf(
        ImmigrationOffice.TOKYO to "东京入管管辖范围广、申请量大，实际处理时间常处于官方区间偏长一端。",
        ImmigrationOffice.OSAKA to "大阪入管覆盖关西地区，处理时间通常接近官方标准区间。",
        ImmigrationOffice.NAGOYA to "名古屋入管覆盖中部地区，处理时间通常接近官方标准区间。",
        ImmigrationOffice.YOKOHAMA to "横滨支局受理量较大，建议预留充足等待时间。",
        ImmigrationOffice.KOBE to "神户支局覆盖兵库一带，处理时间通常接近官方标准区间。",
        ImmigrationOffice.FUKUOKA to "福冈入管覆盖九州地区，处理时间通常接近官方标准区间。",
        ImmigrationOffice.OTHER to "未指定具体入管，使用全国官方标准处理期间作为参考。",
    )

    /** Public data for the given office, or the national default when unknown. */
    fun forOffice(office: ImmigrationOffice?): PublicData {
        val resolved = office ?: ImmigrationOffice.OTHER
        return PublicData(
            regionalNote = regionalNotes[resolved] ?: regionalNotes.getValue(ImmigrationOffice.OTHER),
        )
    }
}
