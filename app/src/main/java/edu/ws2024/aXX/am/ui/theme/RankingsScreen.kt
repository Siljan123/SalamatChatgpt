package edu.ws2024.aXX.am.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.ws2024.aXX.am.data.GameRecord
import edu.ws2024.aXX.am.data.RankingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun RankingsScreen(navController: NavController) {
    val rankings = remember { mutableStateOf<List<GameRecord>>(emptyList()) }
    val context = LocalContext.current // Get the context here

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            rankings.value = RankingsManager.getRankingsList(context) // Use the context
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rankings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (rankings.value.isEmpty()) {
                Text(
                    "No Ranking",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    fontSize = 24.sp
                )
            } else {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rank", fontWeight = FontWeight.Bold)
                    Text("Player", fontWeight = FontWeight.Bold)
                    Text("Coins", fontWeight = FontWeight.Bold)
                    Text("Time", fontWeight = FontWeight.Bold)
                }

                // List
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(rankings.value) { index, record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text((index + 1).toString())
                            Text(record.playerName)
                            Text(record.coins.toString())
                            Text("${record.duration}s")
                        }
                    }
                }
            }
        }
    }
}