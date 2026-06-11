package com.eijyo.tracker.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.eijyo.tracker.core.ui.theme.EijyoTheme
import com.eijyo.tracker.feature.placeholder.PlaceholderScreen
import com.eijyo.tracker.navigation.MainTab

/**
 * The five-tab app shell (首页 / 申请 / 材料 / 数据 / 我的). Only Home is implemented in
 * this milestone; the other tabs render an honest placeholder so navigation works
 * end-to-end.
 */
@Composable
fun MainScaffold(
    onOpenPredictionDetail: () -> Unit = {},
    onOpenRiskDetail: () -> Unit = {},
) {
    var selected by remember { mutableStateOf(MainTab.HOME) }
    val colors = EijyoTheme.colors

    Scaffold(
        containerColor = colors.screen,
        bottomBar = {
            NavigationBar(containerColor = colors.card) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selected == tab,
                        onClick = { selected = tab },
                        icon = { Icon(iconFor(tab), contentDescription = tab.label) },
                        label = { Text(tab.label, style = EijyoTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.mint,
                            selectedTextColor = colors.mint,
                            indicatorColor = colors.mintContainer,
                            unselectedIconColor = colors.inkMuted,
                            unselectedTextColor = colors.inkMuted,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selected) {
                MainTab.HOME -> HomeScreen(
                    onOpenPredictionDetail = onOpenPredictionDetail,
                    onOpenRiskDetail = onOpenRiskDetail,
                )
                MainTab.APPLICATION -> PlaceholderScreen(title = "申请", subtitle = "申请时间线与状态记录，下一里程碑实现。")
                MainTab.DOCUMENTS -> PlaceholderScreen(title = "材料", subtitle = "材料清单与状态更新，下一里程碑实现。")
                MainTab.DATA -> PlaceholderScreen(title = "数据", subtitle = "官方处理期间与公开数据，下一里程碑实现。")
                MainTab.SETTINGS -> PlaceholderScreen(title = "我的", subtitle = "设置与账号，下一里程碑实现。")
            }
        }
    }
}

private fun iconFor(tab: MainTab): ImageVector = when (tab) {
    MainTab.HOME -> Icons.Filled.Home
    MainTab.APPLICATION -> Icons.Filled.Description
    MainTab.DOCUMENTS -> Icons.Filled.FolderOpen
    MainTab.DATA -> Icons.Filled.BarChart
    MainTab.SETTINGS -> Icons.Filled.Person
}
