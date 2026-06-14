package com.eijyo.tracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.DocumentItem
import com.eijyo.tracker.data.model.DocumentStatus
import com.eijyo.tracker.data.model.ImmigrationOffice
import com.eijyo.tracker.data.model.Prediction
import com.eijyo.tracker.data.model.PublicDataDoc
import com.eijyo.tracker.data.model.RiskLevel
import com.eijyo.tracker.data.model.SupplementRequest
import com.eijyo.tracker.data.repository.AnalysisRepository
import com.eijyo.tracker.data.repository.DocumentRepository
import com.eijyo.tracker.data.repository.ProfileRepository
import com.eijyo.tracker.data.repository.PublicDataRepository
import com.eijyo.tracker.data.repository.SupplementRepository
import com.eijyo.tracker.domain.timeline.TimelineBuilder
import com.eijyo.tracker.domain.timeline.TimelineDisplayItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val greeting: String = "",
    val status: ApplicationStatus = ApplicationStatus.PREPARING,
    val statusSummary: String = "",
    val waitDays: Int? = null,
    val predictionRange: String? = null,
    val predictionPlaceholder: String? = null,
    val confidenceLabel: String? = null,
    val progressPercent: Int? = null,
    val riskLevel: RiskLevel? = null,
    val documentsPrepared: Int = 0,
    val documentsTotal: Int = 0,
    val timeline: List<TimelineDisplayItem> = emptyList(),
    val publicDataAsOf: String = "",
    val standardRange: String = "4 - 6 个月",
    val miniTrend: List<Int> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    documentRepository: DocumentRepository,
    analysisRepository: AnalysisRepository,
    supplementRepository: SupplementRepository,
    private val publicDataRepository: PublicDataRepository,
    private val timelineBuilder: TimelineBuilder,
) : ViewModel() {

    init {
        viewModelScope.launch { runCatching { publicDataRepository.load() } }
    }

    // Bundle the three analysis-side flows so the outer combine stays within its 5-arg
    // typed overload while still feeding the timeline summary.
    private data class Analysis(
        val prediction: Prediction?,
        val riskLevel: RiskLevel?,
        val supplements: List<SupplementRequest>,
    )

    val state: StateFlow<HomeUiState> = combine(
        profileRepository.observeUser(),
        profileRepository.observeApplication(),
        documentRepository.observe(),
        combine(
            analysisRepository.observePrediction(),
            analysisRepository.observeRisk(),
            supplementRepository.observeByApplication(),
        ) { prediction, risk, supplements -> Analysis(prediction, risk?.level, supplements) },
        publicDataRepository.state,
    ) { user, application, documents, analysis, dataResult ->
        if (application == null) {
            HomeUiState(loading = true)
        } else {
            buildState(
                nickname = user?.nickname.orEmpty(),
                application = application,
                documents = documents,
                analysis = analysis,
                doc = dataResult?.doc,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    private fun buildState(
        nickname: String,
        application: ApplicationProfile,
        documents: List<DocumentItem>,
        analysis: Analysis,
        doc: PublicDataDoc?,
    ): HomeUiState {
        val prediction = analysis.prediction
        val prepared = documents.count {
            it.status == DocumentStatus.PREPARED || it.status == DocumentStatus.SUBMITTED
        }
        val officeData = application.submittedOffice
            ?.takeIf { it != ImmigrationOffice.OTHER }
            ?.let { doc?.officeData(it.name) }
        val miniTrend = officeData?.permitsByYear?.takeLast(5)?.map { it.count }
            ?: officeData?.monthly?.takeLast(6)?.map { it.processed }
            ?: emptyList()
        return HomeUiState(
            loading = false,
            greeting = greeting(nickname),
            status = application.status,
            statusSummary = statusSummary(application),
            waitDays = prediction?.currentWaitDays.takeIf {
                application.status == ApplicationStatus.REVIEWING
            },
            predictionRange = prediction?.normalRange,
            predictionPlaceholder = placeholderFor(application, prediction),
            confidenceLabel = prediction?.confidenceLevel?.label,
            progressPercent = prediction?.progressPercent,
            riskLevel = analysis.riskLevel,
            documentsPrepared = prepared,
            documentsTotal = documents.size,
            timeline = timelineBuilder.summary(application, analysis.supplements, prediction),
            publicDataAsOf = doc?.dataAsOf?.let { monthLabel(it) } ?: "",
            standardRange = doc?.standardProcessing?.rangeLabel ?: "4 - 6 个月",
            miniTrend = miniTrend,
        )
    }

    /** "2026-03" → "2026年3月"; passthrough if unparseable. */
    private fun monthLabel(ym: String): String {
        val parts = ym.split("-")
        if (parts.size != 2) return ym
        val month = parts[1].toIntOrNull() ?: return ym
        return "${parts[0]}年${month}月"
    }

    private fun greeting(nickname: String): String {
        val name = nickname.ifBlank { "你" }
        val period = when (LocalTime.now().hour) {
            in 5..10 -> "早上好"
            in 11..13 -> "中午好"
            in 14..18 -> "下午好"
            else -> "晚上好"
        }
        return "$period，$name"
    }

    private fun statusSummary(app: ApplicationProfile): String = when (app.status) {
        ApplicationStatus.PREPARING -> "准备中"
        ApplicationStatus.REVIEWING ->
            app.submittedOffice?.let { "${it.label} · 审查中" } ?: "审查中"
        ApplicationStatus.COMPLETED -> "结果已记录"
    }

    private fun placeholderFor(app: ApplicationProfile, prediction: Prediction?): String? = when {
        app.status == ApplicationStatus.PREPARING -> "先完成材料清单和风险确认"
        app.status == ApplicationStatus.COMPLETED -> "结果记录已生成"
        prediction == null -> "补充申请日期后，可以生成预计时间"
        else -> null
    }
}
