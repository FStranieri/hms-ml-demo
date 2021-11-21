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
package com.huawei.mlkit.sample.activity.imageseg

import com.huawei.mlkit.sample.activity.BaseActivity.onCreate
import com.huawei.mlkit.sample.activity.BaseActivity.setStatusBar
import com.huawei.mlkit.sample.activity.BaseActivity.setStatusBarFontColor
import com.huawei.mlkit.sample.activity.adapter.ItemAdapter
import android.widget.TextView
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
import android.widget.Toast
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
import android.widget.CompoundButton
import android.widget.ToggleButton
import com.huawei.mlkit.sample.transactor.LocalObjectTransactor
import com.huawei.mlkit.sample.activity.`object`.ObjectDetectionActivity
import com.bumptech.glide.Glide
import com.huawei.mlkit.sample.activity.adapter.imgseg.MyGridViewAdapter
import com.huawei.mlkit.sample.activity.adapter.ItemAdapter.ItemHolder
import com.huawei.hms.mlsdk.fr.MLFormRecognitionTablesAttribute.TablesContent.TableAttribute.TableCellAttribute
import com.huawei.hms.mlplugin.productvisionsearch.MLProductVisionSearchCapture.AbstractProductFragment
import android.widget.GridView
import com.huawei.mlkit.sample.activity.adapter.BottomSheetAdapter
import com.huawei.mlkit.sample.activity.fragment.ProductFragment
import com.huawei.mlkit.sample.activity.imageseg.LoadHairActivity
import com.huawei.mlkit.sample.activity.imageseg.LoadPhotoActivity
import com.huawei.mlkit.sample.activity.imageseg.StillCutPhotoActivity
import android.widget.AdapterView
import android.widget.LinearLayout
import com.huawei.mlkit.sample.transactor.StillImageSegmentationTransactor
import android.widget.ImageButton
import com.huawei.mlkit.sample.transactor.ImageSegmentationTransactor
import android.renderscript.RenderScript
import android.view.View.OnTouchListener
import android.os.Build
import android.widget.RelativeLayout
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
import android.widget.EditText
import com.huawei.mlkit.sample.activity.scenedection.SceneDectionActivity
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzer
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerSetting
import com.huawei.mlkit.sample.transactor.SceneDetectionTransactor
import android.content.pm.PackageInfo
import android.graphics.Color
import android.graphics.Matrix
import android.hardware.Camera
import android.util.Log
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import android.util.SparseArray
import android.view.*
import androidx.core.content.FileProvider
import com.huawei.mlkit.sample.transactor.LocalImageClassificationTransactor
import com.huawei.mlkit.sample.activity.imageclassfication.ImageClassificationActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationScene
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting
import com.huawei.mlkit.sample.activity.table.TableRecognitionStartActivity
import com.huawei.mlkit.sample.activity.imageseg.ImageSegmentationActivity
import com.huawei.mlkit.sample.activity.``object`
import com.huawei.mlkit.sample.activity.BaseActivity
import com.huawei.mlkit.sample.callback.ImageSegmentationResultCallBack
import com.huawei.mlkit.sample.camera.CameraConfiguration
import com.huawei.mlkit.sample.camera.LensEngine
import com.huawei.mlkit.sample.camera.LensEnginePreview
import com.huawei.mlkit.sample.util.Constant
import com.huawei.mlkit.sample.util.ImageUtils
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.lang.RuntimeException

/**
 * It is applied to the image segmentation function. The application scenario is: open the camera,
 * if there is a human body in the picture, then cut out the human body and replace the background
 * to achieve the real-time human detection effect.
 *
 * @since 2019-12-26
 */
class TakePhotoActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener,
    ImageSegmentationResultCallBack, View.OnClickListener {
    private var lensEngine: LensEngine? = null
    private var preview: LensEnginePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var facingSwitch: ToggleButton? = null
    private var img_takePhoto: ImageButton? = null
    private var img_pic: ImageButton? = null
    private var img_back: ImageButton? = null
    private var cameraConfiguration: CameraConfiguration? = null
    private var index = 0
    private var facing = CameraConfiguration.CAMERA_FACING_FRONT
    private var background: Bitmap? = null
    private var processImage: Bitmap? = null
    private var transactor: ImageSegmentationTransactor? = null
    private var setting: MLImageSegmentationSetting? = null
    private var imgPath: String? = null
    private var mCamera: Camera? = null
    private var isBlur = false
    private var renderScript: RenderScript? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_take_photo)
        if (savedInstanceState != null) {
            facing = savedInstanceState.getInt(Constant.CAMERA_FACING)
        }
        val intent = this.intent
        try {
            index = intent.getIntExtra(Constant.VALUE_KEY, -1)
        } catch (e: RuntimeException) {
            Log.e(TAG, "Get intent value failed:" + e.message)
        }
        if (index < 0) {
            Toast.makeText(
                this.applicationContext,
                R.string.please_select_picture,
                Toast.LENGTH_SHORT
            ).show()
            finish()
        } else {
            // Decode background image.
            val id = Constant.IMAGES[index]
            val `is` = this.resources.openRawResource(id)
            background = BitmapFactory.decodeStream(`is`)
        }
        initView()
        initAction()
        cameraConfiguration = CameraConfiguration()
        cameraConfiguration!!.setCameraFacing(facing)
        cameraConfiguration!!.fps = 6.0f
        cameraConfiguration!!.previewWidth = CameraConfiguration.DEFAULT_WIDTH
        cameraConfiguration!!.previewHeight = CameraConfiguration.DEFAULT_HEIGHT
        createLensEngine()
        renderScript = RenderScript.create(this)
    }

    private fun initView() {
        preview = findViewById(R.id.firePreview)
        graphicOverlay = findViewById(R.id.fireFaceOverlay)
        facingSwitch = findViewById(R.id.facingSwitch)
        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch.setVisibility(View.GONE)
        }
        img_takePhoto = findViewById(R.id.img_takePhoto)
        img_pic = findViewById(R.id.img_pic)
        img_back = findViewById(R.id.back)
    }

    private fun initAction() {
        facingSwitch!!.setOnCheckedChangeListener(this)
        img_back!!.setOnClickListener(this)
        img_pic!!.setOnClickListener(this)
        // Set the display effect when the takePhoto button is clicked.
        img_takePhoto!!.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                img_takePhoto!!.setColorFilter(Color.GRAY)
            }
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                img_takePhoto!!.setColorFilter(Color.WHITE)
            }
            false
        }
        img_takePhoto!!.setOnClickListener {
            // save Picture.
            if (processImage == null) {
                Log.e(TAG, "The image is null, unable to save.")
            } else {
                // save current image to gallery.
                val imageUtils = ImageUtils(this@TakePhotoActivity.applicationContext)
                imageUtils.setImageUtilCallBack { path ->
                    imgPath = path
                    Log.i(TAG, "PATH:$path")
                }
                imageUtils.saveToAlbum(processImage)
                val matrix = Matrix()
                matrix.postScale(0.3f, 0.3f)
                val resizedBitmap = Bitmap.createBitmap(
                    processImage!!,
                    0,
                    0,
                    processImage!!.width,
                    processImage!!.height,
                    matrix,
                    true
                )
                img_pic!!.setImageBitmap(resizedBitmap)
            }
        }
        findViewById<View>(R.id.bt_blur).setOnClickListener {
            isBlur = !isBlur
            if (transactor != null) {
                transactor!!.setBlur(isBlur)
                transactor!!.setRenderScript(renderScript)
            }
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.back) {
            releaseLensEngine()
            finish()
        } else if (view.id == R.id.img_pic) {
            if (imgPath == null) {
                Toast.makeText(applicationContext, "please save a picture", Toast.LENGTH_SHORT)
                    .show()
            } else {
                var intent = Intent()
                val imgFile = File(imgPath)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "image/*"
                } else {
                    intent = Intent(Intent.ACTION_VIEW)
                    val imgUri =
                        FileProvider.getUriForFile(this, this.packageName + ".provider", imgFile)
                    Log.i(TAG, "image uri:$imgUri")
                    intent.setDataAndType(imgUri, "image/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                this.startActivity(intent)
            }
        }
    }

    private fun createLensEngine() {
        // If there's no existing lensEngine, create one.
        if (lensEngine == null) {
            lensEngine = LensEngine(this, cameraConfiguration, graphicOverlay)
        }
        try {
            setting = MLImageSegmentationSetting.Factory()
                .setAnalyzerType(MLImageSegmentationSetting.BODY_SEG)
                .setExact(false)
                .setScene(MLImageSegmentationScene.FOREGROUND_ONLY)
                .create()
            transactor = ImageSegmentationTransactor(this.applicationContext, setting, background)
            transactor!!.setImageSegmentationResultCallBack(this)
            lensEngine!!.setMachineLearningFrameTransactor(transactor)
        } catch (e: Exception) {
            Log.e(TAG, "Can not create image transactor: $e")
            Toast.makeText(
                this.applicationContext,
                "Can not create image transactor: " + e.message,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    private fun startLensEngine() {
        if (lensEngine != null) {
            try {
                if (null != preview) {
                    preview!!.start(lensEngine, true)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start lensEngine.", e)
                lensEngine!!.release()
                lensEngine = null
                imgPath = null
            }
        }
    }

    private fun restartLensEngine() {
        startLensEngine()
        if (null != lensEngine) {
            mCamera = lensEngine!!.camera
            try {
                mCamera.setPreviewTexture(preview!!.surfaceTexture)
            } catch (e: IOException) {
                Log.d(TAG, "initViews IOException")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(Constant.CAMERA_FACING, facing)
        super.onSaveInstanceState(outState)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Log.d(TAG, "Set facing")
        if (lensEngine != null) {
            if (!isChecked) {
                facing = CameraConfiguration.CAMERA_FACING_FRONT
            } else {
                facing = CameraConfiguration.CAMERA_FACING_BACK
            }
            cameraConfiguration!!.setCameraFacing(facing)
            setting = MLImageSegmentationSetting.Factory()
                .setAnalyzerType(MLImageSegmentationSetting.BODY_SEG)
                .create()
            transactor = ImageSegmentationTransactor(this.applicationContext, setting, background)
            transactor!!.setImageSegmentationResultCallBack(this)
            lensEngine!!.setMachineLearningFrameTransactor(transactor)
        }
        preview!!.stop()
        restartLensEngine()
    }

    public override fun onResume() {
        super.onResume()
        startLensEngine()
    }

    override fun onStop() {
        super.onStop()
        preview!!.stop()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        releaseLensEngine()
    }

    private fun releaseLensEngine() {
        if (lensEngine != null) {
            lensEngine!!.release()
            lensEngine = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseLensEngine()
        if (transactor != null) {
            transactor!!.setBlur(false)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RenderScript.releaseAllContexts()
        } else {
            renderScript!!.finish()
        }
    }

    override fun callResultBitmap(bitmap: Bitmap) {
        processImage = bitmap
    }

    companion object {
        private const val TAG = "TakePhotoActivity"
    }
}