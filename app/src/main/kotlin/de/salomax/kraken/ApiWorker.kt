package de.salomax.kraken

import android.content.Context
import androidx.work.*
import com.github.kittinunf.fuel.core.isSuccessful
import de.salomax.kraken.data.*
import java.util.*

class ApiWorker(context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {

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
                    OneTimeWorkRequestBuilder<ApiWorker>()
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
        val (response, result) = InstagramService.getPost(workerParams.inputData.getString(KEY_SHORTCODE))

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
                DownloadWorker.enqueueWork(applicationContext, it, post)
            }
            Result.success()
        }
    }

}
