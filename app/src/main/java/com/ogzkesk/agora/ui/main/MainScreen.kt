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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ogzkesk.agora.navigation.VoiceCallScreenRoute
import com.ogzkesk.agora.ui.theme.AgoraTheme
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navController: NavHostController,
    state: MainScreenState,
    onUiEvent: (MainScreenEvent) -> Unit
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

    LaunchedEffect(Unit) {
        resultLauncher.launch(getRequiredPermissions())
    }

    LaunchedEffect(state.activeCall) {
        if (state.activeCall != null) navController.navigate(VoiceCallScreenRoute)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        }
    ) { paddingValues ->
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
                        onUiEvent(MainScreenEvent.StartVoiceCalling)
                    } else {
                        coroutineScope.launch {
                            snackBarHostState.showSnackbar("Required permissions not granted")
                            resultLauncher.launch(getRequiredPermissions())
                        }
                    }
                },
                modifier = Modifier
                    .padding(12.dp)
            ) {
                Text("Start Voice Calling")
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

@Preview
@Composable
fun MainScreenPreview() {
    AgoraTheme {
        MainScreen(
            navController = rememberNavController(),
            state = MainScreenState(),
            onUiEvent = {}
        )
    }
}