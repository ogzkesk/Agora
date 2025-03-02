package com.ogzkesk.agora.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ogzkesk.agora.navigation.MainNavHost
import com.ogzkesk.agora.service.LocalRecordingService
import com.ogzkesk.agora.ui.theme.AgoraTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgoraTheme {
                MainNavHost()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalRecordingService.start(this)
    }
}