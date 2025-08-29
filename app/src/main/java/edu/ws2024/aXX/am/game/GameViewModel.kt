package edu.ws2024.aXX.am.game

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.*
import edu.ws2024.aXX.am.R
import kotlin.random.Random

class GameViewModel {
    var gameState by mutableStateOf(GameState.RUNNING)
    var coinsCount by mutableStateOf(10)
    var duration by mutableStateOf(0L)

    var isJumping by mutableStateOf(false)
    var jumpProgress by mutableStateOf(0f)

    var obstacles = mutableListOf<GameObject>()
    var coinObjects = mutableListOf<GameObject>()
    var skierPosition by mutableStateOf(Offset(300f, 600f)) // Initial position

    private var gameJob: Job? = null
    private var bgMusic: MediaPlayer? = null
    var groundLevel by mutableStateOf(600f) // default slope ground

    fun startGame(context: Context, playerName: String) {
        if (gameState == GameState.RUNNING) return

        gameState = GameState.RUNNING
        coinsCount = 10
        duration = 0L
        isJumping = false
        jumpProgress = 0f
        obstacles.clear()
        coinObjects.clear()
        skierPosition = Offset(300f, groundLevel)

        // Timer
        gameJob = CoroutineScope(Dispatchers.Main).launch {
            while (gameState != GameState.GAME_OVER) {
                if (gameState == GameState.RUNNING) duration++
                delay(1000)
            }
        }

        // Game loop
        gameJob = CoroutineScope(Dispatchers.Main).launch {
            while (gameState != GameState.GAME_OVER) {
                if (gameState == GameState.RUNNING) {
                    updateGameObjects()
                    updateJump()
                    checkCollisions()
                    generateObjects()
                }
                delay(16) // ~60 FPS
            }
        }

        // Background music
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

    private fun checkCollisions() {
        val skierRect = Rect(
            left = skierPosition.x,
            top = skierPosition.y,
            right = skierPosition.x + 50f,
            bottom = skierPosition.y + 220f
        )

        // Obstacles
        for (obstacle in obstacles) {
            val obstacleRect = Rect(
                left = obstacle.x,
                top = obstacle.y,
                right = obstacle.x + obstacle.width,
                bottom = obstacle.y + obstacle.height
            )
            if (skierRect.overlaps(obstacleRect) && !isJumping) {
                gameState = GameState.GAME_OVER
                bgMusic?.stop()
                return
            }
        }

        // Coins
        val iterator = coinObjects.iterator()
        while (iterator.hasNext()) {
            val coin = iterator.next()
            val coinRect = Rect(
                left = coin.x,
                top = coin.y,
                right = coin.x + coin.width,
                bottom = coin.y + coin.height
            )
            if (skierRect.overlaps(coinRect)) {
                coinsCount++
                iterator.remove()
            }
        }
    }

    private fun generateObjects() {
        if (Random.nextInt(100) < 3 && obstacles.size < 3) {
            obstacles.add(
                GameObject(1000f, groundLevel - 80f, 80f, 80f, ObjectType.OBSTACLE)
            )
        }

        if (Random.nextInt(100) < 5 && coinObjects.size < 5) {
            coinObjects.add(
                GameObject(1000f, groundLevel - 150f, 60f, 60f, ObjectType.COIN)
            )
        }
    }

    fun jump() {
        if (!isJumping && gameState == GameState.RUNNING) {
            isJumping = true
            jumpProgress = 0f
            // TODO: play jump sound
        }
    }

    fun setCoins(value: Int) { coinsCount = value }
    fun setDuration(value: Int) { duration = value.toLong() }

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
    fun decrementCoins() { if (coinsCount > 0) coinsCount-- }
    fun resetCoins(value: Int = 10) { coinsCount = value }
}

// ✅ Improved GameObject
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
