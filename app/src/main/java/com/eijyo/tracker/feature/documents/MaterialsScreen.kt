package com.eijyo.tracker.feature.documents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.eijyo.tracker.R
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eijyo.tracker.core.ui.component.DogFace
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.core.ui.theme.MacaronPalette
import com.eijyo.tracker.data.model.DocumentItem
import com.eijyo.tracker.data.model.DocumentStatus
import com.eijyo.tracker.data.model.RequiredLevel

@Composable
fun MaterialsScreen(viewModel: MaterialsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var pendingStatus by remember { mutableStateOf<DocumentStatus?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MacaronPalette.Cream),
    ) {
        // Background blobs (fixed, behind scroll)
        Box(
            Modifier
                .offset((-58).dp, (-42).dp)
                .size(184.dp)
                .clip(CircleShape)
                .background(MacaronPalette.MintSoft),
        )
        Box(
            Modifier
                .offset(294.dp, 52.dp)
                .size(142.dp)
                .clip(CircleShape)
                .background(MacaronPalette.Peach),
        )
        Box(
            Modifier
                .offset(268.dp, 620.dp)
                .size(154.dp)
                .clip(CircleShape)
                .background(MacaronPalette.LavenderSoft),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 20.dp, end = 24.dp),
                ) {
                    Column {
                        Text(
                            stringResource(R.string.materials_title),
                            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                            color = MacaronPalette.Ink,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.materials_header_summary, state.totalCount, state.pendingCount),
                            style = EijyoTheme.typography.bodyMedium,
                            color = MacaronPalette.InkMuted,
                        )
                    }
                    DogFace(
                        modifier = Modifier.align(Alignment.TopEnd),
                        size = 36.dp,
                    )
                }
            }

            // Progress card
            item {
                Spacer(Modifier.height(16.dp))
                MaterialsProgressCard(
                    readyCount = state.readyCount,
                    totalCount = state.totalCount,
                    needsUpdateCount = state.needsUpdateCount,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Filter chips
            item {
                Spacer(Modifier.height(16.dp))
                FilterChipsRow(
                    selected = state.filter,
                    onSelect = viewModel::setFilter,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Empty state
            if (state.totalCount == 0) {
                item {
                    Spacer(Modifier.height(56.dp))
                    Text(
                        stringResource(R.string.materials_empty_no_data),
                        modifier = Modifier.fillMaxWidth(),
                        style = EijyoTheme.typography.bodyMedium,
                        color = MacaronPalette.InkMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            } else if (state.sections.isEmpty()) {
                item {
                    Spacer(Modifier.height(56.dp))
                    Text(
                        stringResource(R.string.materials_empty_filtered),
                        modifier = Modifier.fillMaxWidth(),
                        style = EijyoTheme.typography.bodyMedium,
                        color = MacaronPalette.InkMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Section cards
            state.sections.forEach { section ->
                item(key = section.category.name) {
                    Spacer(Modifier.height(16.dp))
                    DocumentSectionCard(
                        section = section,
                        selectedItemId = selectedItemId,
                        pendingStatus = pendingStatus,
                        onItemClick = { item ->
                            if (selectedItemId == item.id) {
                                selectedItemId = null
                                pendingStatus = null
                            } else {
                                selectedItemId = item.id
                                pendingStatus = item.status
                            }
                        },
                        onStatusSelect = { status -> pendingStatus = status },
                        onConfirm = {
                            val id = selectedItemId
                            val status = pendingStatus
                            if (id != null && status != null) {
                                viewModel.updateStatus(id, status)
                                selectedItemId = null
                                pendingStatus = null
                            }
                        },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
        }
    }
}

// ── Progress card ──────────────────────────────────────────────────────────────

@Composable
private fun MaterialsProgressCard(
    readyCount: Int,
    totalCount: Int,
    needsUpdateCount: Int,
    modifier: Modifier = Modifier,
) {
    val fraction = if (totalCount > 0) readyCount.toFloat() / totalCount else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = MacaronPalette.Shadow,
                spotColor = MacaronPalette.Shadow,
            )
            .clip(RoundedCornerShape(30.dp))
            .background(MacaronPalette.CreamSoft)
            .padding(horizontal = 22.dp, vertical = 18.dp),
    ) {
        // Paw print decoration (top-right corner)
        PawPrintDecoration(
            color = MacaronPalette.DogPaw,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp)
                .rotate(12f)
                .size(17.dp),
            opacity = 0.18f,
        )

        Column {
            Text(
                stringResource(R.string.materials_progress_title),
                style = EijyoTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MacaronPalette.InkMuted,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "$readyCount / $totalCount",
                    style = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold),
                    color = MacaronPalette.Ink,
                )
                Spacer(Modifier.weight(1f))
                if (needsUpdateCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF0B8))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            stringResource(R.string.materials_needs_update_badge, needsUpdateCount),
                            style = EijyoTheme.typography.labelMedium,
                            color = MacaronPalette.LemonAccent,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            // Progress track + fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(220f / 353f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFF0E3D7)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(5.dp))
                        .background(MacaronPalette.Mint),
                )
            }
        }
    }
}

// ── Filter chips row ───────────────────────────────────────────────────────────

@Composable
private fun FilterChipsRow(
    selected: DocumentFilter,
    onSelect: (DocumentFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DocumentFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(17.dp))
                    .background(if (isSelected) MacaronPalette.Mint else MacaronPalette.CreamSoft)
                    .then(
                        if (!isSelected) Modifier.border(
                            width = 1.dp,
                            color = Color(0xFFEADDD1),
                            shape = RoundedCornerShape(17.dp),
                        ) else Modifier
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(filter.labelRes),
                    style = EijyoTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else MacaronPalette.InkMuted,
                )
            }
        }
    }
}

// ── Section card ───────────────────────────────────────────────────────────────

@Composable
private fun DocumentSectionCard(
    section: DocumentSection,
    selectedItemId: String?,
    pendingStatus: DocumentStatus?,
    onItemClick: (DocumentItem) -> Unit,
    onStatusSelect: (DocumentStatus) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = MacaronPalette.Shadow,
                spotColor = MacaronPalette.Shadow,
            )
            .clip(RoundedCornerShape(26.dp))
            .background(MacaronPalette.CreamSoft),
    ) {
        // Section header
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 14.dp, top = 18.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(section.category.labelRes),
                style = EijyoTheme.typography.titleMedium,
                color = MacaronPalette.Ink,
                modifier = Modifier.weight(1f),
            )
            // Count pill (sky blue)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MacaronPalette.SkySoft)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.materials_section_count, section.items.size),
                    style = EijyoTheme.typography.labelMedium,
                    color = MacaronPalette.SkyAccent,
                )
            }
        }

        section.items.forEach { item ->
            val isSelected = item.id == selectedItemId
            if (isSelected) {
                ExpandedDocumentRow(
                    item = item,
                    modifier = Modifier.padding(horizontal = 14.dp),
                )
                Spacer(Modifier.height(6.dp))
                AnimatedVisibility(
                    visible = true,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    InlineStatusSelector(
                        pendingStatus = pendingStatus ?: item.status,
                        onStatusSelect = onStatusSelect,
                        onConfirm = onConfirm,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )
                }
                Spacer(Modifier.height(10.dp))
            } else {
                CompactDocumentRow(
                    item = item,
                    onClick = { onItemClick(item) },
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 6.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

// ── Document rows ──────────────────────────────────────────────────────────────

@Composable
private fun CompactDocumentRow(
    item: DocumentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = statusColors(item.status)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MacaronPalette.CreamCard)
            .border(1.dp, Color(0xFFF1E6DA), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status dot
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(colors.dotBg),
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                style = EijyoTheme.typography.labelMedium,
                color = MacaronPalette.Ink,
                maxLines = 1,
            )
            if (item.description.isNotBlank()) {
                Text(
                    item.description,
                    style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                    color = MacaronPalette.InkMuted,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        // Status pill
        StatusPill(status = item.status, colors = colors, height = 22.dp)
    }
}

@Composable
private fun ExpandedDocumentRow(
    item: DocumentItem,
    modifier: Modifier = Modifier,
) {
    val colors = statusColors(item.status)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MacaronPalette.MintWash)
            .border(1.4.dp, MacaronPalette.Mint, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Green dot with checkmark
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MacaronPalette.Mint),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "✓",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                style = EijyoTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MacaronPalette.Ink,
                maxLines = 1,
            )
            Text(
                stringResource(R.string.materials_item_selected),
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                color = MacaronPalette.InkMuted,
            )
        }
        Spacer(Modifier.width(8.dp))
        // Status pill (24dp height for expanded)
        StatusPill(status = item.status, colors = colors, height = 24.dp)
    }
}

// ── Inline status selector ─────────────────────────────────────────────────────

@Composable
private fun InlineStatusSelector(
    pendingStatus: DocumentStatus,
    onStatusSelect: (DocumentStatus) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MacaronPalette.CreamCard)
            .border(1.dp, Color(0xFFF1E6DA), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column {
            // Title + hint
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.materials_update_status_title),
                    style = EijyoTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MacaronPalette.Ink,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.materials_update_status_hint),
                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                    color = MacaronPalette.InkMuted,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 2×2 status option grid
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusOptionChip(
                            status = DocumentStatus.NOT_STARTED,
                            selected = pendingStatus == DocumentStatus.NOT_STARTED,
                            onClick = { onStatusSelect(DocumentStatus.NOT_STARTED) },
                        )
                        StatusOptionChip(
                            status = DocumentStatus.PREPARED,
                            selected = pendingStatus == DocumentStatus.PREPARED,
                            onClick = { onStatusSelect(DocumentStatus.PREPARED) },
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusOptionChip(
                            status = DocumentStatus.SUBMITTED,
                            selected = pendingStatus == DocumentStatus.SUBMITTED,
                            onClick = { onStatusSelect(DocumentStatus.SUBMITTED) },
                        )
                        StatusOptionChip(
                            status = DocumentStatus.NEEDS_UPDATE,
                            selected = pendingStatus == DocumentStatus.NEEDS_UPDATE,
                            onClick = { onStatusSelect(DocumentStatus.NEEDS_UPDATE) },
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                // Confirm button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(19.dp))
                        .background(MacaronPalette.Mint)
                        .clickable(onClick = onConfirm)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.materials_confirm_selection),
                        style = EijyoTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusOptionChip(
    status: DocumentStatus,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = statusColors(status)
    Box(
        modifier = Modifier
            .width(74.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(colors.chipBg.copy(alpha = if (selected) 1f else 0.72f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(status.displayLabelRes),
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
            color = colors.chipTextColor.copy(alpha = if (selected) 1f else 0.7f),
        )
    }
}

// ── Status pill ────────────────────────────────────────────────────────────────

@Composable
private fun StatusPill(
    status: DocumentStatus,
    colors: StatusColors,
    height: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier = Modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(colors.pillBg)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(status.displayLabelRes),
            style = EijyoTheme.typography.labelMedium,
            color = colors.pillTextColor,
        )
    }
}

// ── Status color helper ────────────────────────────────────────────────────────

private data class StatusColors(
    val dotBg: Color,
    val pillBg: Color,
    val chipBg: Color,
    val pillTextColor: Color,
    val chipTextColor: Color,
)

private fun statusColors(status: DocumentStatus): StatusColors = when (status) {
    DocumentStatus.NOT_STARTED -> StatusColors(
        dotBg = Color(0xFFFFD4C8),
        pillBg = Color(0xFFFFD4C8),
        chipBg = Color(0xFFFFD4C8),
        pillTextColor = MacaronPalette.Coral,
        chipTextColor = MacaronPalette.Coral,
    )
    DocumentStatus.PREPARED -> StatusColors(
        dotBg = MacaronPalette.Mint,
        pillBg = MacaronPalette.MintContainer,
        chipBg = MacaronPalette.Mint,
        pillTextColor = MacaronPalette.Mint,
        chipTextColor = Color.White,
    )
    DocumentStatus.SUBMITTED -> StatusColors(
        dotBg = MacaronPalette.SkySoft,
        pillBg = MacaronPalette.SkySoft,
        chipBg = MacaronPalette.SkySoft,
        pillTextColor = MacaronPalette.SkyAccent,
        chipTextColor = MacaronPalette.SkyAccent,
    )
    DocumentStatus.NEEDS_UPDATE -> StatusColors(
        dotBg = Color(0xFFFFF0B8),
        pillBg = Color(0xFFFFF0B8),
        chipBg = Color(0xFFFFF0B8),
        pillTextColor = MacaronPalette.LemonAccent,
        chipTextColor = MacaronPalette.LemonAccent,
    )
}

// ── Paw print decoration ───────────────────────────────────────────────────────

@Composable
private fun PawPrintDecoration(
    color: Color,
    modifier: Modifier = Modifier,
    opacity: Float = 0.18f,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Main pad
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .size(8.dp, 6.dp)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = opacity)),
        )
        // Toes
        Row(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            repeat(3) {
                Box(
                    Modifier
                        .size(3.5.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = opacity)),
                )
            }
        }
    }
}

// ── Extension ─────────────────────────────────────────────────────────────────

@get:androidx.annotation.StringRes
private val DocumentStatus.displayLabelRes: Int
    get() = when (this) {
        DocumentStatus.NOT_STARTED -> R.string.materials_status_not_started
        DocumentStatus.PREPARED -> R.string.materials_status_prepared
        DocumentStatus.SUBMITTED -> R.string.materials_status_submitted
        DocumentStatus.NEEDS_UPDATE -> R.string.materials_status_needs_update
    }
