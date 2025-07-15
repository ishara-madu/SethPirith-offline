package com.pixeleye.sethpirith

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pixeleye.sethpirith.navigation.AppNavigator
import com.pixeleye.sethpirith.ui.screens.SplashScreen
import android.content.Intent
import android.content.Intent.ACTION_LOCALE_CHANGED
import android.content.res.Configuration
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
// In MainActivity.kt or a suitable place
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pixeleye.sethpirith.service.MediaPlayerService
import com.pixeleye.sethpirith.ui.theme.SethPirithTheme
import com.pixeleye.sethpirith.ui.util.PirithPrefs
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                // Handle permission denial (e.g., show a message)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestIgnoreBatteryOptimizations(this)
        // Apply saved locale before setting content
        val savedLocale = PirithPrefs.getLocale(this)
        updateLocale(savedLocale)

        setContent {
            SethPirithTheme {
                AppNavigator()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        }
    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration().apply {
            setLocale(locale)
        }
        resources.updateConfiguration(config, resources.displayMetrics)

        val intent = Intent("com.pixeleye.sethpirith.LOCALE_CHANGED")
        intent.putExtra("localeCode", locale)
        this.sendBroadcast(intent)
    }

}