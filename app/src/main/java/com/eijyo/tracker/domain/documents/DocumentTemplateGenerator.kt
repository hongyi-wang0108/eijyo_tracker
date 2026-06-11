package com.eijyo.tracker.domain.documents

import com.eijyo.tracker.data.model.ApplicationPath
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.DocumentCategory
import com.eijyo.tracker.data.model.DocumentItem
import com.eijyo.tracker.data.model.RequiredLevel
import com.eijyo.tracker.data.model.TriState
import com.eijyo.tracker.data.model.VisaType
import javax.inject.Inject

/**
 * Generates the materials checklist from onboarding answers (visa type + path +
 * dependents + supplement), per PM doc 6.3.3. Pure and deterministic so it can be
 * unit-tested and re-run whenever the profile changes. All items start as
 * NOT_STARTED; the user marks progress later on the materials screen.
 */
class DocumentTemplateGenerator @Inject constructor() {

    fun generate(profile: ApplicationProfile): List<DocumentItem> {
        val items = mutableListOf<Spec>()

        items += baseItems
        items += pathItems(profile.applicationPath)
        items += visaItems(profile.visaType)
        if (profile.dependentsCount > 0) items += dependentItems
        if (profile.hasSupplementRequest == TriState.YES) items += supplementItem

        return items.mapIndexed { index, spec ->
            DocumentItem(
                applicationId = profile.id,
                category = spec.category,
                title = spec.title,
                description = spec.description,
                requiredLevel = spec.level,
                source = spec.source,
                sortOrder = index,
            )
        }
    }

    private data class Spec(
        val category: DocumentCategory,
        val title: String,
        val description: String = "",
        val level: RequiredLevel = RequiredLevel.REQUIRED,
        val source: String = "",
    )

    private val baseItems = listOf(
        Spec(DocumentCategory.FORMS, "永住許可申請書", "出入国在留管理庁指定格式", source = "出入国在留管理庁"),
        Spec(DocumentCategory.FORMS, "证明照片（4cm×3cm）", "近 3 个月内拍摄"),
        Spec(DocumentCategory.IDENTITY, "在留卡复印件", "正反两面"),
        Spec(DocumentCategory.IDENTITY, "护照复印件", "身份页与所有在留印章页"),
        Spec(DocumentCategory.IDENTITY, "住民票", "记载全部家庭成员、不省略", source = "区役所 / 市役所"),
        Spec(DocumentCategory.TAX, "住民税課税・納税証明書", "近 3 年", RequiredLevel.REQUIRED, "区役所 / 市役所"),
        Spec(DocumentCategory.PENSION, "年金缴纳记录", "ねんきん定期便 或 ねんきんネット 截图", source = "日本年金机构"),
        Spec(DocumentCategory.HEALTH_INSURANCE, "健康保险证复印件 / 缴纳证明", "确认连续缴纳"),
        Spec(DocumentCategory.INCOME, "在职证明书", "公司开具", source = "所属公司"),
        Spec(DocumentCategory.INCOME, "源泉徴収票 / 收入证明", "近 1 年", RequiredLevel.RECOMMENDED),
        Spec(DocumentCategory.FORMS, "理由书", "说明申请永住的理由", RequiredLevel.RECOMMENDED),
        Spec(DocumentCategory.FORMS, "身元保证书", "由保证人填写", RequiredLevel.RECOMMENDED),
    )

    private fun pathItems(path: ApplicationPath?): List<Spec> = when (path) {
        ApplicationPath.TEN_YEARS -> listOf(
            Spec(DocumentCategory.IDENTITY, "在留资格变迁说明", "梳理 10 年以上在日居住与在留经历", RequiredLevel.RECOMMENDED),
        )
        ApplicationPath.HSP_70, ApplicationPath.HSP_80 -> listOf(
            Spec(DocumentCategory.FORMS, "高度人材ポイント計算表", "按申请时点计算分数"),
            Spec(DocumentCategory.FORMS, "高度人材积分疎明资料", "学历 / 职历 / 年收等佐证材料"),
        )
        ApplicationPath.SPOUSE_OF_JAPANESE -> listOf(
            Spec(DocumentCategory.FAMILY, "配偶者的戸籍謄本", "记载婚姻事实", RequiredLevel.REQUIRED, "本籍地役所"),
            Spec(DocumentCategory.FAMILY, "婚姻届受理证明书", source = "区役所 / 市役所"),
            Spec(DocumentCategory.FAMILY, "质问书", "出入国指定格式"),
        )
        ApplicationPath.SPOUSE_OF_PR -> listOf(
            Spec(DocumentCategory.FAMILY, "配偶者（永住者）的在留卡复印件"),
            Spec(DocumentCategory.FAMILY, "婚姻关系证明材料", "结婚证明 / 戸籍等"),
        )
        ApplicationPath.LONG_TERM_RESIDENT -> listOf(
            Spec(DocumentCategory.FAMILY, "身分关系证明资料", "证明定住者身分基础"),
        )
        ApplicationPath.UNSURE, null -> emptyList()
    }

    private fun visaItems(visa: VisaType?): List<Spec> = when (visa) {
        VisaType.HIGHLY_SKILLED -> listOf(
            Spec(DocumentCategory.INCOME, "高度专业职活动说明", "确认当前在留活动", RequiredLevel.RECOMMENDED),
        )
        VisaType.DEPENDENT -> listOf(
            Spec(DocumentCategory.FAMILY, "扶养者关系证明", "证明与主申请人的家族关系", RequiredLevel.REQUIRED),
        )
        else -> emptyList()
    }

    private val dependentItems = listOf(
        Spec(DocumentCategory.FAMILY, "扶养亲属相关证明", "被扶养人住民票 / 收入证明等", RequiredLevel.CONDITIONAL),
    )

    private val supplementItem = listOf(
        Spec(DocumentCategory.SUPPLEMENT, "补充资料（追加提出）", "按入管补资料通知准备", RequiredLevel.REQUIRED),
    )
}
