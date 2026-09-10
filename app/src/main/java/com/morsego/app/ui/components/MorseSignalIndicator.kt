package com.morsego.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morsego.app.ui.theme.MorseAmber
import com.morsego.app.ui.theme.MorseCyan
import com.morsego.app.ui.theme.RadioDark
import com.morsego.app.ui.theme.RadioSurfaceVariant
import com.morsego.app.ui.theme.TextSecondary

@Composable
fun MorseSignalIndicator(
    isToneActive: Boolean,
    modifier: Modifier = Modifier,
    label: String = "CW TX"
) {
    val indicatorColor by animateColorAsState(
        targetValue = if (isToneActive) MorseAmber else RadioDark,
        animationSpec = tween(durationMillis = 60),
        label = "ledColor"
    )

    val glowElevation by animateDpAsState(
        targetValue = if (isToneActive) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 60),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(RadioSurfaceVariant)
            .border(1.dp, if (isToneActive) MorseAmber else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // LED indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .shadow(elevation = glowElevation, shape = CircleShape, spotColor = MorseAmber)
                        .clip(CircleShape)
                        .background(indicatorColor)
                        .border(1.dp, MorseAmber.copy(alpha = 0.5f), CircleShape)
                )

                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isToneActive) MorseAmber else TextSecondary
                )
            }

            // Signal wave simulation bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val heights = listOf(8, 14, 22, 16, 24, 12, 18, 6)
                heights.forEach { h ->
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = if (isToneActive) h.dp else 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isToneActive) MorseCyan else RadioDark)
                    )
                }
            }
        }
    }
}
