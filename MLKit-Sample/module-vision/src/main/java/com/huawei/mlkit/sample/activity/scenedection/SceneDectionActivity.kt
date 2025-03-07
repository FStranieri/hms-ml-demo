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
package com.huawei.mlkit.sample.activity.scenedection

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.scd.MLSceneDetection
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzer
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerSetting
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.scenedection.SceneDectionActivity
import com.huawei.mlkit.sample.camera.CameraConfiguration
import com.huawei.mlkit.sample.camera.LensEngine
import com.huawei.mlkit.sample.camera.LensEnginePreview
import com.huawei.mlkit.sample.transactor.SceneDetectionTransactor
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.io.IOException
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.util.*

class SceneDectionActivity : Activity(), CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {
    private lateinit var resultText: TextView
    private var _bitmap: Bitmap? = null
    private var imageUri: Uri? = null
    private var analyzer: MLSceneDetectionAnalyzer? = null
    private var setting: MLSceneDetectionAnalyzerSetting? = null
    private var confidence = 0f
    private var operateType = REQUEST_TAKE_PHOTO
    private var lensEngine: LensEngine? = null
    private var preview: LensEnginePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private lateinit var facingSwitch: ToggleButton
    private var cameraConfiguration: CameraConfiguration? = null
    private var facing = CameraConfiguration.CAMERA_FACING_BACK
    private var mCamera: Camera? = null
    private lateinit var iv_return_back: ImageView
    private lateinit var iv_select_album: ImageView
    private lateinit var iv_result: ImageView
    private lateinit var rl_select_album_result: RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene_dection)
        val con = intent.getStringExtra("confidence")
        if (!TextUtils.isEmpty(con)) {
            confidence = java.lang.Float.valueOf(con)
        }
        initView()
        cameraConfiguration = CameraConfiguration()
        cameraConfiguration!!.setCameraFacing(facing)
        facingSwitch!!.setOnCheckedChangeListener(this)
        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch!!.visibility = View.GONE
        }
        if (!allPermissionsGranted()) {
            runtimePermissions
        }
        createLensEngine()
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

    private fun createLensEngine() {
        if (lensEngine == null) {
            lensEngine = LensEngine(this, cameraConfiguration!!, graphicOverlay!!)
        }
        try {
            lensEngine!!.setMachineLearningFrameTransactor(
                SceneDetectionTransactor(
                    applicationContext, confidence
                )
            )
        } catch (e: Exception) {
            Toast.makeText(
                this, "Can not create image transactor: " + e.message,
                Toast.LENGTH_LONG
            ).show()
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
        lensEngine?.let {
            try {
                preview!!.start(lensEngine, false)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start lensEngine.", e)
                lensEngine?.release()
                lensEngine = null
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission)) {
                return false
            }
        }
        return true
    }

    private val requiredPermissions: Array<String?>
        get() = try {
            val info = packageManager
                .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    private val runtimePermissions: Unit
        get() {
            val allNeededPermissions: MutableList<String?> = ArrayList()
            for (permission in requiredPermissions) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission)
                }
            }
            if (allNeededPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    allNeededPermissions.toTypedArray(),
                    PERMISSION_REQUESTS
                )
            }
        }

    public override fun onResume() {
        super.onResume()
        startLensEngine()
    }

    override fun onStop() {
        super.onStop()
        preview?.stop()
    }

    private fun releaseLensEngine() {
        lensEngine?.release()
        lensEngine = null
    }

    private fun initAnalyzer() {
        Log.e("TAG", "confidence =$confidence")
        setting = MLSceneDetectionAnalyzerSetting.Factory().setConfidence(confidence / 100).create()
        analyzer = MLSceneDetectionAnalyzerFactory.getInstance().getSceneDetectionAnalyzer(setting)
    }

    private fun initView() {
        resultText = findViewById(R.id.result)
        preview = findViewById(R.id.preview)
        graphicOverlay = findViewById(R.id.overlay)
        facingSwitch = findViewById(R.id.facingSwitch)
        iv_return_back = findViewById(R.id.iv_return_back)
        iv_select_album = findViewById(R.id.iv_select_album)
        iv_result = findViewById(R.id.iv_result)
        rl_select_album_result = findViewById(R.id.rl_select_album_result)
        iv_return_back.setOnClickListener(this)
        iv_select_album.setOnClickListener(this)
    }

    private fun detectImage() {
        if (_bitmap == null) {
            resultText.text = "Picture error"
            return
        }
        Log.e(TAG, "formType=" + _bitmap!!.config)
        val frame = MLFrame.Creator()
            .setBitmap(_bitmap)
            .create()
        val sparseArray: SparseArray<*>? = analyzer!!.analyseFrame(frame)
        if (sparseArray == null || sparseArray.size() == 0) {
            resultText.text = "No scene was identified "
            operateType = REQUEST_SELECT_ALBUM
            rl_select_album_result.visibility = View.VISIBLE
            iv_result.setImageBitmap(_bitmap)
            return
        }
        var hasBigConfidence = false
        var str = """
            scene count：${sparseArray.size()}
            
            """.trimIndent()
        for (i in 0 until sparseArray.size()) {
            val sceneInfo = sparseArray[i] as MLSceneDetection
            val realConfidence = sceneInfo.confidence
            if (java.lang.Double.doubleToLongBits(realConfidence.toDouble()) >= java.lang.Double.doubleToLongBits(
                    confidence.toDouble()
                )
            ) {
                val a = BigDecimal(realConfidence.toDouble())
                val b = BigDecimal(100)
                val c = a.multiply(b).toDouble()
                val d = BigDecimal(c)
                var variable = d.setScale(2, BigDecimal.ROUND_HALF_UP).toFloat()
                if (variable.toString().contains("E")) {
                    variable = 0.01f
                }
                hasBigConfidence = true
                str += """
                    Scene detection is：${sceneInfo.result}
                    The credibility of the scenario is ：$variable%
                    
                    """.trimIndent()
            }
        }
        if (!hasBigConfidence) {
            str = "No scene was identified"
        }
        resultText.text = str
        operateType = REQUEST_SELECT_ALBUM
        rl_select_album_result.visibility = View.VISIBLE
        iv_result.setImageBitmap(_bitmap)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (analyzer != null) {
            analyzer!!.stop()
        }
        releaseLensEngine()
    }

    private fun selectFromAlbum() {
        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, REQUEST_SELECT_ALBUM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == REQUEST_SELECT_ALBUM && resultCode == RESULT_OK) {
                if (data != null) {
                    imageUri = data.data
                    _bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    releaseLensEngine()
                    initAnalyzer()
                    detectImage()
                }
            } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
                if (imageUri != null) {
                    _bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    releaseLensEngine()
                    initAnalyzer()
                    detectImage()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, e.message!!)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_return_back -> if (operateType == REQUEST_SELECT_ALBUM) {
                rl_select_album_result.visibility = View.GONE
                operateType = REQUEST_TAKE_PHOTO
                createLensEngine()
                restartLensEngine()
            } else {
                finish()
            }
            R.id.iv_select_album -> selectFromAlbum()
        }
    }

    companion object {
        private val TAG = SceneDectionActivity::class.java.simpleName
        private const val PERMISSION_REQUESTS = 1
        private const val REQUEST_SELECT_ALBUM = 10
        private const val REQUEST_TAKE_PHOTO = 20
        private fun isPermissionGranted(context: Context, permission: String?): Boolean {
            return (ContextCompat.checkSelfPermission(context, permission!!)
                    == PackageManager.PERMISSION_GRANTED)
        }

        fun bytetoBuffer(value: ByteArray): ByteBuffer {
            val byteBuffer = ByteBuffer.allocate(value.size)
            byteBuffer.clear()
            byteBuffer[value, 0, value.size]
            return byteBuffer
        }

        /**
         * Convert the bitmap into ARGB data and then into NV21 data.
         */
        fun bitmapToNv21(src: Bitmap?, width: Int, height: Int): ByteArray {
            if (src != null && src.width >= width && src.height >= height) {
                val argb = IntArray(width * height)
                src.getPixels(argb, 0, width, 0, 0, width, height)
                return argbToNv21(argb, width, height)
            }
            return byteArrayOf()
        }

        /**
         * Converting ARGB data to NV21 data
         */
        private fun argbToNv21(argb: IntArray, width: Int, height: Int): ByteArray {
            val frameSize = width * height
            var yIndex = 0
            var uvIndex = frameSize
            var index = 0
            val nv21 = ByteArray(width * height * 3 / 2)
            for (j in 0 until height) {
                for (i in 0 until width) {
                    val r = argb[index] and 0xFF0000 shr 16
                    val g = argb[index] and 0x00FF00 shr 8
                    val b = argb[index] and 0x0000FF
                    val y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                    val u = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                    val v = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128
                    nv21[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
                    if (j % 2 == 0 && index % 2 == 0 && uvIndex < nv21.size - 2) {
                        nv21[uvIndex++] = (if (v < 0) 0 else if (v > 255) 255 else v).toByte()
                        nv21[uvIndex++] = (if (u < 0) 0 else if (u > 255) 255 else u).toByte()
                    }
                    ++index
                }
            }
            return nv21
        }
    }
}