package com.eijyo.tracker.feature.application

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eijyo.tracker.R
import com.eijyo.tracker.core.ui.component.DogFace
import com.eijyo.tracker.core.ui.component.PawPrint
import com.eijyo.tracker.core.ui.component.PawButton
import com.eijyo.tracker.core.ui.theme.MacaronPalette
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.data.model.ResultType

private val CoralSoft = Color(0xFFFFD4C8)
private val LemonBadge = Color(0xFFFFF0B8)
private val DragHandleColor = Color(0xFFE7D8CA)

enum class EventType(val label: String, val icon: ImageVector) {
    SUPPLEMENT_RECEIVED("收到补资料", Icons.Filled.Mail),
    SUPPLEMENT_SUBMITTED("补资料已提交", Icons.Filled.Drafts),
    NOTICE_RECEIVED("收到通知书", Icons.Filled.Description),
    APPROVED("许可", Icons.Filled.CheckCircle),
    REJECTED("不许可", Icons.Filled.Undo),
    WITHDRAWN("撤回", Icons.Filled.Undo),
    CUSTOM_NOTE("自定义备注", Icons.Filled.EditNote),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSupplementReceived: (receivedDate: String, deadline: String, description: String) -> Unit,
    onSupplementSubmitted: (submittedDate: String) -> Unit,
    onResult: (ResultType, date: String) -> Unit,
    onNoticeOrNote: () -> Unit,
) {
    var selectedType by remember { mutableStateOf<EventType?>(null) }

    ModalBottomSheet(
        onDismissRequest = {
            selectedType = null
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color(0xFFFFFEFB),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = { EventSheetDragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
        ) {
            if (selectedType == null) {
                TypePickerStep(
                    onSelect = { selectedType = it },
                )
            } else {
                EventFormStep(
                    type = selectedType!!,
                    onBack = { selectedType = null },
                    onSave = { type, d1, d2, d3 ->
                        when (type) {
                            EventType.SUPPLEMENT_RECEIVED -> onSupplementReceived(d1, d2, d3)
                            EventType.SUPPLEMENT_SUBMITTED -> onSupplementSubmitted(d1)
                            EventType.APPROVED -> onResult(ResultType.APPROVED, d1)
                            EventType.REJECTED -> onResult(ResultType.REJECTED, d1)
                            EventType.WITHDRAWN -> onResult(ResultType.WITHDRAWN, d1)
                            EventType.NOTICE_RECEIVED, EventType.CUSTOM_NOTE -> onNoticeOrNote()
                        }
                        selectedType = null
                        onDismiss()
                    },
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TypePickerStep(onSelect: (EventType) -> Unit) {
    val colors = EijyoTheme.colors
    EventSheetHeader(
        title = stringResource(R.string.event_sheet_title),
        subtitle = stringResource(R.string.event_sheet_subtitle),
    )
    Spacer(Modifier.height(10.dp))
    EventType.entries.forEach { type ->
        val (bg, textColor) = when (type) {
            EventType.SUPPLEMENT_RECEIVED, EventType.NOTICE_RECEIVED -> CoralSoft to colors.coral
            EventType.SUPPLEMENT_SUBMITTED, EventType.APPROVED -> colors.mintWash to colors.mint
            EventType.REJECTED, EventType.WITHDRAWN -> colors.peach to colors.coral
            EventType.CUSTOM_NOTE -> LemonBadge to colors.lemonAccent
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(bg)
                .clickable { onSelect(type) }
                .height(54.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(type.icon, contentDescription = type.label, tint = textColor, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                type.label,
                style = EijyoTheme.typography.labelLarge.copy(fontSize = 15.sp),
                color = textColor,
                modifier = Modifier.weight(1f),
            )
            Text("›", style = EijyoTheme.typography.labelLarge.copy(fontSize = 19.sp), color = textColor.copy(alpha = 0.8f))
        }
    }
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun EventFormStep(
    type: EventType,
    onBack: () -> Unit,
    onSave: (EventType, String, String, String) -> Unit,
) {
    val colors = EijyoTheme.colors
    var field1 by remember { mutableStateOf("") }
    var field2 by remember { mutableStateOf("") }
    var field3 by remember { mutableStateOf("") }

    EventSheetHeader(
        title = type.label,
        subtitle = stringResource(R.string.event_sheet_subtitle),
        onBack = onBack,
    )
    Spacer(Modifier.height(20.dp))

    when (type) {
        EventType.SUPPLEMENT_RECEIVED -> {
            FormField(stringResource(R.string.event_field_received_date), stringResource(R.string.event_field_date_format_hint), field1) { field1 = it }
            Spacer(Modifier.height(12.dp))
            FormField(stringResource(R.string.event_field_deadline_optional), stringResource(R.string.event_field_date_format_hint), field2) { field2 = it }
            Spacer(Modifier.height(12.dp))
            FormField(stringResource(R.string.event_field_supplement_desc), stringResource(R.string.event_field_brief_hint), field3) { field3 = it }
        }
        EventType.SUPPLEMENT_SUBMITTED -> {
            FormField(stringResource(R.string.event_field_submit_date), stringResource(R.string.event_field_date_format_hint), field1) { field1 = it }
        }
        EventType.NOTICE_RECEIVED -> {
            FormField(stringResource(R.string.event_field_received_date), stringResource(R.string.event_field_date_format_hint), field1) { field1 = it }
            Spacer(Modifier.height(12.dp))
            FormField(stringResource(R.string.event_field_note_optional), stringResource(R.string.event_field_notice_hint), field2) { field2 = it }
        }
        EventType.APPROVED, EventType.REJECTED, EventType.WITHDRAWN -> {
            FormField(stringResource(R.string.event_field_result_date), stringResource(R.string.event_field_date_format_hint), field1) { field1 = it }
            Spacer(Modifier.height(12.dp))
            FormField(stringResource(R.string.event_field_note_optional), stringResource(R.string.event_field_other_hint), field2) { field2 = it }
        }
        EventType.CUSTOM_NOTE -> {
            FormField(stringResource(R.string.event_field_date), stringResource(R.string.event_field_date_format_hint), field1) { field1 = it }
            Spacer(Modifier.height(12.dp))
            FormField(stringResource(R.string.event_field_content), stringResource(R.string.event_field_record_hint), field2) { field2 = it }
        }
    }

    Spacer(Modifier.height(24.dp))
    PawButton(
        text = stringResource(R.string.event_save),
        modifier = Modifier.fillMaxWidth(),
        onClick = { onSave(type, field1, field2, field3) },
    )
}

@Composable
private fun EventSheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 55.dp, height = 5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DragHandleColor),
        )
    }
}

@Composable
private fun EventSheetHeader(title: String, subtitle: String, onBack: (() -> Unit)? = null) {
    val colors = EijyoTheme.colors
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 28.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.mintWash)
                        .clickable(onClick = onBack),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = title,
                        tint = colors.mint,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
            } else {
                DogFace(size = 32.dp)
                Spacer(Modifier.width(14.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = EijyoTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                    color = colors.ink,
                )
                Text(
                    subtitle,
                    style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = colors.inkMuted,
                )
            }
        }
        PawPrint(
            color = MacaronPalette.DogPaw,
            size = 12.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 8.dp),
        )
    }
}

@Composable
private fun FormField(label: String, hint: String, value: String, onValueChange: (String) -> Unit) {
    val colors = EijyoTheme.colors
    Column {
        Text(label, style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp), color = colors.inkMuted)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(hint, style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp), color = colors.inkMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
        )
    }
}
