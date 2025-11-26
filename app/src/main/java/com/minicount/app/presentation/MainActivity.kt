package com.minicount.app.presentation

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.minicount.app.data.preferences.PreferencesManager
import com.minicount.app.domain.export.BackupManager
import com.minicount.app.domain.notification.NotificationWorker
import com.minicount.app.presentation.navigation.MiniCountNavigation
import com.minicount.app.presentation.theme.MiniCountTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var backupManager: BackupManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationWorker.scheduleNotificationCheck(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NotificationWorker.scheduleNotificationCheck(this)
        }

        setContent {
            var showOnboarding by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    showOnboarding = !preferencesManager.onboardingCompleted.first()
                }
            }

            MiniCountTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MiniCountNavigation(
                        startWithOnboarding = showOnboarding,
                        backupManager = backupManager
                    )
                }
            }
        }
    }
}
