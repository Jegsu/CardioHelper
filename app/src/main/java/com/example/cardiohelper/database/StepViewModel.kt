package com.example.cardiohelper.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StepViewModel (application: Application): AndroidViewModel(application) {

    private val date = LocalDateTime.now()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val formattedDate = date.format(formatter)

    private val db = UserDB.get(getApplication())

    fun getAllStepsLive(): LiveData<Int> = db.stepDao().getAllStepsLive()

    fun getTodayStepsLive(): LiveData<Int> = db.stepDao().getTodayStepsLive(formattedDate)
}