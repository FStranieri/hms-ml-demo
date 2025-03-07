/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.mlkit.sample.activity.documentskew

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.dsc.*
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.BaseActivity
import com.huawei.mlkit.sample.util.FileUtil
import com.huawei.mlkit.sample.views.DocumentCorrectImageView
import java.io.IOException
import java.util.*

class DocumentSkewCorretionActivity : BaseActivity(), View.OnClickListener {
    private lateinit var desImageView: ImageView
    private lateinit var adjustImgButton: ImageButton
    private var srcBitmap: Bitmap? = null
    private var getCompressesBitmap: Bitmap? = null
    private var imageUri: Uri? = null
    private var analyzer: MLDocumentSkewCorrectionAnalyzer? = null
    private var corrected: Bitmap? = null
    private lateinit var back: ImageView
    private var correctionTask: Task<MLDocumentSkewCorrectionResult>? = null
    private lateinit var documetScanView: DocumentCorrectImageView
    private lateinit var _points: Array<Point>
    private lateinit var layout_image: RelativeLayout
    private var frame: MLFrame? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_skew_corretion)
        setStatusBarColor(this, R.color.black)
        analyzer = createAnalyzer()
        adjustImgButton = findViewById(R.id.adjust)
        layout_image = findViewById(R.id.layout_image)
        desImageView = findViewById(R.id.des_image)
        documetScanView = findViewById(R.id.iv_documetscan)
        back = findViewById(R.id.back)
        adjustImgButton.setOnClickListener(this)
        findViewById<View>(R.id.back).setOnClickListener(this)
        findViewById<View>(R.id.rl_chooseImg).setOnClickListener(this)
        back.setOnClickListener(this)
        val operate_type = intent.getIntExtra("operate_type", 0)
        if (operate_type == 1) {
            takePhoto()
        } else if (operate_type == 2) {
            selectLocalImage()
        }
    }

    private lateinit var chooseTitles: Array<String>

    override fun onClick(v: View) {
        if (v.id == R.id.adjust) {
            val points: MutableList<Point> = ArrayList()
            val cropPoints = documetScanView!!.cropPoints
            if (cropPoints != null) {
                points.add(cropPoints[0])
                points.add(cropPoints[1])
                points.add(cropPoints[2])
                points.add(cropPoints[3])
            }
            val coordinateData = MLDocumentSkewCorrectionCoordinateInput(points)
            getDetectdetectResult(coordinateData, frame)
        } else if (v.id == R.id.rl_chooseImg) {
            chooseTitles = arrayOf(
                resources.getString(R.string.take_photo),
                resources.getString(R.string.select_from_album)
            )
            val builder = AlertDialog.Builder(this)
            builder.setItems(chooseTitles) { dialogInterface, position ->
                if (position == 0) {
                    takePhoto()
                } else {
                    selectLocalImage()
                }
            }
            builder.create().show()
        } else if (v.id == R.id.back) {
            finish()
        }
    }

    private fun createAnalyzer(): MLDocumentSkewCorrectionAnalyzer {
        val setting = MLDocumentSkewCorrectionAnalyzerSetting.Factory()
            .create()
        return MLDocumentSkewCorrectionAnalyzerFactory.getInstance()
            .getDocumentSkewCorrectionAnalyzer(setting)
    }

    private fun takePhoto() {
        layout_image.visibility = View.GONE
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(this.packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri =
                this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            this.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
        }
    }

    private fun selectLocalImage() {
        layout_image.visibility = View.GONE
        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK) {
            imageUri = data!!.data
            try {
                if (imageUri != null) {
                    srcBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    val realPathFromURI = getRealPathFromURI(imageUri!!)
                    val i = readPictureDegree(realPathFromURI)
                    val spBitmap = rotaingImageView(i, srcBitmap)
                    val matrix = Matrix()
                    matrix.setScale(0.5f, 0.5f)
                    getCompressesBitmap = Bitmap.createBitmap(
                        spBitmap, 0, 0, spBitmap.width,
                        spBitmap.height, matrix, true
                    )
                    reloadAndDetectImage()
                }
            } catch (e: IOException) {
                Log.e(TAG, e.message!!)
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            try {
                if (imageUri != null) {
                    srcBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    val realPathFromURI = getRealPathFromURI(imageUri!!)
                    val i = readPictureDegree(realPathFromURI)
                    val spBitmap = rotaingImageView(i, srcBitmap)
                    val matrix = Matrix()
                    matrix.setScale(0.5f, 0.5f)
                    getCompressesBitmap = Bitmap.createBitmap(
                        spBitmap, 0, 0, spBitmap.width,
                        srcBitmap!!.height, matrix, true
                    )
                    reloadAndDetectImage()
                }
            } catch (e: IOException) {
                Log.e(TAG, e.message!!)
            }
        } else if (resultCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_CANCELED) {
            finish()
        }
    }

    private fun reloadAndDetectImage() {
        if (imageUri == null) {
            return
        }
        frame = MLFrame.fromBitmap(getCompressesBitmap)
        val task = analyzer!!.asyncDocumentSkewDetect(frame)
        task.addOnSuccessListener { result ->
            if (result.getResultCode() != 0) {
                corrected = null
                Toast.makeText(
                    this@DocumentSkewCorretionActivity,
                    "The picture does not meet the requirements.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Recognition success.
                val leftTop = result.leftTopPosition
                val rightTop = result.rightTopPosition
                val leftBottom = result.leftBottomPosition
                val rightBottom = result.rightBottomPosition
                _points = arrayOf()
                _points[0] = leftTop
                _points[1] = rightTop
                _points[2] = rightBottom
                _points[3] = leftBottom
                layout_image.visibility = View.GONE
                documetScanView.setImageBitmap(getCompressesBitmap)
                documetScanView.setPoints(_points)
            }
        }.addOnFailureListener { e ->
            Toast.makeText(
                applicationContext,
                e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getDetectdetectResult(
        coordinateData: MLDocumentSkewCorrectionCoordinateInput,
        frame: MLFrame?
    ) {
        try {
            correctionTask = analyzer!!.asyncDocumentSkewCorrect(frame, coordinateData)
        } catch (e: Exception) {
            Log.e(TAG, "The image does not meet the detection requirements.")
        }
        try {
            correctionTask!!.addOnSuccessListener { refineResult -> // The check is successful.
                if (refineResult != null && refineResult.getResultCode() == 0) {
                    corrected = refineResult.getCorrected()
                    layout_image!!.visibility = View.VISIBLE
                    desImageView!!.setImageBitmap(corrected)
                } else {
                    Toast.makeText(
                        this@DocumentSkewCorretionActivity,
                        "The check fails.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Toast.makeText(
                    this@DocumentSkewCorretionActivity,
                    "The check fails.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Please set an image.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (srcBitmap != null) {
            srcBitmap!!.recycle()
        }
        if (getCompressesBitmap != null) {
            getCompressesBitmap!!.recycle()
        }
        if (corrected != null) {
            corrected!!.recycle()
        }
        if (analyzer != null) {
            try {
                analyzer!!.stop()
            } catch (e: IOException) {
                Log.e(TAG, e.message!!)
            }
        }
    }

    private fun getRealPathFromURI(contentURI: Uri): String? {
        return FileUtil.getFilePathByUri(this, contentURI)
    }

    companion object {
        private const val TAG = "SuperResolutionActivity"
        private const val REQUEST_SELECT_IMAGE = 1000
        private const val REQUEST_TAKE_PHOTO = 1
        fun readPictureDegree(path: String?): Int {
            var degree = 0
            try {
                val exifInterface = ExifInterface(path!!)
                val orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            } catch (e: IOException) {
                Log.e(TAG, e.message!!)
            }
            return degree
        }

        fun rotaingImageView(angle: Int, bitmap: Bitmap?): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            return Bitmap.createBitmap(
                bitmap!!, 0, 0,
                bitmap.width, bitmap.height, matrix, true
            )
        }
    }
}