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
import android.graphics.Color
import android.util.Log
import android.util.SparseArray
import android.widget.ImageView
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationAnalyzer
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationClassification
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting
import com.huawei.mlkit.sample.callback.ImageSegmentationResultCallBack
import com.huawei.mlkit.sample.camera.FrameMetadata
import com.huawei.mlkit.sample.util.ImageUtils
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.io.IOException

class StillImageSegmentationTransactor : BaseTransactor<MLImageSegmentation> {
    private val detector: MLImageSegmentationAnalyzer

    //private Context context;
    private var originBitmap: Bitmap
    private var backgroundBitmap: Bitmap?
    private var imageView: ImageView
    private var detectCategory: Int
    private var color: Int
    private var imageSegmentationResultCallBack: ImageSegmentationResultCallBack? = null

    /**
     * @param options          Options.
     * @param originBitmap     Foreground, picture to replace.
     * @param imageView        ImageView.
     * @param detectCategory -1 represents all detections, others represent the type of replacement color currently detected.
     */
    constructor(
        options: MLImageSegmentationSetting?,
        originBitmap: Bitmap,
        imageView: ImageView,
        detectCategory: Int
    ) {
        //this.context = context;
        detector = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(options)
        this.originBitmap = originBitmap
        backgroundBitmap = null
        this.imageView = imageView
        this.detectCategory = detectCategory
        color = Color.WHITE
    }

    /**
     * Replace background.
     *
     * @param options          Options.
     * @param originBitmap     Foreground, picture to replace.
     * @param backgroundBitmap Background.
     * @param imageView        ImageView.
     * @param detectCategory   -1 represents all detections, others represent the type of replacement color currently detected.
     */
    constructor(
        options: MLImageSegmentationSetting?,
        originBitmap: Bitmap,
        backgroundBitmap: Bitmap?,
        imageView: ImageView,
        detectCategory: Int
    ) {
        //this.context = context;
        detector = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(options)
        this.originBitmap = originBitmap
        this.backgroundBitmap = backgroundBitmap
        this.imageView = imageView
        this.detectCategory = detectCategory
        color = Color.WHITE
    }

    // Sets the drawn color of detected features.
    fun setColor(color: Int) {
        this.color = color
    }

    // Interface for obtaining processed image data.
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
        if (results.getMasks() == null) {
            Log.i(TAG, "detection failed, none mask return")
            return
        }
        // If the originBitmap is automatically recycled, the callback is complete.
        if (originBitmap.isRecycled) {
            return
        }
        val pixels: IntArray = if (detectCategory == -1) {
            byteArrToIntArr(results.getMasks())
        } else if (backgroundBitmap == null) {
            changeColor(results.getMasks())
        } else {
            // If the backgroundBitmap is automatically recycled, the callback is complete.
            if (backgroundBitmap!!.isRecycled) {
                return
            }
            changeBackground(results.getMasks())
        }
        val processedBitmap = Bitmap.createBitmap(
            pixels,
            0,
            originBitmap.width,
            originBitmap.width,
            originBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        imageView.setImageBitmap(processedBitmap)
        if (imageSegmentationResultCallBack != null) {
            imageSegmentationResultCallBack!!.callResultBitmap(processedBitmap)
        }
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Image segmentation detection failed: " + e.message)
    }

    private fun byteArrToIntArr(masks: ByteArray): IntArray {
        val results = IntArray(masks.size)
        for (i in masks.indices) {
            when {
                masks[i].toInt() == MLImageSegmentationClassification.TYPE_HUMAN -> {
                    results[i] = Color.BLACK
                }
                masks[i].toInt() == MLImageSegmentationClassification.TYPE_SKY -> {
                    results[i] = Color.BLUE
                }
                masks[i].toInt() == MLImageSegmentationClassification.TYPE_GRASS -> {
                    results[i] = Color.DKGRAY
                }
                masks[i].toInt() == MLImageSegmentationClassification.TYPE_FOOD -> {
                    results[i] = Color.YELLOW
                }
                masks[i].toInt() == MLImageSegmentationClassification.TYPE_CAT -> {
                    results[i] = Color.LTGRAY
                }
                masks[i].toInt() == MLImageSegmentationClassification.TYPE_BUILD -> {
                    results[i] = Color.CYAN
                }
                masks[i].toInt() == MLImageSegmentationClassification.TYPE_FLOWER -> {
                    results[i] = Color.RED
                }
                masks[i].toInt() == MLImageSegmentationClassification.TYPE_WATER -> {
                    results[i] = Color.GRAY
                }
                masks[i].toInt() == MLImageSegmentationClassification.TYPE_SAND -> {
                    results[i] = Color.MAGENTA
                }
                masks[i].toInt() == MLImageSegmentationClassification.TYPE_MOUNTAIN -> {
                    results[i] = Color.GREEN
                }
                else -> {
                    results[i] = Color.WHITE
                }
            }
        }
        return results
    }

    // Cut out the desired element, the background is white.
    private fun changeColor(masks: ByteArray): IntArray {
        val results = IntArray(masks.size)
        val orginPixels = IntArray(originBitmap.width * originBitmap.height)
        originBitmap.getPixels(
            orginPixels,
            0,
            originBitmap.width,
            0,
            0,
            originBitmap.width,
            originBitmap.height
        )
        for (i in masks.indices) {
            if (masks[i].toInt() == detectCategory) {
                results[i] = color
            } else {
                results[i] = orginPixels[i]
            }
        }
        return results
    }

    // Replace background image.
    private fun changeBackground(masks: ByteArray): IntArray {
        // Make the background and foreground images the same size.
        if (backgroundBitmap != null) {
            if (!ImageUtils.Companion.equalImageSize(originBitmap, backgroundBitmap)) {
                backgroundBitmap = ImageUtils.Companion.resizeImageToForegroundImage(
                    originBitmap, backgroundBitmap
                )
            }
        }
        val results = IntArray(masks.size)
        val originPixels = IntArray(originBitmap.width * originBitmap.height)
        val backgroundPixels = IntArray(originPixels.size)
        originBitmap.getPixels(
            originPixels,
            0,
            originBitmap.width,
            0,
            0,
            originBitmap.width,
            originBitmap.height
        )
        if (null != backgroundBitmap) {
            backgroundBitmap!!.getPixels(
                backgroundPixels,
                0,
                originBitmap.width,
                0,
                0,
                originBitmap.width,
                originBitmap.height
            )
        }
        for (i in masks.indices) {
            if (masks[i].toInt() == detectCategory) {
                results[i] = backgroundPixels[i]
            } else {
                results[i] = originPixels[i]
            }
        }
        return results
    }

    companion object {
        private const val TAG = "StillImageSegTransactor"
    }
}