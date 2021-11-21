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
package com.huawei.mlkit.sample.transactor

import android.graphics.Bitmap
import android.annotation.SuppressLint
import android.app.Activity
import kotlin.jvm.Synchronized
import kotlin.Throws
import android.hardware.Camera.PictureCallback
import android.graphics.ImageFormat
import android.hardware.Camera.PreviewCallback
import android.view.WindowManager
import android.hardware.Camera.CameraInfo
import android.view.ViewGroup
import android.view.SurfaceView
import android.graphics.SurfaceTexture
import android.view.SurfaceHolder
import android.view.MotionEvent
import android.hardware.Camera.AutoFocusCallback
import android.graphics.RectF
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.mlkit.sample.transactor.LocalObjectTransactor
import com.huawei.mlkit.sample.views.graphic.LocalObjectGraphic
import com.huawei.mlkit.sample.transactor.RemoteLandmarkTransactor
import com.huawei.mlkit.sample.views.graphic.RemoteLandmarkGraphic
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzer
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerSetting
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import com.huawei.mlkit.sample.views.graphic.SceneDetectionGraphic
import com.huawei.mlkit.sample.transactor.SceneDetectionTransactor
import android.renderscript.RenderScript
import com.huawei.mlkit.sample.transactor.ImageSegmentationTransactor
import android.util.SparseArray
import android.widget.Toast
import android.renderscript.Allocation
import android.renderscript.ScriptIntrinsicBlur
import com.huawei.mlkit.sample.transactor.StillImageSegmentationTransactor
import com.huawei.mlkit.sample.transactor.LocalImageClassificationTransactor
import com.huawei.mlkit.sample.views.graphic.LocalImageClassificationGraphic
import com.huawei.mlkit.sample.transactor.RemoteImageClassificationTransactor
import com.huawei.mlkit.sample.views.graphic.RemoteImageClassificationGraphic
import com.huawei.mlkit.sample.R
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.content.ContentUris
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.content.Intent
import android.graphics.YuvImage
import android.graphics.BitmapFactory
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.content.SharedPreferences
import kotlin.jvm.JvmOverloads
import android.content.res.TypedArray
import android.view.View.MeasureSpec
import android.graphics.Shader
import android.graphics.Xfermode
import android.util.DisplayMetrics
import android.graphics.PorterDuffXfermode
import android.graphics.PorterDuff
import android.graphics.DashPathEffect
import android.widget.GridView
import android.widget.AbsListView
import android.graphics.drawable.Drawable
import android.os.*
import android.util.Log
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmark
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmarkAnalyzer
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmarkAnalyzerSetting
import com.huawei.mlkit.sample.camera.FrameMetadata
import com.huawei.mlkit.sample.util.Constant
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.io.IOException
import java.lang.Exception

class RemoteLandmarkTransactor : BaseTransactor<List<MLRemoteLandmark>?> {
    private val detector: MLRemoteLandmarkAnalyzer
    private var handler: Handler? = null

    constructor(handler: Handler?) : super() {
        detector = MLAnalyzerFactory.getInstance().remoteLandmarkAnalyzer
        this.handler = handler
    }

    constructor(options: MLRemoteLandmarkAnalyzerSetting?) : super() {
        detector = MLAnalyzerFactory.getInstance().getRemoteLandmarkAnalyzer(options)
    }

    override fun stop() {
        super.stop()
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(
                TAG,
                "Exception thrown while trying to close remote landmark transactor: " + e.message
            )
        }
    }

    override fun detectInImage(image: MLFrame?): Task<List<MLRemoteLandmark>?> {
        return detector.asyncAnalyseFrame(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<MLRemoteLandmark>?,
        frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay
    ) {
        handler!!.sendEmptyMessage(Constant.GET_DATA_SUCCESS)
        if (results != null && !results.isEmpty()) {
            graphicOverlay.clear()
            for (landmark in results) {
                val landmarkGraphic = RemoteLandmarkGraphic(graphicOverlay, landmark)
                graphicOverlay.addGraphic(landmarkGraphic)
            }
            graphicOverlay.postInvalidate()
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Remote landmark detection failed: " + e.message)
        handler!!.sendEmptyMessage(Constant.GET_DATA_FAILED)
    }

    companion object {
        private const val TAG = "LandmarkTransactor"
    }
}