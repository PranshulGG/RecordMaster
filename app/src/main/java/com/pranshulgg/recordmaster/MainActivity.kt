package com.pranshulgg.recordmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pranshulgg.recordmaster.helpers.SnackbarManager
import com.pranshulgg.recordmaster.screens.HomeScreen
import com.pranshulgg.recordmaster.screens.PlayRecordingScreen
import com.pranshulgg.recordmaster.screens.RecordingScreen
import com.pranshulgg.recordmaster.ui.components.RecorderSearchBar
import com.pranshulgg.recordmaster.ui.components.Symbol
import com.pranshulgg.recordmaster.ui.components.Tooltip
import com.pranshulgg.recordmaster.ui.theme.RecordMasterTheme
import com.pranshulgg.recordmaster.utils.NavTransitions

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            val navController = rememberNavController()
            val currentMotionScheme = motionScheme
            val motionScheme = remember(currentMotionScheme) { currentMotionScheme }

            LaunchedEffect(Unit) {
                SnackbarManager.init(snackbarHostState, scope)
            }

            RecordMasterTheme() {
                NavHost(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    navController = navController, startDestination = "homePage",
                    enterTransition = {
                        NavTransitions.enter(motionScheme)
                    },
                    exitTransition = {
                        NavTransitions.exit(motionScheme)

                    },
                    popEnterTransition = {
                        NavTransitions.popEnter(motionScheme)

                    },
                    popExitTransition = {
                        NavTransitions.popExit(motionScheme)
                    }
                ) {

                    composable("homePage") { HomeScreen(navController, snackbarHostState = snackbarHostState) }
                    composable("record") { RecordingScreen(onDone = { navController.popBackStack() }) }
                    composable("play/{path}") { backStackEntry ->
                        val encoded = backStackEntry.arguments?.getString("path")

                        val path = encoded?.let { android.net.Uri.decode(it) }
                        path?.let { PlayRecordingScreen(filePath = it, onDone = { navController.popBackStack() }, navController = navController) }
                    }
                }
            }
        }
    }
}


