package com.ogzkesk.agora.ui.voice

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ogzkesk.agora.audio.AudioController
import com.ogzkesk.agora.model.User
import com.ogzkesk.agora.model.VoiceCall
import com.ogzkesk.agora.service.LocalRecordingService
import com.ogzkesk.agora.ui.theme.AgoraTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCallScreen(
    navController: NavHostController,
    state: VoiceCallScreenState,
    onEvent: (VoiceCallScreenEvent) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var popup by remember { mutableStateOf(false) }

    BackHandler(true) {
        onEvent(VoiceCallScreenEvent.EndCall)
        navController.popBackStack()
    }

    LaunchedEffect(state.voiceCall) {
        if (state.voiceCall == null) navController.popBackStack()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                LocalRecordingService.start(context)
            }
            if (event == Lifecycle.Event.ON_RESUME) {
                LocalRecordingService.stop(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            LocalRecordingService.stop(context)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(state.voiceCall?.channelName.orEmpty())
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                onEvent(VoiceCallScreenEvent.EndCall)
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            popup = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = popup,
                        onDismissRequest = { popup = false }
                    ) {
                        repeat(5) {
                            DropdownMenuItem(
                                text = { Text("Test-$it") },
                                onClick = { }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilledTonalIconButton(
                        onClick = { onEvent(VoiceCallScreenEvent.EndCall) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null
                        )
                    }

                    FilledTonalIconButton(
                        onClick = {
                            onEvent(
                                VoiceCallScreenEvent.ToggleCommunicationMode(
                                    if (state.voiceCall?.communicationMode == AudioController.CommunicationMode.SPEAKER)
                                        AudioController.CommunicationMode.EARPIECE
                                    else
                                        AudioController.CommunicationMode.SPEAKER
                                )
                            )
                        },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    ) {
                        Icon(
                            imageVector = if (state.voiceCall?.communicationMode == AudioController.CommunicationMode.SPEAKER)
                                Icons.AutoMirrored.Filled.VolumeOff
                            else
                                Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null
                        )
                    }

                    FilledTonalIconButton(
                        onClick = {
                            onEvent(VoiceCallScreenEvent.ToggleMuteLocal(state.voiceCall?.isLocalMuted == false))
                        },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    ) {
                        Icon(
                            imageVector = if (state.voiceCall?.isLocalMuted == true) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = null
                        )
                    }

                    Slider(
                        value = state.voiceCall?.localVolume?.toFloat() ?: 100F,
                        valueRange = 0f..100f,
                        onValueChange = {
                            onEvent(VoiceCallScreenEvent.LocalVolumeChange(it.toInt()))
                        },
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            items(
                items = state.voiceCall?.remoteUsers.orEmpty()
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
fun RemoteUser(
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
                FilledTonalIconButton(
                    onClick = {
                        onUiEvent(VoiceCallScreenEvent.ToggleMuteRemote(user.id, !user.isMuted))
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (user.isMuted) Color.Red else IconButtonDefaults.filledTonalIconButtonColors().containerColor,
                        contentColor = if (user.isMuted) Color.White else IconButtonDefaults.filledTonalIconButtonColors().contentColor
                    )
                ) {
                    Icon(
                        imageVector = if (user.isMuted) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = null
                    )
                }
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
                voiceCall = VoiceCall.create("test-channel", 0)
            )
        ) {}
    }
}