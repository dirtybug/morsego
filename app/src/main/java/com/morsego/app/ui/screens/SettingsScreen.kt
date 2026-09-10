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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morsego.app.model.KeyerMode
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
fun SettingsScreen(
    viewModel: MorseViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RadioDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CONFIGURAÇÕES DO MANIPULADOR CW",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )

        // WPM Speed Control
        WpmControlCard(
            wpm = settings.wpm,
            onWpmChanged = { viewModel.setWpm(it) }
        )

        // Sidetone Frequency (Pitch)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RadioSurface)
                .border(1.dp, RadioSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOM CW (FREQUÊNCIA DE ÁUDIO)",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Text(
                        text = "${settings.pitchHz.toInt()} Hz",
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MorseCyan
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.playMorsePattern("... --- ...") }
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MorseCyan)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Testar Tom", color = TextPrimary)
                }
            }

            Slider(
                value = settings.pitchHz,
                onValueChange = { viewModel.setTonePitch(it) },
                valueRange = 400f..1000f,
                steps = 11,
                colors = SliderDefaults.colors(
                    thumbColor = MorseCyan,
                    activeTrackColor = MorseCyan,
                    inactiveTrackColor = RadioSurfaceVariant
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Keyer Mode Selection
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RadioSurface)
                .border(1.dp, RadioSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "MODO DO MANIPULADOR",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            KeyerMode.entries.forEach { mode ->
                val isSelected = settings.mode == mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MorseAmber.copy(alpha = 0.15f) else RadioSurfaceVariant)
                        .border(1.dp, if (isSelected) MorseAmber else RadioSurfaceVariant, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setKeyerMode(mode) }
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = mode.label,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MorseAmber else TextPrimary
                        )
                        Text(
                            text = mode.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Sound & Haptics Toggles
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RadioSurface)
                .border(1.dp, RadioSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sidetone de Áudio", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Emitir tom sinusoidal ao pressionar as pás", fontSize = 12.sp, color = TextSecondary)
                }
                Switch(
                    checked = settings.soundEnabled,
                    onCheckedChange = { viewModel.toggleSound() },
                    colors = SwitchDefaults.colors(checkedThumbColor = MorseAmber, checkedTrackColor = MorseAmber.copy(0.5f))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Vibração Tátil", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Feedback háptico ultra-curto a cada toque", fontSize = 12.sp, color = TextSecondary)
                }
                Switch(
                    checked = settings.hapticsEnabled,
                    onCheckedChange = { viewModel.toggleHaptics() },
                    colors = SwitchDefaults.colors(checkedThumbColor = MorseAmber, checkedTrackColor = MorseAmber.copy(0.5f))
                )
            }
        }

        // App Information
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MorseGO v1.0.0",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MorseAmber
            )
            Text(
                text = "Desenvolvido para treino de telegrafia & rádio amador",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}
