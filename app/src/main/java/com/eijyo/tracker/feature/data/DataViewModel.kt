package com.eijyo.tracker.feature.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.repository.ProfileRepository
import com.eijyo.tracker.data.staticdata.PublicData
import com.eijyo.tracker.data.staticdata.PublicDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DataUiState(
    val officeLabel: String = "",
    val visaTypeLabel: String = "",
    val pathLabel: String = "",
    val publicData: PublicData = PublicDataSource.forOffice(null),
)

@HiltViewModel
class DataViewModel @Inject constructor(
    profileRepo: ProfileRepository,
) : ViewModel() {

    val state = profileRepo.observeApplication().map { app ->
        DataUiState(
            officeLabel = app?.submittedOffice?.label ?: "",
            visaTypeLabel = app?.visaType?.label ?: "",
            pathLabel = app?.applicationPath?.label ?: "",
            publicData = PublicDataSource.forOffice(app?.submittedOffice),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DataUiState())
}
