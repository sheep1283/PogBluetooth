package com.example.pogbluetooth.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pogbluetooth.AccEx
import com.example.pogbluetooth.AccExDao
import com.example.pogbluetooth.Accident
import com.example.pogbluetooth.AccidentDao

@Database(entities = [Accident::class, AccEx::class], version = 8, exportSchema = false)
abstract class AccidentDB: RoomDatabase() {
    abstract fun accidentDao(): AccidentDao
    abstract fun accExDao(): AccExDao

    companion object{
        private var INSTANCE: AccidentDB? = null

        fun getInstance(context: Context): AccidentDB? {
            if (INSTANCE == null) {
                synchronized(AccidentDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        AccidentDB::class.java, "accident.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}