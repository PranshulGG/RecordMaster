package com.pranshulgg.recordmaster.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material3.TopAppBarDefaults
import com.pranshulgg.recordmaster.ui.components.Symbol
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import com.pranshulgg.recordmaster.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopBarWithSearch(
    showSearch: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    openDrawer: () -> Unit,
    currentTab: String,
    selectedFolderName: String?,
    clearSearchQuery: () -> Unit,
    onRequestDeleteFolder: (folderName: String?) -> Unit,
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    isSelecting: Boolean = false,
    onDeleteSelected: () -> Unit,
    onShareSelected: () -> Unit,
    onCloseSelection: () -> Unit,
    selectedCount: String
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        actions = {

            if(!isSelecting) {
                if (!showSearch) {
                    IconButton(
                        onClick = { onToggleSearch() },
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Symbol(
                            R.drawable.search_24px,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    IconButton(
                        onClick = { clearSearchQuery() },
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Symbol(
                            R.drawable.close_24px,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if(!isSelecting) {
            if (currentTab != "home" && currentTab != "garbage") {
                IconButton(onClick = {
                    onRequestDeleteFolder(selectedFolderName)
                }, shapes = IconButtonDefaults.shapes()) {
                    Symbol(
                        R.drawable.delete_24px,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

            if(isSelecting){
                IconButton(onClick = {
                    onDeleteSelected()
                }, shapes = IconButtonDefaults.shapes()) {
                    Symbol(
                        R.drawable.delete_24px,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = {
                    onShareSelected()
                }, shapes = IconButtonDefaults.shapes()) {
                    Symbol(
                        R.drawable.share_24px,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }


        },
        title = {
            if (showSearch) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange =  onSearchChange ,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp
                    ),
                    modifier = Modifier
                        .height(56.dp).fillMaxWidth(),

                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->

                        Box(
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search...",
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            } else{
                val titleText = when (currentTab) {
                    "home" -> "Recordings"
                    "garbage" -> "Recently deleted"
                    "folder" -> selectedFolderName ?: "Folder"
                    else -> "Recordings"
                }
                Text(if(isSelecting) "$selectedCount selected" else titleText, fontSize = 20.sp)
            }
        },
        navigationIcon = {
            if(!showSearch && !isSelecting) {
                IconButton(onClick = { openDrawer() }, shapes = IconButtonDefaults.shapes()) {
                    Symbol(R.drawable.menu_24px,  color = MaterialTheme.colorScheme.onSurface)
                }
            } else{
                IconButton(onClick = {
                    if(!isSelecting) {
                        onBack()
                    } else{
                        onCloseSelection()
                    }
                }, shapes = IconButtonDefaults.shapes()) {
                    Symbol(R.drawable.arrow_back_24px,  color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    )
}
