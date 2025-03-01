package com.ogzkesk.agora.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ogzkesk.agora.util.showToast

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resultLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it.values.any { false }) {
            context.showToast("Please grant all permissions")
        }
    }

    LaunchedEffect(Unit) {
        resultLauncher.launch(getRequiredPermissions())
    }

    Scaffold { paddingValues ->
        if (state.activeCall == null) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (checkRequiredPermissions(context)) {
                            viewModel.startVoiceCalling()
                        }
                    },
                    modifier = Modifier
                        .padding(12.dp)
                ) {
                    Text("Start Voice Calling")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "ChannelId: ${state.activeCall?.channelId}\n" +
                            "ChannelName: ${state.activeCall?.channelName}"
                )
                Button(
                    onClick = viewModel::leaveVoiceCalling,
                    modifier = Modifier
                        .padding(12.dp)
                ) {
                    Text("Leave Voice Calling")
                }

                Card(modifier = Modifier.padding(12.dp)) {
                    Text("Remote Users")
                    state.activeCall?.remoteUsers?.forEach { user ->
                        Button(
                            onClick = {
                                if (user.isMuted) {
                                    viewModel.toggleRemoteMute(user.id, false)
                                } else {
                                    viewModel.toggleRemoteMute(user.id, true)
                                }
                            },
                            modifier = Modifier
                                .padding(12.dp),
                        ) {
                            if (user.isMuted) Text("Un-Mute User")
                            else Text("Mute User")
                        }
                    }
                }

                Button(
                    onClick = {
                        if (state.isLocalMuted) {
                            viewModel.toggleMute(false)
                        } else {
                            viewModel.toggleMute(true)
                        }
                    },
                    modifier = Modifier
                        .padding(12.dp),
                ) {
                    if (state.isLocalMuted) Text("Un-Mute Local") else Text("Mute Local")
                }
            }
        }
    }
}

fun checkRequiredPermissions(context: Context): Boolean {
    return !getRequiredPermissions().any {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }
}

fun getRequiredPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(Manifest.permission.RECORD_AUDIO)
    }
}