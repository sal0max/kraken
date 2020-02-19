package de.salomax.tuck

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.squareup.moshi.Moshi
import de.salomax.tuck.data.*
import java.util.*

class InstaApiWorker(context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val notificationHelper by lazy { Notification(context) }

    /**
     * Convenience method for enqueuing work in.
     */
    companion object {
        private const val KEY_SHORTCODE = "KEY_SHORTCODE"

        fun enqueueWork(context: Context, shortcode: String) {

            WorkManager
                .getInstance(context)
                .enqueue(
                    OneTimeWorkRequestBuilder<InstaApiWorker>()
                        .setInputData(
                            Data.Builder().apply {
                                putString(KEY_SHORTCODE, shortcode)
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
    }

    override fun doWork(): Result {
        // get all info about a post via the Instagram api
        val moshi = Moshi.Builder()
            .add(PostAdapter())
            .add(Date::class.java, UnixTimestampDateJsonAdapter())
            .add(Uri::class.java, UriStringJsonAdapter())
            .build()
            .adapter(Post::class.java)
        val (_, response, result) = Fuel
            .get("https://www.instagram.com/p/${workerParams.inputData.getString(KEY_SHORTCODE)}/?__a=1")
            .responseObject(moshiDeserializerOf(moshi))

        // error: just show a message
        return if (!response.isSuccessful) {
            val error = result.component2()?.message
            notificationHelper.notifyDownloadError(Random().nextInt(), error)
            Result.failure()
        }
        // success: download each media file
        else {
            val post = result.component1()
            post?.images?.forEach {
                ImageDownloadWorker.enqueueWork(applicationContext, it, post)
            }
            Thread.sleep(15000)
            Result.success()
        }
    }

}
