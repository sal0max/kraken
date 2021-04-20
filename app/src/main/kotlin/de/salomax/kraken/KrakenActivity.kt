package de.salomax.kraken

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.fondesa.kpermissions.*
import com.fondesa.kpermissions.extension.*
import java.lang.Exception

class KrakenActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // first make sure to be able to write to storage
        permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .build()
            .send { result ->
                when {
                    result.allGranted() -> {
                        startService()
                        finish()
                    }
                    result.allPermanentlyDenied() -> {
                        Notification(this).notifyAskPermission()
                        finish()
                    }
                    result.allDenied() -> {
                        finish()
                    }
                }
            }
    }

    private fun startService() {
        val data: String? = intent?.getStringExtra(Intent.EXTRA_TEXT)

        data?.let {
            val shortcode = parseUri(it)
            val endpoint = when {
                it.contains("/p/") -> "p"
                it.contains("/reel/") -> "reel"
                else -> null
            }
            if (shortcode != null && endpoint != null) {
                ApiWorker.enqueueWork(this, shortcode, endpoint)
            } else {
                Notification(this).notifyWrongLinkError(data)
            }
        }
    }

    private fun parseUri(s: String): String? {
        val uri = Uri.parse(s)
        return try {
            if (
            // check if it's a valid link
                uri != null
                // check if its an Instagram url
                && uri.authority == "www.instagram.com"
            ) {
                // check if its an Instagram picture url
                if (uri.pathSegments.find { it == "p" } != null
                    // check if it has a shortcode (something that follows /p/)
                    && uri.pathSegments.indexOf("p") != uri.pathSegments.size - 1)
                    uri.pathSegments?.let { it[it.indexOf("p") + 1] } as String
                // check if its an Instagram reel url
                else if (uri.pathSegments.find { it == "reel" } != null
                    // check if it has a shortcode (something that follows /reel/)
                    && uri.pathSegments.indexOf("reel") != uri.pathSegments.size - 1)
                    uri.pathSegments?.let { it[it.indexOf("reel") + 1] } as String
                else
                    null
            } else
                null
        } catch (e: Exception) {
            null
        }

    }

}
