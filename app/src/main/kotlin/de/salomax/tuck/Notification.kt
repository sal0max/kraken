package de.salomax.tuck

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.provider.MediaStore

class Notification(private val context: Context) {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "88"
    }

    init {
        // before every notification, the NotificationChannel must be set
        // only on API 26+ because the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name_short)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notifyWrongLinkError(errorMsg: String?) {
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(context.getString(R.string.error_invalid_link))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(errorMsg)
            )
            .build()
        NotificationManagerCompat
            .from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    fun notifyAskPermission() {
        // create link to app settings
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        val pIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, 0)

        // build notification
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.ask_write_storage_permission))
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.ask_write_storage_permission))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pIntent)
            .build()

        // show notification
        NotificationManagerCompat
            .from(context)
            .notify(0, notification)
    }


    fun notifyDownloadProgress(notificationId: Int, current: Int, total: Int) {
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(context.getString(R.string.download_running))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setProgress(total, current, false)
            .setOnlyAlertOnce(true)
            .build()
        NotificationManagerCompat
            .from(context)
            .notify(notificationId, notification)
    }

    fun notifyDownloadError(notificationId: Int, errorMsg: String?) {
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(context.getString(R.string.download_error))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(errorMsg)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat
            .from(context)
            .notify(notificationId, notification)
    }

    fun notifyDownloadSuccess(notificationId: Int, text: String, path: String, isVideo: Boolean) {
        // open image in gallery(intent)
        val intent = Intent()
        intent.action = android.content.Intent.ACTION_VIEW
        intent.setDataAndType(Uri.parse(path), if (isVideo) "video/*" else "image/*")
        val pIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, 0)

        // generate preview
        val preview = if (isVideo)
            ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND)
        else
            BitmapFactory.decodeFile(path)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(context.getString(R.string.download_success))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            )
            // second line: not shown if preview image present
            .setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(preview))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat
            .from(context)
            .notify(notificationId, notification)
    }

}
