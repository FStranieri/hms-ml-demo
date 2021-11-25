package com.huawei.mlkit.sample.activity.table

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzerFactory
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.util.BitmapUtils
import com.huawei.mlkit.sample.util.CommonUtils

class TableRecognitionStartActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var btn: Button
    lateinit var imageView: ImageView
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
        if (requestCode == CommonUtils.PERMISSION_CODE_STORAGE && grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gotoGallery()
        } else if (requestCode == CommonUtils.PERMISSION_CODE_CAMERA && grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
            imageView.setImageBitmap(bitmap)
            uri = data.data
        } else if (requestCode == CommonUtils.REQUEST_TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            val bitmap = BitmapUtils.tableGetBitmap(this, uri)
            imageView.setImageBitmap(bitmap)
            imageView.setImageBitmap(bitmap)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}