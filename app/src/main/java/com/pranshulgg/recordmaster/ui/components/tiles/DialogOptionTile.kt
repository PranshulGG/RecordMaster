package com.pranshulgg.recordmaster.ui.components.tiles

import android.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> DialogOptionTile(
    headline: String,
    description: String? = null,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String = { it.toString() },
    leading: @Composable (() -> Unit)? = null,
    shapes: RoundedCornerShape,
    dialogTitle: String? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),

        shape = shapes,
    ) {
        ListItem(

            modifier = Modifier
                .clickable { showDialog = true },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            leadingContent = leading,
            headlineContent = { Text(headline) },
            supportingContent = {
                if (description != null) Text(description)
                else selectedOption?.let {
                    Text(
                        optionLabel(it),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            },
        )
    }

    if (showDialog) {
        var tempSelection by remember { mutableStateOf(selectedOption) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(dialogTitle ?: headline) },
            text = {
                val listState = rememberLazyListState()

                val showTopDivider by remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
                    }
                }

                val showBottomDivider by remember {
                    derivedStateOf {
                        val info = listState.layoutInfo
                        val total = info.totalItemsCount
                        val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
                        total > 0 && lastVisible < total - 1
                    }
                }

                Box(
                    modifier = Modifier
                        .heightIn(max = 500.dp)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(options) { option ->
                            Row(
                                modifier = Modifier
                                    .clickable { tempSelection = option }
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = option == tempSelection,
                                    onClick = { tempSelection = option }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    optionLabel(option),
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = showTopDivider,
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    }

                    AnimatedVisibility(
                        visible = showBottomDivider,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                TextButton(

                    onClick = {
                        tempSelection?.let { onOptionSelected(it) }
                        showDialog = false
                    }, shapes = ButtonDefaults.shapes()
                ) {

                    Text("Save", fontWeight = FontWeight.W600, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }, shapes = ButtonDefaults.shapes()) {
                    Text("Cancel", fontWeight = FontWeight.W600, fontSize = 16.sp)
                }
            }
        )
    }
}
