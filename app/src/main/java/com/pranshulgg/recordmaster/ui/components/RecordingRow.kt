package com.pranshulgg.recordmaster.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import android.media.MediaMetadataRetriever
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import com.pranshulgg.recordmaster.ui.components.Symbol
import com.pranshulgg.recordmaster.utils.formatMillis
import com.pranshulgg.recordmaster.utils.moveFileToDir
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.pranshulgg.recordmaster.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecordingRow(
    file: File,
    isThisPlaying: Boolean,
    progress: Float,
    currentPositionMillis: Int,
    currentDurationMillis: Int,
    onPlayPause: () -> Unit,
    onItemClick: () -> Unit,
    onDelete: () -> Unit,
    isGarbage: Boolean,
    musicDir: File,
    onRestore: () -> Unit,
    onMoved: () -> Unit,
    onRenamed: () -> Unit,
    onShowMessage: (message: String) -> Unit,
    currentTab: String,
    onLongClick: () -> Unit,
    isSelected: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val sdfDate = SimpleDateFormat("MMM dd", Locale.getDefault())
    val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val date = sdfDate.format(Date(file.lastModified()))
    val time = sdfTime.format(Date(file.lastModified()))



    fun hideSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
            }
        }
    }


    var fileDurationMillis by remember { mutableStateOf(0) }
    LaunchedEffect(file.absolutePath) {
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(file.absolutePath)
            val dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toIntOrNull() ?: 0
            fileDurationMillis = dur
            mmr.release()
        } catch (_: Exception) {
            fileDurationMillis = 0
        }
    }

    var lastPosition by remember { mutableStateOf(0) }

    val displayCurrent = if (isThisPlaying) {
        lastPosition = currentPositionMillis
        currentPositionMillis
    } else {
        lastPosition
    }
    val displayTotal = if (isThisPlaying && currentDurationMillis > 0) currentDurationMillis else fileDurationMillis

    var menuExpanded by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by rememberSaveable { mutableStateOf(file.name) }

    var showMoveDialog by remember { mutableStateOf(false) }
    var availableFolders by remember { mutableStateOf(listOf<File>()) }

    val folderName = file.parentFile?.name ?: "Unknown"

    val tonedDown = Color(
        ColorUtils.blendARGB(
            MaterialTheme.colorScheme.surfaceContainerHigh.toArgb(),
            MaterialTheme.colorScheme.surface.toArgb(),
            0.6f
        )
    )

    Box (
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(shape = RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = { onItemClick() },
                onLongClick = { onLongClick() }
            )
            .background(if(isSelected) MaterialTheme.colorScheme.surfaceContainerLowest else tonedDown)

    ) {
//        if (isSelected) {
//            Row(
//                modifier = Modifier
//                    .padding(10.dp)
//            ) {
//            }
//        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, bottom = 16.dp, end = 5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        file.nameWithoutExtension,
                        fontSize = 26.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )

                    Text(
                        text = "$date at $time",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                Row(
                ) {
                    Text(formatMillis(displayCurrent) + " • ",fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), lineHeight = 1.sp )
                    Text(formatMillis(displayTotal),fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), lineHeight = 1.sp )

                }

                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(end = 5.dp)
                ) {

                    if(!isSelected){
                    IconToggleButton(onCheckedChange = { onPlayPause() }, checked = isThisPlaying,
                        modifier = Modifier.height(34.dp,),
                        shapes = IconButtonDefaults.toggleableShapes(),
                        colors = IconButtonDefaults.iconToggleButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                        ) {
                        if (isThisPlaying) Symbol(R.drawable.pause_24px, color = MaterialTheme.colorScheme.primary) else Symbol(
                            R.drawable.play_arrow_24px, color = MaterialTheme.colorScheme.onSurface)
                    }
                IconButton(onClick = {showBottomSheet = true}, shapes = IconButtonDefaults.shapes(), Modifier.height(34.dp), colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )) {
                    Symbol(R.drawable.more_vert_24px, color = MaterialTheme.colorScheme.onSurface)
                }

            } else{
                Checkbox(checked = true, onCheckedChange = { })

            }
        }
    }

            Spacer(Modifier.height(3.dp))

            val animatedProgress by
            animateFloatAsState(
                targetValue = progress.coerceIn(0f, 1f),
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,

                )

            Column(modifier = Modifier.padding(end = 16.dp)) {
                LinearWavyProgressIndicator(
                    progress = { animatedProgress  },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(5.dp),
                    wavelength = 20.dp,
                    amplitude = { _ -> (progress.coerceIn(0f, 1f) * 5.5f).coerceAtMost(1f) }

                    )
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState,
                    dragHandle = {
                        Box(
                            modifier = Modifier.padding(top = 22.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                        RoundedCornerShape(50.dp)
                                    )
                            )
                        }
                    }
                ) {

                    Column(
                        Modifier.padding(bottom = 16.dp)
                    ) {
                        Spacer(Modifier.height(12.dp))


                        SettingSection(
                            tiles =  if(isGarbage) {
                                listOf(
                                    SettingTile.ActionTile(
                                        leading = {SettingsTileIcon(R.drawable.restore_from_trash_24px)},
                                        title = "Restore",
                                        onClick = {
                                            onRestore()
                                            hideSheet()
                                        }
                                    ),
                                    SettingTile.ActionTile (
                                        leading = {SettingsTileIcon(R.drawable.delete_forever_24px, dangerColor = true)},
                                        title = "Delete forever",
                                        danger = true,
                                        onClick = {
                                        onDelete()
                                        hideSheet()
                                    }
                                )
                            )
                        } else{
                            listOf(
                                SettingTile.ActionTile(
                                    leading = {SettingsTileIcon(R.drawable.delete_24px)},
                                    title = "Delete",
                                    onClick = {
                                        onDelete()
                                        hideSheet()
                                    }
                                ),
                                SettingTile.ActionTile(
                                    leading = {SettingsTileIcon(R.drawable.edit_24px)},
                                    title = "Rename",
                                    onClick = {
                                        renameText = file.name
                                        showRenameDialog = true
                                        hideSheet()
                                    }
                                ),

                                if(currentTab != "home") {
                                    SettingTile.ActionTile(

                                        leading = { SettingsTileIcon(R.drawable.drive_file_move_24px) },
                                        title = "Remove from folder",
                                        onClick = {
                                            val moved = try {
                                                moveFileToDir(file, musicDir)
                                                true
                                            } catch (t: Throwable) {
                                                false
                                            }
                                            if (moved) {
                                                onShowMessage("Removed from folder")
                                                onMoved()
                                            } else {
                                                onShowMessage("Move failed")
                                            }
                                            hideSheet()
                                        }
                                    )
                                } else{
                                    SettingTile.ActionTile(
                                        leading = {SettingsTileIcon(R.drawable.folder_24px)},
                                        title = "Move to folder",
                                        onClick = {
                                            val dirs = (musicDir.listFiles() ?: emptyArray()).filter { it.isDirectory && it.name != "garbage" }
                                            availableFolders = dirs
                                            showMoveDialog = true
                                            hideSheet()
                                        }
                                    )
                                },




                                SettingTile.ActionTile(
                                    leading = {SettingsTileIcon(R.drawable.share_24px)},
                                    title = "Share",
                                    onClick = {
                                        try {
                                            val uri: Uri = try {
                                                FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                            } catch (_: Exception) {
                                                Uri.fromFile(file)
                                            }
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "audio/*"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            val chooser = Intent.createChooser(shareIntent, "Share audio")
                                            context.startActivity(chooser)
                                        } catch (e: Exception) {
                                            onShowMessage("Failed to share file")

                                        }
                                        hideSheet()
                                    }
                                ),
                            )
                        }
                    )
                }
            }
        }



            if (showMoveDialog) {
                AlertDialog(
                    onDismissRequest = { showMoveDialog = false },
                    title = { Text("Move to folder") },
                    text = {
                        Column {
                            if (availableFolders.isEmpty()) {
                                Text("No folders found. Create a folder from the sidebar.")
                            } else {
                                Column {
                                    availableFolders.forEach { dir ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    val moved = try {
                                                        moveFileToDir(file, dir)
                                                        true
                                                    } catch (t: Throwable) {
                                                        false
                                                    }
                                                    showMoveDialog = false
                                                    if (moved) {
                                                        Toast.makeText(
                                                            context,
                                                            "Moved to ${dir.name}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        onMoved()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Move failed",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(dir.name)
                                        }

                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showMoveDialog = false }, shapes = ButtonDefaults.shapes()) { Text("Cancel", fontWeight = FontWeight.W600, fontSize = 16.sp) }
                    }
                )
            }

            if (showRenameDialog) {
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false },
                    title = { Text("Rename") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = renameText,
                                onValueChange = { renameText = it },
                                singleLine = true,
                                placeholder = { "New name" },
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            shapes = ButtonDefaults.shapes(),
                            onClick = {
                            val newNameTrimmed = renameText.trim()
                            if (newNameTrimmed.isNotEmpty()) {
                                val target = File(file.parentFile, newNameTrimmed)
                                val success = try {
                                    file.renameTo(target)
                                } catch (t: Throwable) {
                                    false
                                }
                                showRenameDialog = false
                                if (success) {
                                    onShowMessage("Renamed to $newNameTrimmed")
                                    onRenamed()
                                } else {
                                    onShowMessage("Rename failed")
                                }
                            } else {
                                onShowMessage("Invalid name")
                            }
                        }) { Text("Rename", fontWeight = FontWeight.W600, fontSize = 16.sp) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = false }, shapes = ButtonDefaults.shapes()) { Text("Cancel", fontWeight = FontWeight.W600, fontSize = 16.sp) }
                    }
                )
            }
        }
    }
}


