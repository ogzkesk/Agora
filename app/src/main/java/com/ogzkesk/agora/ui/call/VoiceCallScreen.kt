package com.ogzkesk.agora.ui.call

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ogzkesk.agora.model.User

@Composable
fun CallScreen(
    navController: NavHostController,
    state: VoiceCallScreenState,
    onUiEvent: (VoiceCallScreenEvent) -> Unit
) {
    LaunchedEffect(state.activeCall) {
        if (state.activeCall == null) navController.popBackStack()
    }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Slider(
                            value = state.localVolume.toFloat(),
                            valueRange = 0f..100f,
                            steps = 100,
                            onValueChange = {
                                onUiEvent(VoiceCallScreenEvent.LocalVolumeChange(it.toInt()))
                            },
                            modifier = Modifier.width(120.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            state.activeCall?.remoteUsers?.forEach { user ->
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
        modifier = modifier.padding(12.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "UserId: ${user.id}",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = user.volume.toFloat(),
                    valueRange = 0f..100f,
                    steps = 100,
                    onValueChange = {
                        onUiEvent(
                            VoiceCallScreenEvent.RemoteVolumeChange(
                                user.id,
                                it.toInt()
                            )
                        )
                    },
                    modifier = Modifier.width(120.dp)
                )
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null
                )
            }
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
    }
}
