package com.minicount.app.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.minicount.app.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AdBanner(
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAd by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            showAd = preferencesManager.showAds.first()
        }
    }

    if (showAd) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    // Test ad unit ID - replace with real ad unit before publishing
                    adUnitId = "ca-app-pub-3940256099942544/6300978111"
                    setAdSize(AdSize.BANNER)
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
