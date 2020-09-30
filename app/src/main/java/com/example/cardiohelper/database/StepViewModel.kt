package com.example.cardiohelper.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class StepViewModel (application: Application): AndroidViewModel(application) {

    private val db = UserDB.get(getApplication())

    fun getAllSteps(): LiveData<Int> = db.stepDao().getAllSteps()
}