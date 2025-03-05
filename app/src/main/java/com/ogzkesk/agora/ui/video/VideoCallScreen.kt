package com.ogzkesk.agora.ui.video

import android.view.SurfaceView
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.eventFlow
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ogzkesk.agora.service.LocalRecordingService
import com.ogzkesk.agora.ui.composable.NavigationIcon
import com.ogzkesk.agora.ui.composable.NoiseSuppressionMenu
import com.ogzkesk.agora.ui.theme.AgoraTheme

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
                    NavigationIcon {
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
            VideoCallView(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .size(100.dp, 180.dp)
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
    setZOrder: Boolean = true,
    onAttach: (SurfaceView) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = {
            SurfaceView(it)
                .apply {
                    layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setZOrderMediaOverlay(setZOrder)
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