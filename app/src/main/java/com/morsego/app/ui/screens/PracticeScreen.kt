package com.morsego.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morsego.app.model.KeyerMode
import com.morsego.app.model.MorseDictionary
import com.morsego.app.ui.components.MorsePaddleControls
import com.morsego.app.ui.components.MorseSignalIndicator
import com.morsego.app.ui.theme.MorseAmber
import com.morsego.app.ui.theme.MorseCyan
import com.morsego.app.ui.theme.MorseGreen
import com.morsego.app.ui.theme.MorseRed
import com.morsego.app.ui.theme.RadioDark
import com.morsego.app.ui.theme.RadioSurface
import com.morsego.app.ui.theme.RadioSurfaceVariant
import com.morsego.app.ui.theme.TextPrimary
import com.morsego.app.ui.theme.TextSecondary
import com.morsego.app.viewmodel.MorseViewModel

@Composable
fun PracticeScreen(
    viewModel: MorseViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Escuta (Listening), 1: Transmissão (Sending)
    val isTonePlaying by viewModel.isTonePlaying.collectAsState()
    val isDitPressed by viewModel.inputManager.isDitPressed.collectAsState()
    val isDahPressed by viewModel.inputManager.isDahPressed.collectAsState()
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RadioDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Signal LED
            MorseSignalIndicator(isToneActive = isTonePlaying, label = "PRÁTICA CW")

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = RadioSurface,
                contentColor = MorseAmber,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MorseAmber
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hearing, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Treino de Escuta")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Treino de Envio")
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                ListeningQuizView(viewModel)
            } else {
                SendingDrillView(viewModel)
            }
        }

        // On-screen paddle controls for Transmit mode
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "PÁS DE MANIPULAÇÃO (OU HARDWARE CW OTG)",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )

            MorsePaddleControls(
                isDitPressed = isDitPressed,
                isDahPressed = isDahPressed,
                onDitPress = { viewModel.setTouchDit(it) },
                onDahPress = { viewModel.setTouchDah(it) },
                reversed = settings.reversePaddles,
                showStraightKeyOnly = settings.mode == KeyerMode.STRAIGHT_KEY
            )
        }
    }
}

@Composable
fun ListeningQuizView(viewModel: MorseViewModel) {
    val alphabet = remember { ('A'..'Z').map { it.toString() } }
    var targetChar by remember { mutableStateOf(alphabet.random()) }
    var options by remember { mutableStateOf(generateOptions(targetChar, alphabet)) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var scoreCorrect by remember { mutableIntStateOf(0) }
    var totalQuestions by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetChar) {
        viewModel.playCharacterSound(targetChar)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RadioSurface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "OUÇA E IDENTIFIQUE A LETRA",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Text(
                text = "Pontuação: $scoreCorrect / $totalQuestions",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MorseAmber
            )
        }

        Button(
            onClick = { viewModel.playCharacterSound(targetChar) },
            colors = ButtonDefaults.buttonColors(containerColor = RadioSurfaceVariant),
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.VolumeUp, contentDescription = "Ouvir", tint = MorseCyan, modifier = Modifier.size(36.dp))
        }

        Text(
            text = "Toque no altifalante para repetir o áudio",
            fontSize = 12.sp,
            color = TextSecondary
        )

        // 4 Multiple Choice Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.chunked(2).forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowOptions.forEach { opt ->
                        val isPicked = selectedAnswer == opt
                        val isTarget = opt == targetChar
                        val btnColor = when {
                            selectedAnswer == null -> RadioSurfaceVariant
                            isTarget -> MorseGreen.copy(alpha = 0.3f)
                            isPicked -> MorseRed.copy(alpha = 0.3f)
                            else -> RadioSurfaceVariant
                        }
                        val borderColor = when {
                            selectedAnswer == null -> RadioSurfaceVariant
                            isTarget -> MorseGreen
                            isPicked -> MorseRed
                            else -> RadioSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(btnColor)
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                .clickable(enabled = selectedAnswer == null) {
                                    selectedAnswer = opt
                                    totalQuestions++
                                    if (opt == targetChar) scoreCorrect++
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        if (selectedAnswer != null) {
            Button(
                onClick = {
                    targetChar = alphabet.random()
                    options = generateOptions(targetChar, alphabet)
                    selectedAnswer = null
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MorseAmber)
            ) {
                Text("Próxima Pergunta", color = RadioDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SendingDrillView(viewModel: MorseViewModel) {
    val promptChar by viewModel.currentPromptChar.collectAsState()
    val promptMorse = MorseDictionary.getMorse(promptChar) ?: ""
    val currentPattern by viewModel.decoder.currentPattern.collectAsState()
    val feedback by viewModel.practiceFeedback.collectAsState()
    val isCorrect by viewModel.isPracticeCorrect.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RadioSurface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "TRANSMITA A LETRA INDICADA:",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )

        Text(
            text = promptChar,
            fontSize = 54.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MorseAmber
        )

        Text(
            text = promptMorse,
            fontSize = 20.sp,
            fontFamily = FontFamily.Monospace,
            color = MorseCyan
        )

        Text(
            text = "Buffer atual: ${currentPattern.ifEmpty { "—" }}",
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = TextSecondary
        )

        if (feedback != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isCorrect == true) MorseGreen.copy(alpha = 0.15f)
                        else MorseRed.copy(alpha = 0.15f)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = feedback ?: "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect == true) MorseGreen else MorseRed
                )
            }
        }

        Button(
            onClick = { viewModel.nextPracticePrompt() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RadioSurfaceVariant)
        ) {
            Text("Próximo Caractere", color = TextPrimary)
        }
    }
}

private fun generateOptions(target: String, all: List<String>): List<String> {
    val wrong = all.filter { it != target }.shuffled().take(3)
    return (wrong + target).shuffled()
}
