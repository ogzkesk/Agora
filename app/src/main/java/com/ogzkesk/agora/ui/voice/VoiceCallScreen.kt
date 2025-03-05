package com.ogzkesk.agora.ui.voice

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.eventFlow
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ogzkesk.agora.lib.enums.CommunicationMode
import com.ogzkesk.agora.lib.model.ActiveCall
import com.ogzkesk.agora.lib.model.User
import com.ogzkesk.agora.service.LocalRecordingService
import com.ogzkesk.agora.ui.composable.CallBottomBar
import com.ogzkesk.agora.ui.composable.MicIconButton
import com.ogzkesk.agora.ui.composable.NavigationIcon
import com.ogzkesk.agora.ui.composable.NoiseSuppressionMenu
import com.ogzkesk.agora.ui.theme.AgoraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCallScreen(
    navController: NavHostController,
    state: VoiceCallScreenState,
    onEvent: (VoiceCallScreenEvent) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var popup by remember { mutableStateOf(false) }

    BackHandler(true) {
        onEvent(VoiceCallScreenEvent.EndCall)
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
                title = {
                    Text(state.activeCall?.channelName.orEmpty())
                },
                navigationIcon = {
                    NavigationIcon {
                        onEvent(VoiceCallScreenEvent.EndCall)
                        navController.popBackStack()
                    }
                },
                actions = {
                    NoiseSuppressionMenu(
                        activeNoiseSuppressionMode = state.activeCall?.noiseSuppressionMode,
                        expanded = popup,
                        onExpandChanged = { popup = it },
                        onMenuItemClicked = { isActive, mode ->
                            onEvent(
                                VoiceCallScreenEvent.ToggleNoiseSuppressionMode(
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
                        VoiceCallScreenEvent.ToggleCommunicationMode(
                            if (isSpeakerActive) CommunicationMode.EARPIECE else CommunicationMode.SPEAKER
                        )
                    )
                },
                onLocalVolumeChanged = { onEvent(VoiceCallScreenEvent.LocalVolumeChange(it)) },
                onToggleLocalMute = { onEvent(VoiceCallScreenEvent.ToggleMuteLocal(it)) },
                onEndCallClicked = {
                    onEvent(VoiceCallScreenEvent.EndCall)
                    navController.popBackStack()
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            items(
                items = state.activeCall?.remoteUsers.orEmpty()
            ) { user ->
                RemoteUser(
                    user = user,
                    onUiEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun RemoteUser(
    modifier: Modifier = Modifier,
    user: User,
    onUiEvent: (VoiceCallScreenEvent) -> Unit
) {
    Card(
        modifier = modifier
            .padding(12.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "UserId: ${user.id}",
                    style = MaterialTheme.typography.bodyMedium
                )
                MicIconButton(
                    isMuted = user.isMuted,
                    onClick = {
                        onUiEvent(VoiceCallScreenEvent.ToggleMuteRemote(user.id, !user.isMuted))
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = user.volume.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = {
                        onUiEvent(
                            VoiceCallScreenEvent.RemoteVolumeChange(
                                user.id,
                                it.toInt()
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${user.volume}",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun VoiceCallScreenPreview() {
    AgoraTheme {
        VoiceCallScreen(
            rememberNavController(),
            VoiceCallScreenState(
                activeCall = ActiveCall.create("test-channel", 0)
            )
        ) {}
    }
}