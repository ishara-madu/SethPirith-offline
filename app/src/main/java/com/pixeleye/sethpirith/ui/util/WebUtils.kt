package com.pixeleye.sethpirith.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object WebUtils {
    fun openWebsite(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}