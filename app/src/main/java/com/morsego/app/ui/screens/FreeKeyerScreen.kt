package com.morsego.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morsego.app.model.KeyerMode
import com.morsego.app.ui.components.MorsePaddleControls
import com.morsego.app.ui.components.MorseSignalIndicator
import com.morsego.app.ui.components.WpmControlCard
import com.morsego.app.ui.theme.MorseAmber
import com.morsego.app.ui.theme.MorseCyan
import com.morsego.app.ui.theme.RadioDark
import com.morsego.app.ui.theme.RadioSurface
import com.morsego.app.ui.theme.RadioSurfaceVariant
import com.morsego.app.ui.theme.TextPrimary
import com.morsego.app.ui.theme.TextSecondary
import com.morsego.app.viewmodel.MorseViewModel

@Composable
fun FreeKeyerScreen(
    viewModel: MorseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTonePlaying by viewModel.isTonePlaying.collectAsState()
    val isDitPressed by viewModel.inputManager.isDitPressed.collectAsState()
    val isDahPressed by viewModel.inputManager.isDahPressed.collectAsState()
    val decodedText by viewModel.decoder.decodedText.collectAsState()
    val currentPattern by viewModel.decoder.currentPattern.collectAsState()
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RadioDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Signal LED and Audio status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MorseSignalIndicator(
                    isToneActive = isTonePlaying,
                    modifier = Modifier.weight(1f),
                    label = "TRANSMISSÃO CW (${settings.mode.label.uppercase()})"
                )

                IconButton(
                    onClick = { viewModel.toggleSound() },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RadioSurfaceVariant)
                ) {
                    Icon(
                        imageVector = if (settings.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Som",
                        tint = if (settings.soundEnabled) MorseAmber else TextSecondary
                    )
                }
            }

            // Current Morse element buffer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(RadioSurface)
                    .border(1.dp, RadioSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BUFFER: ${currentPattern.ifEmpty { "—" }}",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MorseCyan
                    )

                    Text(
                        text = "${settings.wpm} WPM",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MorseAmber
                    )
                }
            }

            // Decoded Text Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RadioSurface)
                    .border(1.dp, RadioSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TEXTO DESCODIFICADO",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("MorseGO", decodedText))
                                    Toast.makeText(context, "Texto copiado!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copiar",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.decoder.backspace() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Backspace,
                                    contentDescription = "Apagar",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.decoder.clear() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Limpar",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = decodedText.ifEmpty { "Pressione as pás físicas ou os botões abaixo para transmitir..." },
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (decodedText.isEmpty()) TextSecondary else TextPrimary,
                        lineHeight = 28.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            // Speed control
            WpmControlCard(
                wpm = settings.wpm,
                onWpmChanged = { viewModel.setWpm(it) }
            )
        }

        // On-screen paddle touch controls
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "PÁS DE MANIPULAÇÃO (OU USE O HARDWARE USB-C)",
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
