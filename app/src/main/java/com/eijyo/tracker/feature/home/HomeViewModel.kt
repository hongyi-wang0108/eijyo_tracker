package com.eijyo.tracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.ApplicationStatus
import com.eijyo.tracker.data.model.DocumentItem
import com.eijyo.tracker.data.model.DocumentStatus
import com.eijyo.tracker.data.model.Prediction
import com.eijyo.tracker.data.model.RiskLevel
import com.eijyo.tracker.data.repository.AnalysisRepository
import com.eijyo.tracker.data.repository.DocumentRepository
import com.eijyo.tracker.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    documentRepository: DocumentRepository,
    analysisRepository: AnalysisRepository,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        profileRepository.observeUser(),
        profileRepository.observeApplication(),
        documentRepository.observe(),
        analysisRepository.observePrediction(),
        analysisRepository.observeRisk(),
    ) { user, application, documents, prediction, risk ->
        if (application == null) {
            HomeUiState(loading = true)
        } else {
            buildState(
                nickname = user?.nickname.orEmpty(),
                application = application,
                documents = documents,
                prediction = prediction,
                riskLevel = risk?.level,
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
        prediction: Prediction?,
        riskLevel: RiskLevel?,
    ): HomeUiState {
        val prepared = documents.count {
            it.status == DocumentStatus.PREPARED || it.status == DocumentStatus.SUBMITTED
        }
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
            riskLevel = riskLevel,
            documentsPrepared = prepared,
            documentsTotal = documents.size,
        )
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
