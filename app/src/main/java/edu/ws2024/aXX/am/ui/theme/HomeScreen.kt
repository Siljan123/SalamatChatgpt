package edu.ws2024.aXX.am.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import edu.ws2024.aXX.am.R
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import edu.ws2024.aXX.am.game.GameViewModel

@SuppressLint("RememberedMutableState")
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: GameViewModel = viewModel()
    val playerName = remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6AB7FF), Color.White)
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            // Title
            Text(
                text = "Go Skiing",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Player name input
            OutlinedTextField(
                value = playerName.value,
                onValueChange = { playerName.value = it },
                label = { Text("Player name", color = Color.Black, fontWeight = FontWeight.Bold) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    cursorColor = Color.Black
                ),
                shape = RectangleShape
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Start Game button
            Button(
                onClick = {
                    if (playerName.value.isBlank()) {
                        showError = true
                    } else {
                        navController.navigate("game/${playerName.value}")
                    }
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF90CAF9),
                    contentColor = Color.Black
                ),
                shape = RectangleShape
            ) {
                Text("Start Game", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rankings button
            Button(
                onClick = {
                    viewModel.cleanup()
                    navController.navigate(
                        "rankings/$playerName?coins=${viewModel.coinsCount}&duration=${viewModel.duration}"
                    )
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF90CAF9),
                    contentColor = Color.Black
                ),
                shape = RectangleShape
            ) {
                Text("Rankings", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings button
            Button(
                onClick = { navController.navigate("settings") },
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF90CAF9),
                    contentColor = Color.Black
                ),
                shape = RectangleShape
            ) {
                Text("Setting", fontWeight = FontWeight.Bold)
            }
        }

        // Error dialog
        if (showError) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showError = false },
                title = { Text("Invalid") },
                text = { Text("Please enter a player name") },
                confirmButton = {
                    Button(onClick = { showError = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}