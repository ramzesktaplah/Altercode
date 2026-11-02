package com.ai.altercode

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ai.altercode.data.ThemeMode
import com.ai.altercode.ui.navigation.AppNavigation
import com.ai.altercode.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by ServiceLocator.settings.themeMode.collectAsStateWithLifecycle()
            val isDark = themeMode == ThemeMode.DARK ||
                (themeMode == ThemeMode.SYSTEM && androidx.compose.foundation.isSystemInDarkTheme())

            // System bar icons flip with the appearance: dark icons on the
            // light theme, light icons on the dark theme.
            androidx.compose.runtime.LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
            }

            AppTheme(themeMode) {
                AppNavigation()
            }
        }
    }
}
