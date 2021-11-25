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

import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import android.widget.ToggleButton
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzerSetting
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.BaseActivity
import com.huawei.mlkit.sample.camera.CameraConfiguration
import com.huawei.mlkit.sample.camera.LensEngine
import com.huawei.mlkit.sample.camera.LensEnginePreview
import com.huawei.mlkit.sample.transactor.LocalObjectTransactor
import com.huawei.mlkit.sample.util.Constant
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.io.IOException

class ObjectDetectionActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {
    private var lensEngine: LensEngine? = null
    private lateinit var preview: LensEnginePreview
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var facingSwitch: ToggleButton
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
        preview.stop()
        reStartLensEngine()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(Constant.CAMERA_FACING, facing)
        super.onSaveInstanceState(outState)
    }

    private fun createLensEngine() {
        if (lensEngine == null) {
            lensEngine = LensEngine(this, cameraConfiguration!!, graphicOverlay)
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
                mCamera!!.setPreviewDisplay(preview.surfaceHolder)
            } catch (e: IOException) {
                Log.d(TAG, "initViews IOException")
            }
        }
    }

    private fun startLensEngine() {
        lensEngine?.let {
            try {
                preview.start(it, false)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start lensEngine.", e)
                it.release()
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
        preview.stop()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        releaseLensEngine()
    }

    private fun releaseLensEngine() {
        lensEngine?.release()
        lensEngine = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseLensEngine()
    }

    companion object {
        private const val TAG = "ObjectDetectionActivity"
    }
}