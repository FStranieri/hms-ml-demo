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
package com.huawei.mlkit.sample.activity.`object`

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
import android.hardware.Camera
import android.util.Log
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import android.util.SparseArray
import android.view.*
import com.huawei.mlkit.sample.transactor.LocalImageClassificationTransactor
import com.huawei.mlkit.sample.activity.imageclassfication.ImageClassificationActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzerSetting
import com.huawei.mlkit.sample.activity.table.TableRecognitionStartActivity
import com.huawei.mlkit.sample.activity.imageseg.ImageSegmentationActivity
import com.huawei.mlkit.sample.activity.``object`
import com.huawei.mlkit.sample.activity.BaseActivity
import com.huawei.mlkit.sample.camera.CameraConfiguration
import com.huawei.mlkit.sample.camera.LensEngine
import com.huawei.mlkit.sample.camera.LensEnginePreview
import com.huawei.mlkit.sample.util.Constant
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.io.IOException
import java.lang.Exception

class ObjectDetectionActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {
    private var lensEngine: LensEngine? = null
    private var preview: LensEnginePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var facingSwitch: ToggleButton? = null
    private var mCamera: Camera? = null
    private var cameraConfiguration: CameraConfiguration? = null
    private var facing = CameraConfiguration.CAMERA_FACING_BACK
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBar()
        this.setContentView(R.layout.activity_object_detection)
        if (savedInstanceState != null) {
            facing = savedInstanceState.getInt(Constant.CAMERA_FACING)
        }
        preview = findViewById(R.id.object_preview)
        findViewById<View>(R.id.object_back).setOnClickListener(this)
        facingSwitch = findViewById(R.id.object_facingSwitch)
        facingSwitch.setOnCheckedChangeListener(this)
        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch.setVisibility(View.GONE)
        }
        graphicOverlay = findViewById(R.id.object_overlay)
        cameraConfiguration = CameraConfiguration()
        cameraConfiguration!!.setCameraFacing(facing)
        createLensEngine()
    }

    override fun onClick(view: View) {
        if (view.id == R.id.object_back) {
            releaseLensEngine()
            finish()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (lensEngine != null) {
            if (isChecked) {
                facing = CameraConfiguration.CAMERA_FACING_FRONT
                cameraConfiguration!!.setCameraFacing(facing)
            } else {
                facing = CameraConfiguration.CAMERA_FACING_BACK
                cameraConfiguration!!.setCameraFacing(facing)
            }
        }
        preview!!.stop()
        reStartLensEngine()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(Constant.CAMERA_FACING, facing)
        super.onSaveInstanceState(outState)
    }

    private fun createLensEngine() {
        if (lensEngine == null) {
            lensEngine = LensEngine(this, cameraConfiguration, graphicOverlay)
        }
        try {
            val options = MLObjectAnalyzerSetting.Factory()
                .setAnalyzerType(MLObjectAnalyzerSetting.TYPE_VIDEO)
                .allowMultiResults()
                .allowClassification().create()
            lensEngine!!.setMachineLearningFrameTransactor(LocalObjectTransactor(options))
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Can not create face detection transactor: " + e.message,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    private fun reStartLensEngine() {
        startLensEngine()
        if (null != lensEngine) {
            mCamera = lensEngine!!.camera
            try {
                mCamera.setPreviewDisplay(preview!!.surfaceHolder)
            } catch (e: IOException) {
                Log.d(TAG, "initViews IOException")
            }
        }
    }

    private fun startLensEngine() {
        if (lensEngine != null) {
            try {
                preview!!.start(lensEngine, false)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start lensEngine.", e)
                lensEngine!!.release()
                lensEngine = null
            }
        }
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
    }

    companion object {
        private const val TAG = "ObjectDetectionActivity"
    }
}