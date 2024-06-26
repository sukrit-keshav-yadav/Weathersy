package com.hyskytech.weathersy.view

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import com.hyskytech.weathersy.Constants.Constants
import com.hyskytech.weathersy.R
import com.hyskytech.weathersy.api.ApiInterface.Companion.API_KEY
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
    var city: String? = null
    var temperature: Double? = 0.0
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

        searchWeatherOfACity()

        val sharedPreferences: SharedPreferences =
            this@WeatherActivity.getSharedPreferences("application", MODE_PRIVATE)
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
        val sharedPreferences: SharedPreferences =
            this@WeatherActivity.getSharedPreferences("application", MODE_PRIVATE)
        city = sharedPreferences.getString(Constants.CITY, "mumbai").toString()
        temperature = sharedPreferences.getLong(Constants.TEMP, 34).toDouble()
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
//        changeUiBasedOnCondition(condition!!)
    }

    private fun getWeatherData(cityName: String) {

        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.isIndeterminate = true
        binding.progressBar.animate()
        val response = retrofitInstance.retrofit.getWeatherDetails(
            cityName,
            API_KEY,
            "metric"
        )

        response.enqueue(object : Callback<WeatherRes> {
            override fun onResponse(call: Call<WeatherRes>, response: Response<WeatherRes>) {
                val responseBody = response.body()
                if (response.isSuccessful) {
                    if (responseBody != null) {
                        when (responseBody.cod) {
                            in 200..299 -> {
                                binding.progressBar.visibility = View.INVISIBLE
                                responseBody.let {
                                    Log.d("TAG", "$responseBody")
                                    city = it.name
                                    temperature = it.main.temp
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

                                    updateDataOnUi()
                                    changeUiBasedOnCondition(condition!!)


                                    val sharedPreferences: SharedPreferences =
                                        this@WeatherActivity.getSharedPreferences(
                                            "application",
                                            MODE_PRIVATE
                                        )
                                    val editor: SharedPreferences.Editor = sharedPreferences.edit()

                                    editor.clear()
                                    editor.apply {
                                        putString(Constants.CITY, city)
                                        putLong(Constants.TEMP, temperature!!.toLong())
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
                                }
                            }

                            in 300..399 -> {
                                Toast.makeText(this@WeatherActivity,"Redirection",Toast.LENGTH_LONG).show()
                                binding.progressBar.visibility=View.INVISIBLE
                                Log.d("Response", "Redirection")
                            }

                            in 400..499 -> {
                                if (responseBody.message !=null){
                                    Toast.makeText(this@WeatherActivity,responseBody.message,Toast.LENGTH_LONG).show()
                                    binding.progressBar.visibility=View.INVISIBLE
                                }else{
                                Toast.makeText(this@WeatherActivity,"Client Error",Toast.LENGTH_LONG).show()
                                binding.progressBar.visibility=View.INVISIBLE
                                Log.d("Response", "Client Error")
                                }
                            }

                            in 500..599 -> {
                                Toast.makeText(this@WeatherActivity,"Server Error",Toast.LENGTH_LONG).show()
                                binding.progressBar.visibility=View.INVISIBLE
                                Log.d("Response", "Server Error")
                            }
                        }
                    }

                }
            }


            override fun onFailure(call: Call<WeatherRes>, t: Throwable) {
                t.let {
                    binding.progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this@WeatherActivity, t.message, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun changeUiBasedOnCondition(condition: String) {
        when (condition) {
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_clear)
                binding.lottieCondition.setAnimation(R.raw.sun)
            }

            "Partly Clouds", "Clouds", "Mist", "Foggy", "Haze", "Smoke", "Overcast" -> {
                binding.root.setBackgroundResource(R.drawable.cloudy)
                binding.lottieCondition.setAnimation(R.raw.cloud)
            }

            "Light Rain", "Drizzle", "Rain", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rainy)
                binding.lottieCondition.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snowwy)
                binding.lottieCondition.setAnimation(R.raw.snow)
            }
            else ->{
                binding.root.setBackgroundResource(R.drawable.snowwy)
                binding.lottieCondition.setAnimation(R.raw.snow)
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
        changeUiBasedOnCondition(condition!!)
        binding.apply {
            consMain.visibility = View.VISIBLE
            tempText.text = "${temperature!!.toInt()}°C"
            cityText.text = city
            minMaxTemp.text = "Max :$maxTemp°C\nMin :$minTemp°C"
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