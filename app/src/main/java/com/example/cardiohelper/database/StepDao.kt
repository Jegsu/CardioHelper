package com.example.cardiohelper.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StepDao {

    @Insert( onConflict = OnConflictStrategy.REPLACE)
    fun insert(step: Step)

    @Query("SELECT COUNT(*) FROM step")
    fun getAllStepsLive(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM step")
    fun getAllSteps(): Int

    @Query("SELECT COUNT(*) FROM step WHERE step.time = :today")
    fun getTodayStepsLive(today: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM step WHERE step.time = :today")
    fun getTodaySteps(today: String): Int
}