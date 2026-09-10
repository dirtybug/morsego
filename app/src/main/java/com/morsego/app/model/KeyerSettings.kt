package com.morsego.app.model

import android.view.KeyEvent

enum class KeyerMode(val label: String, val description: String) {
    IAMBIC_B(
        label = "Iambic B",
        description = "Modo padrão de pás duplas com inserção alternada ao soltar durante o elemento (estilo Curtis)."
    ),
    IAMBIC_A(
        label = "Iambic A",
        description = "Modo clássico de pás duplas, para imediatamente ao soltar as pás sem elemento extra."
    ),
    STRAIGHT_KEY(
        label = "Manual / Straight",
        description = "Manipulador manual ou quando o próprio hardware (PCB) gera a cadência de pontos e traços."
    )
}

data class KeyerSettings(
    val wpm: Int = 15,
    val farnsworthWpm: Int = 15,
    val farnsworthEnabled: Boolean = false,
    val pitchHz: Float = 700f,
    val mode: KeyerMode = KeyerMode.IAMBIC_B,
    val reversePaddles: Boolean = false, // Swap left/right paddles
    // Default keycodes for the Amazon CW Keyer Trainer (Mode 2 sends Left/Right Ctrl)
    val ditKeyCode: Int = KeyEvent.KEYCODE_CTRL_LEFT,
    val dahKeyCode: Int = KeyEvent.KEYCODE_CTRL_RIGHT,
    // Alternative keycodes supported (Space, Alt, etc.)
    val alternativeDitKeyCode: Int = KeyEvent.KEYCODE_ALT_LEFT,
    val alternativeDahKeyCode: Int = KeyEvent.KEYCODE_ALT_RIGHT,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)
