package com.example.cardiohelper.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Step(val time: String, @PrimaryKey (autoGenerate = true) val id: Int = 0)
