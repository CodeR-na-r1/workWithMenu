package com.example.whattheweatherlike

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class DataModel : ViewModel() {
    val data: MutableLiveData<MutableList<Weather>> by lazy {
        MutableLiveData (mutableListOf())
    }
}