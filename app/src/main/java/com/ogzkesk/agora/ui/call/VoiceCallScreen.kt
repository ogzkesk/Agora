package com.ogzkesk.agora.ui.call

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
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
    onUiEvent: (VoiceCallScreenEvent) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.voiceCall) {
        if (state.voiceCall == null) navController.popBackStack()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val serviceIntent = Intent(context, LocalRecordingService::class.java)
                context.startForegroundService(serviceIntent)
            }
            if(event == Lifecycle.Event.ON_RESUME){
                val serviceIntent = Intent(context, LocalRecordingService::class.java)
                context.stopService(serviceIntent)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
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
                                onUiEvent(VoiceCallScreenEvent.EndCall)
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
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
                        onClick = { onUiEvent(VoiceCallScreenEvent.EndCall) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null
                        )
                    }
                    FilledTonalIconButton(
                        onClick = {
                            onUiEvent(VoiceCallScreenEvent.ToggleMuteLocal(!state.isLocalMuted))
                        },
                        modifier = Modifier.padding(horizontal = 12.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (state.isLocalMuted) Color.Red else IconButtonDefaults.filledTonalIconButtonColors().containerColor,
                            contentColor = if (state.isLocalMuted) Color.White else IconButtonDefaults.filledTonalIconButtonColors().contentColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null
                        )
                    }

                    Slider(
                        value = state.localVolume.toFloat(),
                        valueRange = 0f..100f,
                        onValueChange = {
                            onUiEvent(VoiceCallScreenEvent.LocalVolumeChange(it.toInt()))
                        },
                        modifier = Modifier.width(120.dp)
                    )
                    Text(
                        "${state.localVolume}",
                        modifier = Modifier.padding(start = 8.dp)
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
                    onUiEvent = onUiEvent
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
                        imageVector = Icons.Default.Call,
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
                isLocalMuted = true,
                voiceCall = VoiceCall(
                    "test-channel",
                    0,
                    listOf(User(0, true, 100))
                )
            )
        ) {}
    }
}