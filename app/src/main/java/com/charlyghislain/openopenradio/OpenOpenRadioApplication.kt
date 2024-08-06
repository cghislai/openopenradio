package com.charlyghislain.openopenradio

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.charlyghislain.openopenradio.service.worker.ContentFetchWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class OpenOpenRadioApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory // Inject directly


    override fun onCreate() {
        super.onCreate()
        scheduleContentFetch()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun scheduleContentFetch() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val initialRequest =
            OneTimeWorkRequestBuilder<ContentFetchWorker>()
                .setConstraints(constraints)
                .build()

        val periodicRequest =
            PeriodicWorkRequestBuilder<ContentFetchWorker>(
                1, TimeUnit.DAYS // Adjust repeat interval as needed
            ).setConstraints(constraints)
                .build()


        WorkManager.getInstance(this).enqueueUniqueWork(
            "initialContentFetch",
            ExistingWorkPolicy.KEEP,
            initialRequest
        )

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "periodicContentFetch",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

}
