package de.salomax.kraken

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.work.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import de.salomax.kraken.data.*
import java.io.File
import java.io.OutputStream
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*

class DownloadWorker(context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val notificationHelper by lazy { Notification(context) }

    /**
     * Convenience method for enqueuing work in.
     */
    companion object {
        private const val KEY_IS_VIDEO = "KEY_IS_VIDEO"
        private const val KEY_DOWNLOAD_URL = "KEY_DOWNLOAD_URL"
        private const val KEY_FILENAME = "KEY_FILENAME"

        fun enqueueWork(context: Context, image: Image, post: Post) {

            // gather media properties
            val downloadUrl = if (image.isVideo) image.videoUrl!! else image.imageUrl
            val filename = post.owner.username.replace('.', '_') +
                    ".${post.dateTime.format()}" +
                    ".${image.shortcode}" +
                    ".${getFileExtension(downloadUrl)}"

            WorkManager.getInstance(context).enqueue(
                OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(
                        Data.Builder().apply {
                            putBoolean(KEY_IS_VIDEO, image.isVideo)
                            putString(KEY_DOWNLOAD_URL, downloadUrl.toString())
                            putString(KEY_FILENAME, filename)
                        }.build()
                    )
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
            )
        }

        /**
         * Formats the provided date in a simple yyyyMMdd manner
         */
        private fun Date.format(): String {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            return sdf.format(this)
        }

        /**
         * gets the file extension (.jpg, .mp4, ...) from the provided Uri
         */
        private fun getFileExtension(uri: Uri): String {
            return uri.path!!.substring(uri.path!!.lastIndexOf('.') + 1)
        }
    }

    /**
     * Convenience method for enqueuing work in.
     */

    /**
     * Starts a download for an image/video of a post
     * Uses Filesystem for Android < Q (10) & MediaStore for >= Q
     */
    override fun doWork(): Result {

        // Get the input data
        val isVideo = workerParams.inputData.getBoolean(KEY_IS_VIDEO, false)
        val downloadUrl = workerParams.inputData.getString(KEY_DOWNLOAD_URL)!!.toUri()
        val filename = workerParams.inputData.getString(KEY_FILENAME)!!

        val resolver = applicationContext.contentResolver
        val notificationId = Random().nextInt() // each download has to have an unique notification id

        // prepare media store
        val values = ContentValues().apply {
            // name
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.TITLE, filename)
            // mimeType
            put(MediaStore.MediaColumns.MIME_TYPE, URLConnection.guessContentTypeFromName(filename))
        }

        val itemUri: Uri
        val outputStream: OutputStream

        /*
         * >= Android Q: Use Media Store
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.apply {
                put(MediaStore.MediaColumns.IS_PENDING, 1)
                put(MediaStore.MediaColumns.RELATIVE_PATH, (if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES) + "/Kraken")
            }
            val tableUri =
                if (isVideo)
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                else
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            // check if already downloaded (based on filename)
            val cursor = resolver.query(tableUri, null, "${MediaStore.MediaColumns.DISPLAY_NAME}='$filename'", null, null, null)
            // skip, if already downloaded
            if (cursor != null &&  cursor.count > 0) {
                Log.d("!!!!", "already downloaded")
                cursor.moveToFirst()
                val existingImageUri = ContentUris.withAppendedId(
                    if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                )
                notificationHelper.notifyDownloadSuccess(notificationId, filename, existingImageUri, isVideo)
                cursor.close()
                return Result.success()
            }
            // not existing: don't skip & prepare outputStream to save to
            else {
                Log.d("!!!!", "not downloaded")
                itemUri = resolver.insert(tableUri, values)!!
                outputStream = resolver.openOutputStream(itemUri, "w")!!
            }
            cursor?.close()
        }

        /*
         * < Android Q: Use Filesystem: prepare directory & file for downloading
         */
        else {
            @Suppress("DEPRECATION")
            val videoFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath + "/Kraken/")
            @Suppress("DEPRECATION")
            val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Kraken/")
            // create directory if not existent
            if (isVideo && !videoFolder.exists())
                videoFolder.mkdirs()
            else if (!imageFolder.exists())
                imageFolder.mkdirs()

            // create file if not existent, but skip, if already downloaded
            val targetPath = if (isVideo) videoFolder.resolve(filename) else imageFolder.resolve(filename)
            if (!targetPath.createNewFile()) {
                notificationHelper.notifyDownloadSuccess(notificationId, filename, Uri.fromFile(targetPath), isVideo)
                return Result.success()
            }
            // not existing: don't skip &
            else {
                @Suppress("DEPRECATION")
                values.put(MediaStore.Images.Media.DATA, targetPath.toString())
                val tableUri = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                itemUri = resolver.insert(tableUri, values)!!
                outputStream = resolver.openOutputStream(itemUri, "w")!!
            }
        }

        // write data to image: DOWNLOAD!
        var lastUpdate = 0L
        val (_, response, result) = Fuel
            .download(downloadUrl.toString())
            .streamDestination { response, _ ->
                Pair(outputStream, { response.body().toStream() })
            }
            .progress { readBytes, totalBytes ->
                // max 2 updates/second - more than 10/second will be blocked
                if (System.currentTimeMillis() - lastUpdate > 500) {
                    lastUpdate = System.currentTimeMillis()
                    notificationHelper.notifyDownloadProgress(notificationId, readBytes.toInt(), totalBytes.toInt())
                }
            }
            .response()
        outputStream.close()

        // check for errors
        if (response.isSuccessful) {
            /*
             * >= Android Q: remove pending status
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
            }
            notificationHelper.notifyDownloadSuccess(notificationId, filename, itemUri, isVideo)
            return Result.success()
        } else {
            // some error occurred
            val error = result.component2()?.message
            notificationHelper.notifyDownloadError(notificationId, error)
            // cleanup: remove row from Media Store
            resolver.delete(itemUri, null, null)
            return Result.failure()
        }
    }

}
