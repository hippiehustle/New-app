package com.minicount.app.presentation.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minicount.app.BuildConfig
import com.minicount.app.domain.export.BackupManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onPremiumClick: () -> Unit,
    backupManager: BackupManager,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isPremium by viewModel.isPremium.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var importMessage by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                backupManager.exportToFile(it).fold(
                    onSuccess = { exportMessage = "Backup exported successfully!" },
                    onFailure = { exportMessage = "Export failed: ${it.message}" }
                )
                showExportDialog = true
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                backupManager.importFromFile(it).fold(
                    onSuccess = { count -> importMessage = "Imported $count events successfully!" },
                    onFailure = { importMessage = "Import failed: ${it.message}" }
                )
                showImportDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Premium Section
            if (!isPremium) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable(onClick = onPremiumClick),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Upgrade to Premium",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Unlock all features",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }

            // Data & Backup
            SettingsSection(title = "Data & Backup")

            SettingsItem(
                icon = Icons.Default.Upload,
                title = "Export Data",
                subtitle = "Backup your events to JSON file",
                onClick = {
                    exportLauncher.launch("minicount_backup_${System.currentTimeMillis()}.json")
                }
            )

            SettingsItem(
                icon = Icons.Default.Download,
                title = "Import Data",
                subtitle = "Restore events from backup file",
                onClick = {
                    importLauncher.launch("application/json")
                }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Notifications
            SettingsSection(title = "Notifications")

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Default Reminder",
                subtitle = "${settings.defaultEventReminderDays} day(s) before event",
                onClick = { /* Show dialog */ }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Appearance
            SettingsSection(title = "Appearance")

            SettingsItem(
                icon = Icons.Default.Palette,
                title = "Theme",
                subtitle = settings.theme.name.lowercase().replaceFirstChar { it.uppercase() },
                onClick = { /* Show theme picker */ }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Vibration, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Haptic Feedback", fontWeight = FontWeight.Medium)
                        Text(
                            "Vibrate on interactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                Switch(
                    checked = settings.hapticFeedbackEnabled,
                    onCheckedChange = { viewModel.updateHapticFeedback(it) }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // About
            SettingsSection(title = "About")

            SettingsItem(
                icon = Icons.Default.Info,
                title = "Version",
                subtitle = "1.0.0 (${BuildConfig.VERSION_CODE})",
                onClick = { }
            )

            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                subtitle = "View our privacy policy",
                onClick = { /* Open privacy policy */ }
            )

            SettingsItem(
                icon = Icons.Default.Article,
                title = "Terms of Service",
                subtitle = "View terms and conditions",
                onClick = { /* Open terms */ }
            )

            SettingsItem(
                icon = Icons.Default.RateReview,
                title = "Rate App",
                subtitle = "Rate us on Google Play",
                onClick = { /* Open Play Store */ }
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Complete") },
            text = { Text(exportMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Complete") },
            text = { Text(importMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
