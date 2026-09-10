package com.morsego.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morsego.app.ui.theme.MorseAmber
import com.morsego.app.ui.theme.RadioSurface
import com.morsego.app.ui.theme.RadioSurfaceVariant
import com.morsego.app.ui.theme.TextPrimary
import com.morsego.app.ui.theme.TextSecondary

@Composable
fun WpmControlCard(
    wpm: Int,
    onWpmChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "VELOCIDADE (WPM)",
    minWpm: Int = 5,
    maxWpm: Int = 40
) {
    Column(
        modifier = modifier
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
                    text = title,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Text(
                    text = "$wpm WPM",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MorseAmber
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { if (wpm > minWpm) onWpmChanged(wpm - 1) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RadioSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Diminuir WPM",
                        tint = TextPrimary
                    )
                }

                IconButton(
                    onClick = { if (wpm < maxWpm) onWpmChanged(wpm + 1) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RadioSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar WPM",
                        tint = TextPrimary
                    )
                }
            }
        }

        Slider(
            value = wpm.toFloat(),
            onValueChange = { onWpmChanged(it.toInt()) },
            valueRange = minWpm.toFloat()..maxWpm.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = MorseAmber,
                activeTrackColor = MorseAmber,
                inactiveTrackColor = RadioSurfaceVariant
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
