package de.salomax.tuck

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.fondesa.kpermissions.extension.*

class TuckActivity : Activity() {

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
            if (it.contains("instagram.com/p/")) {
                // get shortcode of post
                Uri.parse(it).pathSegments?.last()?.let { shortcode: String ->
                    // fire off service
                    TuckService.enqueueWork(this, shortcode)
                }
            } else {
                Notification(this).notifyWrongLinkError()
            }
        }
    }

}
