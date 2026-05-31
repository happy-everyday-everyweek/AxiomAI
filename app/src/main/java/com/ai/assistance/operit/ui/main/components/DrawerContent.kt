package com.ai.assistance.operit.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.operit.R
import com.ai.assistance.operit.data.preferences.UserPreferencesManager
import com.ai.assistance.operit.ui.common.NavItem
import com.ai.assistance.operit.ui.main.screens.ScreenRouteRegistry
import com.ai.assistance.operit.ui.main.navigation.NavigationEntrySpec
import com.ai.assistance.operit.ui.main.screens.Screen
import com.ai.assistance.operit.ui.theme.liquidGlass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
        navItems: List<NavItem>,
        pluginEntries: List<NavigationEntrySpec>,
        selectedItem: NavItem?,
        selectedRouteId: String,
        isNetworkAvailable: Boolean,
        networkType: String,
        appearance: NavigationDrawerAppearance,
        topContentPadding: Dp? = null,
        scope: CoroutineScope,
        drawerState: androidx.compose.material3.DrawerState,
        onScreenSelected: (Screen) -> Unit,
        onNavigationEntrySelected: (NavigationEntrySpec) -> Unit
) {
        val context = LocalContext.current
        val userPreferencesManager = remember(context) { UserPreferencesManager.getInstance(context) }
        val softwareIdentity by
                userPreferencesManager.softwareIdentity.collectAsState(
                        initial = UserPreferencesManager.SOFTWARE_IDENTITY_OPERIT
                )
        val drawerBrandName =
                if (softwareIdentity == UserPreferencesManager.SOFTWARE_IDENTITY_LINGSHU) {
                        context.getString(R.string.software_identity_option_lingshu)
                } else {
                        context.getString(R.string.software_identity_option_operit)
                }
        val bottomInset =
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val resolvedTopContentPadding =
                topContentPadding ?:
                WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        val chatHistoryItems =
                remember(navItems) {
                        navItems.filter {
                                it == NavItem.AiChat ||
                                        it == NavItem.ChatHistorySettings ||
                                        it == NavItem.MemoryBase ||
                                        it == NavItem.UserPreferencesGuide ||
                                        it == NavItem.UserPreferencesSettings
                        }
                }

        val handleScreenSelection: (Screen) -> Unit = { screen ->
                val shouldCloseBeforeNavigate =
                        drawerState.currentValue == DrawerValue.Open ||
                                drawerState.targetValue == DrawerValue.Open

                scope.launch {
                        if (shouldCloseBeforeNavigate) {
                                drawerState.close()
                        }
                        onScreenSelected(screen)
                }
        }
        val handleNavItemClick: (NavItem) -> Unit = { item ->
                handleScreenSelection(ScreenRouteRegistry.defaultScreenForNavItem(item))
        }
        val handleNavigationEntryClick: (NavigationEntrySpec) -> Unit = { entry ->
                val shouldCloseBeforeNavigate =
                        drawerState.currentValue == DrawerValue.Open ||
                                drawerState.targetValue == DrawerValue.Open
                scope.launch {
                        if (shouldCloseBeforeNavigate) {
                                drawerState.close()
                        }
                        onNavigationEntrySelected(entry)
                }
        }

        Column(
                modifier =
                        Modifier.fillMaxHeight()
                                .padding(
                                        top = resolvedTopContentPadding,
                                        end = 8.dp,
                                        bottom = bottomInset
                                )
        ) {
                SidebarHeader(
                        brandName = drawerBrandName,
                        appearance = appearance,
                        onNewChat = {
                                handleScreenSelection(ScreenRouteRegistry.defaultScreenForNavItem(NavItem.AiChat))
                        }
                )

                Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                ) {
                        SidebarGroup(
                                title = null,
                                appearance = appearance
                        ) {
                                SidebarReservedButton(
                                        icon = NavItem.Workflow.icon,
                                        label = stringResource(id = R.string.sidebar_reserved_workflow),
                                        selected = selectedItem == NavItem.Workflow,
                                        appearance = appearance,
                                        onClick = { handleNavItemClick(NavItem.Workflow) }
                                )
                                SidebarReservedButton(
                                        icon = NavItem.Todo.icon,
                                        label = stringResource(id = R.string.sidebar_reserved_todo),
                                        selected = selectedItem == NavItem.Todo,
                                        appearance = appearance,
                                        onClick = { handleNavItemClick(NavItem.Todo) }
                                )
                                SidebarReservedButton(
                                        icon = NavItem.Schedule.icon,
                                        label = stringResource(id = R.string.sidebar_reserved_schedule),
                                        selected = selectedItem == NavItem.Schedule,
                                        appearance = appearance,
                                        onClick = { handleNavItemClick(NavItem.Schedule) }
                                )
                        }

                        SidebarGroup(
                                title = stringResource(id = R.string.sidebar_chat_history),
                                appearance = appearance
                        ) {
                                chatHistoryItems.forEach { item ->
                                        CompactNavigationDrawerItem(
                                                icon = item.icon,
                                                label = stringResource(id = item.titleResId),
                                                selected = selectedItem == item,
                                                appearance = appearance,
                                                onClick = { handleNavItemClick(item) }
                                        )
                                }
                                if (pluginEntries.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        pluginEntries.forEach { entry ->
                                                CompactNavigationDrawerItem(
                                                        icon = entry.icon,
                                                        label = entry.title,
                                                        selected = selectedRouteId == entry.routeId,
                                                        appearance = appearance,
                                                        onClick = { handleNavigationEntryClick(entry) }
                                                )
                                        }
                                }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                }

                SidebarFooter(
                        appearance = appearance,
                        onSettingsClick = { handleNavItemClick(NavItem.Settings) }
                )
        }
}

@Composable
fun CollapsedDrawerContent(
        navItems: List<NavItem>,
        pluginEntries: List<NavigationEntrySpec>,
        selectedItem: NavItem?,
        selectedRouteId: String,
        isNetworkAvailable: Boolean,
        appearance: NavigationDrawerAppearance,
        onScreenSelected: (Screen) -> Unit,
        onNavigationEntrySelected: (NavigationEntrySpec) -> Unit
) {
        Column(
                modifier =
                        Modifier.fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                        modifier =
                                Modifier.size(44.dp)
                                        .liquidGlass(
                                                enabled = appearance.buttonLiquidGlassEnabled,
                                                shape = CircleShape,
                                                containerColor = appearance.buttonContainerColor,
                                                shadowElevation = 5.dp,
                                                borderWidth = 0.5.dp,
                                                blurRadius = 14.dp,
                                                overlayAlphaBoost = 0.05f,
                                                enableLens = false
                                        )
                                        .clip(CircleShape),
                        color = Color.Transparent,
                        shape = CircleShape
                ) {
                        IconButton(
                                onClick = {
                                        onScreenSelected(
                                                ScreenRouteRegistry.defaultScreenForNavItem(NavItem.AiChat)
                                        )
                                }
                        ) {
                                Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(id = R.string.sidebar_new_chat),
                                        tint = appearance.selectedContentColor,
                                        modifier = Modifier.size(24.dp)
                                )
                        }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(0.6f),
                        color = appearance.dividerColor
                )
                Spacer(modifier = Modifier.height(16.dp))

                val collapsedNavItems = remember(navItems) {
                        navItems.filter {
                                it == NavItem.AiChat ||
                                        it == NavItem.ChatHistorySettings ||
                                        it == NavItem.MemoryBase ||
                                        it == NavItem.Workflow ||
                                        it == NavItem.Todo ||
                                        it == NavItem.Schedule
                        }
                }

                for (item in collapsedNavItems) {
                        CollapsedSidebarItem(
                                item = item,
                                selectedItem = selectedItem,
                                appearance = appearance,
                                onClick = {
                                        onScreenSelected(
                                                ScreenRouteRegistry.defaultScreenForNavItem(item)
                                        )
                                }
                        )
                }

                if (pluginEntries.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(0.45f),
                                color = appearance.dividerColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        pluginEntries.forEach { entry ->
                                val selectedGlassOverlayColor =
                                        if (selectedRouteId == entry.routeId) {
                                                appearance.selectedContainerColor.copy(alpha = 0.18f)
                                        } else {
                                                Color.Transparent
                                        }
                                Surface(
                                        modifier =
                                                Modifier.padding(vertical = 8.dp)
                                                        .size(44.dp)
                                                        .liquidGlass(
                                                                enabled = appearance.buttonLiquidGlassEnabled,
                                                                shape = CircleShape,
                                                                containerColor = appearance.buttonContainerColor,
                                                                shadowElevation =
                                                                        if (selectedRouteId == entry.routeId) 6.dp else 5.dp,
                                                                borderWidth = 0.5.dp,
                                                                blurRadius = 14.dp,
                                                                overlayAlphaBoost = 0.05f,
                                                                enableLens = false
                                                        )
                                                        .clip(CircleShape)
                                                        .background(selectedGlassOverlayColor),
                                        color = Color.Transparent,
                                        shape = CircleShape
                                ) {
                                        IconButton(onClick = { onNavigationEntrySelected(entry) }) {
                                                Icon(
                                                        imageVector = entry.icon,
                                                        contentDescription = entry.title,
                                                        tint =
                                                                if (selectedRouteId == entry.routeId) {
                                                                        if (appearance.buttonLiquidGlassEnabled) {
                                                                                appearance.selectedContentColor
                                                                        } else {
                                                                                appearance.titleColor
                                                                        }
                                                                } else {
                                                                        appearance.itemColor
                                                                },
                                                        modifier = Modifier.size(24.dp)
                                                )
                                        }
                                }
                        }
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                        modifier =
                                Modifier.size(44.dp)
                                        .liquidGlass(
                                                enabled = appearance.buttonLiquidGlassEnabled,
                                                shape = CircleShape,
                                                containerColor = appearance.buttonContainerColor,
                                                shadowElevation = 5.dp,
                                                borderWidth = 0.5.dp,
                                                blurRadius = 14.dp,
                                                overlayAlphaBoost = 0.05f,
                                                enableLens = false
                                        )
                                        .clip(CircleShape),
                        color = Color.Transparent,
                        shape = CircleShape
                ) {
                        IconButton(
                                onClick = {
                                        onScreenSelected(
                                                ScreenRouteRegistry.defaultScreenForNavItem(NavItem.Settings)
                                        )
                                }
                        ) {
                                Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = stringResource(id = R.string.sidebar_settings),
                                        tint =
                                                if (selectedItem == NavItem.Settings) appearance.selectedContentColor
                                                else appearance.itemColor,
                                        modifier = Modifier.size(24.dp)
                                )
                        }
                }

                Spacer(modifier = Modifier.height(16.dp))
        }
}

@Composable
private fun SidebarHeader(
        brandName: String,
        appearance: NavigationDrawerAppearance,
        onNewChat: () -> Unit
) {
        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
                        text = brandName,
                        style = MaterialTheme.typography.titleLarge.copy(
                                letterSpacing = 0.5.sp
                        ),
                        color = appearance.titleColor,
                        fontWeight = FontWeight.Bold
                )

                val buttonShape = RoundedCornerShape(10.dp)
                Surface(
                        modifier =
                                Modifier.size(36.dp)
                                        .liquidGlass(
                                                enabled = appearance.buttonLiquidGlassEnabled,
                                                shape = buttonShape,
                                                containerColor = appearance.buttonContainerColor,
                                                shadowElevation = 3.dp,
                                                borderWidth = 0.5.dp,
                                                blurRadius = 10.dp,
                                                overlayAlphaBoost = 0.04f,
                                                enableLens = false
                                        )
                                        .clip(buttonShape),
                        color =
                                if (appearance.buttonLiquidGlassEnabled) Color.Transparent
                                else appearance.selectedContainerColor.copy(alpha = 0.3f),
                        shape = buttonShape
                ) {
                        IconButton(
                                onClick = onNewChat,
                                modifier = Modifier.size(36.dp)
                        ) {
                                Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(id = R.string.sidebar_new_chat),
                                        tint = appearance.selectedContentColor,
                                        modifier = Modifier.size(20.dp)
                                )
                        }
                }
        }
}

@Composable
private fun SidebarGroup(
        title: String?,
        appearance: NavigationDrawerAppearance,
        content: @Composable () -> Unit
) {
        if (title != null) {
                Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = appearance.titleColor.copy(alpha = 0.82f),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 28.dp, end = 20.dp, top = 8.dp, bottom = 2.dp)
                )
        }
        content()
}

@Composable
private fun SidebarReservedButton(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        label: String,
        selected: Boolean,
        appearance: NavigationDrawerAppearance,
        onClick: () -> Unit
) {
        CompactNavigationDrawerItem(
                icon = icon,
                label = label,
                selected = selected,
                appearance = appearance,
                onClick = onClick
        )
}

@Composable
private fun SidebarFooter(
        appearance: NavigationDrawerAppearance,
        onSettingsClick: () -> Unit
) {
        HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                thickness = 0.5.dp,
                color = appearance.dividerColor.copy(alpha = 0.5f)
        )

        val itemShape = MaterialTheme.shapes.small
        Surface(
                modifier =
                        Modifier.fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .height(40.dp)
                                .liquidGlass(
                                        enabled = appearance.buttonLiquidGlassEnabled,
                                        shape = itemShape,
                                        containerColor = appearance.buttonContainerColor,
                                        shadowElevation = 4.dp,
                                        borderWidth = 0.5.dp,
                                        blurRadius = 12.dp,
                                        overlayAlphaBoost = 0.04f,
                                        enableLens = false
                                )
                                .clip(itemShape),
                onClick = onSettingsClick,
                color =
                        if (appearance.buttonLiquidGlassEnabled) {
                                Color.Transparent
                        } else {
                                Color.Transparent
                        },
                shape = itemShape
        ) {
                Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = appearance.itemColor,
                                modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                                text = stringResource(id = R.string.sidebar_settings),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal,
                                color = appearance.itemColor
                        )
                }
        }
}

@Composable
private fun CollapsedSidebarItem(
        item: NavItem,
        selectedItem: NavItem?,
        appearance: NavigationDrawerAppearance,
        onClick: () -> Unit
) {
        val selectedGlassOverlayColor =
                if (selectedItem == item) {
                        appearance.selectedContainerColor.copy(alpha = 0.18f)
                } else {
                        Color.Transparent
                }
        Surface(
                modifier =
                        Modifier.padding(vertical = 8.dp)
                                .size(44.dp)
                                .liquidGlass(
                                        enabled = appearance.buttonLiquidGlassEnabled,
                                        shape = CircleShape,
                                        containerColor = appearance.buttonContainerColor,
                                        shadowElevation =
                                                if (selectedItem == item) 6.dp else 5.dp,
                                        borderWidth = 0.5.dp,
                                        blurRadius = 14.dp,
                                        overlayAlphaBoost = 0.05f,
                                        enableLens = false
                                )
                                .clip(CircleShape)
                                .background(selectedGlassOverlayColor),
                color = Color.Transparent,
                shape = CircleShape
        ) {
                IconButton(onClick = onClick) {
                        Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(id = item.titleResId),
                                tint =
                                        if (selectedItem == item) {
                                                if (appearance.buttonLiquidGlassEnabled) {
                                                        appearance.selectedContentColor
                                                } else {
                                                        appearance.titleColor
                                                }
                                        } else {
                                                appearance.itemColor
                                        },
                                modifier = Modifier.size(24.dp)
                        )
                }
        }
}
