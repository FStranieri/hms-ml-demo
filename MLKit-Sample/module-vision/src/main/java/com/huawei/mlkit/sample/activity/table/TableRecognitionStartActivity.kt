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
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.mlkit.sample.transactor.LocalImageClassificationTransactor
import com.huawei.mlkit.sample.activity.imageclassfication.ImageClassificationActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.mlkit.sample.activity.table.TableRecognitionStartActivity
import com.huawei.mlkit.sample.activity.imageseg.ImageSegmentationActivity
import com.huawei.mlkit.sample.activity.``object`
import com.huawei.mlkit.sample.util.BitmapUtils
import com.huawei.mlkit.sample.util.CommonUtils

class TableRecognitionStartActivity : AppCompatActivity(), View.OnClickListener {
    var btn: Button? = null
    var imageView: ImageView? = null
    var checkedItem = 0
    var uri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_recognition_start)
        findView()
    }

    private fun findView() {
        btn = findViewById(R.id.btn)
        imageView = findViewById(R.id.image)
        btn.setOnClickListener(this)
        imageView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn -> if (uri != null) {
                val newOpts = BitmapFactory.Options()
                val b = BitmapUtils.getBitmapFromUri(this, uri, newOpts)
                val frame = MLFrame.fromBitmap(b)
                val analyzer =
                    MLFormRecognitionAnalyzerFactory.getInstance().formRecognitionAnalyzer
                val task = analyzer.asyncAnalyseFrame(frame)
                task.addOnSuccessListener { jsonObject ->
                    val i = Intent(
                        this@TableRecognitionStartActivity,
                        TableRecognitionActivity::class.java
                    )
                    i.putExtra("uri", uri)
                    i.putExtra("json", jsonObject.toString())
                    startActivity(i)
                }.addOnFailureListener { }
            } else {
                Toast.makeText(this, "No picture", Toast.LENGTH_SHORT).show()
            }
            R.id.image -> showAlertDialog()
            else -> {}
        }
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.select)
        val choice =
            arrayOf(resources.getString(R.string.camera), resources.getString(R.string.gallery))
        builder.setSingleChoiceItems(choice, checkedItem) { dialog, which ->
            checkedItem = which
            if (which == 0) {
                if (ContextCompat.checkSelfPermission(
                        this@TableRecognitionStartActivity,
                        CommonUtils.CAMERA_PERMISSION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    gotoCamera()
                } else {
                    ActivityCompat.requestPermissions(
                        this@TableRecognitionStartActivity, arrayOf(
                            CommonUtils.CAMERA_PERMISSION
                        ), CommonUtils.PERMISSION_CODE_CAMERA
                    )
                }
            } else if (which == 1) {
                if (ContextCompat.checkSelfPermission(
                        this@TableRecognitionStartActivity,
                        CommonUtils.STORAGE_PERMISSION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    gotoGallery()
                } else {
                    ActivityCompat.requestPermissions(
                        this@TableRecognitionStartActivity, arrayOf(
                            CommonUtils.STORAGE_PERMISSION
                        ), CommonUtils.PERMISSION_CODE_STORAGE
                    )
                }
            }
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == CommonUtils.PERMISSION_CODE_STORAGE && grantResults != null && grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gotoGallery()
        } else if (requestCode == CommonUtils.PERMISSION_CODE_CAMERA && grantResults != null && grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gotoCamera()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun gotoGallery() {
        val i = Intent()
        i.action = Intent.ACTION_PICK
        i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(i, CommonUtils.REQUEST_PIC)
    }

    private fun gotoCamera() {
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        val values = ContentValues()
        val photoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        uri = photoUri
        startActivityForResult(intent, CommonUtils.REQUEST_TAKE_PHOTO_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CommonUtils.REQUEST_PIC && resultCode == RESULT_OK && data!!.data != null) {
            val bitmap = BitmapUtils.tableGetBitmap(this, data.data)
            imageView!!.setImageBitmap(bitmap)
            uri = data.data
        } else if (requestCode == CommonUtils.REQUEST_TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            val bitmap = BitmapUtils.tableGetBitmap(this, uri)
            imageView!!.setImageBitmap(bitmap)
            imageView!!.setImageBitmap(bitmap)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}