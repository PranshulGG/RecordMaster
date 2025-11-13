package com.pranshulgg.recordmaster.helpers

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


object SnackbarManager {
    private var hostState: SnackbarHostState? = null
    private var scope: CoroutineScope? = null

    fun init(snackbarHostState: SnackbarHostState, coroutineScope: CoroutineScope) {
        hostState = snackbarHostState
        scope = coroutineScope
    }

    fun showMessage(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        val state = hostState
        val coroutineScope = scope
        if (state != null && coroutineScope != null) {
            coroutineScope.launch {
                state.currentSnackbarData?.dismiss()
                state.showSnackbar(message, duration = duration)
            }
        } else {
            println("Error")
        }
    }
}


