package de.salomax.tuck

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import de.salomax.tuck.data.InstagramService
import io.reactivex.disposables.Disposable
import de.salomax.tuck.data.Post
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import zlc.season.rxdownload3.RxDownload
import zlc.season.rxdownload3.core.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import android.media.MediaScannerConnection

class TuckService : Service() {

    private var disposables: MutableList<Disposable> = mutableListOf()
    private val idGenerator = AtomicInteger()

    private val instagramService by lazy { InstagramService.create() }
    private val notificationHelper by lazy { Notification(this) }

    // where to save to
    private val picturePath by lazy { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/Tuck/" }
    private val moviePath by lazy { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath + "/Tuck/" }

    /**
     * Convenience method for enqueuing work in to this service.
     */
    companion object {
        fun enqueueWork(context: Context, shortcode: String) {
            val work = Intent(context, TuckService::class.java)
            work.putExtra("shortcode", shortcode)
            context.startService(work)
        }
    }

    override fun onBind(work: Intent?): IBinder? {
        return null
        //    stopSelf() TODO: stop at some point??
    }

    override fun onDestroy() {
        disposables.forEach {
            it.dispose()
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        getPostDetails(intent.getStringExtra("shortcode"))
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Gets all info about a post via Instagram API:
     * url(s), datetime, user, ...
     */
    private fun getPostDetails(shortcode: String) {
        disposables.add(
            instagramService.getPost(shortcode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> parseResult(result) },
                    { error -> notificationHelper.notifyDownloadError(idGenerator.incrementAndGet(), error.message) }
                )
        )
    }

    /**
     * Starts a download for each image/video of the post
     */
    @SuppressLint("CheckResult")
    private fun parseResult(post: Post) {
        post.images
            .forEach { image ->
                // each download has to have an unique id for the notifications
                val id = idGenerator.incrementAndGet()

                // create download mission
                val url = if (image.isVideo) image.videoUrl!! else image.imageUrl
                val path = if (image.isVideo) moviePath else picturePath
                val filename = post.owner.username.replace('.', '_') +
                        ".${post.dateTime.format()}" +
                        ".${image.shortcode}" +
                        "." +
                        getFileExtension(url)
                val mission = Mission(url.toString(), filename, path)

                // download
                disposables.add(RxDownload.create(mission, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { status ->
                        when (status) {
                            is Normal -> { }
                            is Suspend -> { }
                            is Waiting -> { }
                            is Downloading -> {
                                notificationHelper.notifyDownloadProgress(id, status.downloadSize.toInt(), status.totalSize.toInt())
                            }
                            is Failed -> {
                                notificationHelper.notifyDownloadError(id, status.throwable.message)
                            }
                            is Succeed -> {
                                notificationHelper.notifyDownloadSuccess(id, filename, path + filename, image.isVideo)
                                // notify system about new image
                                MediaScannerConnection.scanFile(this, arrayOf(path + filename), null) { _, _ -> }
                            }
                        }
                    }
                )
            }
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
