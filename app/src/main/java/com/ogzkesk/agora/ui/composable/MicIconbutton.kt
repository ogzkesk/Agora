package com.ogzkesk.agora.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MicIconButton(
    modifier: Modifier = Modifier,
    isMuted: Boolean,
    colors: IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = if (isMuted) Color.Red else IconButtonDefaults.filledTonalIconButtonColors().containerColor,
        contentColor = if (isMuted) Color.White else IconButtonDefaults.filledTonalIconButtonColors().contentColor
    ),
    onClick: (isMuted: Boolean) -> Unit
) {
    FilledTonalIconButton(
        modifier = modifier,
        colors = colors,
        onClick = {
            onClick(isMuted)
        }
    ) {
        Icon(
            imageVector = if (isMuted) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = null
        )
    }
}
