package com.morsego.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morsego.app.ui.theme.MorseAmber
import com.morsego.app.ui.theme.MorseCyan
import com.morsego.app.ui.theme.RadioDark
import com.morsego.app.ui.theme.RadioSurface
import com.morsego.app.ui.theme.RadioSurfaceVariant
import com.morsego.app.ui.theme.TextPrimary
import com.morsego.app.ui.theme.TextSecondary

@Composable
fun MorsePaddleControls(
    isDitPressed: Boolean,
    isDahPressed: Boolean,
    onDitPress: (Boolean) -> Unit,
    onDahPress: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    reversed: Boolean = false,
    showStraightKeyOnly: Boolean = false
) {
    if (showStraightKeyOnly) {
        // Single straight key pad
        TouchPaddleButton(
            isPressed = isDitPressed,
            onPressChanged = onDitPress,
            title = "MANUAL KEY",
            symbol = "TAP / HOLD",
            activeColor = MorseAmber,
            modifier = modifier
                .fillMaxWidth()
                .height(130.dp)
        )
    } else {
        // Dual iambic paddles
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val leftIsDit = !reversed

            TouchPaddleButton(
                isPressed = if (leftIsDit) isDitPressed else isDahPressed,
                onPressChanged = if (leftIsDit) onDitPress else onDahPress,
                title = if (leftIsDit) "DIT (•)" else "DAH (—)",
                symbol = if (leftIsDit) "Ponto [Esquerda]" else "Traço [Esquerda]",
                activeColor = if (leftIsDit) MorseAmber else MorseCyan,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )

            TouchPaddleButton(
                isPressed = if (leftIsDit) isDahPressed else isDitPressed,
                onPressChanged = if (leftIsDit) onDahPress else onDitPress,
                title = if (leftIsDit) "DAH (—)" else "DIT (•)",
                symbol = if (leftIsDit) "Traço [Direita]" else "Ponto [Direita]",
                activeColor = if (leftIsDit) MorseCyan else MorseAmber,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
fun TouchPaddleButton(
    isPressed: Boolean,
    onPressChanged: (Boolean) -> Unit,
    title: String,
    symbol: String,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val bgBrush = if (isPressed) {
        Brush.verticalGradient(
            colors = listOf(activeColor.copy(alpha = 0.35f), activeColor.copy(alpha = 0.15f))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(RadioSurfaceVariant, RadioSurface)
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgBrush)
            .border(
                width = if (isPressed) 2.dp else 1.dp,
                color = if (isPressed) activeColor else RadioSurfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Press -> onPressChanged(true)
                            PointerEventType.Release -> onPressChanged(false)
                        }
                    }
                }
            }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isPressed) activeColor else TextPrimary
            )
            Text(
                text = symbol,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
