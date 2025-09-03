package edu.ws2024.aXX.am.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import edu.ws2024.aXX.am.data.GameRecord
import edu.ws2024.aXX.am.data.RankingsManager
import edu.ws2024.aXX.am.game.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RankingEntry(
    val rank: Int,
    val playerName: String,
    val coins: String,
    val duration: String,
    val isCurrentPlayer: Boolean = false
)

@Composable
fun RankingsScreen(
    navController: NavController? = null,
    playerName: String,
    currentCoins: Int,
    currentDuration: Long
) {
    val viewModel: GameViewModel = viewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var allRankings by remember { mutableStateOf<List<GameRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load rankings when screen opens
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                allRankings = withContext(Dispatchers.IO) {
                    RankingsManager.loadRankings(context)
                }
                println("DEBUG: Loaded ${allRankings.size} records -> $allRankings")
            } catch (e: Exception) {
                println("Error loading rankings: ${e.message}")
                allRankings = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    // Process and format ranking data
    val formattedRankings = remember(allRankings, playerName, currentCoins, currentDuration) {
        if (allRankings.isEmpty()) {
            listOf(
                RankingEntry(
                    rank = 1,
                    playerName = playerName,
                    coins = currentCoins.toString(),
                    duration = "${currentDuration}s",
                    isCurrentPlayer = true
                )
            ) + (2..12).map {
                RankingEntry(it, "---", "---", "---")
            }
        } else {
            val sorted = allRankings.sortedWith(
                compareByDescending<GameRecord> { it.coins }
                    .thenBy { it.duration }
            )

            val withNumbers = sorted.mapIndexed { index, record ->
                RankingEntry(
                    rank = index + 1,
                    playerName = record.playerName,
                    coins = record.coins.toString(),
                    duration = "${record.duration}s",
                    isCurrentPlayer = record.playerName == playerName &&
                            record.coins == currentCoins &&
                            record.duration == currentDuration.toInt()
                )
            }

            val padded = withNumbers.toMutableList()
            while (padded.size < 12) {
                padded.add(
                    RankingEntry(
                        rank = padded.size + 1,
                        playerName = "---",
                        coins = "---",
                        duration = "---"
                    )
                )
            }
            padded.take(12)
        }
    }

    // ===== UI =====
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Back + Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { navController?.popBackStack() }) {
                Text("back", color = Color.Black, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        Text(
            text = "Rankings",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            textAlign = TextAlign.Center
        )

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Black)
            }
        } else {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ranking", Modifier.weight(1f))
                Text("player name", Modifier.weight(1.5f), textAlign = TextAlign.Center)
                Text("coin", Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("duration", Modifier.weight(1f), textAlign = TextAlign.End)
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(formattedRankings) { index, entry ->
                    RankingRow(entry = entry)
                    if (index < formattedRankings.size - 1) {
                        Divider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun RankingRow(entry: RankingEntry) {
    val textColor = if (entry.isCurrentPlayer) Color(0xFFD32F2F) else Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(entry.rank.toString(), Modifier.weight(1f), color = textColor)
        Text(entry.playerName, Modifier.weight(1.5f), color = textColor, textAlign = TextAlign.Center)
        Text(entry.coins, Modifier.weight(1f), color = textColor, textAlign = TextAlign.Center)
        Text(entry.duration, Modifier.weight(1f), color = textColor, textAlign = TextAlign.End)
    }
}