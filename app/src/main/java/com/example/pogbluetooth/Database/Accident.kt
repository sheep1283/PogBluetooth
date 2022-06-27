package com.example.pogbluetooth

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


class AccExp(
    var id: Int?,
    var date: String,
    var impulseX: String,
    var impulseY: String,
    var impulseZ: String,
    var accelerationOfGravityX: String,
    var accelerationOfGravityY: String,
    var accelerationOfGravityZ: String,
    var frontBack: String,
    var leftRight: String,
)
@Entity
class Accident(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "date") var date: String,
    @Embedded val accident: List<AccExp>?
){
    constructor(): this(null, "", null) // null 넣으면 안됨 init때문에
}

//@Entity
//class AccExp(
//    @PrimaryKey(autoGenerate = true) var id: Int?,
//    @ColumnInfo(name = "date") var date: String,
//    @ColumnInfo(name = "impulseX") var impulseX: String,
//    @ColumnInfo(name = "impulseY") var impulseY: String,
//    @ColumnInfo(name = "impulseZ") var impulseZ: String,
//    @ColumnInfo(name = "accelerationOfGravityX") var accelerationOfGravityX: String,
//    @ColumnInfo(name = "accelerationOfGravityY") var accelerationOfGravityY: String,
//    @ColumnInfo(name = "accelerationOfGravityZ") var accelerationOfGravityZ: String,
//    @ColumnInfo(name = "frontBack") var frontBack: String,
//    @ColumnInfo(name = "leftRight") var leftRight: String,
//
//    ) {
//    constructor(): this(null, "", "", "", "", "", "", "", "", "")
//}

