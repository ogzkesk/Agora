package com.ogzkesk.agora.ui.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ogzkesk.agora.ui.theme.AgoraTheme

@Composable
fun VideoCallScreen(
    navController: NavHostController,
    state: VideoCallScreenState,
    onEvent: (VideoCallScreenEvent) -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

        }
    }
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