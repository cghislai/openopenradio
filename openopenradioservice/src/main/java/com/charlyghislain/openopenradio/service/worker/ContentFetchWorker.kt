package com.charlyghislain.openopenradio.service.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.charlyghislain.openopenradio.service.radio.repository.CountryRepository
import com.charlyghislain.openopenradio.service.radio.repository.GenreRepository
import com.charlyghislain.openopenradio.service.radio.repository.LanguageRepository
import com.charlyghislain.openopenradio.service.radio.repository.StationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.future.await

@HiltWorker
class ContentFetchWorker
@AssistedInject
constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val countryRepository: CountryRepository,
    private val genreRepository: GenreRepository,
    private val languageRepository: LanguageRepository,
    private val stationRepository: StationRepository
) : CoroutineWorker(appContext, workerParams) {


    override suspend fun doWork(): Result {
        return try {
            genreRepository.fetchGenres().await()
            countryRepository.fetchCountries().await()
            languageRepository.fetchLanguages().await()
            stationRepository.fetchStations().await()

            Result.success()
        } catch (e: Exception) {
            Result.retry() // Retry on failure
        }
    }
}