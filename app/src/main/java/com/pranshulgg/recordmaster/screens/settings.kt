package com.pranshulgg.recordmaster.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.pranshulgg.recordmaster.R
import com.pranshulgg.recordmaster.helpers.PreferencesHelper
import com.pranshulgg.recordmaster.ui.components.ColorPickerSheetTheme
import com.pranshulgg.recordmaster.ui.components.SettingSection
import com.pranshulgg.recordmaster.ui.components.SettingTile
import com.pranshulgg.recordmaster.ui.components.SettingsTileIcon
import com.pranshulgg.recordmaster.ui.components.Symbol
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    navController: NavController,
    context: Context,
    onThemeChanged: (Boolean) -> Unit,
    onSeedChanged: (String) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onExpressiveColorChanged: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showBottomSheet by remember { mutableStateOf(false) }
    val controller = remember { ColorPickerController() }
    val defaultPickerColor = PreferencesHelper.getString("seedColor") ?: "0xff0000FF"
    val initialColorInt = remember(defaultPickerColor) {
        Color(defaultPickerColor.removePrefix("0x").toLong(16).toInt())
    }
    var pickedColor = PreferencesHelper.getString("seedColor") ?: "0xff0000FF"
    var currentTheme by remember {
        mutableStateOf(
            PreferencesHelper.getString("AppTheme") ?: "Light"
        )
    }

    var useCustomColor by remember {
        mutableStateOf(
            PreferencesHelper.getBool("useCustomColor") ?: false
        )
    }

    var useDynamicColor by remember {
        mutableStateOf(
            PreferencesHelper.getBool("useDynamicColors") ?: false
        )
    }

    var useExpressiveColor by remember {
        mutableStateOf(
            PreferencesHelper.getBool("useExpressiveColor") ?: true
        )
    }

    fun hideColorSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
            }
        }
    }

    @Composable
    fun openColorPickerLead() {
        Surface(
            shape = RoundedCornerShape(50.dp),
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(

                modifier = Modifier
                    .width(24.dp)
                    .height(36.dp)
                    .background(
                        color = Color(
                            defaultPickerColor.removePrefix("0x").toLong(16).toInt()
                        )
                    )
                    .clickable(
//                        onClick = { showBottomSheet = true }
                        onClick = {
                            showBottomSheet = true

                        }
                    ),
            ) {
            }
        }
    }



    val isSysDark = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Symbol(
                            R.drawable.arrow_back_24px,
                            desc = "Back",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                SettingSection(
                    title = "App looks",
                    tiles = listOf(
                        SettingTile.DialogOptionTile(
                            leading = { SettingsTileIcon(R.drawable.routine_24px) },
                            title = "App Theme",
                            options = listOf("Light", "Dark", "System"),
                            selectedOption = currentTheme,
                            onOptionSelected = { selectedOption ->
                                currentTheme = selectedOption
                                val isDark = when (selectedOption) {
                                    "Light" -> false
                                    "Dark" -> true
                                    "System" -> isSysDark
                                    else -> false
                                }
                                PreferencesHelper.setBool("dark_theme", isDark)
                                PreferencesHelper.setString("AppTheme", selectedOption)

                                onThemeChanged(isDark)

                            }
                        ),
                        SettingTile.SwitchTile(
                            leading = {
                                if (useCustomColor) openColorPickerLead() else SettingsTileIcon(
                                    R.drawable.colorize_24px
                                )
                            },
                            title = "Use custom color",
                            description = "Select a seed color to generate the theme",
                            checked = useCustomColor,
                            enabled = !useDynamicColor,
                            onCheckedChange = { checked ->
                                PreferencesHelper.setBool("useCustomColor", checked)

                                useCustomColor = checked

                                if (!checked) {
                                    PreferencesHelper.setString("seedColor", "0xff0000FF")
                                    onSeedChanged("0xff0000FF")
                                }

                            }
                        ),
                        SettingTile.SwitchTile(
                            leading = {
                                SettingsTileIcon(
                                    R.drawable.image_24px
                                )
                            },
                            title = "Dynamic colors",
                            description = "Use wallpaper colors",
                            checked = useDynamicColor,
                            enabled = !useCustomColor,
                            onCheckedChange = { checked ->
                                PreferencesHelper.setBool("useDynamicColors", checked)
                                PreferencesHelper.setBool("useCustomColor", !checked)
                                onDynamicColorChanged(checked)
                                if (useCustomColor) {
                                    useCustomColor = !checked
                                }
                                useDynamicColor = checked
                            }
                        ),
                    )
                )
            }
            item {
                Spacer(Modifier.height(10.dp))
                SettingSection(
                    title = "Additional",
                    tiles = listOf(
                        SettingTile.ActionTile(
                            leading = {SettingsTileIcon(R.drawable.info_24px)},
                            title = "About app",
                            description = "Terms, Version, License, and More",
                            onClick = {
                                navController.navigate("OpenAboutScreen")
                            },
                        )
                    )
                )
            }
        }

        if(showBottomSheet){
            ColorPickerSheetTheme (
                onShowSheet = showBottomSheet,
                sheetState = sheetState,
                initialColorInt = initialColorInt,
                controller = controller,
                onPickedColor = { argbHex ->
                    pickedColor = argbHex
                },
                onExpressiveColorChanged = { checked ->
                    PreferencesHelper.setBool("useExpressiveColor", checked)

                    useExpressiveColor = checked
                    onExpressiveColorChanged(checked)
                },
                useExpressiveColor = useExpressiveColor,
                onSeedChanged = {
                    onSeedChanged(pickedColor)
                    PreferencesHelper.setString("seedColor", pickedColor)
                },
                hideColorSheet ={
                    hideColorSheet()
                }
            )
        }

    }
}


