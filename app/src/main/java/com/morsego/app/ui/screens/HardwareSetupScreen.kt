package com.morsego.app.ui.screens

import android.view.KeyEvent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.morsego.app.keyer.CalibrationTarget
import com.morsego.app.ui.components.MorseSignalIndicator
import com.morsego.app.ui.theme.MorseAmber
import com.morsego.app.ui.theme.MorseCyan
import com.morsego.app.ui.theme.MorseGreen
import com.morsego.app.ui.theme.RadioDark
import com.morsego.app.ui.theme.RadioSurface
import com.morsego.app.ui.theme.RadioSurfaceVariant
import com.morsego.app.ui.theme.TextPrimary
import com.morsego.app.ui.theme.TextSecondary
import com.morsego.app.viewmodel.MorseViewModel

@Composable
fun HardwareSetupScreen(
    viewModel: MorseViewModel,
    modifier: Modifier = Modifier
) {
    val isDitPressed by viewModel.inputManager.isDitPressed.collectAsState()
    val isDahPressed by viewModel.inputManager.isDahPressed.collectAsState()
    val isTonePlaying by viewModel.isTonePlaying.collectAsState()
    val calibrationState by viewModel.inputManager.calibrationState.collectAsState()
    val recentLogs by viewModel.inputManager.recentLogs.collectAsState()
    val settings by viewModel.settings.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(RadioDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Hardware Banner
            HardwareDeviceBanner()
        }

        item {
            // Live TX Indicator
            MorseSignalIndicator(
                isToneActive = isTonePlaying,
                label = "ESTADO DO MANIPULADOR CW"
            )
        }

        item {
            // Live Paddle Status Monitor
            PaddleStatusCard(
                isDitPressed = isDitPressed,
                isDahPressed = isDahPressed,
                reversed = settings.reversePaddles
            )
        }

        item {
            // Calibration Wizard Card
            CalibrationWizardCard(
                calibrationState = calibrationState,
                ditKeyCode = settings.ditKeyCode,
                dahKeyCode = settings.dahKeyCode,
                onCalibrateDit = { viewModel.inputManager.startCalibration(CalibrationTarget.DIT_PADDLE) },
                onCalibrateDah = { viewModel.inputManager.startCalibration(CalibrationTarget.DAH_PADDLE) },
                onCancelCalibration = { viewModel.inputManager.cancelCalibration() },
                onResetDefaults = {
                    viewModel.setWpm(settings.wpm)
                    viewModel.inputManager.updateSettings(
                        settings.copy(
                            ditKeyCode = KeyEvent.KEYCODE_CTRL_LEFT,
                            dahKeyCode = KeyEvent.KEYCODE_CTRL_RIGHT
                        )
                    )
                }
            )
        }

        item {
            // Reverse Paddles Switch Card
            ReversePaddlesCard(
                reversed = settings.reversePaddles,
                onToggle = { viewModel.toggleReversePaddles() }
            )
        }

        item {
            // Instructions for the Amazon Trainer
            TrainerInstructionsCard()
        }

        item {
            Text(
                text = "LOG DE EVENTOS USB EM TEMPO REAL",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (recentLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RadioSurface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum sinal USB recebido ainda.\nConecte o manipulador Type-C e toque nas pás!",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(recentLogs.reversed().take(8)) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(RadioSurface)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.eventType,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (log.isDown) MorseAmber else MorseCyan
                    )
                    Text(
                        text = "${log.keyName} (#${log.keyCode})",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HardwareDeviceBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RadioSurface)
            .border(1.dp, MorseCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MorseCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Usb,
                    contentDescription = null,
                    tint = MorseCyan,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column {
                Text(
                    text = "Manipulador CW Trainer Type-C",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Compatível com chaveador PCB 4 Nd Magnet & Jack 3.5mm",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun PaddleStatusCard(
    isDitPressed: Boolean,
    isDahPressed: Boolean,
    reversed: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RadioSurface)
            .padding(16.dp)
    ) {
        Text(
            text = "MONITOR DE PÁS FÍSICAS",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Dit paddle status
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDitPressed) MorseAmber else RadioSurfaceVariant)
                    .border(
                        1.dp,
                        if (isDitPressed) MorseAmber else RadioSurfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (!reversed) "PÁ ESQUERDA (DIT •)" else "PÁ ESQUERDA (DAH —)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isDitPressed) RadioDark else TextPrimary
                    )
                    Text(
                        text = if (isDitPressed) "PRESSIONADA" else "LIVRE",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isDitPressed) RadioDark else TextSecondary
                    )
                }
            }

            // Dah paddle status
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDahPressed) MorseCyan else RadioSurfaceVariant)
                    .border(
                        1.dp,
                        if (isDahPressed) MorseCyan else RadioSurfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (!reversed) "PÁ DIREITA (DAH —)" else "PÁ DIREITA (DIT •)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isDahPressed) RadioDark else TextPrimary
                    )
                    Text(
                        text = if (isDahPressed) "PRESSIONADA" else "LIVRE",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isDahPressed) RadioDark else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun CalibrationWizardCard(
    calibrationState: CalibrationTarget,
    ditKeyCode: Int,
    dahKeyCode: Int,
    onCalibrateDit: () -> Unit,
    onCalibrateDah: () -> Unit,
    onCancelCalibration: () -> Unit,
    onResetDefaults: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RadioSurface)
            .padding(16.dp)
    ) {
        Text(
            text = "CALIBRAÇÃO AUTOMÁTICA DE TECLAS",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Text(
            text = "Clique no botão e toque na pá do manipulador para detetar o sinal.",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        if (calibrationState != CalibrationTarget.NONE) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MorseAmber.copy(alpha = 0.2f))
                    .border(1.dp, MorseAmber, RoundedCornerShape(8.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (calibrationState == CalibrationTarget.DIT_PADDLE)
                            "TOQUE NA PÁ DO DIT (PONTO) AGORA..."
                        else
                            "TOQUE NA PÁ DO DAH (TRAÇO) AGORA...",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MorseAmber
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onCancelCalibration) {
                        Text("Cancelar", color = TextPrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCalibrateDit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = RadioSurfaceVariant)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Calibrar Dit", fontSize = 12.sp, color = MorseAmber, fontWeight = FontWeight.Bold)
                    Text("Key: ${KeyEvent.keyCodeToString(ditKeyCode)}", fontSize = 10.sp, color = TextSecondary)
                }
            }

            Button(
                onClick = onCalibrateDah,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = RadioSurfaceVariant)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Calibrar Dah", fontSize = 12.sp, color = MorseCyan, fontWeight = FontWeight.Bold)
                    Text("Key: ${KeyEvent.keyCodeToString(dahKeyCode)}", fontSize = 10.sp, color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onResetDefaults,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.SettingsBackupRestore, contentDescription = null, tint = TextSecondary)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Restaurar Padrão VBand (Ctrl Esquerdo/Direito)", fontSize = 12.sp, color = TextPrimary)
        }
    }
}

@Composable
fun ReversePaddlesCard(
    reversed: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RadioSurface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Inverter Pás (Mão Esquerda / Destro)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = if (reversed) "Esquerda = Dah (—) | Direita = Dit (•)" else "Esquerda = Dit (•) | Direita = Dah (—)",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Switch(
            checked = reversed,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MorseAmber,
                checkedTrackColor = MorseAmber.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun TrainerInstructionsCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RadioSurface)
            .padding(16.dp)
    ) {
        Text(
            text = "DICAS DE CONEXÃO DO MANIPULADOR",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MorseCyan,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "1. Conexão USB-C: Ligue diretamente à porta Type-C do telemóvel/tablet usando o cabo OTG incluído.\n\n" +
                    "2. Modos do Dispositivo:\n" +
                    "   • Modo 2 (Recomendado - LED a piscar): Emula teclado VBand (Ctrl Esquerdo e Direito). MorseGO é 100% nativo neste modo!\n" +
                    "   • Modo 1 (LED fixo): Emula rato USB. MorseGO suporta ambos!\n\n" +
                    "3. Troca de modo: Ligue o cabo USB mantendo premido o botão do circuito PCB para alternar entre Modo 1 e Modo 2.\n\n" +
                    "4. Conector Jack 3.5mm: Pode ligar auscultadores ou uma chave manual externa.",
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 18.sp
        )
    }
}
