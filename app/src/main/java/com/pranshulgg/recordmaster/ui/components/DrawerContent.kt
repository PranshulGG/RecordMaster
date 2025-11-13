package com.pranshulgg.recordmaster.ui.components

import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import java.util.Locale
import com.pranshulgg.recordmaster.ui.components.Symbol
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pranshulgg.recordmaster.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DrawerContent(
    currentTab: String,
    selectedFolderName: String?,
    rootDirKey: Long,
    musicDir: File,
    onSelectTab: (tab: String, folder: String?) -> Unit,
    onRequestCreateFolder: () -> Unit,
    onRequestDeleteFolder: (folderName: String) -> Unit,
    onfoldersExpanded: (Boolean) -> Unit,
    foldersExpanded: Boolean
) {

    val folders = remember(rootDirKey) {
        (musicDir.listFiles() ?: emptyArray())
            .filter { it.isDirectory && it.name != "garbage" }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    ModalDrawerSheet(
        modifier = Modifier.width(280.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
        Text(
            "RecordMaster",
            modifier = Modifier.padding(16.dp, bottom = 0.dp, end = 16.dp, top = 16.dp),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Column(Modifier.padding(14.dp)) {
            NavigationDrawerItem(
                label = { Text("Recordings") },
                selected = currentTab == "home",
                icon = {
                    if (currentTab == "home")
                        Symbol(R.drawable.home_24px, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    else
                        Symbol(R.drawable.home_outlined_24px, color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                onClick = {
                    onSelectTab("home", null)
                }
            )

            NavigationDrawerItem(
                label = { Text("Recently deleted") },
                selected = currentTab == "garbage",
                icon = {
                    if (currentTab == "garbage")
                        Symbol(R.drawable.folder_delete_24px, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    else
                        Symbol(R.drawable.folder_delete_outlined_24px, color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                onClick = {
                    onSelectTab("garbage", null)
                }
            )



            Spacer(modifier = Modifier.height(14.dp))


            if(folders.isNotEmpty()) {
                Text(
                    "Folders", fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.W700
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

                val context = LocalContext.current
                val musicDir = remember {
                    context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
                }


                folders.forEach { dir ->
                    val recordingsCount = dir.listFiles()?.count { it.isFile } ?: 0
                    NavigationDrawerItem(
                        label = { Text(dir.name) },
                        selected = currentTab == "folder" && selectedFolderName == dir.name,
                        icon = {
                            if(currentTab == "folder" && selectedFolderName == dir.name){
                                Symbol(
                                    R.drawable.folder_24px,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            } else{
                                Symbol(
                                    R.drawable.folder_outlined_24px,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                        },
                        badge = {
                                Text(recordingsCount.toString(), fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                        },
                        onClick = { onSelectTab("folder", dir.name) }
                    )
                }
            Spacer(Modifier.height(80.dp))

        }
    }
    }

        FloatingActionButton(
            onClick = { onRequestCreateFolder() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Symbol(R.drawable.create_new_folder_24px, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
    }

}