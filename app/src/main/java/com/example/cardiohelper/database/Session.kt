package com.example.cardiohelper.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.geojson.Point


@Entity
data class Session(
    val startTime: Long,
    val endTime: Long,
    val geoPoints: String,
    val distance: Double,
    val speed: Double,
    @PrimaryKey(autoGenerate = true) var id: Int = 0
)
