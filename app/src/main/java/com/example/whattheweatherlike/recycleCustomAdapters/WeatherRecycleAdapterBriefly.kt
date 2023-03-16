package com.example.whattheweatherlike.recycleCustomAdapters

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.whattheweatherlike.R
import com.example.whattheweatherlike.Weather
import android.view.LayoutInflater

class WeatherRecycleAdapterBriefly(private var weatherList: ArrayList<Weather>) :  RecyclerView.Adapter<WeatherRecycleAdapterBriefly.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var city = view.findViewById(R.id.cityName) as TextView
        var icon = view.findViewById(R.id.icon) as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_item_briefly, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.city.text = this.weatherList[position].city
        holder.icon.setImageResource(this.weatherList[position].iconId)
    }

    override fun getItemCount(): Int {
        return this.weatherList.size
    }

    fun updateData(newData: ArrayList<Weather>){
        this.weatherList.clear()
        this.weatherList.addAll(newData)
        notifyDataSetChanged()
    }
}