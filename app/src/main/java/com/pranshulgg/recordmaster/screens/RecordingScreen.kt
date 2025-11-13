package com.pranshulgg.recordmaster.screens

import android.Manifest
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.pranshulgg.recordmaster.ui.components.Symbol
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.pranshulgg.recordmaster.R
import com.pranshulgg.recordmaster.ui.components.ConfirmDialog


var mainTitle = "Record"
var mainSub = ""

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun RecordingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0L) }

    val recorderRef = remember { mutableStateOf<MediaRecorder?>(null) }
    val outputFileRef = remember { mutableStateOf<File?>(null) }


    val amplitudes = remember { mutableStateListOf<Float>() }
    val maxSamples = 40

    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                startRecording(context, recorderRef, outputFileRef, { started ->
                    if (started) {
                        isRecording = true
                        isPaused = false
                        elapsedSeconds = 0L
                        amplitudes.clear()
                        repeat(maxSamples) { amplitudes.add(0f) }
                    } else coroutineScope.launch { snackbarHostState.showSnackbar("Could not start recorder") }
                }, namePrefix = "User")
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Mic permission is required to record")
                }
            }
        }
    )

    LaunchedEffect(isRecording, isPaused) {
        if (isRecording && !isPaused) {
            while (isRecording && !isPaused) {
                delay(1000)
                elapsedSeconds += 1
            }
        }
    }

    LaunchedEffect(isRecording, isPaused, recorderRef.value) {
        if (isRecording && !isPaused && recorderRef.value != null) {
            try {
                val amp = recorderRef.value?.maxAmplitude
            } catch (e: Exception) {
                Log.w("RecordingScreen", "prime amp failed", e)
            }

            var prev = amplitudes.lastOrNull() ?: 0f

            while (isRecording && !isPaused && recorderRef.value != null) {
                try {
                    val amp = recorderRef.value?.maxAmplitude ?: 0
                    val normalized = (amp / 32767f).coerceIn(0f, 1f)
                    val smoothed = prev * 0.85f + normalized * 0.15f
                    amplitudes.add(smoothed)
                    if (amplitudes.size > maxSamples) amplitudes.removeAt(0)
                    prev = smoothed
                } catch (e: Exception) {
                    Log.w("RecordingScreen", "amp poll failed", e)
                }
                delay(60) // 16-20 samples per second
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                recorderRef.value?.let { rec ->
                    try { rec.stop() } catch (_: Exception) {}
                    try { rec.reset() } catch (_: Exception) {}
                    try { rec.release() } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                Log.w("RecordingScreen", "cleanup error", e)
            } finally {
                recorderRef.value = null
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {Text(mainTitle)},
                subtitle = {Text(SimpleDateFormat("MMM dd 'at' h:mm a", Locale.getDefault()).format(Date()))},
                navigationIcon = {
                    IconButton(
                        onClick = {onDone()}
                    ) {
                        Symbol(R.drawable.arrow_back_24px, color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(shapes = IconButtonDefaults.shapes(), onClick = {
                        showConfirmDeleteDialog = true
                    }) {
                        Symbol(R.drawable.delete_forever_24px)
                    }

                    if (showConfirmDeleteDialog) {
                        ConfirmDialog(
                            title = "Delete recording",
                            message = "This recording will be permanently deleted. This cannot be undone",
                            confirmText = "Delete",
                            cancelText = "Cancel",
                            onConfirm = {
                                outputFileRef.value?.let { file ->
                                    if (file.exists()) file.delete()
                                }
                                recorderRef.value = null
                                outputFileRef.value = null
                                isRecording = false
                                isPaused = false
                                onDone()
                            },
                            onDismiss = { showConfirmDeleteDialog = false }
                        )
                    }
                }
            )
        }


    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {



                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = 0.7f)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Waveform(amplitudes = amplitudes.toList())
                }


                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRecording && !isPaused) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = formatElapsed(elapsedSeconds), fontSize = 40.sp, color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.height(24.dp))


                Row(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (!isRecording) Arrangement.Center else Arrangement.spacedBy(5.dp)
                ) {


                    val buttonLabel = when {
                        !isRecording -> "Record"
                        isRecording && !isPaused -> "Pause"
                        else -> "Resume"
                    }


                    Box(
                       modifier = if (!isRecording) Modifier.fillMaxWidth() else Modifier.weight(1f),
                        contentAlignment = Alignment.Center

                    ) {
                                ToggleButton(
                                    checked = isRecording && !isPaused,
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        checkedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    onCheckedChange = {
                                        when {
                                            !isRecording -> {
                                                val permState = ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.RECORD_AUDIO
                                                )
                                                if (permState == PackageManager.PERMISSION_GRANTED) {
                                                    startRecording(
                                                        context,
                                                        recorderRef,
                                                        outputFileRef,
                                                        { started ->
                                                            if (started) {
                                                                isRecording = true
                                                                isPaused = false
                                                                elapsedSeconds = 0L
                                                                amplitudes.clear()
                                                                repeat(maxSamples) {
                                                                    amplitudes.add(
                                                                        0f
                                                                    )
                                                                }
                                                            } else coroutineScope.launch {
                                                                snackbarHostState.showSnackbar("Could not start recorder")
                                                            }
                                                        },
                                                        namePrefix = "User"
                                                    )
                                                } else {
                                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                }
                                            }

                                            isRecording && !isPaused -> {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                    try {
                                                        recorderRef.value?.pause()
                                                        isPaused = true
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                "Paused"
                                                            )
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.w("RecordingScreen", "pause failed", e)
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                "Pause failed on this device"
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "Pause not supported on this device"
                                                        )
                                                    }
                                                }
                                            }

                                            isRecording && isPaused -> {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                    try {
                                                        recorderRef.value?.resume()
                                                        isPaused = false
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                "Resumed"
                                                            )
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.w("RecordingScreen", "resume failed", e)
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                "Resume failed on this device"
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "Resume not supported on this device"
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.heightIn(ButtonDefaults.LargeContainerHeight).fillMaxWidth(if (!isRecording) 0.6f else 1f),
                                    contentPadding = if (!isRecording)  ButtonDefaults.contentPaddingFor(ButtonDefaults.LargeContainerHeight) else PaddingValues(0.dp),

                                    shapes = ToggleButtonDefaults.shapes(),
                                ) {
                                    val iconRes = when {
                                        !isRecording -> R.drawable.fiber_manual_record_24px
                                        isPaused -> R.drawable.play_arrow_24px
                                        else -> R.drawable.pause_24px
                                    }

                                    val contentColor = if(isRecording && !isPaused) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimaryContainer

                                    Symbol(iconRes, size = 28.dp, color = contentColor,)
                                    Spacer(Modifier.width(5.dp))
                                    Text(buttonLabel, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                    }

                    val stopVisible by remember { derivedStateOf { isRecording } }

                    val stopAlpha by animateFloatAsState(if (stopVisible) 1f else 0f)
                    val stopWeight by animateFloatAsState(if (stopVisible) 1f else 0.001f)

                    Box(
                        modifier = Modifier
                            .weight(stopWeight)
                            .alpha(stopAlpha)
                    ) {
                                Button(

                                    onClick = {
                                    stopRecording(recorderRef, outputFileRef) { path ->
                                        isRecording = false
                                        isPaused = false
                                        amplitudes.clear()
                                        elapsedSeconds = 0L
                                        coroutineScope.launch {
                                            if (path != null) snackbarHostState.showSnackbar("Saved: $path")
                                            else snackbarHostState.showSnackbar("Save failed")
                                        }
                                    }
                                },
                                    modifier = Modifier.fillMaxWidth().heightIn(ButtonDefaults.LargeContainerHeight),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )

                                ) {
                                    Symbol(R.drawable.stop_24px, size = 28.dp, color = MaterialTheme.colorScheme.onErrorContainer)
                                    Spacer(Modifier.width(5.dp))
                                    Text("Stop", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                    }

                }

            }
                    Spacer(modifier = Modifier.height(16.dp))

//                Button(onClick = { onDone() }) {
//                    Text("Done")
                }
}
}

@Composable
private fun Waveform(amplitudes: List<Float>) {
    val display = remember { mutableStateListOf<Float>() }
    val colorBar = MaterialTheme.colorScheme.primary

    LaunchedEffect(amplitudes.size) {
        if (display.size < amplitudes.size) {
            repeat(amplitudes.size - display.size) { display.add(0f) }
        } else if (display.size > amplitudes.size) {
            for (i in display.size - 1 downTo amplitudes.size) display.removeAt(i)
        }
    }

    LaunchedEffect(amplitudes) {
        val frameDelay = 16L // 60 fps
        val followFactor = 0.15f

        while (isActive) {
            val target = amplitudes
            if (display.size < target.size) {
                repeat(target.size - display.size) { display.add(0f) }
            }

            for (i in target.indices) {
                val curr = display.getOrNull(i) ?: 0f
                val next = curr * (1 - followFactor) + target[i] * followFactor
                if (i < display.size) display[i] = next else display.add(next)
            }

            delay(frameDelay)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val count = display.size.coerceAtLeast(1)
        val barWidth = (w / count).coerceAtLeast(2f)
        val gap = (barWidth * 0.2f).coerceAtMost(6f)
        val actualBar = barWidth - gap

        for (i in 0 until count) {
            val amp = display.getOrNull(i) ?: 0f
            val scale = 3f
            val barHeight = (amp * scale).coerceIn(0f, 1f) * h

            val left = i * barWidth + gap / 2f
            val top = (h - barHeight) / 2f
            drawRoundRect(
                color = colorBar,
                topLeft = Offset(left, top),
                size = Size(actualBar, barHeight),
                cornerRadius = CornerRadius(x = actualBar / 2f, y = actualBar / 2f)
            )
        }
    }
}

private fun startRecording(
    context: Context,
    recorderRef: MutableState<MediaRecorder?>,
    outputFileRef: MutableState<File?>,
    callback: (Boolean) -> Unit,
    namePrefix: String = "REC"
) {
    try {
        val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
        val count = (dir.listFiles()?.size ?: 0) + 1
        val fileName =  "${namePrefix}_${String.format(Locale.getDefault(),"%03d", count)}.m4a"
        val file = File(dir, fileName)


        mainTitle = "${namePrefix}_${String.format(Locale.getDefault(),"%03d", count)}"


        val recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(128000)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        try {
            val amp = recorder.maxAmplitude
        } catch (e: Exception) {
            Log.w("RecordingScreen", "prime amp after start failed", e)
        }

        recorderRef.value = recorder
        outputFileRef.value = file
        callback(true)
    } catch (e: Exception) {
        Log.e("RecordingScreen", "startRecording error", e)
        try { recorderRef.value?.release() } catch (_: Exception) {}
        recorderRef.value = null
        outputFileRef.value = null
        callback(false)
    }
}

private fun stopRecording(
    recorderRef: MutableState<MediaRecorder?>,
    outputFileRef: MutableState<File?>,
    callback: (String?) -> Unit
) {
    val recorder = recorderRef.value
    val outFile = outputFileRef.value
    try {
        recorder?.let {
            try { it.stop() } catch (e: Exception) { Log.w("RecordingScreen", "stop() threw", e) }
            try { it.reset() } catch (_: Exception) {}
            try { it.release() } catch (_: Exception) {}
        }
    } catch (e: Exception) {
        Log.w("RecordingScreen", "error stopping/releasing", e)
    } finally {
        recorderRef.value = null
    }
    callback(outFile?.absolutePath)
}

private fun formatElapsed(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}
