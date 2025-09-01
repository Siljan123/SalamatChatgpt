package edu.ws2024.aXX.am

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.ws2024.aXX.am.game.GameScreen
import edu.ws2024.aXX.am.ui.theme.HomeScreen
import edu.ws2024.aXX.am.ui.theme.RankingsScreen
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
                        composable("rankings/{highlightId}") { backStackEntry ->
                            val highlightId = backStackEntry.arguments?.getString("highlightId")?.toLongOrNull()
                            RankingsScreen(navController, highlightId)
                        }
                        composable("settings") { SettingsScreen(navController) }
                    }
                }
            }
        }
    }
}