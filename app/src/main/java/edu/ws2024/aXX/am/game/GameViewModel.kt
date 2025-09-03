package edu.ws2024.aXX.am.game

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import edu.ws2024.aXX.am.R
import edu.ws2024.aXX.am.data.GameRecord
import edu.ws2024.aXX.am.data.RankingsManager
import kotlinx.coroutines.*
import kotlin.random.Random

class GameViewModel : ViewModel() {

    private var playerName: String = "Player"
    var gameState by mutableStateOf(GameState.RUNNING)
    var coinsCount by mutableStateOf(10)
    var duration by mutableStateOf(0L)

    var isJumping by mutableStateOf(false)
    var jumpProgress by mutableStateOf(0f)

    var obstacles = mutableListOf<GameObject>()
    var coinObjects = mutableListOf<GameObject>()
    var skierPosition by mutableStateOf(Offset(300f, 600f))
    var groundLevel by mutableStateOf(600f)

    private var gameJob: Job? = null
    private var bgMusic: MediaPlayer? = null

    fun startGame(context: Context, playerName: String) {
        if (gameState == GameState.RUNNING) return

        this.playerName = playerName
        gameState = GameState.RUNNING
        coinsCount = 10
        duration = 0L
        isJumping = false
        jumpProgress = 0f
        obstacles.clear()
        coinObjects.clear()
        skierPosition = Offset(300f, groundLevel)

        gameJob = CoroutineScope(Dispatchers.Main).launch {
            while (gameState != GameState.GAME_OVER) {
                if (gameState == GameState.RUNNING) {
                    duration++
                    updateGameObjects()
                    updateJump()
                    checkCollisions(context)
                    generateObjects()
                }
                delay(1000L / 60L) // ~60 FPS
            }
        }

        try {
            bgMusic = MediaPlayer.create(context, R.raw.bgm)
            bgMusic?.isLooping = true
            bgMusic?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateGameObjects() {
        obstacles.forEach { it.x -= 5f }
        obstacles.removeAll { it.x + it.width < 0f }

        coinObjects.forEach { it.x -= 8f }
        coinObjects.removeAll { it.x + it.width < 0f }
    }

    private fun updateJump() {
        if (isJumping) {
            jumpProgress += 0.05f
            if (jumpProgress >= 1f) {
                isJumping = false
                jumpProgress = 0f
            }
            val jumpHeight = calculateJumpHeight(jumpProgress)
            skierPosition = Offset(300f, groundLevel - jumpHeight)
        } else {
            skierPosition = Offset(300f, groundLevel)
        }
    }

    private fun calculateJumpHeight(progress: Float): Float {
        return 150f * (4 * progress * (1 - progress))
    }

    private fun checkCollisions(context: Context) {
        val skierRect = Rect(
            skierPosition.x,
            skierPosition.y,
            skierPosition.x + 50f,
            skierPosition.y + 220f
        )

        for (obstacle in obstacles) {
            val obstacleRect = Rect(
                obstacle.x,
                obstacle.y,
                obstacle.x + obstacle.width,
                obstacle.y + obstacle.height
            )
            if (skierRect.overlaps(obstacleRect) && !isJumping) {
                gameState = GameState.GAME_OVER
                bgMusic?.stop()

                CoroutineScope(Dispatchers.IO).launch {
                     val record = GameRecord(
                        playerName = playerName,
                        coins = coinsCount,
                        duration = duration.toInt()
                    )
                    RankingsManager.saveRanking(context, record)
                }
                return
            }
        }

        val iterator = coinObjects.iterator()
        while (iterator.hasNext()) {
            val coin = iterator.next()
            val coinRect = Rect(
                coin.x,
                coin.y,
                coin.x + coin.width,
                coin.y + coin.height
            )
            if (skierRect.overlaps(coinRect)) {
                coinsCount++
                iterator.remove()
            }
        }
    }

    private fun generateObjects() {
        if (Random.nextInt(100) < 3 && obstacles.size < 3) {
            obstacles.add(GameObject(1000f, groundLevel - 80f, 80f, 80f, ObjectType.OBSTACLE))
        }
        if (Random.nextInt(100) < 5 && coinObjects.size < 5) {
            coinObjects.add(GameObject(1000f, groundLevel - 150f, 60f, 60f, ObjectType.COIN))
        }
    }

    fun jump() {
        if (!isJumping && gameState == GameState.RUNNING) {
            isJumping = true
            jumpProgress = 0f
            // TODO: play jump sound
        }
    }

    fun togglePause() {
        gameState = if (gameState == GameState.PAUSED) {
            bgMusic?.start()
            GameState.RUNNING
        } else {
            bgMusic?.pause()
            GameState.PAUSED
        }
    }

    fun restartGame(context: Context, playerName: String) {
        gameJob?.cancel()
        bgMusic?.stop()
        startGame(context, playerName)
    }

    fun cleanup() {
        gameJob?.cancel()
        bgMusic?.release()
    }
    fun incrementCoins() { coinsCount++ }
}

// GameObject and ObjectType remain unchanged
data class GameObject(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val type: ObjectType
)

enum class ObjectType {
    OBSTACLE,
    COIN
}