package com.huawei.mlkit.sample.activity.table

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.huawei.hms.mlsdk.fr.MLFormRecognitionConstant
import com.huawei.hms.mlsdk.fr.MLFormRecognitionTablesAttribute
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.adapter.ItemAdapter
import com.huawei.mlkit.sample.util.BitmapUtils
import com.huawei.mlkit.sample.util.CommonUtils
import jxl.Workbook
import jxl.write.Label
import jxl.write.WritableWorkbook
import jxl.write.WriteException
import java.io.File
import java.io.IOException

class TableRecognitionActivity : AppCompatActivity() {
    lateinit var imageView: ImageView
    private lateinit var recyclerView: RecyclerView
    var adapter: ItemAdapter? = null
    lateinit var fab: FloatingActionButton
    private lateinit var text_error: TextView

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_recognition)
        imageView = findViewById(R.id.image)
        recyclerView = findViewById(R.id.rv)
        fab = findViewById(R.id.fab)
        text_error = findViewById(R.id.error)
        val u = intent.getParcelableExtra<Uri>("uri")
        val str = intent.getStringExtra("json")
        val bitmap = BitmapUtils.tableGetBitmap(this, u)
        imageView.setImageBitmap(bitmap)
        adapter = ItemAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
        recyclerView.adapter = adapter
        try {
            val gson = Gson()
            val attribute = gson.fromJson(str, MLFormRecognitionTablesAttribute::class.java)
            if (attribute.retCode == MLFormRecognitionConstant.SUCCESS) {
                for (i in attribute.tablesContent.tableAttributes[0].tableCellAttributes.indices) {
                    adapter!!.list.add(attribute.tablesContent.tableAttributes[0].tableCellAttributes[i])
                }
                adapter!!.notifyDataSetChanged()
            } else if (attribute.retCode == MLFormRecognitionConstant.FAILED) {
                text_error.text = getString(R.string.teble_error)
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, e.message!!)
        }
        fab.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@TableRecognitionActivity,
                    CommonUtils.STORAGE_PERMISSION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    createExcel()
                } catch (e: IOException) {
                    Log.e(TAG, e.message!!)
                } catch (e: WriteException) {
                    Log.e(TAG, e.message!!)
                }
            } else {
                ActivityCompat.requestPermissions(
                    this@TableRecognitionActivity, arrayOf(
                        CommonUtils.STORAGE_PERMISSION
                    ), CommonUtils.PERMISSION_CODE_STORAGE
                )
            }
        }
    }

    @SuppressLint("LongLogTag")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == CommonUtils.PERMISSION_CODE_STORAGE && grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                createExcel()
            } catch (e: IOException) {
                Log.e(TAG, e.message!!)
            } catch (e: WriteException) {
                Log.e(TAG, e.message!!)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @Throws(IOException::class, WriteException::class)
    fun createExcel() {
        val workbook: WritableWorkbook?
        val fileName = externalCacheDir!!.path + File.separator + "TableRecognition.xls"
        val file = File(fileName)
        if (!file.exists()) {
            file.delete()
            file.createNewFile()
        }
        workbook = Workbook.createWorkbook(file)
        val sheet = workbook.createSheet("sheet 1", 0)
        for (i in adapter!!.list.indices) {
            val l = Label(
                adapter!!.list[i]!!.startCol, adapter!!.list[i]!!.startRow, adapter!!.list[i]!!
                    .textInfo
            )
            sheet.addCell(l)
            sheet.mergeCells(
                adapter!!.list[i]!!.startCol, adapter!!.list[i]!!.startRow, adapter!!.list[i]!!
                    .endCol, adapter!!.list[i]!!.endRow
            )
        }
        workbook.write()
        workbook.close()
        Toast.makeText(this, "create table successfully,location\n $fileName", Toast.LENGTH_SHORT)
            .show()
    }

    companion object {
        private const val TAG = "TableRecognitionActivity"
    }
}