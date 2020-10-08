package com.example.cardiohelper.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class SessionViewModel (application: Application): AndroidViewModel(application)  {

    private val db = UserDB.get(getApplication())

    fun getAllSessions(): LiveData<List<Session>> = db.sessionDao().getAllSessions()

    fun getSessionFromId(id: Int): Session = db.sessionDao().getSessionFromId(id)

    fun getTotalMetersLive(): LiveData<Double> = db.sessionDao().getTotalMetersLive()

    fun getAvgSpeedLive(): LiveData<Double> = db.sessionDao().getAvgSpeedLive()

    fun getAvgTimeLive(): LiveData<Long> = db.sessionDao().getAvgTimeLive()
}