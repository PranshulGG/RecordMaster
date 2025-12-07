package com.pranshulgg.recordmaster.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@Composable
fun topPadding(): Dp {
    val view = LocalView.current
    val insets = ViewCompat.getRootWindowInsets(view)
    val topInsetPx = insets?.getInsets(WindowInsetsCompat.Type.systemBars())?.top ?: 0

    val topInsetDp = with(LocalDensity.current) { topInsetPx.toDp() }

    return topInsetDp
}