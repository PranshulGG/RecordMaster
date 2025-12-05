package com.pranshulgg.recordmaster.ui.components

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.pranshulgg.recordmaster.R
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenu(navController: NavController, onDelete: () -> Unit, onShare: () -> Unit, file: File) {
    var expanded by remember { mutableStateOf(false) }
    var showAboutBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    var showCurrentRecordDeleteDialog by remember { mutableStateOf(false) }

    val info = getAudioInfo(file)

    Tooltip("More options", preferredPosition = TooltipAnchorPosition.Below, spacing = 10.dp) {
        IconButton(
            onClick = { expanded = !expanded }, shapes = IconButtonDefaults.shapes()
        ) {
            Symbol(
                R.drawable.more_vert_24px,
                desc = "More options",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        shape = RoundedCornerShape(16.dp),
        offset = DpOffset(x = (-10).dp, y = (-48).dp)
    ) {
        DropdownMenuItem(
            text = { DropDownMenuText("Delete") },
            onClick = {
                showCurrentRecordDeleteDialog = true
                expanded = false
            },
            leadingIcon = { DropDownMenuIcon(R.drawable.delete_forever_24px) }
        )
        DropdownMenuItem(
            text = { DropDownMenuText("Share") },
            onClick = {
                onShare()
                expanded = false
            },
            leadingIcon = { DropDownMenuIcon(R.drawable.share_24px) }
        )
        DropdownMenuItem(
            text = { DropDownMenuText("About file") },
            onClick = {
                showAboutBottomSheet = true
                expanded = false
            },
            leadingIcon = { DropDownMenuIcon(R.drawable.info_24px) }
        )
    }



    if (showAboutBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAboutBottomSheet = false
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))
                SettingSection(
                    tiles = listOf(
                        SettingTile.TextTile(
                            leading = { SettingsTileIcon(R.drawable.graphic_eq_24px) },
                            title = "Encoding",
                            description = info.encoding
                        ),
                        SettingTile.TextTile(
                            leading = { SettingsTileIcon(R.drawable.clock_loader_60_24px) },
                            title = "Audio duration",
                            description = formatDuration(info.duration)
                        ),
                        SettingTile.TextTile(
                            leading = { SettingsTileIcon(R.drawable.storage_24px) },
                            title = "File size",
                            description = formatFileSize(info.size)
                        ),
                        SettingTile.TextTile(
                            leading = { SettingsTileIcon(R.drawable.folder_24px) },
                            title = "File path",
                            description = info.path
                        )
                    )
                )

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showAboutBottomSheet = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(fraction = 0.9f)
                ) {
                    Text("Close")
                }

                Spacer(Modifier.height(10.dp))

            }

        }
        }


        if (showCurrentRecordDeleteDialog) {
        ConfirmDialog(
            title = "Delete recording",
            message = "The recording will be permanently deleted and cannot be undone",
            confirmText = "Delete",
            cancelText = "Cancel",
            onConfirm = {
                onDelete()
                showCurrentRecordDeleteDialog = false
            },
            onDismiss = { showCurrentRecordDeleteDialog = false }
        )
    }

}


@Composable
fun DropDownMenuText(text: String) =
    Text(
        text,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(end = 10.dp)
    )

@Composable
fun DropDownMenuIcon(icon: Int) =
    Symbol(icon, color = MaterialTheme.colorScheme.onSurface, size = 22.dp, paddingStart = 3.dp)



fun getAudioEncoding(file: File): String {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(file.absolutePath)
        val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        retriever.release()
        mime ?: file.extension
    } catch (e: Exception) {
        file.extension
    }
}

fun getAudioInfo(file: File): AudioInfo {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(file.absolutePath)

        val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        val durationMs =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                ?: 0L

        retriever.release()

        AudioInfo(
            encoding = mime ?: file.extension,
            duration = durationMs,
            size = file.length(),
            path = file.absolutePath
        )
    } catch (e: Exception) {
        AudioInfo(
            encoding = file.extension,
            duration = 0L,
            size = file.length(),
            path = file.absolutePath
        )
    }
}

data class AudioInfo(
    val encoding: String,
    val duration: Long,
    val size: Long,
    val path: String
)

fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0)
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    else
        String.format("%02d:%02d", minutes, seconds)
}

fun formatFileSize(bytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024

    return when {
        bytes >= gb -> String.format("%.2f GB", bytes / gb)
        bytes >= mb -> String.format("%.2f MB", bytes / mb)
        bytes >= kb -> String.format("%.2f KB", bytes / kb)
        else -> "$bytes B"
    }
}
