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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eijyo.tracker.core.ui.component.PawButton
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.data.model.ResultType

private val CoralSoft = Color(0xFFFFD4C8)
private val LemonBadge = Color(0xFFFFF0B8)

enum class EventType(val label: String, val emoji: String) {
    SUPPLEMENT_RECEIVED("收到补资料", "📩"),
    SUPPLEMENT_SUBMITTED("补资料已提交", "✉️"),
    NOTICE_RECEIVED("收到通知书", "📬"),
    APPROVED("许可", "✅"),
    REJECTED("不许可", "❌"),
    WITHDRAWN("撤回", "↩️"),
    CUSTOM_NOTE("自定义备注", "📝"),
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
        containerColor = EijyoTheme.colors.screen,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
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
    Text(
        "添加事件",
        style = EijyoTheme.typography.headlineMedium.copy(fontSize = 20.sp),
        color = colors.ink,
    )
    Text(
        "选择发生了什么",
        style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp),
        color = colors.inkMuted,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
    )
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
                .padding(vertical = 5.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(bg)
                .clickable { onSelect(type) }
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(type.emoji, fontSize = 18.sp)
            Spacer(Modifier.width(14.dp))
            Text(
                type.label,
                style = EijyoTheme.typography.labelLarge.copy(fontSize = 15.sp),
                color = textColor,
            )
        }
    }
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

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.mintWash)
                .clickable(onClick = onBack),
        ) {
            Text("‹", style = EijyoTheme.typography.labelLarge.copy(fontSize = 20.sp), color = colors.mint)
        }
        Spacer(Modifier.width(12.dp))
        Text(type.label, style = EijyoTheme.typography.headlineMedium.copy(fontSize = 20.sp), color = colors.ink)
    }
    Spacer(Modifier.height(20.dp))

    when (type) {
        EventType.SUPPLEMENT_RECEIVED -> {
            FormField("收到日期", "YYYY-MM-DD", field1) { field1 = it }
            Spacer(Modifier.height(12.dp))
            FormField("提交期限（可选）", "YYYY-MM-DD", field2) { field2 = it }
            Spacer(Modifier.height(12.dp))
            FormField("补资料内容", "简要描述", field3) { field3 = it }
        }
        EventType.SUPPLEMENT_SUBMITTED -> {
            FormField("提交日期", "YYYY-MM-DD", field1) { field1 = it }
        }
        EventType.NOTICE_RECEIVED -> {
            FormField("收到日期", "YYYY-MM-DD", field1) { field1 = it }
            Spacer(Modifier.height(12.dp))
            FormField("备注（可选）", "通知内容描述", field2) { field2 = it }
        }
        EventType.APPROVED, EventType.REJECTED, EventType.WITHDRAWN -> {
            FormField("结果日期", "YYYY-MM-DD", field1) { field1 = it }
            Spacer(Modifier.height(12.dp))
            FormField("备注（可选）", "其他备注", field2) { field2 = it }
        }
        EventType.CUSTOM_NOTE -> {
            FormField("日期", "YYYY-MM-DD", field1) { field1 = it }
            Spacer(Modifier.height(12.dp))
            FormField("内容", "记录内容", field2) { field2 = it }
        }
    }

    Spacer(Modifier.height(24.dp))
    PawButton(
        text = "保存",
        modifier = Modifier.fillMaxWidth(),
        onClick = { onSave(type, field1, field2, field3) },
    )
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
