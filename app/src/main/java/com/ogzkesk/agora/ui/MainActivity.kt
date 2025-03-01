package com.ogzkesk.agora.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ogzkesk.agora.ui.main.MainScreen
import com.ogzkesk.agora.ui.main.MainViewModel
import com.ogzkesk.agora.navigation.MainNavHost
import com.ogzkesk.agora.navigation.MainScreenRoute
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
}
