# P2 主题系统与侧边栏重构 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现明快/温暖双主题系统，并按 shadcn/ui Sidebar 模式重构侧边栏结构，移除冗余区域、精简导航层级。
**Architecture:** 主题系统通过在 `Color.kt` 中定义两套完整色彩方案（明快与温暖），在 `UserPreferencesManager` 中新增 `themeStyle` 偏好字段持久化选择，`Theme.kt` 的 `OperitTheme` 根据该字段切换 `ColorScheme`。侧边栏重构将现有 `DrawerContent` 拆分为 `AppSidebar` / `SidebarHeader` / `SidebarContent` / `SidebarGroup` / `SidebarFooter` / `SidebarRail` 六个组件，移除快捷操作卡片、AI功能分组、底部快捷行，将工具箱入口移入设置页面，并支持默认 16rem 宽度与图标折叠模式。
**Tech Stack:** Kotlin, Jetpack Compose, Material 3

---

## Task 1: 主题系统 - 明快/温暖双主题（PLAN.md 8.1）

### 1.1 定义双主题色彩方案

- [ ] **`ui/theme/Color.kt`** - 新增温暖主题色彩常量

  在现有 `Purple40`/`Purple80` 等明快主题色之后，新增温暖主题色彩定义：

  ```
  // 明快主题（Bright）- 黑白灰配色，强调色仅用于点缀
  val BrightPrimary = Color(0xFF1C1B1F)           // 近黑
  val BrightOnPrimary = Color(0xFFFFFFFF)          // 白
  val BrightPrimaryContainer = Color(0xFFE6E1E5)   // 浅灰
  val BrightOnPrimaryContainer = Color(0xFF1C1B1F)
  val BrightSecondary = Color(0xFF49454F)
  val BrightOnSecondary = Color(0xFFFFFFFF)
  val BrightSecondaryContainer = Color(0xFFE8DEF8)
  val BrightOnSecondaryContainer = Color(0xFF1D192B)
  val BrightTertiary = Color(0xFF6B5F7B)           // 仅用于强调
  val BrightBackground = Color(0xFFFFFBFE)
  val BrightSurface = Color(0xFFFFFBFE)
  val BrightOnBackground = Color(0xFF1C1B1F)
  val BrightOnSurface = Color(0xFF1C1B1F)
  val BrightSurfaceVariant = Color(0xFFE7E0EC)
  val BrightOnSurfaceVariant = Color(0xFF49454F)
  val BrightOutline = Color(0xFF79747E)
  val BrightOutlineVariant = Color(0xFFCAC4D0)

  // 明快主题暗色变体
  val BrightDarkPrimary = Color(0xFFD0BCFF)
  val BrightDarkOnPrimary = Color(0xFF381E72)
  val BrightDarkPrimaryContainer = Color(0xFF4F378B)
  val BrightDarkOnPrimaryContainer = Color(0xFFEADDFF)
  val BrightDarkSecondary = Color(0xFFCCC2DC)
  val BrightDarkOnSecondary = Color(0xFF332D41)
  val BrightDarkSecondaryContainer = Color(0xFF4A4458)
  val BrightDarkOnSecondaryContainer = Color(0xFFE8DEF8)
  val BrightDarkTertiary = Color(0xFFEFB8C8)
  val BrightDarkBackground = Color(0xFF1C1B1F)
  val BrightDarkSurface = Color(0xFF1C1B1F)
  val BrightDarkOnBackground = Color(0xFFE6E1E5)
  val BrightDarkOnSurface = Color(0xFFE6E1E5)
  val BrightDarkSurfaceVariant = Color(0xFF49454F)
  val BrightDarkOnSurfaceVariant = Color(0xFFCAC4D0)
  val BrightDarkOutline = Color(0xFF938F99)
  val BrightDarkOutlineVariant = Color(0xFF49454F)

  // 温暖主题（Warm）- 暖黄色、棕色等暖色调
  val WarmPrimary = Color(0xFF8B5E34)              // 暖棕
  val WarmOnPrimary = Color(0xFFFFFFFF)
  val WarmPrimaryContainer = Color(0xFFFFDDB3)     // 浅暖黄
  val WarmOnPrimaryContainer = Color(0xFF2E1500)
  val WarmSecondary = Color(0xFF725A42)            // 中棕
  val WarmOnSecondary = Color(0xFFFFFFFF)
  val WarmSecondaryContainer = Color(0xFFFDE0C7)   // 浅杏色
  val WarmOnSecondaryContainer = Color(0xFF291806)
  val WarmTertiary = Color(0xFF5E6135)             // 橄榄绿（暖调点缀）
  val WarmOnTertiary = Color(0xFFFFFFFF)
  val WarmTertiaryContainer = Color(0xFFE4E6AE)
  val WarmOnTertiaryContainer = Color(0xFF1B1D00)
  val WarmBackground = Color(0xFFFFFBFF)
  val WarmSurface = Color(0xFFFFFBFF)
  val WarmOnBackground = Color(0xFF201A13)
  val WarmOnSurface = Color(0xFF201A13)
  val WarmSurfaceVariant = Color(0xFFF2DFC8)
  val WarmOnSurfaceVariant = Color(0xFF51443A)
  val WarmOutline = Color(0xFF837468)
  val WarmOutlineVariant = Color(0xFFD5C3B0)

  // 温暖主题暗色变体
  val WarmDarkPrimary = Color(0xFFFFB870)          // 暖橙
  val WarmDarkOnPrimary = Color(0xFF4A2800)
  val WarmDarkPrimaryContainer = Color(0xFF6C3E16)
  val WarmDarkOnPrimaryContainer = Color(0xFFFFDDB3)
  val WarmDarkSecondary = Color(0xFFE0C4A5)
  val WarmDarkOnSecondary = Color(0xFF402D18)
  val WarmDarkSecondaryContainer = Color(0xFF59432C)
  val WarmDarkOnSecondaryContainer = Color(0xFFFDE0C7)
  val WarmDarkTertiary = Color(0xFFC8CA94)
  val WarmDarkOnTertiary = Color(0xFF31330B)
  val WarmDarkTertiaryContainer = Color(0xFF474A20)
  val WarmDarkOnTertiaryContainer = Color(0xFFE4E6AE)
  val WarmDarkBackground = Color(0xFF18120C)
  val WarmDarkSurface = Color(0xFF18120C)
  val WarmDarkOnBackground = Color(0xFFEDE0D5)
  val WarmDarkOnSurface = Color(0xFFEDE0D5)
  val WarmDarkSurfaceVariant = Color(0xFF51443A)
  val WarmDarkOnSurfaceVariant = Color(0xFFD5C3B0)
  val WarmDarkOutline = Color(0xFF9E8E82)
  val WarmDarkOutlineVariant = Color(0xFF51443A)
  ```

### 1.2 构建完整 ColorScheme 对象

- [ ] **`ui/theme/Theme.kt`** - 新增明快/温暖主题的 `lightColorScheme()` 和 `darkColorScheme()` 定义

  在现有 `LightColorScheme` / `DarkColorScheme` 之后新增：

  ```kotlin
  private val BrightLightColorScheme = lightColorScheme(
      primary = BrightPrimary,
      onPrimary = BrightOnPrimary,
      primaryContainer = BrightPrimaryContainer,
      onPrimaryContainer = BrightOnPrimaryContainer,
      secondary = BrightSecondary,
      onSecondary = BrightOnSecondary,
      secondaryContainer = BrightSecondaryContainer,
      onSecondaryContainer = BrightOnSecondaryContainer,
      tertiary = BrightTertiary,
      background = BrightBackground,
      surface = BrightSurface,
      onBackground = BrightOnBackground,
      onSurface = BrightOnSurface,
      surfaceVariant = BrightSurfaceVariant,
      onSurfaceVariant = BrightOnSurfaceVariant,
      outline = BrightOutline,
      outlineVariant = BrightOutlineVariant,
  )

  private val BrightDarkColorScheme = darkColorScheme(
      primary = BrightDarkPrimary,
      onPrimary = BrightDarkOnPrimary,
      primaryContainer = BrightDarkPrimaryContainer,
      onPrimaryContainer = BrightDarkOnPrimaryContainer,
      secondary = BrightDarkSecondary,
      onSecondary = BrightDarkOnSecondary,
      secondaryContainer = BrightDarkSecondaryContainer,
      onSecondaryContainer = BrightDarkOnSecondaryContainer,
      tertiary = BrightDarkTertiary,
      background = BrightDarkBackground,
      surface = BrightDarkSurface,
      onBackground = BrightDarkOnBackground,
      onSurface = BrightDarkOnSurface,
      surfaceVariant = BrightDarkSurfaceVariant,
      onSurfaceVariant = BrightDarkOnSurfaceVariant,
      outline = BrightDarkOutline,
      outlineVariant = BrightDarkOutlineVariant,
  )

  private val WarmLightColorScheme = lightColorScheme(
      primary = WarmPrimary,
      onPrimary = WarmOnPrimary,
      primaryContainer = WarmPrimaryContainer,
      onPrimaryContainer = WarmOnPrimaryContainer,
      secondary = WarmSecondary,
      onSecondary = WarmOnSecondary,
      secondaryContainer = WarmSecondaryContainer,
      onSecondaryContainer = WarmOnSecondaryContainer,
      tertiary = WarmTertiary,
      onTertiary = WarmOnTertiary,
      tertiaryContainer = WarmTertiaryContainer,
      onTertiaryContainer = WarmOnTertiaryContainer,
      background = WarmBackground,
      surface = WarmSurface,
      onBackground = WarmOnBackground,
      onSurface = WarmOnSurface,
      surfaceVariant = WarmSurfaceVariant,
      onSurfaceVariant = WarmOnSurfaceVariant,
      outline = WarmOutline,
      outlineVariant = WarmOutlineVariant,
  )

  private val WarmDarkColorScheme = darkColorScheme(
      primary = WarmDarkPrimary,
      onPrimary = WarmDarkOnPrimary,
      primaryContainer = WarmDarkPrimaryContainer,
      onPrimaryContainer = WarmDarkOnPrimaryContainer,
      secondary = WarmDarkSecondary,
      onSecondary = WarmDarkOnSecondary,
      secondaryContainer = WarmDarkSecondaryContainer,
      onSecondaryContainer = WarmDarkOnSecondaryContainer,
      tertiary = WarmDarkTertiary,
      onTertiary = WarmDarkOnTertiary,
      tertiaryContainer = WarmDarkTertiaryContainer,
      onTertiaryContainer = WarmDarkOnTertiaryContainer,
      background = WarmDarkBackground,
      surface = WarmDarkSurface,
      onBackground = WarmDarkOnBackground,
      onSurface = WarmDarkOnSurface,
      surfaceVariant = WarmDarkSurfaceVariant,
      onSurfaceVariant = WarmDarkOnSurfaceVariant,
      outline = WarmDarkOutline,
      outlineVariant = WarmDarkOutlineVariant,
  )
  ```

### 1.3 新增主题风格偏好持久化

- [ ] **`data/preferences/UserPreferencesManager.kt`** - 新增 `themeStyle` 偏好字段

  在 companion object 中新增：
  ```kotlin
  private val THEME_STYLE = stringPreferencesKey("theme_style")

  const val THEME_STYLE_BRIGHT = "bright"
  const val THEME_STYLE_WARM = "warm"
  ```

  新增 Flow 读取：
  ```kotlin
  val themeStyle: Flow<String> =
      context.userPreferencesDataStore.data.map { preferences ->
          preferences[THEME_STYLE] ?: THEME_STYLE_BRIGHT
      }
  ```

  在 `saveThemeSettings()` 方法签名中新增参数：
  ```kotlin
  themeStyle: String? = null,
  ```

  在 `saveThemeSettings()` 的 edit lambda 中新增：
  ```kotlin
  themeStyle?.let { preferences[THEME_STYLE] = it }
  ```

  在 `resetThemeSettings()` 中新增：
  ```kotlin
  preferences.remove(THEME_STYLE)
  ```

  在导出/导入逻辑中同步处理 `THEME_STYLE` 字段。

### 1.4 在 OperitTheme 中集成主题风格切换

- [ ] **`ui/theme/Theme.kt`** - 修改 `OperitTheme` composable

  在 `OperitTheme` 函数顶部新增读取 `themeStyle`：
  ```kotlin
  val themeStyle by preferencesManager.themeStyle.collectAsState(
      initial = UserPreferencesManager.THEME_STYLE_BRIGHT
  )
  ```

  修改 `colorScheme` 的确定逻辑，在 `dynamicColor` 判断之前插入主题风格判断：
  ```kotlin
  var colorScheme = when {
      useCustomColors && customPrimaryColor != null -> {
          val primary = Color(customPrimaryColor!!)
          val secondary = customSecondaryColor?.let { Color(it) } ?: colorScheme.secondary
          if (darkTheme) generateDarkColorScheme(primary, secondary, onColorMode)
          else generateLightColorScheme(primary, secondary, onColorMode)
      }
      themeStyle == UserPreferencesManager.THEME_STYLE_WARM -> {
          if (darkTheme) WarmDarkColorScheme else WarmLightColorScheme
      }
      else -> {
          if (darkTheme) BrightDarkColorScheme else BrightLightColorScheme
      }
  }
  ```

  注意：当 `useCustomColors` 为 true 时，自定义颜色优先级高于主题风格；当 `dynamicColor` 可用且用户未选择自定义颜色或特定主题风格时，仍可使用动态取色。具体策略为：自定义颜色 > 主题风格 > 动态取色 > 默认明快主题。

### 1.5 在主题设置页面添加主题风格选择器

- [ ] **`ui/features/settings/screens/ThemeSettingsScreen.kt`** - 新增主题风格选择区域

  在主题模式（明/暗/跟随系统）选择区域之后，新增主题风格选择：
  ```kotlin
  val themeStyle by preferencesManager.themeStyle.collectAsState(
      initial = UserPreferencesManager.THEME_STYLE_BRIGHT
  )

  // 主题风格选择 - 在 themeMode 选择器之后添加
  Text(
      text = stringResource(id = R.string.theme_style_label),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold
  )
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      FilterChip(
          selected = themeStyle == UserPreferencesManager.THEME_STYLE_BRIGHT,
          onClick = {
              scope.launch {
                  preferencesManager.saveThemeSettings(
                      themeStyle = UserPreferencesManager.THEME_STYLE_BRIGHT
                  )
              }
          },
          label = { Text(stringResource(id = R.string.theme_style_bright)) }
      )
      FilterChip(
          selected = themeStyle == UserPreferencesManager.THEME_STYLE_WARM,
          onClick = {
              scope.launch {
                  preferencesManager.saveThemeSettings(
                      themeStyle = UserPreferencesManager.THEME_STYLE_WARM
                  )
              }
          },
          label = { Text(stringResource(id = R.string.theme_style_warm)) }
      )
  }
  ```

- [ ] **`app/src/main/res/values/strings.xml`** - 新增字符串资源

  ```xml
  <string name="theme_style_label">主题风格</string>
  <string name="theme_style_bright">明快</string>
  <string name="theme_style_warm">温暖</string>
  ```

### 1.6 编译验证

- [ ] 执行 `./gradlew assembleDebug` 确认无编译错误
- [ ] 在设置页面切换明快/温暖主题，确认亮色和暗色模式下色彩方案均正确应用
- [ ] 确认自定义颜色优先级高于主题风格选择

---

## Task 2: 侧边栏重构（PLAN.md 8.2）

### 2.1 新建侧边栏组件文件

- [ ] **`ui/main/components/sidebar/SidebarHeader.kt`** - 新建

  ```kotlin
  @Composable
  fun SidebarHeader(
      onNewChat: () -> Unit,
      appearance: NavigationDrawerAppearance,
      modifier: Modifier = Modifier
  )
  ```

  包含：
  - 品牌名称文字（复用现有 `SidebarInfoCard` 中的品牌名逻辑）
  - "新建对话" 按钮（`FilledTonalButton`，图标 `Icons.Default.Add`，文字 "新对话"）
  - 网络状态指示器（复用现有 `SidebarInfoCard` 中的网络状态逻辑，简化为小圆点+文字）

- [ ] **`ui/main/components/sidebar/SidebarGroup.kt`** - 新建

  ```kotlin
  @Composable
  fun SidebarGroup(
      title: String,
      appearance: NavigationDrawerAppearance,
      modifier: Modifier = Modifier,
      content: @Composable ColumnScope.() -> Unit
  )
  ```

  包含：
  - 可选的分组标题（`Text` + `titleSmall` 样式）
  - `Column` 容器承载子项

- [ ] **`ui/main/components/sidebar/SidebarContent.kt`** - 新建

  ```kotlin
  @Composable
  fun SidebarContent(
      selectedItem: NavItem?,
      selectedRouteId: String,
      appearance: NavigationDrawerAppearance,
      navItems: List<NavItem>,
      pluginEntries: List<NavigationEntrySpec>,
      onNavItemClick: (NavItem) -> Unit,
      onNavigationEntryClick: (NavigationEntrySpec) -> Unit,
      modifier: Modifier = Modifier
  )
  ```

  包含：
  - `SidebarGroup` - 预留按钮区：工作流按钮 + [预留]日历按钮 + [预留]待办按钮
    - 工作流按钮：使用 `CompactNavigationDrawerItem` 样式，图标 `Icons.Default.AccountTree`
    - 日历按钮：`CompactNavigationDrawerItem` + `enabled = false`，图标 `Icons.Default.CalendarToday`，标签 "日历（预留）"
    - 待办按钮：`CompactNavigationDrawerItem` + `enabled = false`，图标 `Icons.Default.CheckCircle`，标签 "待办（预留）"
  - `SidebarGroup` - 历史对话列表：遍历 `navItems` 渲染 `CompactNavigationDrawerItem`；遍历 `pluginEntries` 渲染插件入口

- [ ] **`ui/main/components/sidebar/SidebarFooter.kt`** - 新建

  ```kotlin
  @Composable
  fun SidebarFooter(
      selectedItem: NavItem?,
      appearance: NavigationDrawerAppearance,
      onNavItemClick: (NavItem) -> Unit,
      modifier: Modifier = Modifier
  )
  ```

  包含：
  - 设置按钮（`NavItem.Settings`），使用 `CompactNavigationDrawerItem` 样式

- [ ] **`ui/main/components/sidebar/SidebarRail.kt`** - 新建

  ```kotlin
  @Composable
  fun SidebarRail(
      isCollapsed: Boolean,
      onToggleCollapse: () -> Unit,
      appearance: NavigationDrawerAppearance,
      modifier: Modifier = Modifier
  )
  ```

  包含：
  - 折叠/展开拖拽手柄（右侧竖条，`Modifier.draggable`）
  - 折叠状态下仅显示图标列表

- [ ] **`ui/main/components/sidebar/AppSidebar.kt`** - 新建

  ```kotlin
  @Composable
  fun AppSidebar(
      navItems: List<NavItem>,
      pluginEntries: List<NavigationEntrySpec>,
      selectedItem: NavItem?,
      selectedRouteId: String,
      isNetworkAvailable: Boolean,
      networkType: String,
      appearance: NavigationDrawerAppearance,
      scope: CoroutineScope,
      drawerState: DrawerState,
      onScreenSelected: (Screen) -> Unit,
      onNavigationEntrySelected: (NavigationEntrySpec) -> Unit,
      modifier: Modifier = Modifier
  )
  ```

  组合结构：
  ```
  Column {
      SidebarHeader(onNewChat = { ... })
      HorizontalDivider()
      SidebarContent(...)    // weight(1f), verticalScroll
      HorizontalDivider()
      SidebarFooter(...)
  }
  SidebarRail(isCollapsed, onToggleCollapse)  // 右侧拖拽条
  ```

  宽度逻辑：
  - 默认展开宽度 `256.dp`（16rem）
  - 折叠为图标模式宽度 `56.dp`
  - 使用 `animateDpAsState` 过渡动画

### 2.2 新增侧边栏折叠状态持久化

- [ ] **`data/preferences/UserPreferencesManager.kt`** - 新增侧边栏折叠偏好

  在 companion object 中新增：
  ```kotlin
  private val SIDEBAR_COLLAPSED = booleanPreferencesKey("sidebar_collapsed")
  ```

  新增 Flow 读取：
  ```kotlin
  val sidebarCollapsed: Flow<Boolean> =
      context.userPreferencesDataStore.data.map { preferences ->
          preferences[SIDEBAR_COLLAPSED] ?: false
      }
  ```

  新增保存方法：
  ```kotlin
  suspend fun saveSidebarCollapsed(collapsed: Boolean) {
      context.userPreferencesDataStore.edit { preferences ->
          preferences[SIDEBAR_COLLAPSED] = collapsed
      }
  }
  ```

### 2.3 移除旧侧边栏中的冗余区域

- [ ] **`ui/main/components/DrawerContent.kt`** - 移除以下内容

  1. 移除 `SidebarQuickActionCard` 组件（包管理、权限、工作流三个卡片）
  2. 移除 `SidebarQuickActionBadge` 组件
  3. 移除 `NewSidebarTopContent` 中的快捷操作卡片行（第474-507行的 `Row` 块）
  4. 移除 AI 功能分组标题和导航项（第511-529行，`nav_group_ai_features` 分组）
  5. 移除 `DrawerBottomShortcutRow` 组件（关于、帮助、设置三个底部按钮）
  6. 移除 `BottomShortcutDrawerItem` 组件
  7. 移除 `fixedBottomItems` 和 `quickActionItems` 集合定义
  8. 移除 `SidebarInfoCard` 组件（品牌名+网络状态卡片，其功能已移入 `SidebarHeader`）

- [ ] **`ui/main/components/DrawerContent.kt`** - 重写 `DrawerContent` composable

  将 `DrawerContent` 的实现替换为调用新的 `AppSidebar`：
  ```kotlin
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
      drawerState: DrawerState,
      onScreenSelected: (Screen) -> Unit,
      onNavigationEntrySelected: (NavigationEntrySpec) -> Unit
  ) {
      AppSidebar(
          navItems = navItems,
          pluginEntries = pluginEntries,
          selectedItem = selectedItem,
          selectedRouteId = selectedRouteId,
          isNetworkAvailable = isNetworkAvailable,
          networkType = networkType,
          appearance = appearance,
          scope = scope,
          drawerState = drawerState,
          onScreenSelected = onScreenSelected,
          onNavigationEntrySelected = onNavigationEntrySelected
      )
  }
  ```

  保持 `DrawerContent` 函数签名不变，以最小化对 `PhoneLayout.kt` 和 `TabletLayout.kt` 的改动。

### 2.4 将工具箱入口移入设置页面

- [ ] **`ui/features/settings/screens/SettingsScreen.kt`**（或设置页面的主入口文件）- 新增工具箱入口

  在设置页面中新增一个导航项：
  ```kotlin
  NavigationDrawerItem(
      icon = { Icon(Icons.Default.Apps, contentDescription = null) },
      label = { Text(stringResource(id = R.string.toolbox)) },
      selected = false,
      onClick = { navController.navigate(NavItem.Toolbox.route) }
  )
  ```

  放置位置：在"通用"设置分组中，或作为独立分组"工具"的首项。

- [ ] **`ui/common/NavItem.kt`** - 确认 `NavItem.Toolbox` 已存在（当前已有 `Toolbox` 定义，无需修改）

### 2.5 更新 PhoneLayout 和 TabletLayout

- [ ] **`ui/main/layout/PhoneLayout.kt`** - 适配新侧边栏

  - `DrawerContent` 的调用签名不变，无需修改调用方式
  - 确认 `ModalNavigationDrawer` 的 `drawerContent` lambda 中 `DrawerContent` 调用参数完整
  - 如果侧边栏宽度需要调整（从当前默认改为 256.dp），修改 `ModalNavigationDrawer` 的 `drawerState` 或 `gesturesEnabled` 配置

- [ ] **`ui/main/layout/TabletLayout.kt`** - 适配折叠模式

  - `CollapsedDrawerContent` 需要适配新的图标折叠模式
  - 在折叠模式下，`SidebarRail` 仅显示图标列表（工作流图标 + 各导航项图标 + 设置图标）
  - 折叠宽度 56.dp

### 2.6 清理移除项的字符串资源

- [ ] **`app/src/main/res/values/strings.xml`** - 清理不再使用的字符串

  以下字符串在移除快捷操作卡片和底部快捷后可能不再使用，需逐一确认并移除：
  - `sidebar_permission_short`（权限快捷卡片标签）
  - `nav_group_ai_features`（AI功能分组标题）
  - 其他仅被已移除组件引用的字符串

### 2.7 清理移除项的 NavItem 引用

- [ ] **`ui/main/components/DrawerContent.kt`** - 确认以下 NavItem 不再在侧边栏直接展示

  - `NavItem.Packages` - 包管理入口移入设置页面或通过其他方式访问
  - `NavItem.ShizukuCommands` - 权限入口移入设置页面
  - `NavItem.About` - 关于入口移入设置页面
  - `NavItem.Help` - 帮助入口移入设置页面
  - `NavItem.AiChat`、`NavItem.MemoryBase`、`NavItem.AssistantConfig` - AI功能分组移除后，这些入口需确认是否仍需在 `SidebarContent` 的历史对话列表分组中展示，或移入设置页面

  注意：`NavItem` 枚举本身不删除，仅调整其在侧边栏中的可见性。路由注册保持不变。

### 2.8 编译验证

- [ ] 执行 `./gradlew assembleDebug` 确认无编译错误
- [ ] 确认侧边栏新结构：Header（品牌名+新建对话+网络状态） -> 预留按钮区（工作流+日历预留+待办预留） -> 历史对话列表 -> Footer（设置）
- [ ] 确认快捷操作卡片、AI功能分组、底部快捷行已移除
- [ ] 确认工具箱入口可在设置页面访问
- [ ] 确认侧边栏默认宽度 16rem（256.dp），折叠模式宽度 56.dp
- [ ] 确认折叠/展开动画流畅

---

## 验证清单

- [ ] `./gradlew assembleDebug` 编译通过
- [ ] 明快主题亮色模式：黑白灰配色，强调色仅用于 tertiary
- [ ] 明快主题暗色模式：暗色背景 + 浅色文字，强调色仅用于 tertiary
- [ ] 温暖主题亮色模式：暖黄/棕色调
- [ ] 温暖主题暗色模式：暗棕/暖橙色调
- [ ] 主题切换即时生效，无需重启应用
- [ ] 自定义颜色优先级高于主题风格
- [ ] 侧边栏结构符合 shadcn/ui Sidebar 规范
- [ ] 预留按钮区正确显示（工作流可点击，日历/待办置灰）
- [ ] 侧边栏宽度默认 256.dp，可折叠为 56.dp 图标模式
- [ ] 工具箱入口在设置页面可正常访问
- [ ] 全局搜索 `SidebarQuickActionCard`、`DrawerBottomShortcutRow`、`SidebarInfoCard` 确认无残留引用
