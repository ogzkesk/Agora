package com.ogzkesk.agora.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ogzkesk.agora.ui.call.VoiceCallScreen
import com.ogzkesk.agora.ui.call.VoiceCallViewModel
import com.ogzkesk.agora.ui.main.MainScreen
import com.ogzkesk.agora.ui.main.MainViewModel

@Composable
fun MainNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = MainScreenRoute
    ) {
        composable<MainScreenRoute> {
            val viewModel: MainViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            MainScreen(
                navController = navController,
                state = state,
                onUiEvent = viewModel::onUiEvent
            )
        }
        composable<VoiceCallScreenRoute> {
            val viewModel: VoiceCallViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            VoiceCallScreen(
                navController = navController,
                state = state,
                onUiEvent = viewModel::onUiEvent
            )
        }
    }
}