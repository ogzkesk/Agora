package com.ogzkesk.agora.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CallBottomBar(
    modifier: Modifier = Modifier,
    isSpeakerActive: Boolean,
    isLocalMuted: Boolean,
    localVolume: Int,
    onEndCallClicked: () -> Unit,
    onToggleCommunicationMode: (isSpeakerActive: Boolean) -> Unit,
    onToggleLocalMute: (isMuted: Boolean) -> Unit,
    onLocalVolumeChanged: (volume: Int) -> Unit
) {
    BottomAppBar(
        modifier = modifier
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            FilledTonalIconButton(
                onClick = onEndCallClicked,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = null
                )
            }

            FilledTonalIconButton(
                onClick = {
                    onToggleCommunicationMode(isSpeakerActive)

                },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isSpeakerActive) IconButtonDefaults.filledTonalIconButtonColors().containerColor else Color.Red,
                    contentColor = if (isSpeakerActive) IconButtonDefaults.filledTonalIconButtonColors().contentColor else Color.White
                )
            ) {
                Icon(
                    imageVector = if (isSpeakerActive)
                        Icons.AutoMirrored.Filled.VolumeOff
                    else
                        Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null
                )
            }

            MicIconButton(
                isMuted = isLocalMuted,
                onClick = {
                    onToggleLocalMute(!it)
                }
            )

            Slider(
                value = localVolume.toFloat(),
                valueRange = 0f..100f,
                onValueChange = {
                    onLocalVolumeChanged(it.toInt())
                },
                modifier = Modifier.width(120.dp)
            )
        }
    }
}
