package com.example.pogbluetooth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface AccidentDao {
    @Query("SELECT * FROM accident")
    fun getAll(): List<Accident>

    @Insert(onConflict = REPLACE)
    fun insert(accident: Accident)

    @Query("DELETE from accident")
    fun deleteAll()
}