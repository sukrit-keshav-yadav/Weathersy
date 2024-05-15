package com.hyskytech.weathersy.view

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import com.hyskytech.weathersy.Constants.Constants
import com.hyskytech.weathersy.R
import com.hyskytech.weathersy.api.ApiInterface
import com.hyskytech.weathersy.api.retrofitInstance
import com.hyskytech.weathersy.data.WeatherRes
import com.hyskytech.weathersy.databinding.ActivityWeatherBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class WeatherActivity : AppCompatActivity() {


    private lateinit var binding: ActivityWeatherBinding
    lateinit var city: String

    var temperature: String? = null
    var condition: String? = null
    var minTemp: String? = null
    var maxTemp: String? = null
    var day: String? = null
    var date: String? = null
    var humidity: String? = null
    var windSpeed: String? = null
    var sunrise: Long? = null
    var sunset: Long? = null
    var sea: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("application", MODE_PRIVATE)
        if (sharedPreferences.contains(Constants.IS_AVAILABLE)) {
            getDataFromSharedPref()
        } else {
            binding.searchView.requestFocus()
            searchWeatherOfACity()
        }
    }

    private fun searchWeatherOfACity() {
        val searchView: SearchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean {
                if (q != null) {
                    getWeatherData(q)
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }
        })
    }

    private fun getDataFromSharedPref() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("application", MODE_PRIVATE)
        city = sharedPreferences.getString(Constants.CITY, "mumbai").toString()
        temperature = sharedPreferences.getString(Constants.TEMP, "34").toString()
        minTemp = sharedPreferences.getString(Constants.MIN_TEMP, "33.94").toString()
        maxTemp = sharedPreferences.getString(Constants.MAX_TEMP, "34.99").toString()
        day = sharedPreferences.getString(Constants.DAY, "Wednesday").toString()
        date = sharedPreferences.getString(Constants.DATE, "15 May 2023").toString()
        condition = sharedPreferences.getString(Constants.CONDITION, "Haze").toString()
        humidity = sharedPreferences.getString(Constants.HUMIDITY, "67").toString()
        windSpeed = sharedPreferences.getString(Constants.WIND, "6.17").toString()
        sunrise = sharedPreferences.getLong(Constants.SUNRISE, 1715733250)
        sunset = sharedPreferences.getLong(Constants.SUNSET, 1715780156)
        sea = sharedPreferences.getString(Constants.SEA, "1006").toString()

        updateDataOnUi()
        changeUiBasedOnCondition(condition!!)
    }

    private fun getWeatherData(cityName: String) {
        val response =
            retrofitInstance.retrofit.getWeatherDetails(cityName, ApiInterface.API_KEY, "metric")

        response.enqueue(object : Callback<WeatherRes> {
            override fun onResponse(call: Call<WeatherRes>, response: Response<WeatherRes>) {
                val responseBody = response.body()
                if (responseBody?.cod == 200) {
                    if (response.isSuccessful) {
                        responseBody.let {
                            temperature = it.main.temp.toInt().toString()
                            windSpeed = it.wind.speed.toString()
                            condition = it.weather.firstOrNull()?.main ?: "Unknown"
                            minTemp = it.main.temp_min.toString()
                            maxTemp = it.main.temp_max.toString()
                            humidity = it.main.humidity.toString()
                            sunrise = it.sys.sunrise.toLong()
                            sunset = it.sys.sunset.toLong()
                            sea = it.main.pressure.toString()
                            day = getCurrentDay()
                            date = getCurrentDate()

                            val sharedPreferences: SharedPreferences = this@WeatherActivity.getSharedPreferences("application", MODE_PRIVATE)
                            val editor: SharedPreferences.Editor = sharedPreferences.edit()

                            editor.clear()
                            editor.apply {
                                putString(Constants.CITY, city)
                                putString(Constants.TEMP, temperature)
                                putString(Constants.MIN_TEMP, minTemp)
                                putString(Constants.MAX_TEMP, maxTemp)
                                putString(Constants.DAY, day)
                                putString(Constants.DATE, date)
                                putString(Constants.CONDITION, condition)
                                putString(Constants.HUMIDITY, humidity)
                                putString(Constants.WIND, windSpeed)
                                putLong(Constants.SUNSET, sunset!!)
                                putLong(Constants.SUNRISE, sunrise!!)
                                putString(Constants.SEA, sea)
                                putBoolean(Constants.IS_AVAILABLE, true)
                            }

                            editor.apply()

                            updateDataOnUi()
                            changeUiBasedOnCondition(condition!!)
                        }
                    }
                }else{
                    if (responseBody?.cod==404) Toast.makeText(this@WeatherActivity,"city not found",Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<WeatherRes>, t: Throwable) {
                t.let {
                    Toast.makeText(this@WeatherActivity, t.message, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun changeUiBasedOnCondition(condition: String) {
        when (condition) {
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_clear)
                binding.lottieCondition.setAnimation(R.raw.sunny)
            }

            "Partly Clouds", "Clouds", "Mist", "Foggy", "Overcast" -> {
                binding.root.setBackgroundResource(R.drawable.cloudy)
                binding.lottieCondition.setAnimation(R.raw.cloudy)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rainy)
                binding.lottieCondition.setAnimation(R.raw.rainy)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snowwy)
                binding.lottieCondition.setAnimation(R.raw.snowwy)
            }
        }
        binding.lottieCondition.playAnimation()
    }


    private fun getTime(timestamps: Long?): String? {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamps!! * 1000)))
    }

    private fun getCurrentDate(): String? {
        val sdf = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun getCurrentDay(): String? {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }

    @SuppressLint("SetTextI18n")
    private fun updateDataOnUi() {
        binding.apply {
            consMain.visibility = View.VISIBLE
            tempText.text = "$temperature${R.string.celsius_sign}"
            cityText.text = city
            minMaxTemp.text = "Max :$maxTemp${R.string.celsius_sign}\nMin :$minTemp${R.string.celsius_sign}"
            conditionText.text = "$condition"
            dayDate.text = "$day  $date"
            conditionVal.text = "$condition"
            humidityVal.text = "$humidity%"
            windVal.text = "$windSpeed m/s"
            sunriseVal.text = "${getTime(sunrise)}"
            sunsetVal.text = "${getTime(sunset)}"
            seaVal.text = "$sea hPa"
        }
    }
}