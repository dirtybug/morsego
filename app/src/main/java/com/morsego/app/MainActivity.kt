package com.morsego.app

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.morsego.app.ui.navigation.Screen
import com.morsego.app.ui.screens.DictionaryScreen
import com.morsego.app.ui.screens.FreeKeyerScreen
import com.morsego.app.ui.screens.HardwareSetupScreen
import com.morsego.app.ui.screens.LearnScreen
import com.morsego.app.ui.screens.PracticeScreen
import com.morsego.app.ui.screens.SettingsScreen
import com.morsego.app.ui.theme.MorseAmber
import com.morsego.app.ui.theme.MorseGOTheme
import com.morsego.app.ui.theme.RadioDark
import com.morsego.app.ui.theme.RadioSurface
import com.morsego.app.ui.theme.RadioSurfaceVariant
import com.morsego.app.ui.theme.TextPrimary
import com.morsego.app.ui.theme.TextSecondary
import com.morsego.app.viewmodel.MorseViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MorseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initSystemServices(this)

        setContent {
            MorseGOTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }

    /**
     * Intercepts physical CW Keyer events (Type-C USB OTG keyboard emulation / Vband mode)
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (viewModel.handleKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * Intercepts mouse button events for keyers operating in Mode 1 (Mouse emulation)
     */
    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (viewModel.handleGenericMotionEvent(event)) {
            return true
        }
        return super.dispatchGenericMotionEvent(event)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: MorseViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MorseGO",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MorseAmber
                    )
                },
                actions = {
                    Text(
                        text = "${settings.wpm} WPM",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Definições",
                            tint = if (currentRoute == Screen.Settings.route) MorseAmber else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RadioDark,
                    titleContentColor = MorseAmber
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = RadioSurface,
                tonalElevation = 8.dp
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RadioDark,
                            selectedTextColor = MorseAmber,
                            indicatorColor = MorseAmber,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RadioDark)
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Learn.route
            ) {
                composable(Screen.Learn.route) {
                    LearnScreen(viewModel = viewModel)
                }
                composable(Screen.Practice.route) {
                    PracticeScreen(viewModel = viewModel)
                }
                composable(Screen.FreeKeyer.route) {
                    FreeKeyerScreen(viewModel = viewModel)
                }
                composable(Screen.Dictionary.route) {
                    DictionaryScreen(viewModel = viewModel)
                }
                composable(Screen.HardwareSetup.route) {
                    HardwareSetupScreen(viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
