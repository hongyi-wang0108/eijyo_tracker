package com.eijyo.tracker.feature.settings

import android.content.Context
import android.net.Uri
import com.eijyo.tracker.EijyoApp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.local.AppPreferences
import com.eijyo.tracker.data.local.EijyoDatabase
import com.eijyo.tracker.data.local.LanguagePrefs
import com.eijyo.tracker.data.repository.DocumentRepository
import com.eijyo.tracker.data.repository.ProfileRepository
import com.eijyo.tracker.data.repository.SupplementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.time.LocalDate
import javax.inject.Inject

data class SettingsUiState(
    val displayName: String = "",
    val statusLabel: String = "",
    val officeLabel: String = "",
    val currentLanguage: String = "zh",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val documentRepo: DocumentRepository,
    private val supplementRepo: SupplementRepository,
    private val database: EijyoDatabase,
    private val appPreferences: AppPreferences,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val state = combine(
        profileRepo.observeUser(),
        profileRepo.observeApplication(),
    ) { user, app ->
        SettingsUiState(
            displayName = user?.nickname?.takeIf { it.isNotBlank() } ?: "",
            statusLabel = app?.status?.let { appContext.getString(it.labelRes) } ?: "",
            officeLabel = app?.submittedOffice?.let { appContext.getString(it.labelRes) } ?: "",
            currentLanguage = LanguagePrefs.get(appContext),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState(currentLanguage = LanguagePrefs.get(appContext)),
    )

    fun saveLanguage(code: String) {
        LanguagePrefs.set(appContext, code)
        // Refresh the live application resources so @ApplicationContext consumers
        // (ViewModels/domain via context.getString) switch language immediately; the
        // Activity itself picks it up through attachBaseContext on recreate().
        (appContext as? EijyoApp)?.applyLocale()
    }

    /** Suggested filename for the export "save document" picker. */
    fun exportFileName(): String = "eijyo-backup-${LocalDate.now()}.json"

    /**
     * Writes a JSON backup of all local data (profile, application, documents,
     * supplements) to the user-picked [uri]. Invokes [onResult] with success/failure.
     */
    fun exportTo(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = runCatching {
                val json = buildExportJson()
                withContext(Dispatchers.IO) {
                    appContext.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    } ?: error("openOutputStream returned null")
                }
            }.isSuccess
            onResult(ok)
        }
    }

    private suspend fun buildExportJson(): String {
        val user = profileRepo.observeUser().first()
        val app = profileRepo.getApplication()
        val documents = documentRepo.observe().first()
        val supplements = supplementRepo.observeByApplication().first()

        val obj = buildJsonObject {
            put("exportedAt", LocalDate.now().toString())
            put("schema", "eijyo-backup-v1")
            put("user", buildJsonObject {
                put("nickname", user?.nickname ?: "")
            })
            if (app == null) {
                put("application", JsonNull)
            } else {
                put("application", buildJsonObject {
                    put("status", app.status.name)
                    put("visaType", app.visaType?.name ?: "")
                    put("applicationPath", app.applicationPath?.name ?: "")
                    put("submittedOffice", app.submittedOffice?.name ?: "")
                    put("submittedDate", app.submittedDate ?: "")
                    put("submittedDatePrecision", app.submittedDatePrecision.name)
                    put("resultType", app.resultType.name)
                    put("resultDate", app.resultDate ?: "")
                    put("annualIncomeRange", app.annualIncomeRange?.name ?: "")
                    put("dependentsCount", app.dependentsCount)
                    put("taxPaidStatus", app.taxPaidStatus.name)
                    put("pensionPaidStatus", app.pensionPaidStatus.name)
                    put("healthInsuranceStatus", app.healthInsuranceStatus.name)
                    put("hasSupplementRequest", app.hasSupplementRequest.name)
                })
            }
            putJsonArray("documents") {
                documents.forEach { doc ->
                    add(buildJsonObject {
                        put("title", doc.title)
                        put("category", doc.category.name)
                        put("status", doc.status.name)
                    })
                }
            }
            putJsonArray("supplements") {
                supplements.forEach { sup ->
                    add(buildJsonObject {
                        put("type", sup.type)
                        put("status", sup.status.name)
                        put("receivedDate", sup.receivedDate ?: "")
                        put("deadlineDate", sup.deadlineDate ?: "")
                        put("submittedDate", sup.submittedDate ?: "")
                    })
                }
            }
        }
        return prettyJson.encodeToString(JsonObject.serializer(), obj)
    }

    /**
     * Permanently deletes all local data: every Room table plus preferences (onboarding
     * flag included), returning the app to first-run. Calls [onDone] after completion.
     */
    fun deleteAll(onDone: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
            appPreferences.clearAll()
            onDone()
        }
    }

    private companion object {
        val prettyJson = Json { prettyPrint = true }
    }
}
