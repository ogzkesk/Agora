package com.ogzkesk.agora.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ogzkesk.agora.lib.enums.NoiseSuppressionMode

@Composable
fun NoiseSuppressionMenu(
    modifier: Modifier = Modifier,
    activeNoiseSuppressionMode: NoiseSuppressionMode?,
    expanded: Boolean,
    onExpandChanged: (Boolean) -> Unit,
    onMenuItemClicked: (isActive: Boolean, mode: NoiseSuppressionMode) -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = {
            onExpandChanged(true)
        }
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = null
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandChanged(false) }
    ) {
        NoiseSuppressionMode.entries.forEach { mode ->
            val isActive = activeNoiseSuppressionMode == mode
            DropdownMenuItem(
                text = {
                    Text(
                        "Noise suppression: ${mode.name}",
                        color = if (isActive) Color.Green else Color.Unspecified
                    )
                },
                onClick = {
                    onMenuItemClicked(isActive, mode)
                    onExpandChanged(false)
                }
            )
        }
    }
}
