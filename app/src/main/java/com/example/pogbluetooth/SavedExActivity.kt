package com.example.pogbluetooth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pogbluetooth.Database.AccidentDB
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SavedExActivity : AppCompatActivity() {
    private var accidentDb: AccidentDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_ex)
        accidentDb = AccidentDB.getInstance(this)


        fun <T> csvOf(
            headers: List<String>,
            data: List<T>,
            itemBuilder: (T) -> List<String>
        ) = buildString {
            append(headers.joinToString(",") { "\"$it\"" })
            append("\n")
            data.forEach { item ->
                append(itemBuilder(item).joinToString(",") { "\"$it\"" })
                append("\n")
            }
        }

        val oneDayClassAdapter = ExRecyclerViewAdapter()
        val oneRecyclerView = findViewById<RecyclerView>(R.id.ex_recycle)
        oneRecyclerView.layoutManager = LinearLayoutManager(this)
        oneRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
        }
        oneRecyclerView.setHasFixedSize(true)
        oneRecyclerView.adapter = oneDayClassAdapter
        // 처음에 있는 실험값 받아오
        val r = Runnable{
            var exList = accidentDb!!.accExDao().getAllExp()
            oneDayClassAdapter.setData(ArrayList(exList))
        }
        val thread = Thread(r)
        thread.start()

        oneDayClassAdapter.setItemClickListener(object : ExRecyclerViewAdapter.ItemClickListener{
            override fun onClick(view: View, position: Int) {
                var save = false
                val r = Runnable{
                    val exList = accidentDb!!.accidentDao().getExp(position) // 일단 0번 실험 목록
                    Log.d("exList IS data: ", exList[0].date)
                    Log.d("exList IS X: ", exList[0].impulseX)
                    Log.d("exList IS Y: ", exList[0].impulseY)
                    Log.d("exList IS Z: ", exList[0].impulseZ)
                    Log.d("exList IS ax: ", exList[0].accelerationOfGravityX)
                    Log.d("exList IS ay: ", exList[0].accelerationOfGravityY)
                    Log.d("exList IS az: ", exList[0].accelerationOfGravityZ)
                    Log.d("exList IS f: ", exList[0].frontBack)
                    Log.d("exList IS r: ", exList[0].leftRight.substring(0, 5))
                    var exListToCsv = arrayListOf<Experiment>()
                    val csvString = StringBuilder()
                    csvString.append("date")
                    csvString.append(",")
                    csvString.append("accelerationOfGravityX")
                    csvString.append(",")
                    csvString.append("accelerationOfGravityY")
                    csvString.append(",")
                    csvString.append("accelerationOfGravityZ")
                    csvString.append(",")
                    csvString.append("impluseX")
                    csvString.append(",")
                    csvString.append("impluseY")
                    csvString.append(",")
                    csvString.append("impluseZ")
                    csvString.append(",")
                    csvString.append("frontback")
                    csvString.append(",")
                    csvString.append("leftright")
                    csvString.append("\n")

                    for (i in exList){
                        csvString.append(i.date.substring(0, 19).toString())
                        csvString.append(",")
                        csvString.append(i.accelerationOfGravityX.toString())
                        csvString.append(",")
                        csvString.append(i.accelerationOfGravityY.toString())
                        csvString.append(",")
                        csvString.append(i.accelerationOfGravityZ.toString())
                        csvString.append(",")
                        csvString.append(i.impulseX.toString())
                        csvString.append(",")
                        csvString.append(i.impulseY.toString())
                        csvString.append(",")
                        csvString.append(i.impulseZ.toString())
                        csvString.append(",")
                        csvString.append(i.frontBack.toString())
                        csvString.append(",")
                        csvString.append(i.leftRight.substring(0, 5).toString())
                        csvString.append("\n")
                    }

                    Log.d("exList IS r: ", csvString.toString())

//                Log.d("exList csv : ", csv)


                    fun saveData(){
                        val long_now = System.currentTimeMillis()
                        val t_date = Date(long_now)
                        val t_dateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm:ss E", Locale("ko", "KR"))

                        val str_date = t_dateFormat.format(t_date)
                        var root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        root = File(root, "PogBluetoothApp")
                        root.mkdir()

                        root = File(root, "pog_data.csv")
                        try {
                            var fout = FileOutputStream(root);
                            fout.write(csvString.toString().toByteArray());
                            save = true
                            fout.close();
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace();

                            var bool = false;
                            try {
                                // try to create the file
                                bool = root.createNewFile();
                            } catch (ex: IOException) {
                                ex.printStackTrace();
                            }

                            if (bool){
                                // call the method again
                                saveData()
                            }else {
                                throw IllegalStateException("Failed to create image file");
                            }
                        } catch (e: IOException) {
                            e.printStackTrace();
                        }
                    }
                    saveData()
                }
                val thread = Thread(r)
                thread.start()
                if (save == true){
                    Toast.makeText(this@SavedExActivity, "다운로드 폴더에 pog_data.csv 생성이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }

        })



    }
}