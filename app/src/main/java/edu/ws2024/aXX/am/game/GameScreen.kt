
package edu.ws2024.aXX.am.game

import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.zIndex
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import edu.ws2024.aXX.am.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

// px → dp helper
fun Float.toDp(): Dp = (this / 3.5f).dp

// function to calculate y along slope line
fun slopeTopY(x: Float, width: Float, height: Float): Float {
    return (height * 0.6f) + ((height * 0.4f) / width) * x
}

// Snow particle data class
data class SnowParticle(
    var x: Float,
    var y: Float,
    var speed: Float,
    var size: Float,
    var opacity: Float = 1f
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GameScreen(navController: NavController, playerName: String) {
    val viewModel: GameViewModel = viewModel()

    val context = LocalContext.current

    val vibrator = context.getSystemService(Vibrator::class.java)


    var showGameOverDialog by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    // Background scroll
    var backgroundOffset by remember { mutableStateOf(0f) }

    // Obstacles / coins
    var obstaclePositions by remember {
        mutableStateOf(List(1) { Offset((screenWidth + it * 600).toFloat(), 0f) })
    }
    var coinPositions by remember {
        mutableStateOf(List(3) { Offset((screenWidth + 200 + it * 400).toFloat(), 0f) })
    }

    // Jump
    var skierJump by remember { mutableStateOf(0f) }

    // Animation states
    var animationTime by remember { mutableStateOf(0f) }

    // Snow effects
    var snowParticles by remember {
        mutableStateOf(
            List(15) {
                SnowParticle(
                    x = Random.nextFloat() * screenWidth * 2,
                    y = Random.nextFloat() * screenHeight,
                    speed = Random.nextFloat() * 3f + 1f,
                    size = Random.nextFloat() * 4f + 2f
                )
            }
        )
    }

    // Media
    var bgmPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var jumpPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var coinPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var gameOverPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    var duration by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.startGame(context, playerName)
        vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))

        bgmPlayer = MediaPlayer.create(context, R.raw.bgm).apply {
            isLooping = true
            start()
        }
        jumpPlayer = MediaPlayer.create(context, R.raw.jump)
        coinPlayer = MediaPlayer.create(context, R.raw.coin)
        gameOverPlayer = MediaPlayer.create(context, R.raw.game_over)

        viewModel.coinsCount = 10
        val skierX = 100f

        // Timer coroutine (only when running)
        launch {
            while (true) {
                delay(1000)
                if (viewModel.gameState == GameState.RUNNING && !showGameOverDialog) {
                    duration++
                    viewModel.duration = duration.toLong()
                }
            }
        }

        while (true) {
            delay(25)

            // Only freeze background/obstacles/coins if game over

            if (!showGameOverDialog) {
                if (viewModel.gameState != GameState.RUNNING || showGameOverDialog) {
                    continue
                }

                // === Normal game updates (only runs when RUNNING) ===
                backgroundOffset -= 4f
                if (backgroundOffset < -screenWidth) backgroundOffset = 0f

                animationTime += 0.16f
                // FIXED: Calculate skier position including jump
                val skierYCurrent = slopeTopY(skierX, screenWidth.toFloat(), screenHeight.toFloat()) - skierJump - 60f

                // Obstacles - FIXED collision detection for all jump phases
                obstaclePositions = obstaclePositions.map { pos ->
                    val newX = pos.x - 6f
                    val newY = slopeTopY(newX, screenWidth.toFloat(), screenHeight.toFloat())

                    // FIXED: Check collision with skier's actual current position
                    val dx = kotlin.math.abs(newX - skierX)

                    // FIXED: Check collision at multiple points of skier body during jump/fall
                    val skierTopY = skierYCurrent
                    val skierCenterY = skierYCurrent + 60f
                    val skierBottomY = skierYCurrent + 120f

                    val dyTop = kotlin.math.abs(newY - skierTopY)
                    val dyCenter = kotlin.math.abs(newY - skierCenterY)
                    val dyBottom = kotlin.math.abs(newY - skierBottomY)

                    // FIXED: Collision if obstacle hits ANY part of skier during any jump phase
                    val collision = dx < 20 && (dyTop < 20 || dyCenter < 10 || dyBottom < 40)

                    if (collision && !showGameOverDialog) {
                        println("COLLISION DETECTED!")
                        println("Jump height: $skierJump")
                        println("Obstacle: x=$newX, y=$newY")
                        println("Skier: x=$skierX, top=$skierTopY, center=$skierCenterY, bottom=$skierBottomY")
                        println("Distance: dx=$dx, dyTop=$dyTop, dyCenter=$dyCenter, dyBottom=$dyBottom")

                        gameOverPlayer?.let { if (it.isPlaying) it.seekTo(0) else it.start() }
                        vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                        showGameOverDialog = true
                        bgmPlayer?.pause()
                    }

                    if (newX < -120f) {
                        Offset((screenWidth + 100).toFloat(), slopeTopY((screenWidth + 200).toFloat(), screenWidth.toFloat(), screenHeight.toFloat()))
                    } else Offset(newX, newY)
                }

                // Coins
                coinPositions = coinPositions.map { pos ->
                    val newX = pos.x - 8f
                    val newY = slopeTopY(newX, screenWidth.toFloat(), screenHeight.toFloat()) - 50f

                    val dx = kotlin.math.abs(newX - skierX)
                    val dy = kotlin.math.abs(newY - skierYCurrent)
                    if (dx < 40 && dy < 40) {
                        coinPlayer?.let { if (it.isPlaying) it.seekTo(0) else it.start() }
                        viewModel.incrementCoins()
                        Offset(
                            (screenWidth + 200).toFloat(),
                            slopeTopY((screenWidth + 200).toFloat(), screenWidth.toFloat(), screenHeight.toFloat()) - 50f
                        )
                    } else {
                        if (newX < -100f) {
                            Offset(
                                (screenWidth + 200).toFloat(),
                                slopeTopY((screenWidth + 200).toFloat(), screenWidth.toFloat(), screenHeight.toFloat()) - 50f
                            )
                        } else Offset(newX, newY)
                    }
                }
            }

            // ✅ Jump keeps running even after Game Over
            if (skierJump > 0f) {
                skierJump -= 10f   // slower decay
                if (skierJump < 0f) skierJump = 0f
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            bgmPlayer?.release()
            jumpPlayer?.release()
            coinPlayer?.release()
            gameOverPlayer?.release()
        }
    }

    // ===== Root layout =====
    Box(Modifier.fillMaxSize()) {
        if (viewModel.gameState == GameState.PAUSED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // dim background
                    .zIndex(15f), // keep it above game content but below dialog
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Game suspended…",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // === Game content area (full screen background) ===
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Background mountains (bg.png)
            Image(
                painter = painterResource(id = R.drawable.bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrolling trees layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {


                // Trees scrolling background
                Image(
                    painter = painterResource(id = R.drawable.trees),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top=300.dp)
                        .padding(top=200.dp)
                        .offset(x = backgroundOffset.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.trees),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top=300.dp)
                        .offset(x = (backgroundOffset + screenWidth).dp)
                )
            }

            // White slope
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val w = size.width
                val h = size.height
                drawPath(
                    path = Path().apply {
                        moveTo(0f, h * 0.6f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    },
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFEEEEEE), Color(0xFFCCCCCC))
                    )
                )
            }
        }


            // Obstacles
            obstaclePositions.forEach { pos ->
                Image(
                    painter = painterResource(id = R.drawable.obstacle),
                    contentDescription = "Obstacle",
                    modifier = Modifier
                        .size(50.dp)
                        .absoluteOffset(x = pos.x.dp, y = pos.y.dp)
                )


            // Coins with subtle floating animation
            coinPositions.forEachIndexed { index, pos ->
                val floatOffset = sin(animationTime * 2f + index) * 2f
                Image(
                    painter = painterResource(id = R.drawable.coin),
                    contentDescription = "Coin",
                    modifier = Modifier
                        .size(30.dp)
                        .absoluteOffset(x = pos.x.dp, y = (pos.y + floatOffset).dp)
                )
            }

            // Original Skier image (unchanged)
            val skierX = 50f
            val skierYCurrent =
                slopeTopY(skierX, screenWidth.toFloat(), screenHeight.toFloat()) - skierJump - 60f

            Image(
                painter = painterResource(id = R.drawable.skier),
                contentDescription = "Skier",
                modifier = Modifier
                    .size(120.dp)
                    .absoluteOffset(x = skierX.dp, y = skierYCurrent.dp)
                    .clickable {
                        if (skierJump == 0f) {
                            skierJump = 200f
                            jumpPlayer?.let { if (it.isPlaying) it.seekTo(0) else it.start() }
                        }
                    }
            )
        }


        // Game Over dialog
        if (showGameOverDialog) {
            AlertDialog(
                modifier = Modifier.zIndex(20f),
                onDismissRequest = { showGameOverDialog = false },
                title = { Text("Game Over") },
                text = {
                    Column {
                        Text("Player: $playerName")

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.coin),
                                contentDescription = "Coins",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )
                            Text("${viewModel.coinsCount}")
                        }

                        Text("Time: ${viewModel.duration}s")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.restartGame(context, playerName)

                        // reset local game state
                        showGameOverDialog = false
                        skierJump = 0f
                        duration = 0
                        animationTime = 0f
                        backgroundOffset = 0f
                        obstaclePositions = List(1) { Offset((screenWidth + it * 600).toFloat(), 0f) }
                        coinPositions = List(3) { Offset((screenWidth + 200 + it * 400).toFloat(), 0f) }

                        // Reset snow particles
                        snowParticles = List(15) {
                            SnowParticle(
                                x = Random.nextFloat() * screenWidth * 2,
                                y = Random.nextFloat() * screenHeight,
                                speed = Random.nextFloat() * 3f + 1f,
                                size = Random.nextFloat() * 4f + 2f
                            )
                        }

                        bgmPlayer?.seekTo(0)
                        bgmPlayer?.start()
                    }) {
                        Text("Restart")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.cleanup()
                        navController.navigate(
                            "rankings/$playerName?coins=${viewModel.coinsCount}&duration=${viewModel.duration}"
                        )
                    }) {
                        Text("Go To Rankings")
                    }
                },
                containerColor = Color.White
            )
        }




    }
        // === Top HEADER - Floating ===
        Box(

            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 700.dp)
                .height(80.dp)
                .background(Color.Transparent)

                .padding(8.dp)
        ) {
            // Pause andd play button on the left
            IconButton(
                onClick = {
                    if (viewModel.gameState == GameState.RUNNING) {
                        viewModel.gameState = GameState.PAUSED
                        bgmPlayer?.pause()
                    } else {
                        viewModel.gameState = GameState.RUNNING
                        bgmPlayer?.start()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0x66000000),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .align(Alignment.CenterStart) //
            ) {
                Icon(
                    imageVector = if (viewModel.gameState == GameState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = "Pause/Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Center info (Player name)
            Text(
                text = playerName,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.Center) //
                    .background(
                        color = Color(0x66000000),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Right side info (Coins + Time)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.align(Alignment.CenterEnd) //
            ) {
                // Coins counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = Color(0x88FFA500),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.coin),
                        contentDescription = "Coins",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = "${viewModel.coinsCount}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Time counter
                Text(
                    text = "${viewModel.duration}s",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            color = Color(0x66000000),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }


}