package me.dio.copa.catar.notification.scheduler.extensions

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.dio.copa.catar.domain.model.MatchDomain
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import java.time.Duration
import java.time.LocalDateTime

const val NOTIFICATION_TITLE_KEY = "NOTIFICATION_TITLE_KEY"
const val NOTIFICATION_CONTENT_KEY = "NOTIFICATION_CONTENT_KEY"

class NotificationMatcherWorker(
    private val context: Context,
    workerParameters: WorkerParameters
): Worker(context, workerParameters) {

    override fun doWork(): Result {
        val title = inputData.getString(NOTIFICATION_TITLE_KEY)
            ?: throw IllegalArgumentException("title is required")
        val content = inputData.getString(NOTIFICATION_CONTENT_KEY)
            ?: throw IllegalArgumentException("content is required")

        context.showNotification(title, content)

        return Result.success()
    }

    companion object {
        fun start(context: Context, match: MatchDomain) {
            val (id, _, _, team1, team2, matchDate) = match

            val initialDelay = Duration.between(LocalDateTime.now(), matchDate).minusMinutes(5)
            val inputData = workDataOf(
                NOTIFICATION_TITLE_KEY to "The game is starting",
                NOTIFICATION_CONTENT_KEY to "Hoje tem ${team1.flag} x ${team2.flag}"
            )

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    id,
                    ExistingWorkPolicy.KEEP,
                    createRequest(initialDelay, inputData)
                )
        }

        fun cancel(context: Context, match: MatchDomain) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(match.id)
        }

        private fun createRequest(initialDelay: Duration, inputData: Data): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<NotificationMatcherWorker>()
                .setInitialDelay(initialDelay)
                .setInputData(inputData)
                .build()


    }
}