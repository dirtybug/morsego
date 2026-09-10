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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun LearnScreen(
    viewModel: MorseViewModel,
    modifier: Modifier = Modifier
) {
    val selectedIndex by viewModel.selectedLessonIndex.collectAsState()
    val promptChar by viewModel.currentPromptChar.collectAsState()
    val feedback by viewModel.practiceFeedback.collectAsState()
    val isCorrect by viewModel.isPracticeCorrect.collectAsState()
    val isTonePlaying by viewModel.isTonePlaying.collectAsState()
    val isDitPressed by viewModel.inputManager.isDitPressed.collectAsState()
    val isDahPressed by viewModel.inputManager.isDahPressed.collectAsState()
    val currentPattern by viewModel.decoder.currentPattern.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val lessons = viewModel.kochLessons
    val currentLesson = lessons[selectedIndex]

    val promptMorse = MorseDictionary.getMorse(promptChar) ?: ""

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
            MorseSignalIndicator(isToneActive = isTonePlaying, label = "MÉTODO KOCH CW")

            // Lesson selector horizontal carousel
            Text(
                text = "SELECIONE A LIÇÃO (1 A 40)",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(lessons) { idx, lesson ->
                    val isSelected = idx == selectedIndex
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MorseAmber else RadioSurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) MorseAmber else RadioSurfaceVariant,
                                CircleShape
                            )
                            .clickable { viewModel.selectLesson(idx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${idx + 1}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) RadioDark else TextPrimary
                        )
                    }
                }
            }

            // Current Lesson Overview Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RadioSurface)
                    .border(1.dp, RadioSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = currentLesson.title.uppercase(),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MorseCyan
                )
                Text(
                    text = currentLesson.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Characters badge list
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentLesson.allCharacters.forEach { charStr ->
                        val morseStr = MorseDictionary.getMorse(charStr) ?: ""
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RadioSurfaceVariant)
                                .clickable { viewModel.playCharacterSound(charStr) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = charStr,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MorseAmber
                                )
                                Text(
                                    text = morseStr,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Transmit Drill Challenge Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RadioSurface)
                    .border(
                        1.dp,
                        when (isCorrect) {
                            true -> MorseGreen
                            false -> MorseRed
                            null -> RadioSurfaceVariant
                        },
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TRANSMITA COM O SEU MANIPULADOR CW:",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                // Big target character
                Text(
                    text = promptChar,
                    fontSize = 64.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MorseAmber,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "Código: $promptMorse",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MorseCyan
                )

                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.playCharacterSound(promptChar) }
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MorseCyan)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Ouvir Ritmo", color = TextPrimary)
                    }

                    Button(
                        onClick = { viewModel.nextPracticePrompt() },
                        colors = ButtonDefaults.buttonColors(containerColor = RadioSurfaceVariant)
                    ) {
                        Text("Outra Letra", color = TextPrimary)
                        Spacer(modifier = Modifier.size(6.dp))
                        Icon(Icons.Default.NavigateNext, contentDescription = null, tint = TextPrimary)
                    }
                }

                // Buffer status
                Text(
                    text = "A introduzir: ${currentPattern.ifEmpty { "—" }}",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 12.dp)
                )

                if (feedback != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
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
            }
        }

        // On-screen paddle controls
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
