package com.pixeleye.sethpirith.ui.components

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixeleye.sethpirith.R
import com.pixeleye.sethpirith.ui.util.PirithPrefs
import java.util.Locale

@Composable
fun CustomLanguageDropdown() {
    val context = LocalContext.current
    val languages = listOf(
        Pair(stringResource(R.string.language_english), "en"),
        Pair(stringResource(R.string.language_sinhala), "si")
    )
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(languages[0].first) }

    // Load saved language preference
    LaunchedEffect(Unit) {
        val savedLocale = PirithPrefs.getLocale(context)
        Log.d("CustomLanguageDropdown", "Loaded saved locale: $savedLocale")
        selectedLanguage = languages.find { it.second == savedLocale }?.first
            ?: languages[0].first
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
    ) {
        // Main button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(1.dp, Color.White, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = selectedLanguage,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }

        // Dropdown content
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            languages.forEach { (language, localeCode) ->
                DropdownMenuItem(
                    text = { Text(language, color = Color.White) },
                    onClick = {
                        selectedLanguage = language
                        expanded = false
                        Log.d("CustomLanguageDropdown", "Selected locale: $localeCode")
                        updateLocale(context, localeCode)
                        PirithPrefs.saveLocale(context, localeCode)
                        (context as? ComponentActivity)?.recreate()
                    }
                )
            }
        }
    }
}

private fun updateLocale(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = Configuration().apply {
        setLocale(locale)
    }
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    Log.d("CustomLanguageDropdown", "Updated locale to: $languageCode")
}