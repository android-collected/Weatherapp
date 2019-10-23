package com.faskn.app.weatherapp.repo

import NetworkBoundResource
import androidx.lifecycle.LiveData
import com.faskn.app.weatherapp.db.entity.ForecastEntity
import com.faskn.app.weatherapp.domain.ApiResponse
import com.faskn.app.weatherapp.domain.datasource.ForecastLocalDataSource
import com.faskn.app.weatherapp.domain.datasource.ForecastRemoteDataSource
import com.faskn.app.weatherapp.domain.model.ForecastResponse
import com.faskn.app.weatherapp.utils.AppExecutors
import com.faskn.app.weatherapp.utils.domain.RateLimiter
import com.faskn.app.weatherapp.utils.domain.Resource
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Furkan on 2019-10-21
 */

class ForecastRepository @Inject constructor(private val forecastRemoteDataSource: ForecastRemoteDataSource, private val forecastLocalDataSource: ForecastLocalDataSource, private val appExecutors: AppExecutors) {

    private val forecastListRateLimit = RateLimiter<String>(30, TimeUnit.SECONDS)

    fun loadForecastByCityName(cityName: String, fetchRequired: Boolean): LiveData<Resource<ForecastEntity>> {
        return object : NetworkBoundResource<ForecastEntity, ForecastResponse>(appExecutors) {
            override fun saveCallResult(item: ForecastResponse) = forecastLocalDataSource.insertForecast(item)

            override fun shouldFetch(data: ForecastEntity?): Boolean = fetchRequired

            override fun loadFromDb(): LiveData<ForecastEntity> = forecastLocalDataSource.getForecastByCityName(city = cityName.substringBefore(","))

            override fun createCall(): LiveData<ApiResponse<ForecastResponse>> = forecastRemoteDataSource.getForecasyByCityName(city = cityName)

            override fun onFetchFailed() = forecastListRateLimit.reset(RATE_LIMITER_TYPE)
        }.asLiveData()
    }

    companion object {

        private const val RATE_LIMITER_TYPE = "data"
    }
}