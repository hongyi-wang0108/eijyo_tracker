package com.eijyo.tracker.feature.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eijyo.tracker.data.model.DocumentCategory
import com.eijyo.tracker.data.model.DocumentItem
import com.eijyo.tracker.data.model.DocumentStatus
import com.eijyo.tracker.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DocumentFilter(val label: String) {
    ALL("全部"),
    INCOMPLETE("未完成"),
    COMPLETE("已完成"),
    NEEDS_UPDATE("需更新"),
}

data class DocumentSection(
    val category: DocumentCategory,
    val items: List<DocumentItem>,
)

data class MaterialsUiState(
    val sections: List<DocumentSection> = emptyList(),
    val totalCount: Int = 0,
    val pendingCount: Int = 0,
    val readyCount: Int = 0,
    val needsUpdateCount: Int = 0,
    val filter: DocumentFilter = DocumentFilter.ALL,
)

@HiltViewModel
class MaterialsViewModel @Inject constructor(
    private val repo: DocumentRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(DocumentFilter.ALL)

    val state = combine(repo.observe(), _filter) { docs, filter ->
        val filtered = when (filter) {
            DocumentFilter.ALL -> docs
            DocumentFilter.INCOMPLETE -> docs.filter { it.status == DocumentStatus.NOT_STARTED }
            DocumentFilter.COMPLETE -> docs.filter {
                it.status == DocumentStatus.PREPARED || it.status == DocumentStatus.SUBMITTED
            }
            DocumentFilter.NEEDS_UPDATE -> docs.filter { it.status == DocumentStatus.NEEDS_UPDATE }
        }
        val sections = filtered
            .groupBy { it.category }
            .entries
            .sortedBy { (cat, _) -> cat.ordinal }
            .map { (cat, items) -> DocumentSection(cat, items) }
        MaterialsUiState(
            sections = sections,
            totalCount = docs.size,
            pendingCount = docs.count {
                it.status == DocumentStatus.NOT_STARTED || it.status == DocumentStatus.NEEDS_UPDATE
            },
            readyCount = docs.count {
                it.status == DocumentStatus.PREPARED || it.status == DocumentStatus.SUBMITTED
            },
            needsUpdateCount = docs.count { it.status == DocumentStatus.NEEDS_UPDATE },
            filter = filter,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MaterialsUiState())

    fun setFilter(f: DocumentFilter) { _filter.value = f }

    fun updateStatus(itemId: String, status: DocumentStatus) {
        viewModelScope.launch { repo.updateStatus(itemId, status) }
    }
}
