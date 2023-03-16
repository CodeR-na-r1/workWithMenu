package com.example.whattheweatherlike

import android.app.Application
import java.util.*

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        var language = sharedPreferences.getString("language", "ru")
        MainActivity.dLocale = Locale(language ?: "ru") //set any locale you want here
    }
}