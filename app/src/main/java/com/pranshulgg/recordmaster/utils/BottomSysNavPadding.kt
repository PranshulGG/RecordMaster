package com.pranshulgg.recordmaster.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@Composable
fun bottomPadding(): Dp {
    val view = LocalView.current
    val insets = ViewCompat.getRootWindowInsets(view)
    val bottomInsetPx = insets?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: 0

    val bottomInsetDp = with(LocalDensity.current) { bottomInsetPx.toDp() }

    return bottomInsetDp
}