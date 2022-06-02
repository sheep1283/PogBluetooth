package com.example.pogbluetooth

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity
class Accident(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "impulseX") var impulseX: String,
    @ColumnInfo(name = "impulseY") var impulseY: String,
    @ColumnInfo(name = "impulseZ") var impulseZ: String,
    @ColumnInfo(name = "accelerationOfGravityX") var accelerationOfGravityX: String,
    @ColumnInfo(name = "accelerationOfGravityY") var accelerationOfGravityY: String,
    @ColumnInfo(name = "accelerationOfGravityZ") var accelerationOfGravityZ: String,
    @ColumnInfo(name = "frontBack") var frontBack: String,
    @ColumnInfo(name = "leftRight") var leftRight: String,

    ) {
    constructor(): this(null, "", "", "", "", "", "", "", "", "")
}