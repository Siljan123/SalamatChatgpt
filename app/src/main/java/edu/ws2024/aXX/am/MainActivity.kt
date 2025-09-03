package edu.ws2024.aXX.am

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.ws2024.aXX.am.game.GameScreen
import edu.ws2024.aXX.am.ui.theme.RankingsScreen
import edu.ws2024.aXX.am.ui.theme.HomeScreen

import edu.ws2024.aXX.am.ui.theme.SettingsScreen
import edu.ws2024.aXX.am.ui.theme.GoSkiingTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoSkiingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),

                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") { HomeScreen(navController) }
                        composable("game/{playerName}") { backStackEntry ->
                            val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
                            GameScreen(navController, playerName)
                        }
                        composable("rankings/{playerName}?coins={coins}&duration={duration}") { backStackEntry ->
                            val playerName = backStackEntry.arguments?.getString("playerName") ?: "Player"
                            val coins = backStackEntry.arguments?.getString("coins")?.toIntOrNull() ?: 0
                            val duration = backStackEntry.arguments?.getString("duration")?.toLongOrNull() ?: 0L

                            RankingsScreen(
                                navController = navController,
                                playerName = playerName,
                                currentCoins = coins,
                                currentDuration = duration
                            )
                        }
                        composable("settings") { SettingsScreen(navController) }
                    }
                }
            }
        }
    }
}