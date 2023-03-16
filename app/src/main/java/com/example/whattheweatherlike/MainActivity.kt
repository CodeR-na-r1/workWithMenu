package com.example.whattheweatherlike

import android.content.res.Configuration
import com.example.whattheweatherlike.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*
import androidx.databinding.DataBindingUtil
import com.example.whattheweatherlike.fragments.FragmentWeatherBriefly
import com.example.whattheweatherlike.fragments.FragmentWeatherDetail
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.reflect.Type

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var API_KEY: String

    private var cities = mutableListOf<String>()
    private var _innerWeather = mutableListOf<Weather>()
    private val weatherInfoList: DataModel by viewModels()

    var currentFragment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("myLog", "onCreate")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_layout, FragmentWeatherDetail.newInstance()).commit()

        this.API_KEY = resources.getString(R.string.API_KEY)

        binding.btnAddCity.setOnClickListener {
            if (addCity(binding.inputCityName.text.toString())) {
                this.getWeatherForCity(binding.inputCityName.text.toString())
                binding.inputCityName.setText("")
                this.weatherInfoList.data.value = _innerWeather
            }
        }

        binding.btnUpdateTemp.setOnClickListener {
            this.updateWeatherForAllCities()
            this.weatherInfoList.data.value = _innerWeather
        }

        binding.btnChangeFragment.setOnClickListener {
            if (currentFragment) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_layout, FragmentWeatherDetail.newInstance())
                    .commit()
                binding.btnChangeFragment.text = "Кратко"
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_layout, FragmentWeatherBriefly.newInstance())
                    .commit()
                binding.btnChangeFragment.text = "Подробнее"
            }
            currentFragment = !currentFragment
        }
    }

    private fun addCity(text: String): Boolean {
        return if (text == "") {
            Toast.makeText(this, "The name of the city is empty", Toast.LENGTH_SHORT).show()
            false
        } else {
            this.cities.add(text)
            true
        }
    }

    private fun requestWeatherForCity(nameCity: String): Weather {
        val weatherURL =
            "https://api.openweathermap.org/data/2.5/weather?q=${nameCity}&appid=${this.API_KEY}&units=metric";
        var data = ""
        val weather = Weather()
        weather.city = nameCity

        lateinit var stream: InputStream

        try {
            stream = URL(weatherURL).getContent() as InputStream
        } catch (e: IOException) {
            weather.city = "Error Internet connection!"
        }

        try {
            data = Scanner(stream).nextLine() ?: ""

            val jsonObj = JSONObject(data)

            weather.temp = jsonObj.getJSONObject("main").getString("temp").let { "$it°" }
            weather.humidity = jsonObj.getJSONObject("main").getString("humidity").let { "$it%" }

            val iconName =
                JSONObject(jsonObj.getJSONArray("weather").getString(0)).getString("icon")
                    .let { "_${it.subSequence(1, it.length)}" }
            weather.iconId = resources.getIdentifier(iconName, "drawable", packageName)
        } catch (e: Exception) {
            weather.city = "Ошибка запроса! (проверьте название города)"
        }

        return weather
    }

    private fun updateWeatherForAllCities() {
        GlobalScope.launch(Dispatchers.IO) {
            weatherInfoList.data.value?.clear()

            cities.forEach { it -> getWeatherForCity(it) }
        }
    }

    private fun getWeatherForCity(nameCity: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val resWeather = requestWeatherForCity(nameCity)
            _innerWeather.add(resWeather)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("myLog", "onPause")

        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)

        val dataCities = Gson().toJson(this.cities)
        sharedPreferences.edit().putString("citiesData", dataCities).apply()

        val dataWeatherInfo = Gson().toJson(this.weatherInfoList.data.value)
        sharedPreferences.edit().putString("weatherInfoData", dataWeatherInfo).apply()
    }

    override fun onResume() {
        super.onResume()
        Log.d("myLog", "onResume")

        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)

        // load list of cities

        val dataCitiesJson = sharedPreferences.getString("citiesData", "")

        try {
            val type: Type = object : TypeToken<MutableList<String>>() {}.type
            this.cities = Gson().fromJson<MutableList<String>>(dataCitiesJson, type)
        } catch (e: Exception) {
            Log.d("myLog", "Error Load Data about cities!")
        }

        // load list with weather for cities

        val dataWeatherInfoJson = sharedPreferences.getString("weatherInfoData", "")

        try {
            val type: Type = object : TypeToken<MutableList<Weather>>() {}.type
            val dataFromStorage = Gson().fromJson<MutableList<Weather>>(dataWeatherInfoJson, type)

            if (dataFromStorage != null) {
                this.weatherInfoList.data.value = dataFromStorage
                _innerWeather = dataFromStorage
            }
        } catch (e: Exception) {
            Log.d("myLog", "Error Load Data about weather!")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 0, 0, "Английский язык")
        menu?.add(0, 1, 0, "Русский язык")

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)

        when (item.itemId) {
            0 -> {
                Log.d("myTag", "EN")
                sharedPreferences.edit().putString("language", "en").apply()
            }
            1 -> {
                Log.d("myTag", "RU")
                sharedPreferences.edit().putString("language", "ru").apply()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        public var dLocale: Locale? = null
    }

    init {
        updateConfig(this)
    }

    fun updateConfig(wrapper: ContextThemeWrapper) {
        if (dLocale == Locale("") ) // Do nothing if dLocale is null
            return

        Locale.setDefault(dLocale)
        val configuration = Configuration()
        configuration.setLocale(dLocale)
        wrapper.applyOverrideConfiguration(configuration)
    }
}