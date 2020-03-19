package de.salomax.kraken

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.fondesa.kpermissions.extension.*
import java.lang.Exception

class KrakenActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // first make sure to be able to write to storage
        permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .build()
            .onAccepted {
                startService()
                finish()
            }
            .onPermanentlyDenied {
                Notification(this).notifyAskPermission()
                finish()
            }
            .onShouldShowRationale { _, nonce ->
                nonce.use()
            }
            .send()
    }

    private fun startService() {
        val data: String? = intent?.getStringExtra(Intent.EXTRA_TEXT)

        data?.let {
            val shortcode = parseUri(it)
            if (shortcode != null) {
                ApiWorker.enqueueWork(this, shortcode)
            } else {
                Notification(this).notifyWrongLinkError(data)
            }
        }
    }

    private fun parseUri(s: String): String? {
        val uri = Uri.parse(s)
        return try {
            if (uri != null // check if its a valid link
                && uri.authority == "www.instagram.com" // check if its an instagram url
                && uri.pathSegments.find { it == "p" } != null // check if its a instagram picture url
                && uri.pathSegments.indexOf("p") != uri.pathSegments.size - 1 // check if it has a shortcode (something that follows /p/)
            )
                uri.pathSegments?.let { it[it.indexOf("p") + 1] } as String
            else
                null
        } catch (e: Exception) {
            null
        }

    }

}
