package com.morsego.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Learn : Screen("learn", "Aprender", Icons.Default.School)
    data object Practice : Screen("practice", "Praticar", Icons.Default.FitnessCenter)
    data object FreeKeyer : Screen("free_keyer", "Manipulador", Icons.Default.Radio)
    data object Dictionary : Screen("dictionary", "Dicionário", Icons.Default.MenuBook)
    data object HardwareSetup : Screen("hardware_setup", "Hardware CW", Icons.Default.Build)
    data object Settings : Screen("settings", "Definições", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Learn, Practice, FreeKeyer, Dictionary, HardwareSetup)
    }
}
