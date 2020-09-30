package com.example.cardiohelper.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Step::class], version = 1)

abstract class UserDB : RoomDatabase() {
    abstract fun stepDao(): StepDao

    companion object {

        private var instance: UserDB? = null

        @Synchronized
        fun get(context: Context): UserDB {
            if (instance == null) {
                instance =
                    Room.databaseBuilder(context.applicationContext, UserDB::class.java, "cardiohelper.db")
                        .build()
            }
            return instance!!
        }
    }
}