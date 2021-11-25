/**
 * Copyright 2018 Google LLC
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
 * 2020.2.21-Changed name from VisionProcessorBase to BaseTransactor.
 * 2020.2.21-Deleted method: process(Bitmap bitmap, GraphicOverlay graphicOverlay,
 * String path,boolean flag);
 * process(Bitmap bitmap, GraphicOverlay graphicOverlay,String path);
 * onSuccess(
 * @Nullable Bitmap originalCameraImage,
 * @NonNull T results,
 * @NonNull FrameMetadata frameMetadata,
 * @NonNull GraphicOverlay graphicOverlay, String path, boolean flag);
 * onSuccess(
 * @Nullable Bitmap originalCameraImage,
 * @NonNull T results,
 * @NonNull FrameMetadata frameMetadata,
 * @NonNull GraphicOverlay graphicOverlay, String path);
 * writeFileSdcard(String message);
 * Huawei Technologies Co., Ltd.
 */
package com.huawei.mlkit.sample.transactor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.util.Log
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.mlkit.sample.camera.CameraConfiguration
import com.huawei.mlkit.sample.camera.FrameMetadata
import com.huawei.mlkit.sample.util.BitmapUtils
import com.huawei.mlkit.sample.util.NV21ToBitmapConverter
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.nio.ByteBuffer

abstract class BaseTransactor<T> : ImageTransactor {
    // To keep the latest images and its metadata.
    private var latestImage: ByteBuffer? = null
    private var latestImageMetaData: FrameMetadata? = null

    // To keep the images and metadata in process.
    private var transactingImage: ByteBuffer? = null
    private var transactingMetaData: FrameMetadata? = null
    private var mContext: Context? = null
    private var converter: NV21ToBitmapConverter? = null

    constructor() {}
    constructor(context: Context?) {
        mContext = context
        converter = NV21ToBitmapConverter(mContext)
    }

    @Synchronized
    override fun process(
        data: ByteBuffer?,
        frameMetadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (transactingImage == null && transactingMetaData == null) {
            processLatestImage(graphicOverlay)
        }
    }

    override fun process(bitmap: Bitmap?, graphicOverlay: GraphicOverlay) {
        val frame = MLFrame.Creator().setBitmap(bitmap).create()
        detectInVisionImage(bitmap, frame, null, graphicOverlay)
    }

    @Synchronized
    private fun processLatestImage(graphicOverlay: GraphicOverlay) {
        transactingImage = latestImage
        transactingMetaData = latestImageMetaData
        latestImage = null
        latestImageMetaData = null
        var bitmap: Bitmap? = null
        if (transactingImage != null && transactingMetaData != null) {
            val width: Int = transactingMetaData!!.width
            val height: Int = transactingMetaData!!.height
            val metadata = MLFrame.Property.Creator().setFormatType(ImageFormat.NV21)
                .setWidth(width)
                .setHeight(height)
                .setQuadrant(transactingMetaData!!.rotation)
                .create()
            if (isFaceDetection) {
                Log.d(TAG, "Total HMSFaceProc getBitmap start")
                bitmap = converter!!.getBitmap(transactingImage, transactingMetaData)
                Log.d(TAG, "Total HMSFaceProc getBitmap end")
                val resizeBitmap = BitmapUtils.scaleBitmap(
                    bitmap, CameraConfiguration.Companion.DEFAULT_HEIGHT,
                    CameraConfiguration.Companion.DEFAULT_WIDTH
                )
                Log.d(TAG, "Total HMSFaceProc resizeBitmap end")
                detectInVisionImage(
                    bitmap, MLFrame.fromBitmap(resizeBitmap), transactingMetaData,
                    graphicOverlay
                )
            } else {
                bitmap = BitmapUtils.getBitmap(transactingImage, transactingMetaData)
                detectInVisionImage(
                    bitmap, MLFrame.fromByteBuffer(transactingImage, metadata),
                    transactingMetaData, graphicOverlay
                )
            }
        }
    }

    private fun detectInVisionImage(
        bitmap: Bitmap?, image: MLFrame, metadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay
    ) {
        detectInImage(image).addOnSuccessListener { results ->
            if (metadata == null || metadata.cameraFacing == CameraConfiguration.cameraFacing) {
                this@BaseTransactor.onSuccess(bitmap, results, metadata!!, graphicOverlay)
            }
            processLatestImage(graphicOverlay)
        }
            .addOnFailureListener { e -> this@BaseTransactor.onFailure(e) }
    }

    override fun stop() {}

    /**
     * Detect image
     *
     * @param image MLFrame object
     * @return Task object
     */
    protected abstract fun detectInImage(image: MLFrame?): Task<T>

    /**
     * Callback that executes with a successful detection result.
     *
     * @param originalCameraImage hold the original image from camera, used to draw the background image.
     * @param results T object
     * @param frameMetadata FrameMetadata object
     * @param graphicOverlay GraphicOverlay object
     */
    protected abstract fun onSuccess(
        originalCameraImage: Bitmap?,
        results: T,
        frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay
    )

    /**
     * Callback that executes with failure detection result.
     *
     * @param exception Exception object
     */
    protected abstract fun onFailure(exception: Exception)
    override val isFaceDetection: Boolean
        get() = false

    companion object {
        private const val TAG = "BaseTransactor"
    }
}