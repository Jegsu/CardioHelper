package com.example.cardiohelper.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SessionDao {

    @Insert( onConflict = OnConflictStrategy.REPLACE)
    fun insert(session: Session)

    @Query("SELECT * FROM session")
    fun getAllSessions(): LiveData<List<Session>>

    @Query("SELECT * FROM session WHERE id= :id")
    fun getSessionFromId(id: Int): Session

    @Query("SELECT SUM(distance) FROM session")
    fun getTotalMetersLive(): LiveData<Double>

    @Query("SELECT AVG(speed) FROM session")
    fun getAvgSpeedLive(): LiveData<Double>

    @Query("SELECT AVG(endTime - startTime) FROM session")
    fun getAvgTimeLive(): LiveData<Long>
}