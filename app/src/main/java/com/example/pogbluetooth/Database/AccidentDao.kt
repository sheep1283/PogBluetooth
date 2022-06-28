package com.example.pogbluetooth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface AccidentDao {
    @Query("SELECT * FROM accident")
    fun getAllAcc(): List<Accident>

    @Query("SELECT * FROM accident WHERE exId = :exId")
    fun getExp(exId: Int): List<Accident>

    @Insert(onConflict = REPLACE)
    fun insertAcc(accident: Accident)

    @Query("DELETE from accident")
    fun deleteAll()


//    @Query("SELECT MAX(exId) FROM accex") // 다음 실험 번호
//    fun getNextId(): Int

}

@Dao
interface AccExDao{
    @Query("SELECT * FROM accex")
    fun getAllExp(): List<AccEx>

    @Insert(onConflict = REPLACE)
    fun insertEx(accEx: AccEx)

    @Query("DELETE from accex")
    fun deleteAll()
}