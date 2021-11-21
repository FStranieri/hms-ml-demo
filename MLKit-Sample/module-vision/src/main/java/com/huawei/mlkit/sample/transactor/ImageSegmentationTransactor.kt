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

import android.annotation.SuppressLint
import android.app.Activity
import kotlin.jvm.Synchronized
import kotlin.Throws
import android.hardware.Camera.PictureCallback
import android.hardware.Camera.PreviewCallback
import android.view.WindowManager
import android.hardware.Camera.CameraInfo
import android.view.ViewGroup
import android.view.SurfaceView
import android.view.SurfaceHolder
import android.view.MotionEvent
import android.hardware.Camera.AutoFocusCallback
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.mlkit.sample.transactor.LocalObjectTransactor
import com.huawei.mlkit.sample.transactor.RemoteLandmarkTransactor
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzer
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerSetting
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import com.huawei.mlkit.sample.transactor.SceneDetectionTransactor
import com.huawei.mlkit.sample.transactor.ImageSegmentationTransactor
import android.util.SparseArray
import android.widget.Toast
import com.huawei.mlkit.sample.transactor.StillImageSegmentationTransactor
import com.huawei.mlkit.sample.transactor.LocalImageClassificationTransactor
import com.huawei.mlkit.sample.transactor.RemoteImageClassificationTransactor
import com.huawei.mlkit.sample.R
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.content.ContentUris
import android.content.Context
import android.os.Environment
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.content.SharedPreferences
import kotlin.jvm.JvmOverloads
import android.content.res.TypedArray
import android.graphics.*
import android.view.View.MeasureSpec
import android.os.Parcelable
import android.os.Parcel
import android.util.DisplayMetrics
import android.widget.GridView
import android.widget.AbsListView
import android.graphics.drawable.Drawable
import android.renderscript.*
import android.util.Log
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationAnalyzer
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting
import com.huawei.mlkit.sample.callback.ImageSegmentationResultCallBack
import com.huawei.mlkit.sample.camera.CameraConfiguration
import com.huawei.mlkit.sample.camera.FrameMetadata
import com.huawei.mlkit.sample.util.BitmapUtils
import com.huawei.mlkit.sample.views.graphic.*
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.io.IOException
import java.lang.Exception
import java.lang.NullPointerException

/**
 * A transactor to run object detector.
 *
 * @since 2019-12-26
 */
class ImageSegmentationTransactor(
    private val context: Context,
    options: MLImageSegmentationSetting?,
    backgroundBitmap: Bitmap?
) : BaseTransactor<MLImageSegmentation>() {
    private val detector: MLImageSegmentationAnalyzer
    private var foregroundBitmap: Bitmap? = null
    private var backgroundBitmap: Bitmap?
    private var imageSegmentationResultCallBack: ImageSegmentationResultCallBack? = null
    private var isBlur = false
    private var renderScript: RenderScript? = null

    // Return to processed image.
    fun setImageSegmentationResultCallBack(imageSegmentationResultCallBack: ImageSegmentationResultCallBack?) {
        this.imageSegmentationResultCallBack = imageSegmentationResultCallBack
    }

    override fun stop() {
        super.stop()
        try {
            detector.stop()
        } catch (e: IOException) {
            Log.e(
                TAG,
                "Exception thrown while trying to close image segmentation transactor: " + e.message
            )
        }
    }

    override fun detectInImage(frame: MLFrame?): Task<MLImageSegmentation> {
        return detector.asyncAnalyseFrame(frame)
    }

    protected fun analyseFrame(frame: MLFrame?): SparseArray<MLImageSegmentation> {
        return detector.analyseFrame(frame)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: MLImageSegmentation,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()
        if (results.getForeground() == null) {
            Log.i(TAG, "detection failed.")
            return
        }
        foregroundBitmap = results.foreground

        // Replace background.
        var resultBitmap = changeNextBackground(foregroundBitmap)
        if (frameMetadata.cameraFacing == CameraConfiguration.Companion.CAMERA_FACING_FRONT) {
            resultBitmap = convert(resultBitmap)
        }
        if (imageSegmentationResultCallBack != null) {
            imageSegmentationResultCallBack!!.callResultBitmap(resultBitmap)
        }
        val imageGraphic = CameraImageGraphic(graphicOverlay, resultBitmap)
        graphicOverlay.addGraphic(imageGraphic)
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Image segmentation detection failed: " + e.message)
    }

    /**
     * Replace the images in the assets directory as the background image in order.
     */
    private fun changeNextBackground(foregroundBitmap: Bitmap?): Bitmap? {
        val result: Bitmap?
        if (backgroundBitmap == null) {
            Toast.makeText(context, "No Background Image", Toast.LENGTH_SHORT).show()
            throw NullPointerException("No background image")
        }
        if (!equalToForegroundImageSize()) {
            backgroundBitmap = resizeImageToForegroundImage(backgroundBitmap)
        }
        val pixels = IntArray(backgroundBitmap!!.width * backgroundBitmap!!.height)
        backgroundBitmap!!.getPixels(
            pixels, 0, backgroundBitmap!!.width, 0, 0,
            backgroundBitmap!!.width, backgroundBitmap!!.height
        )
        result = BitmapUtils.joinBitmap(
            if (isBlur) blur(backgroundBitmap, 20) else backgroundBitmap,
            foregroundBitmap
        )
        return result
    }

    /**
     * Blur Bitmap
     * @param bitmap Original Bitmap
     * @param radius Blur Radius (1-25)
     * @return Bitmap
     */
    private fun blur(bitmap: Bitmap?, radius: Int): Bitmap {
        val outBitmap = Bitmap.createBitmap(bitmap!!)
        val `in` = Allocation.createFromBitmap(renderScript, bitmap)
        val out = Allocation.createTyped(renderScript, `in`.type)
        val scriptintrinsicblur =
            ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        scriptintrinsicblur.setRadius(radius.toFloat())
        scriptintrinsicblur.setInput(`in`)
        scriptintrinsicblur.forEach(out)
        out.copyTo(outBitmap)
        return outBitmap
    }

    /**
     * Stretch background image size to foreground image's.
     *
     * @param bitmap bitmap
     * @return Bitmap object
     */
    private fun resizeImageToForegroundImage(bitmap: Bitmap?): Bitmap? {
        var bitmap = bitmap
        val scaleWidth = foregroundBitmap!!.width.toFloat() / bitmap!!.width
        val scaleHeigth = foregroundBitmap!!.height
            .toFloat() / bitmap.height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeigth)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return bitmap
    }

    private fun equalToForegroundImageSize(): Boolean {
        Log.i(
            TAG,
            "FOREGREOUND SIZE;" + foregroundBitmap!!.width + ", height:" + foregroundBitmap!!.height
        )
        return backgroundBitmap!!.height == foregroundBitmap!!.height && backgroundBitmap!!.width == foregroundBitmap!!.width
    }

    /**
     * Front camera image position changed.
     */
    private fun convert(bitmap: Bitmap?): Bitmap {
        val m = Matrix()
        m.setScale(-1f, 1f) // horizontal flip.
        return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    fun setBlur(blur: Boolean) {
        isBlur = blur
    }

    fun setRenderScript(renderScript: RenderScript?) {
        this.renderScript = renderScript
    }

    companion object {
        private const val TAG = "ImageSegTransactor"
    }

    /**
     * Constructor for real-time replacement background.
     *
     * @param context              context.
     * @param options              options.
     * @param backgroundBitmap     background image.
     */
    init {
        MLAnalyzerFactory.getInstance().imageSegmentationAnalyzer
        detector = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(options)
        this.backgroundBitmap = backgroundBitmap
    }
}