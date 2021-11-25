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
package com.huawei.mlkit.sample.activity.imageclassfication

import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.app.ActivityCompat
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.BaseActivity
import com.huawei.mlkit.sample.activity.RemoteDetectionActivity
import com.huawei.mlkit.sample.activity.dialog.AddPictureDialog
import com.huawei.mlkit.sample.camera.CameraConfiguration
import com.huawei.mlkit.sample.camera.LensEngine
import com.huawei.mlkit.sample.camera.LensEnginePreview
import com.huawei.mlkit.sample.transactor.LocalImageClassificationTransactor
import com.huawei.mlkit.sample.util.Constant
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.io.IOException

class ImageClassificationActivity : BaseActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback, CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {
    private var lensEngine: LensEngine? = null
    private lateinit var preview: LensEnginePreview
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var facingSwitch: ToggleButton
    private lateinit var imageSwitch: ImageButton
    var cameraConfiguration: CameraConfiguration? = null
    private var facing = CameraConfiguration.CAMERA_FACING_BACK
    private var mCamera: Camera? = null
    private var addPictureDialog: AddPictureDialog? = null
    private var isInitialization = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_image_classification)
        if (savedInstanceState != null) {
            facing = savedInstanceState.getInt(Constant.CAMERA_FACING)
        }
        preview = findViewById(R.id.classification_preview)
        findViewById<View>(R.id.classification_back).setOnClickListener(this)
        imageSwitch = findViewById(R.id.imageSwitch)
        imageSwitch.setOnClickListener(this)
        graphicOverlay = findViewById(R.id.classification_overlay)
        cameraConfiguration = CameraConfiguration()
        cameraConfiguration!!.setCameraFacing(facing)
        facingSwitch = findViewById(R.id.classification_facingSwitch)
        facingSwitch.setOnCheckedChangeListener(this)
        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch.setVisibility(View.GONE)
        }
        createDialog()
        createLensEngine()
        setStatusBar()
    }

    override fun onClick(view: View) {
        if (view.id == R.id.classification_back) {
            releaseLensEngine()
            finish()
        } else if (view.id == R.id.imageSwitch) {
            showDialog()
        }
    }

    private fun createDialog() {
        addPictureDialog = AddPictureDialog(this)
        val intent = Intent(this@ImageClassificationActivity, RemoteDetectionActivity::class.java)
        intent.putExtra(Constant.MODEL_TYPE, Constant.CLOUD_IMAGE_CLASSIFICATION)
        addPictureDialog!!.setClickListener(object : AddPictureDialog.ClickListener {
            override fun takePicture() {
                lensEngine!!.release()
                isInitialization = false
                intent.putExtra(Constant.ADD_PICTURE_TYPE, Constant.TYPE_TAKE_PHOTO)
                this@ImageClassificationActivity.startActivity(intent)
            }

            override fun selectImage() {
                intent.putExtra(Constant.ADD_PICTURE_TYPE, Constant.TYPE_SELECT_IMAGE)
                this@ImageClassificationActivity.startActivity(intent)
            }

            override fun doExtend() {}
        })
    }

    private fun showDialog() {
        addPictureDialog!!.show()
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
        restartLensEngine()
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
            lensEngine!!.setMachineLearningFrameTransactor(LocalImageClassificationTransactor(this.applicationContext))
            isInitialization = true
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Can not create image transactor: " + e.message,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    private fun restartLensEngine() {
        startLensEngine()
        if (null != lensEngine) {
            mCamera = lensEngine!!.camera
            try {
                mCamera?.setPreviewDisplay(preview!!.surfaceHolder)
            } catch (e: IOException) {
                Log.d(TAG, "initViews IOException")
            }
        }
    }

    private fun startLensEngine() {
        if (lensEngine != null) {
            try {
                preview.start(lensEngine, false)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start lensEngine.", e)
                lensEngine?.release()
                lensEngine = null
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if (!isInitialization) {
            createLensEngine()
        }
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
        private const val TAG = "ClassificationActivity"
    }
}