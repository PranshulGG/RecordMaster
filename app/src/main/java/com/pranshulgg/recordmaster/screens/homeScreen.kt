package com.pranshulgg.recordmaster.screens

import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pranshulgg.recordmaster.R
import com.pranshulgg.recordmaster.ui.components.DrawerContent
import com.pranshulgg.recordmaster.ui.components.EmptyContainerPlaceholder
import com.pranshulgg.recordmaster.ui.components.RecordingRow
import com.pranshulgg.recordmaster.ui.components.Symbol
import com.pranshulgg.recordmaster.ui.components.TopBarWithSearch
import com.pranshulgg.recordmaster.utils.computeDirKey
import com.pranshulgg.recordmaster.utils.moveFileToDir
import com.pranshulgg.recordmaster.utils.stopIfPlayingAndCleanup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.core.content.FileProvider
import com.pranshulgg.recordmaster.ui.components.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(navController: NavController, snackbarHostState: SnackbarHostState){
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var currentTab by remember { mutableStateOf("home") }
    val scope = rememberCoroutineScope()
    var selectedFolderName by remember { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    var playingPath by remember { mutableStateOf<String?>(null) }

    var selectedPaths by remember { mutableStateOf(setOf<String>()) }

    BackHandler(enabled = showSearch) {
        searchQuery = ""
        showSearch = false
    }

    BackHandler(enabled = selectedPaths.isNotEmpty()) {
        selectedPaths = emptySet()
    }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by rememberSaveable { mutableStateOf("") }
    var foldersExpanded by remember { mutableStateOf(true) }

    var showDeleteFolderDialog by remember { mutableStateOf(false) }
    var folderToDeleteName by remember { mutableStateOf<String?>(null) }
    var selectedFolderKey by remember { mutableStateOf(0L) }

    val showMessage: suspend (String) -> Unit = { msg ->
        if (snackbarHostState != null) {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val musicDir = remember {
        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
    }

    val garbageDir = remember {
        File(musicDir, "garbage").apply { if (!exists()) mkdirs() }
    }

    var rootDirKey by remember { mutableStateOf(0L) }
    var garbageDirKey by remember { mutableStateOf(0L) }


    fun toggleSelection(path: String) {
        selectedPaths = if (selectedPaths.contains(path)) selectedPaths - path else selectedPaths + path
    }
    var showGarbageConfirmDeleteSelectedDialog by remember { mutableStateOf(false) }
    var showGarbageConfirmDeleteDialog by remember { mutableStateOf(false) }


    fun shareSelected() {
        if (selectedPaths.isEmpty()) return
        try {
            val uris = ArrayList<Uri>()
            selectedPaths.forEach { path ->
                val f = File(path)
                val uri: Uri = try {
                    FileProvider.getUriForFile(context, "${context.packageName}.provider", f)
                } catch (_: Exception) {
                    Uri.fromFile(f)
                }
                uris.add(uri)
            }

            val intent = if (uris.size == 1) {
                Intent(Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, uris[0])
                }
            } else {
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "audio/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                }
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(intent, "Share audio"))
        } catch (e: Exception) {
            scope.launch {
                if (snackbarHostState != null) snackbarHostState.showSnackbar("Failed to share files")
                else Toast.makeText(context, "Failed to share files", Toast.LENGTH_SHORT).show()
            }
        } finally {
            selectedPaths = emptySet()
        }
    }

    var filesToDelete: List<File> by remember { mutableStateOf(emptyList()) }

    fun deleteSelected() {
        if (selectedPaths.isEmpty()) return

        val files = selectedPaths.map { File(it) }
        files.forEach { file ->
            stopIfPlayingAndCleanup(file, mediaPlayer, playingPath)
        }

        if (currentTab == "garbage") {
            filesToDelete = files
            showGarbageConfirmDeleteSelectedDialog = true
        } else {
            files.forEach { file ->
                try { moveFileToDir(file, garbageDir) } catch (_: Throwable) {}
            }
            scope.launch { showMessage("Moved to garbage") }
        }

        rootDirKey = computeDirKey(musicDir)
        garbageDirKey = computeDirKey(garbageDir)
        if (currentTab == "folder" && selectedFolderName != null) {
            selectedFolderKey = computeDirKey(File(musicDir, selectedFolderName!!))
        }

        selectedPaths = emptySet()
    }

    if (showGarbageConfirmDeleteSelectedDialog) {
        ConfirmDialog(
            title = "Delete recordings",
            message = "The selected recording(s) will be permanently deleted and cannot be undone",
            confirmText = "Delete",
            cancelText = "Cancel",
            onConfirm = {
                filesToDelete.forEach { file ->
                    try { file.delete() } catch (_: Throwable) {}
                }
                filesToDelete = emptyList()
                showGarbageConfirmDeleteSelectedDialog = false
            },
            onDismiss = { showGarbageConfirmDeleteSelectedDialog = false }
        )
    }

    LaunchedEffect(musicDir.absolutePath) {
        rootDirKey = computeDirKey(musicDir)
        garbageDirKey = computeDirKey(garbageDir)
    }

    LaunchedEffect(selectedFolderName, musicDir.absolutePath) {
        if (!selectedFolderName.isNullOrEmpty()) {
            val folder = File(musicDir, selectedFolderName!!)
            selectedFolderKey = computeDirKey(folder)
        } else {
            selectedFolderKey = 0L
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    ModalNavigationDrawer(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentTab = currentTab,
                selectedFolderName = selectedFolderName,
                rootDirKey = rootDirKey,
                musicDir = musicDir,
                onSelectTab = { tab, folder ->
                    currentTab = tab
                    selectedFolderName = folder
                    scope.launch { drawerState.close() }
                },
                onRequestCreateFolder = { showCreateFolderDialog = true },
                onRequestDeleteFolder = { name ->
                    folderToDeleteName = name
                    showDeleteFolderDialog = true
                },
                onfoldersExpanded = { value ->
                    foldersExpanded = value
                },
                foldersExpanded = foldersExpanded,
                navController = navController,
                closeDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                AnimatedVisibility(
                    visible =  currentTab == "home" && !showSearch,
                    enter = scaleIn(
                        initialScale = 0.8f,
                        animationSpec = motionScheme.defaultSpatialSpec()
                    ) + fadeIn(),
                    exit = scaleOut(
                        targetScale = 0.8f,
                        animationSpec = motionScheme.defaultSpatialSpec()
                    ) + fadeOut(),
                ) {
                    FloatingActionButton(
                        modifier = Modifier.size(96.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        onClick = { navController.navigate("record") },
                        shape = CircleShape
                    ) {
                        Symbol(
                            R.drawable.fiber_manual_record_24px,
                            size = 36.dp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            },
            topBar = {
                TopBarWithSearch(
                    showSearch = showSearch,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onToggleSearch = { showSearch = !showSearch },
                    openDrawer = { scope.launch { drawerState.open() } },
                    currentTab = currentTab,
                    selectedFolderName = selectedFolderName,
                    clearSearchQuery = {searchQuery = ""},
                    onRequestDeleteFolder = { name ->
                        folderToDeleteName = name
                        showDeleteFolderDialog = true
                    },
                    onBack = {
                        searchQuery = ""
                        showSearch = false
                    },
                    scrollBehavior = scrollBehavior,
                    isSelecting = selectedPaths.isNotEmpty(),
                    onShareSelected = {
                        shareSelected()
                    },
                    onDeleteSelected = {
                        deleteSelected()

                    },
                    onCloseSelection = {
                        selectedPaths = emptySet()
                    },
                    selectedCount = selectedPaths.size.toString()
                )

            }

        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize()
            ) {


                val rootFiles by remember(rootDirKey) {
                    mutableStateOf(
                        (musicDir.listFiles() ?: emptyArray())
                            .filter { it.isFile }
                            .sortedByDescending { it.lastModified() }
                    )
                }

                val garbageFiles by remember(garbageDirKey) {
                    mutableStateOf(
                        (garbageDir.listFiles() ?: emptyArray())
                            .filter { it.isFile }
                            .sortedByDescending { it.lastModified() }
                    )
                }

                val folderFiles by remember(selectedFolderKey, selectedFolderName) {
                    mutableStateOf(
                        if (!selectedFolderName.isNullOrEmpty()) {
                            val f = File(musicDir, selectedFolderName!!)
                            (f.listFiles() ?: emptyArray()).filter { it.isFile }.sortedByDescending { it.lastModified() }
                        } else emptyList()
                    )
                }



                var isPlaying by remember { mutableStateOf(false) }
                var currentPos by remember { mutableStateOf(0) }
                var duration by remember { mutableStateOf(0) }

                LaunchedEffect(isPlaying, mediaPlayer.value) {
                    while (isPlaying && mediaPlayer.value != null) {
                        try { currentPos = mediaPlayer.value?.currentPosition ?: 0 } catch (_: Exception) {}
                        delay(200)
                    }
                }

                val displayedRecordings = if (searchQuery.isBlank()) {
                    when (currentTab) {
                        "home" -> rootFiles
                        "garbage" -> garbageFiles
                        "folder" -> folderFiles
                        else -> rootFiles
                    }
                } else {
                    val allCandidates = mutableListOf<File>()
                    allCandidates += rootFiles
                    allCandidates += garbageFiles
                    val allSubfolderFiles = (musicDir.listFiles() ?: emptyArray())
                        .filter { it.isDirectory && it.name != "garbage" }
                        .flatMap { (it.listFiles() ?: emptyArray()).filter { f -> f.isFile } }
                    allCandidates += allSubfolderFiles
                    allCandidates.filter { it.name.contains(searchQuery, ignoreCase = true) }
                        .sortedByDescending { it.lastModified() }
                }




                if (displayedRecordings.isEmpty()) {
                    EmptyContainerPlaceholder(R.drawable.graphic_eq_24px, "No recordings")
                } else {
                    val sdfMonth = SimpleDateFormat("MMMM", Locale.getDefault())
                    val grouped = displayedRecordings
                        .groupBy { sdfMonth.format(Date(it.lastModified())) }
                        .entries
                        .sortedByDescending { entry -> entry.value.maxOf { it.lastModified() } }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        grouped.forEach { (month, files) ->
                            item {
                                    Text(
                                        text = month,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.W700,
                                        fontSize = 16.sp

                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                            }

                            items(files) { file ->
                                RecordingRow(
                                    file = file,
                                    isThisPlaying = playingPath == file.absolutePath && isPlaying,
                                    progress = if (playingPath == file.absolutePath && duration > 0) (currentPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f,
                                    currentPositionMillis = currentPos,
                                    currentDurationMillis = duration,
                                    onItemClick = {
                                        if (selectedPaths.isNotEmpty()) {
                                            toggleSelection(file.absolutePath)
                                        } else {
                                            val encoded = Uri.encode(file.absolutePath)
                                            navController.navigate("play/$encoded")
                                        }
                                    },
                                    onLongClick = {
                                        toggleSelection(file.absolutePath)
                                    },
                                    onPlayPause = {
                                        if (playingPath == file.absolutePath) {
                                            val mp = mediaPlayer.value
                                            if (mp != null && mp.isPlaying) {
                                                mp.pause(); isPlaying = false
                                            } else if (mp != null) {
                                                mp.start(); isPlaying = true
                                            }
                                        } else {
                                            try {
                                                mediaPlayer.value?.stop()
                                                mediaPlayer.value?.release()
                                            } catch (_: Exception) {}
                                            mediaPlayer.value = null
                                            try {
                                                val mp = MediaPlayer().apply {
                                                    setDataSource(file.absolutePath)
                                                    prepare()
                                                    start()
                                                }
                                                mediaPlayer.value = mp
                                                playingPath = file.absolutePath
                                                isPlaying = true
                                                duration = mp.duration
                                            } catch (e: Exception) {
                                                isPlaying = false
                                                playingPath = null
                                                mediaPlayer.value?.release()
                                                mediaPlayer.value = null
                                            }
                                        }
                                    },

                                    isGarbage = currentTab == "garbage",
                                    musicDir = musicDir,
                                    onDelete = {
                                        if (currentTab == "garbage") {

                                            showGarbageConfirmDeleteDialog = true

                                        } else {
                                            val fromParent = file.parentFile
                                            moveFileToDir(file, garbageDir)
                                            if (playingPath == file.absolutePath) playingPath = null
                                            rootDirKey = computeDirKey(musicDir)
                                            garbageDirKey = computeDirKey(garbageDir)
                                            if (currentTab == "folder" && fromParent?.name == selectedFolderName) {
                                                selectedFolderKey = computeDirKey(File(musicDir, selectedFolderName!!))
                                            }
                                            scope.launch { showMessage("Moved to garbage") }

                                        }
                                        selectedPaths = selectedPaths - file.absolutePath
                                    },

                                    isSelected = selectedPaths.contains(file.absolutePath),

                                    onRestore = {
                                        val fromParent = file.parentFile
                                        moveFileToDir(file, musicDir)
                                        if (playingPath == file.absolutePath) playingPath = null
                                        rootDirKey = computeDirKey(musicDir)
                                        garbageDirKey = computeDirKey(garbageDir)

                                        if (currentTab == "folder" && fromParent?.name == selectedFolderName) {
                                            selectedFolderKey = computeDirKey(File(musicDir, selectedFolderName!!))
                                        }
                                    },

                                    onMoved = {
                                        rootDirKey = computeDirKey(musicDir)
                                        garbageDirKey = computeDirKey(garbageDir)
                                        selectedFolderName?.let {
                                            selectedFolderKey = computeDirKey(File(musicDir, it))
                                        }
                                    },

                                    onRenamed = {
                                        rootDirKey = computeDirKey(musicDir)
                                        garbageDirKey = computeDirKey(garbageDir)
                                        selectedFolderName?.let {
                                            selectedFolderKey = computeDirKey(File(musicDir, it))
                                        }
                                    },
                                    onShowMessage = { message ->
                                        scope.launch {
                                            showMessage(message)
                                        }
                                    },
                                    currentTab = currentTab

                                )

                                if (showGarbageConfirmDeleteDialog) {
                                    ConfirmDialog(
                                        title = "Delete recording",
                                        message = "The recording will be permanently deleted and cannot be undone",
                                        confirmText = "Delete",
                                        cancelText = "Cancel",
                                        onConfirm = {
                                            stopIfPlayingAndCleanup(file, mediaPlayer, playingPath)
                                            if (file.delete()) {  }
                                            garbageDirKey = computeDirKey(garbageDir)
                                        },
                                        onDismiss = { showGarbageConfirmDeleteDialog = false }
                                    )
                                }
                            }



                            item {
                                Spacer(modifier = Modifier.height(140.dp))
                            }
                        }
                    }
                }

                if (showCreateFolderDialog) {
                    AlertDialog(
                        onDismissRequest = { showCreateFolderDialog = false; newFolderName = "" },
                        title = { Text("Create new folder") },
                        text = {
                                OutlinedTextField(
                                    value = newFolderName,
                                    placeholder = {Text("Folder name")},
                                    onValueChange = { newFolderName = it },
                                    singleLine = true
                                )
                        },
                        confirmButton = {
                            TextButton(
                                shapes = ButtonDefaults.shapes(),
                                onClick = {
                                val name = newFolderName.trim()
                                if (name.isNotEmpty()) {
                                    val newDir = File(musicDir, name)
                                    val created = if (!newDir.exists()) newDir.mkdirs() else true
                                    if (created) {
                                        scope.launch {
                                            showMessage("Folder \"$name\" created")
                                        }
                                        rootDirKey = computeDirKey(musicDir)
                                    } else {
                                        scope.launch {
                                            showMessage("Failed to create folder")
                                        }
                                    }
                                }
                                showCreateFolderDialog = false
                                newFolderName = ""
                                foldersExpanded = false
                            }) { Text("Create", fontWeight = FontWeight.W600, fontSize = 16.sp) }
                        },
                        dismissButton = {
                            TextButton(
                                shapes = ButtonDefaults.shapes(),
                                onClick = { showCreateFolderDialog = false; newFolderName = "" }) {
                                Text("Cancel", fontWeight = FontWeight.W600, fontSize = 16.sp)
                            }
                        }
                    )
                }

                if (showDeleteFolderDialog && !folderToDeleteName.isNullOrEmpty()) {
                    AlertDialog(
                        onDismissRequest = { showDeleteFolderDialog = false; folderToDeleteName = null },
                        title = { Text("Delete folder \"${folderToDeleteName}\"?") },
                        text = {
                            Text("Contents will be moved to Recently deleted, then the folder will be removed.")
                        },
                        confirmButton = {
                            TextButton(
                                shapes = ButtonDefaults.shapes(),
                                onClick = {
                                val name = folderToDeleteName!!
                                val target = File(musicDir, name)
                                try {
                                    (target.listFiles() ?: emptyArray()).filter { it.isFile }.forEach { f ->
                                        try { moveFileToDir(f, garbageDir) } catch (_: Throwable) {}
                                    }
                                    target.deleteRecursively()
                                    scope.launch {
                                        showMessage("Folder \"$name\" deleted")
                                    }

                                } catch (t: Throwable) {
                                    scope.launch {
                                        showMessage("Failed to delete folder")
                                    }
                                }
                                folderToDeleteName = null
                                showDeleteFolderDialog = false
                                rootDirKey = computeDirKey(musicDir)
                                garbageDirKey = computeDirKey(garbageDir)
                                currentTab = "home"
                            }) { Text("Delete", fontWeight = FontWeight.W600, fontSize = 16.sp) }
                        },
                        dismissButton = {
                            TextButton(
                                shapes = ButtonDefaults.shapes(),
                                onClick = { showDeleteFolderDialog = false; folderToDeleteName = null }) {
                                Text("Cancel", fontWeight = FontWeight.W600, fontSize = 16.sp)
                            }
                        }
                    )
                }

                DisposableEffect(Unit) {
                    onDispose {
                        try { mediaPlayer.value?.stop() } catch (_: Exception) {}
                        try { mediaPlayer.value?.release() } catch (_: Exception) {}
                        mediaPlayer.value = null
                    }
                }
            }
        }
    }
}


