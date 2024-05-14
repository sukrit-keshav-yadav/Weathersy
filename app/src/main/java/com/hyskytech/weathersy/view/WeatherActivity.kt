package com.hyskytech.weathersy.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.hyskytech.weathersy.api.ApiInterface
import com.hyskytech.weathersy.api.retrofitInstance
import com.hyskytech.weathersy.data.WeatherRes
import com.hyskytech.weathersy.databinding.ActivityWeatherBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WeatherActivity : AppCompatActivity() {

    lateinit var binding : ActivityWeatherBinding
    lateinit var city : String

    var temperature : Double ? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getDataFromSharedPref()
        getWeatherData()

    }

    private fun getDataFromSharedPref() {

    }

    private fun getWeatherData() {
       val response = retrofitInstance.retrofit.getWeatherDetails(city,ApiInterface.API_KEY,"metric")

        response.enqueue(object : Callback<WeatherRes>{
            override fun onResponse(call: Call<WeatherRes>, response: Response<WeatherRes>) {
                val responseBody = response.body()
                if (response.isSuccessful){
                    response.body()?.let {
                        temperature = it.main.temp

                        binding.apply {
                            tempText.text = temperature.toString()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<WeatherRes>, t: Throwable) {
                t.let {
                    Toast.makeText(this@WeatherActivity,t.message,Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}