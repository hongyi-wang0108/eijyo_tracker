package com.eijyo.tracker.feature.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eijyo.tracker.BuildConfig
import com.eijyo.tracker.core.ui.component.DogFace
import com.eijyo.tracker.core.ui.component.DogProfile
import com.eijyo.tracker.core.ui.component.PawPrint
import com.eijyo.tracker.core.ui.component.WelcomeBackground
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.core.ui.theme.MacaronPalette

private val ShadowTint = Color(0x1A8C5C3D)
private val DragHandleColor = Color(0xFFE7D8CA)
private val DisclaimerBg = Color(0xFFFFF8E6)
private val PrivacyDeleteBg = Color(0xFFFFE3E3)
private val PrivacyDeleteText = Color(0xFFF08A8A)

private enum class SettingsSheet { LANGUAGE, PRIVACY, DISCLAIMER, ABOUT }

/** Unwrap a possibly-wrapped Compose context down to the host Activity. */
private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = EijyoTheme.colors
    val context = LocalContext.current

    var activeSheet by remember { mutableStateOf<SettingsSheet?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            viewModel.exportTo(uri) { ok ->
                Toast.makeText(
                    context,
                    if (ok) "已导出本地备份" else "导出失败，请重试",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.screen)) {
        WelcomeBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Header()
            DogStage()
            Spacer(Modifier.height(16.dp))
            ProfileCard(state)
            Spacer(Modifier.height(16.dp))
            SettingsListCard(onRowClick = { activeSheet = it })
            Spacer(Modifier.height(40.dp))
        }
    }

    activeSheet?.let { sheet ->
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = sheetState,
            containerColor = Color(0xFFFFFEFB),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            dragHandle = { SheetDragHandle() },
        ) {
            when (sheet) {
                SettingsSheet.LANGUAGE -> LanguageSheetContent(
                    currentLanguage = state.currentLanguage,
                    onConfirm = { code ->
                        viewModel.saveLanguage(code)
                        activeSheet = null
                        context.findActivity()?.recreate()
                    },
                )
                SettingsSheet.PRIVACY -> PrivacySheetContent(
                    onExport = {
                        activeSheet = null
                        exportLauncher.launch(viewModel.exportFileName())
                    },
                    onDelete = {
                        activeSheet = null
                        showDeleteConfirm = true
                    },
                )
                SettingsSheet.DISCLAIMER -> DisclaimerSheetContent(onDismiss = { activeSheet = null })
                SettingsSheet.ABOUT -> AboutSheetContent(onDismiss = { activeSheet = null })
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除申请档案？") },
            text = { Text("将清除本机所有申请数据（档案、材料、补资料、预测）。删除后不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteAll { context.findActivity()?.recreate() }
                }) {
                    Text("删除", color = PrivacyDeleteText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            },
        )
    }
}

// ─── Page Sections ────────────────────────────────────────────────────────────

@Composable
private fun Header() {
    val colors = EijyoTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
    ) {
        Text(
            "我的",
            style = EijyoTheme.typography.headlineMedium.copy(fontSize = 28.sp),
            color = colors.ink,
        )
        Text(
            "偏好、隐私和 App 信息",
            style = EijyoTheme.typography.labelMedium.copy(fontSize = 14.sp),
            color = colors.inkMuted,
        )
    }
}

@Composable
private fun DogStage() {
    // Exact positions from design node Q9Zjqe (353×250dp stage).
    // Children use Alignment.TopStart + offset to match design coordinates.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        // Mint blob: 132×132 at (18, 14)
        Box(
            modifier = Modifier
                .offset(18.dp, 14.dp)
                .size(132.dp)
                .clip(CircleShape)
                .background(MacaronPalette.MintContainer),
        )
        // Peach blob: 96×96 at (222, 20)
        Box(
            modifier = Modifier
                .offset(222.dp, 20.dp)
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD4C8)),
        )
        // Sky blob: 160×72 ellipse at (102, 160)
        Box(
            modifier = Modifier
                .offset(102.dp, 160.dp)
                .size(160.dp, 72.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(MacaronPalette.SkySoft),
        )
        // Lemon blob: 72×72 at (238, 146)
        Box(
            modifier = Modifier
                .offset(238.dp, 146.dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(MacaronPalette.LemonSoft),
        )
        // Large profile dog: 186×161 at (82, 34)
        DogProfile(
            modifier = Modifier.offset(82.dp, 34.dp),
            size = 186.dp,
            wagging = true,
        )
    }
}

@Composable
private fun SettingsCardContainer(
    modifier: Modifier = Modifier,
    radius: Dp = 28.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = EijyoTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(radius), spotColor = ShadowTint, ambientColor = ShadowTint)
            .clip(RoundedCornerShape(radius))
            .background(colors.card)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        content = content,
    )
}

@Composable
private fun ProfileCard(state: SettingsUiState) {
    val colors = EijyoTheme.colors
    SettingsCardContainer(radius = 28.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.mintWash),
                contentAlignment = Alignment.Center,
            ) {
                DogFace(size = 36.dp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    state.displayName.ifEmpty { "用户" },
                    style = EijyoTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                    color = colors.ink,
                )
                Spacer(Modifier.height(2.dp))
                val sub = buildString {
                    append("永住申请档案")
                    if (state.statusLabel.isNotEmpty()) append(" · ${state.statusLabel}")
                }
                Text(
                    sub,
                    style = EijyoTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = colors.inkMuted,
                )
            }
            if (state.officeLabel.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(13.dp))
                        .background(colors.skySoft)
                        .height(26.dp)
                        .padding(horizontal = 10.dp),
                ) {
                    Text(
                        state.officeLabel,
                        style = EijyoTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = colors.skyAccent,
                    )
                }
            }
        }
    }
}

private data class SettingsRowData(
    val circleColor: Color,
    val mark: String,
    val markColor: Color,
    val title: String,
    val subtitle: String,
    val sheet: SettingsSheet,
    val enabled: Boolean = true,
)

@Composable
private fun SettingsListCard(onRowClick: (SettingsSheet) -> Unit) {
    val colors = EijyoTheme.colors
    val rows = listOf(
        // 语言设置暂缓：本地化（strings.xml + 日英翻译）是独立工程，先灰掉入口。
        SettingsRowData(MacaronPalette.MintWash, "文", MacaronPalette.Mint, "语言设置", "多语言即将支持", SettingsSheet.LANGUAGE, enabled = false),
        SettingsRowData(MacaronPalette.SkySoft, "锁", MacaronPalette.SkyAccent, "隐私与数据", "导出数据 / 删除档案", SettingsSheet.PRIVACY),
        SettingsRowData(MacaronPalette.LemonSoft, "!", MacaronPalette.LemonAccent, "免责声明", "预测仅供参考", SettingsSheet.DISCLAIMER),
        SettingsRowData(MacaronPalette.LavenderSoft, "i", MacaronPalette.LavenderAccent, "关于 App", "版本、数据来源、联系", SettingsSheet.ABOUT),
    )
    SettingsCardContainer(radius = 28.dp) {
        Text(
            "设置",
            style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp),
            color = colors.ink,
        )
        Spacer(Modifier.height(10.dp))
        rows.forEachIndexed { index, row ->
            if (index > 0) Spacer(Modifier.height(2.dp))
            SettingsListRow(row, onClick = { onRowClick(row.sheet) })
        }
    }
}

@Composable
private fun SettingsListRow(row: SettingsRowData, onClick: () -> Unit) {
    val colors = EijyoTheme.colors
    val rowAlpha = if (row.enabled) 1f else 0.45f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(if (row.enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(row.circleColor)
                .graphicsLayer { alpha = rowAlpha },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                row.mark,
                style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp),
                color = row.markColor,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer { alpha = rowAlpha },
        ) {
            Text(
                row.title,
                style = EijyoTheme.typography.labelLarge.copy(fontSize = 13.sp),
                color = colors.ink,
            )
            Text(
                row.subtitle,
                style = EijyoTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight(200),
                ),
                color = colors.inkMuted,
            )
        }
        if (row.enabled) {
            Text(
                "›",
                style = EijyoTheme.typography.labelLarge.copy(fontSize = 19.sp),
                color = colors.inkMuted,
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.border)
                    .height(20.dp)
                    .padding(horizontal = 8.dp),
            ) {
                Text(
                    "即将支持",
                    style = EijyoTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = colors.inkMuted,
                )
            }
        }
    }
}

// ─── Shared Sheet Components ───────────────────────────────────────────────────

@Composable
private fun SheetDragHandle() {
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
private fun SheetHeader(title: String, subtitle: String) {
    val colors = EijyoTheme.colors
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 52.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DogFace(size = 32.dp)
            Spacer(Modifier.width(14.dp))
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
                .padding(end = 31.dp, top = 8.dp)
                .graphicsLayer { alpha = 0.13f; rotationZ = 12f },
        )
    }
}

@Composable
private fun SheetConfirmButton(label: String, onClick: () -> Unit) {
    val colors = EijyoTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.mint)
            .height(48.dp)
            .clickable(onClick = onClick),
    ) {
        Text(
            label,
            style = EijyoTheme.typography.labelLarge.copy(fontSize = 16.sp),
            color = Color.White,
        )
    }
}

// ─── Language Sheet ────────────────────────────────────────────────────────────

@Composable
private fun LanguageSheetContent(currentLanguage: String, onConfirm: (String) -> Unit) {
    val langs = listOf("中文" to "zh", "日本語" to "ja", "English" to "en")
    var selected by rememberSaveable { mutableStateOf(currentLanguage) }
    val colors = EijyoTheme.colors

    SheetHeader("语言设置", "选择 App 显示语言")
    Spacer(Modifier.height(10.dp))
    langs.forEach { (label, code) ->
        val isSelected = selected == code
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isSelected) colors.mintWash else colors.card)
                .clickable { selected = code }
                .height(42.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = EijyoTheme.typography.labelLarge.copy(fontSize = 15.sp),
                color = colors.ink,
                modifier = Modifier.weight(1f),
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(colors.mint),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓", style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp), color = Color.White)
                }
            }
        }
    }
    Spacer(Modifier.height(18.dp))
    SheetConfirmButton("确认") { onConfirm(selected) }
    Spacer(Modifier.height(32.dp))
}

// ─── Privacy Sheet ─────────────────────────────────────────────────────────────

@Composable
private fun PrivacySheetContent(onExport: () -> Unit, onDelete: () -> Unit) {
    val colors = EijyoTheme.colors
    SheetHeader("隐私与数据", "管理本机申请档案数据")
    Spacer(Modifier.height(14.dp))
    PrivacyActionRow(
        bg = colors.skySoft,
        title = "导出数据",
        titleColor = colors.ink,
        subtitle = "保存一份本地备份（JSON）",
        chevronColor = colors.inkMuted,
        onClick = onExport,
    )
    Spacer(Modifier.height(8.dp))
    PrivacyActionRow(
        bg = PrivacyDeleteBg,
        title = "删除申请档案",
        titleColor = PrivacyDeleteText,
        subtitle = "删除后不可恢复",
        chevronColor = PrivacyDeleteText,
        onClick = onDelete,
    )
    Spacer(Modifier.height(32.dp))
}

@Composable
private fun PrivacyActionRow(
    bg: Color,
    title: String,
    titleColor: Color,
    subtitle: String,
    chevronColor: Color,
    onClick: () -> Unit,
) {
    val colors = EijyoTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .height(52.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = EijyoTheme.typography.labelLarge.copy(fontSize = 15.sp), color = titleColor)
            Text(subtitle, style = EijyoTheme.typography.labelSmall.copy(fontSize = 11.sp), color = colors.inkMuted)
        }
        Text("›", style = EijyoTheme.typography.labelLarge.copy(fontSize = 20.sp), color = chevronColor)
    }
}

// ─── Disclaimer Sheet ──────────────────────────────────────────────────────────

@Composable
private fun DisclaimerSheetContent(onDismiss: () -> Unit) {
    val colors = EijyoTheme.colors
    SheetHeader("免责声明", "请在使用预测结果前确认")
    Spacer(Modifier.height(14.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(DisclaimerBg)
            .padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Text(
            "预测结果仅供参考，不构成法律意见，也不保证永住许可结果。实际审查会受到申请材料、入管判断和个别情况影响。",
            style = EijyoTheme.typography.labelMedium.copy(fontSize = 14.sp, lineHeight = 22.sp),
            color = colors.ink,
        )
    }
    Spacer(Modifier.height(18.dp))
    SheetConfirmButton("我知道了", onClick = onDismiss)
    Spacer(Modifier.height(32.dp))
}

// ─── About Sheet ───────────────────────────────────────────────────────────────

@Composable
private fun AboutSheetContent(onDismiss: () -> Unit) {
    val colors = EijyoTheme.colors
    val version = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    SheetHeader("关于 App", "版本与数据来源信息")
    Spacer(Modifier.height(14.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MacaronPalette.LavenderSoft)
            .padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AboutRow("版本", version)
            AboutRow("数据来源", "日本出入国在留管理厅官方公开数据")
            AboutRow("预测方法", "基于公开审查期间统计，不代表官方判断")
        }
    }
    Spacer(Modifier.height(18.dp))
    SheetConfirmButton("知道了", onClick = onDismiss)
    Spacer(Modifier.height(32.dp))
}

@Composable
private fun AboutRow(label: String, value: String) {
    val colors = EijyoTheme.colors
    Column {
        Text(
            label,
            style = EijyoTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = colors.inkMuted,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = EijyoTheme.typography.labelMedium.copy(fontSize = 13.sp),
            color = colors.ink,
        )
    }
}
