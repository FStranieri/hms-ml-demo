package com.huawei.mlkit.sample.activity.table

import com.huawei.mlkit.sample.activity.BaseActivity.onCreate
import com.huawei.mlkit.sample.activity.BaseActivity.setStatusBar
import com.huawei.mlkit.sample.activity.BaseActivity.setStatusBarFontColor
import com.huawei.mlkit.sample.activity.adapter.ItemAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import com.huawei.mlkit.sample.R
import android.graphics.Bitmap
import com.google.gson.Gson
import com.huawei.hms.mlsdk.fr.MLFormRecognitionTablesAttribute
import com.huawei.hms.mlsdk.fr.MLFormRecognitionConstant
import com.huawei.mlkit.sample.activity.table.TableRecognitionActivity
import android.content.pm.PackageManager
import jxl.write.WriteException
import kotlin.Throws
import jxl.write.WritableWorkbook
import jxl.Workbook
import jxl.write.WritableSheet
import android.graphics.BitmapFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzer
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzerFactory
import com.google.gson.JsonObject
import com.huawei.hmf.tasks.OnSuccessListener
import android.content.Intent
import com.huawei.hmf.tasks.OnFailureListener
import android.content.DialogInterface
import android.provider.MediaStore
import android.content.ContentValues
import android.app.Activity
import com.huawei.mlkit.sample.transactor.LocalObjectTransactor
import com.huawei.mlkit.sample.activity.`object`.ObjectDetectionActivity
import com.bumptech.glide.Glide
import com.huawei.mlkit.sample.activity.adapter.imgseg.MyGridViewAdapter
import com.huawei.mlkit.sample.activity.adapter.ItemAdapter.ItemHolder
import com.huawei.hms.mlsdk.fr.MLFormRecognitionTablesAttribute.TablesContent.TableAttribute.TableCellAttribute
import com.huawei.hms.mlplugin.productvisionsearch.MLProductVisionSearchCapture.AbstractProductFragment
import com.huawei.mlkit.sample.activity.adapter.BottomSheetAdapter
import com.huawei.mlkit.sample.activity.fragment.ProductFragment
import com.huawei.mlkit.sample.activity.imageseg.LoadHairActivity
import com.huawei.mlkit.sample.activity.imageseg.LoadPhotoActivity
import com.huawei.mlkit.sample.activity.imageseg.StillCutPhotoActivity
import com.huawei.mlkit.sample.transactor.StillImageSegmentationTransactor
import com.huawei.mlkit.sample.transactor.ImageSegmentationTransactor
import android.renderscript.RenderScript
import android.view.View.OnTouchListener
import android.os.Build
import android.graphics.drawable.BitmapDrawable
import androidx.viewpager.widget.ViewPager
import com.huawei.mlkit.sample.activity.adapter.TabFragmentAdapter
import com.huawei.mlkit.sample.activity.imageseg.ImageSegmentationActivity.PagerChangeListener
import com.huawei.mlkit.sample.activity.fragment.BackgroundChangeFragment
import com.huawei.mlkit.sample.activity.fragment.CaptureImageFragment
import com.huawei.mlkit.sample.activity.fragment.SliceImageFragment
import com.huawei.mlkit.sample.activity.fragment.HairImageFragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.huawei.mlkit.sample.activity.documentskew.DocumentSkewStartActivity
import com.huawei.mlkit.sample.activity.documentskew.DocumentSkewCorretionActivity
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzer
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionResult
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionCoordinateInput
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerSetting
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerFactory
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewDetectResult
import com.huawei.mlkit.sample.activity.scenedection.SceneDectionActivity
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzer
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerSetting
import com.huawei.mlkit.sample.transactor.SceneDetectionTransactor
import android.content.pm.PackageInfo
import android.net.Uri
import android.util.Log
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.huawei.mlkit.sample.transactor.LocalImageClassificationTransactor
import com.huawei.mlkit.sample.activity.imageclassfication.ImageClassificationActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.mlkit.sample.activity.table.TableRecognitionStartActivity
import com.huawei.mlkit.sample.activity.imageseg.ImageSegmentationActivity
import com.huawei.mlkit.sample.activity.``object`
import com.huawei.mlkit.sample.util.BitmapUtils
import com.huawei.mlkit.sample.util.CommonUtils
import jxl.write.Label
import java.io.File
import java.io.IOException
import java.lang.RuntimeException

class TableRecognitionActivity : AppCompatActivity() {
    var imageView: ImageView? = null
    var recyclerView: RecyclerView? = null
    var adapter: ItemAdapter? = null
    var fab: FloatingActionButton? = null
    private var text_error: TextView? = null
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
        recyclerView.setLayoutManager(LinearLayoutManager(this, RecyclerView.VERTICAL, false))
        recyclerView.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
        recyclerView.setAdapter(adapter)
        try {
            val gson = Gson()
            val attribute = gson.fromJson(str, MLFormRecognitionTablesAttribute::class.java)
            if (attribute.retCode == MLFormRecognitionConstant.SUCCESS) {
                for (i in attribute.tablesContent.tableAttributes[0].tableCellAttributes.indices) {
                    adapter!!.list.add(attribute.tablesContent.tableAttributes[0].tableCellAttributes[i])
                }
                adapter!!.notifyDataSetChanged()
            } else if (attribute.retCode == MLFormRecognitionConstant.FAILED) {
                text_error.setText(getString(R.string.teble_error))
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, e.message!!)
        }
        fab.setOnClickListener(View.OnClickListener {
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
        })
    }

    @SuppressLint("LongLogTag")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == CommonUtils.PERMISSION_CODE_STORAGE && grantResults != null && grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        var workbook: WritableWorkbook? = null
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