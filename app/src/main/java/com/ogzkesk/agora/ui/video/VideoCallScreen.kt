package com.ogzkesk.agora.ui.video

import android.graphics.Matrix
import android.view.SurfaceView
import android.view.TextureView
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.eventFlow
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ogzkesk.agora.lib.enums.CommunicationMode
import com.ogzkesk.agora.service.LocalRecordingService
import com.ogzkesk.agora.ui.composable.CallBottomBar
import com.ogzkesk.agora.ui.composable.NavigationIcon
import com.ogzkesk.agora.ui.composable.NoiseSuppressionMenu
import com.ogzkesk.agora.ui.theme.AgoraTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    navController: NavHostController,
    state: VideoCallScreenState,
    onEvent: (VideoCallScreenEvent) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var popup by remember { mutableStateOf(false) }

    BackHandler(true) {
        onEvent(VideoCallScreenEvent.EndCall)
        navController.popBackStack()
    }

    LaunchedEffect(state.activeCall) {
        if (state.activeCall == null) navController.popBackStack()
    }

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle
            .eventFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .collect {
                when (it) {
                    Lifecycle.Event.ON_RESUME -> LocalRecordingService.stop(context)
                    Lifecycle.Event.ON_PAUSE -> LocalRecordingService.start(context)
                    else -> {}
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    NavigationIcon(
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        onEvent(VideoCallScreenEvent.EndCall)
                        navController.popBackStack()
                    }
                },
                actions = {
                    NoiseSuppressionMenu(
                        activeNoiseSuppressionMode = state.activeCall?.noiseSuppressionMode,
                        expanded = popup,
                        onExpandChanged = {
                            popup = it
                        },
                        onMenuItemClicked = { isActive, mode ->
                            onEvent(
                                VideoCallScreenEvent.ToggleNoiseSuppressionMode(
                                    enabled = !isActive,
                                    mode = mode
                                )
                            )
                        }
                    )
                }
            )
        },
        bottomBar = {
            CallBottomBar(
                isSpeakerActive = state.activeCall?.communicationMode == CommunicationMode.SPEAKER,
                isLocalMuted = state.activeCall?.isLocalMuted == true,
                localVolume = state.activeCall?.localVolume ?: 100,
                onToggleCommunicationMode = { isSpeakerActive ->
                    onEvent(
                        VideoCallScreenEvent.ToggleCommunicationMode(
                            if (isSpeakerActive) CommunicationMode.EARPIECE else CommunicationMode.SPEAKER
                        )
                    )
                },
                onLocalVolumeChanged = { onEvent(VideoCallScreenEvent.LocalVolumeChange(it)) },
                onToggleLocalMute = { onEvent(VideoCallScreenEvent.ToggleMuteLocal(it)) },
                onEndCallClicked = {
                    onEvent(VideoCallScreenEvent.EndCall)
                    navController.popBackStack()
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Black),
        ) {
            state.activeCall?.let { call ->
                call.remoteUsers.firstOrNull()?.let { user ->
                    VideoCallView(
                        modifier = Modifier.fillMaxSize(),
                        onAttach = {
                            onEvent(VideoCallScreenEvent.RemoteViewAttached(user.id, it))
                        }
                    )
                }
            }

            val offsetX = remember { mutableFloatStateOf(0f) }
            val offsetY = remember { mutableFloatStateOf(0f) }

            VideoCallView(
                modifier = Modifier
                    .offset {
                        IntOffset(offsetX.floatValue.roundToInt(), offsetY.floatValue.roundToInt())
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX.floatValue += dragAmount.x
                            offsetY.floatValue += dragAmount.y
                        }
                    }
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .height(200.dp)
                    .width(140.dp)
                    .align(Alignment.TopEnd)
                    .border(1.dp, Color.Green, shape = RoundedCornerShape(8.dp)),
                onAttach = {
                    onEvent(VideoCallScreenEvent.LocalViewAttached(it))
                }
            )
        }
    }
}

@Composable
private fun VideoCallView(
    modifier: Modifier = Modifier,
    onAttach: (TextureView) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = {
            TextureView(it)
                .apply {
                    layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
                .also(onAttach)
        },
    )
}

@Preview
@Composable
private fun VideoCallScreenPreview() {
    AgoraTheme {
        VideoCallScreen(
            rememberNavController(),
            VideoCallScreenState()
        ) {}
    }
}