package com.example.cardiohelper.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Formatter {

    fun formatDistance(distance: Double): String {
        val kilometers = distance / 1000
        return when {
            kilometers > 1 -> "${String.format("%.2f", kilometers)} km"
            distance > 0 -> "${String.format("%.0f", distance)} m"
            else -> {
                "0 m"
            }
        }
    }

    fun formatDuration(startTime: Long, endTime: Long): String {
        val difference = endTime - startTime
        val hours = TimeUnit.MILLISECONDS.toHours(difference).toInt() % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(difference).toInt() % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(difference).toInt() % 60
        return when {
            hours > 0 -> "$hours hours $minutes minutes $seconds seconds"
            minutes > 0 -> "$minutes minutes $seconds seconds"
            seconds > 0 -> "$seconds seconds"
            else -> {
                "0"
            }
        }
    }

    fun formatAverageDuration(time: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(time).toInt() % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(time).toInt() % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(time).toInt() % 60
        return when {
            hours > 0 -> "$hours hours $minutes m $seconds s"
            minutes > 0 -> "$minutes m $seconds s"
            seconds > 0 -> "$seconds s"
            else -> {
                "0"
            }
        }
    }

    fun formatSpeed(metersPerSecond: Float): String {
        val kilometersPerHour = metersPerSecond * 3.6
        return "${String.format("%.2f", kilometersPerHour)} km/h"
    }

    fun formatAverageSpeed(kilometersPerHour: Double): String {
        return "${String.format("%.2f", kilometersPerHour)} km/h"
    }

    @SuppressLint("SimpleDateFormat")
    fun formatDate(startTime: Long): String {
        return SimpleDateFormat("MMM d YYYY").format(Date(startTime))
    }
}