package de.salomax.kraken

import android.content.Context
import androidx.work.*
import com.github.kittinunf.fuel.core.isSuccessful
import de.salomax.kraken.repository.*
import java.util.*

class ApiWorker(context: Context, private val workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val notificationHelper by lazy { Notification(context) }

    /**
     * Convenience method for enqueuing work in.
     */
    companion object {
        private const val KEY_SHORTCODE = "KEY_SHORTCODE"
        private const val KEY_ENDPOINT = "KEY_ENDPOINT"

        fun enqueueWork(context: Context, shortcode: String, endpoint: String) {

            WorkManager
                .getInstance(context)
                .enqueue(
                    OneTimeWorkRequestBuilder<ApiWorker>()
                        .setInputData(
                            Data.Builder().apply {
                                putString(KEY_SHORTCODE, shortcode)
                                putString(KEY_ENDPOINT, endpoint)
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
        val (response, result) = InstagramService.getPost(
            workerParams.inputData.getString(KEY_SHORTCODE)!!,
            workerParams.inputData.getString(KEY_ENDPOINT)!!
        )

        // error: just show a message
        return if (!response.isSuccessful) {
            val error = if (response.statusCode == 404)
                applicationContext.getString(R.string.download_error_private)
            else
                result.component2()?.message
            notificationHelper.notifyDownloadError(Random().nextInt(), error)
            Result.failure()
        }
        // some other error
        else if (result.component1() == null) {
            notificationHelper.notifyDownloadError(
                Random().nextInt(),
                applicationContext.getString(R.string.download_error_generic)
            )
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
