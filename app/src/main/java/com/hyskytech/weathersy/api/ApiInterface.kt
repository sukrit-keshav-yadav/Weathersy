package com.hyskytech.weathersy.api

import com.hyskytech.weathersy.data.WeatherRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    companion object{
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
        const val API_KEY = "f6bf60a6801514b1af4576964676a629"
    }

    @GET("weather")
    fun getWeatherDetails(
        @Query("q") city :String,
        @Query("appid") appId : String,
        @Query("units") units : String
    ) : Call<WeatherRes>
}