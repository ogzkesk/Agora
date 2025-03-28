package com.ogzkesk.agora.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ogzkesk.agora.lib.model.CallType
import com.ogzkesk.agora.navigation.VideoCallScreenRoute
import com.ogzkesk.agora.navigation.VoiceCallScreenRoute
import com.ogzkesk.agora.ui.theme.AgoraTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    state: MainScreenState,
    onEvent: (MainScreenEvent) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val resultLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it.values.any { false }) {
            coroutineScope.launch {
                snackBarHostState.showSnackbar("Required permissions not granted")
            }
        }
    }

    LaunchedEffect(state) {
        if (state.activeCall != null) {
            when (state.activeCall.callType) {
                is CallType.Voice -> navController.navigate(VoiceCallScreenRoute)
                is CallType.Camera -> navController.navigate(VideoCallScreenRoute)
            }
        }
        if (state.errorMsg != null) {
            snackBarHostState.showSnackbar(state.errorMsg, duration = SnackbarDuration.Short)
            onEvent(MainScreenEvent.ResetErrorState)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Main") },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = state.channelName,
                onValueChange = {
                    onEvent(MainScreenEvent.ChannelNameChangedEvent(it))
                },
                placeholder = {
                    Text("Enter channel name")
                },
                modifier = Modifier.padding(8.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Use temporary token")
                    Checkbox(
                        checked = state.useTemporaryToken,
                        onCheckedChange = {
                            onEvent(MainScreenEvent.ToggleTemporaryToken(it))
                        }
                    )
                }
                AnimatedVisibility(state.useTemporaryToken) {
                    TextField(
                        value = state.tempToken,
                        onValueChange = {
                            onEvent(MainScreenEvent.TempTokenChangedEvent(it))
                        },
                        placeholder = {
                            Text("Enter temporary token")
                        },
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Button(
                onClick = {
                    if (checkRequiredPermissions(context)) {
                        onEvent(MainScreenEvent.StartVoiceCalling)
                    } else {
                        resultLauncher.launch(getRequiredPermissions())
                    }
                }, modifier = Modifier.padding(8.dp)
            ) {
                Text("Start Voice Calling")
            }

            Button(
                onClick = {
                    if (checkRequiredPermissions(context)) {
                        onEvent(MainScreenEvent.StartVideoCalling)
                    } else {
                        resultLauncher.launch(getRequiredPermissions())
                    }
                }, modifier = Modifier.padding(8.dp)
            ) {
                Text("Start Video Calling")
            }
        }
    }

    if (state.isLoading) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3F))
                .clickable(false) {}, Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

private fun checkRequiredPermissions(context: Context): Boolean {
    return !getRequiredPermissions().any {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }
}

private fun getRequiredPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    AgoraTheme {
        MainScreen(
            navController = rememberNavController(),
            state = MainScreenState(),
            onEvent = {}
        )
    }
}
