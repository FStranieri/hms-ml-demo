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
 * 2020.2.21-Changed name from CameraSource to LensEngine, and adjusted the architecture, except for the classes: start and stop
 * Huawei Technologies Co., Ltd.
 */
package com.huawei.mlkit.sample.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.hardware.Camera.PreviewCallback
import android.util.Log
import androidx.annotation.RequiresPermission
import com.huawei.hms.common.size.Size
import com.huawei.mlkit.sample.transactor.*
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.max
import kotlin.math.min

/**
 * Manages the camera and allows UI updates on top of it (e.g. overlaying extra Graphics or
 * displaying extra information). This receives preview frames from the camera at a specified rate,
 * sending those frames to child classes' detectors / classifiers as fast as it is able to process.
 *
 * @since 2019-12-26
 */
@SuppressLint("MissingPermission")
class LensEngine(
    private var activity: Activity,
    configuration: CameraConfiguration,
    graphicOverlay: GraphicOverlay
) {
    @get:Synchronized
    var camera: Camera? = null
        private set
    private var transactingThread: Thread? = null
    private val transactingRunnable: FrameTransactingRunnable = FrameTransactingRunnable()
    private val transactorLock = ReentrantLock()
    private val transactorCondition = transactorLock.newCondition()
    private var frameTransactor: ImageTransactor? = null
    private val selector: CameraSelector = CameraSelector(activity, configuration)
    private val bytesToByteBuffer: MutableMap<ByteArray, ByteBuffer> = IdentityHashMap()
    private val overlay: GraphicOverlay = graphicOverlay

    /**
     * Stop the camera and release the resources of the camera and analyzer.
     */
    fun release() {
        transactorLock.withLock {
            stop()
            transactingRunnable.release()
            if (frameTransactor != null) {
                frameTransactor!!.stop()
                frameTransactor = null
            }
        }
    }

    /**
     * Turn on the camera and start sending preview frames to the analyzer for detection.
     *
     * @throws IOException IO Exception
     */
    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.CAMERA)
    @Synchronized
    @Throws(
        IOException::class
    )
    fun run(): LensEngine {
        if (camera != null) {
            return this
        }
        camera = createCamera()
        camera!!.startPreview()
        initializeOverlay()
        transactingThread = Thread(transactingRunnable)
        transactingRunnable.setActive(true)
        transactingThread!!.start()
        return this
    }

    /**
     * Take pictures.
     *
     * @param pictureCallback  Callback function after obtaining photo data.
     */
    @Synchronized
    fun takePicture(pictureCallback: PictureCallback?) {
        transactorLock.withLock {
            camera?.takePicture(null, null, null, pictureCallback)
        }
    }

    private fun initializeOverlay() {
        val min: Int
        val max: Int
        if (frameTransactor!!.isFaceDetection) {
            min = CameraConfiguration.Companion.DEFAULT_HEIGHT
            max = CameraConfiguration.Companion.DEFAULT_WIDTH
            if (activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                overlay.setCameraInfo(min, max, 0)
            } else {
                overlay.setCameraInfo(max, min, 0)
            }
        } else {
            val size = previewSize
            min = min(size!!.width, size.height)
            max = max(size.width, size.height)
            if (activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                overlay.setCameraInfo(min, max, facing)
            } else {
                overlay.setCameraInfo(max, min, facing)
            }
        }
        overlay.clear()
    }

    /**
     * Get camera preview size.
     *
     * @return Size Size of camera preview.
     */
    val previewSize: Size?
        get() = selector.previewSize
    val facing: Int
        get() = selector.facing

    /**
     * Turn off the camera and stop transmitting frames to the analyzer.
     */
    @Synchronized
    fun stop() {
        transactingRunnable.setActive(false)
        if (transactingThread != null) {
            try {
                transactingThread!!.join()
            } catch (e: InterruptedException) {
                Log.d(TAG, "Frame transacting thread interrupted on release.")
            }
            transactingThread = null
        }
        camera?.stopPreview()
        camera?.setPreviewCallbackWithBuffer(null)
        try {
            camera?.setPreviewDisplay(null)
            camera?.setPreviewTexture(null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear camera preview: $e")
        }
        camera?.release()
        camera = null
        bytesToByteBuffer.clear()
    }

    @SuppressLint("InlinedApi")
    @Throws(IOException::class)
    private fun createCamera(): Camera {
        val newCamera = selector.createCamera()
        newCamera.setPreviewCallbackWithBuffer(CameraPreviewCallback())
        newCamera.addCallbackBuffer(createPreviewBuffer(selector.previewSize))
        newCamera.addCallbackBuffer(createPreviewBuffer(selector.previewSize))
        newCamera.addCallbackBuffer(createPreviewBuffer(selector.previewSize))
        newCamera.addCallbackBuffer(createPreviewBuffer(selector.previewSize))
        return newCamera
    }

    /**
     * Create a buffer for the camera preview callback. The size of the buffer is based on the camera preview size and the camera image format.
     *
     * @param previewSize Preview size
     * @return Image data from the camera
     */
    @SuppressLint("InlinedApi")
    private fun createPreviewBuffer(previewSize: Size?): ByteArray {
        val bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21)
        val sizeInBits = previewSize!!.height.toLong() * previewSize.width * bitsPerPixel
        val bufferSize = Math.ceil(sizeInBits / 8.0).toInt() + 1
        val byteArray = ByteArray(bufferSize)
        val buffer = ByteBuffer.wrap(byteArray)
        check(!(!buffer.hasArray() || buffer.array() != byteArray)) { "Failed to create valid buffer for lensEngine." }
        bytesToByteBuffer[byteArray] = buffer
        return byteArray
    }

    private inner class CameraPreviewCallback : PreviewCallback {
        override fun onPreviewFrame(data: ByteArray, camera: Camera) {
            transactingRunnable.setNextFrame(data, camera)
        }
    }

    fun setMachineLearningFrameTransactor(transactor: ImageTransactor?) {
        transactorLock.withLock {
            frameTransactor?.stop()
            frameTransactor = transactor
        }
    }

    /**
     * It is used to receive the frame captured by the camera and pass it to the analyzer.
     */
    private inner class FrameTransactingRunnable internal constructor() : Runnable {
        private val lock = ReentrantLock()
        private val condition = lock.newCondition()
        private var active = true
        private var pendingFrameData: ByteBuffer? = null

        /**
         * Frees the transactor and can safely perform this operation only after the associated thread has completed.
         */
        @SuppressLint("Assert")
        fun release() {
            lock.withLock { assert(transactingThread!!.state == Thread.State.TERMINATED) }
        }

        fun setActive(active: Boolean) {
            lock.withLock {
                this.active = active
                condition.signalAll()
            }
        }

        /**
         * Sets the frame data received from the camera. Adds a previously unused frame buffer (if exit) back to the camera.
         */
        fun setNextFrame(data: ByteArray, camera: Camera) {
            lock.withLock {
                if (pendingFrameData != null) {
                    camera.addCallbackBuffer(pendingFrameData!!.array())
                    pendingFrameData = null
                }
                if (!bytesToByteBuffer.containsKey(data)) {
                    Log.d(
                        TAG, "Skipping frame. Could not find ByteBuffer associated with the image "
                                + "data from the camera."
                    )
                    return
                }
                pendingFrameData = bytesToByteBuffer[data]
                condition.signalAll()
            }
        }

        @SuppressLint("InlinedApi")
        override fun run() {
            var data: ByteBuffer?
            while (true) {
                lock.withLock {
                    while (active && pendingFrameData == null) {
                        try {
                            // Waiting for next frame.
                            condition.await()
                        } catch (e: InterruptedException) {
                            Log.w(TAG, "Frame transacting loop terminated.", e)
                            return
                        }
                    }
                    if (!active) {
                        pendingFrameData = null
                        return
                    }
                    data = pendingFrameData
                    pendingFrameData = null
                }
                try {
                    transactorLock.withLock {
                        frameTransactor!!.process(
                            data,
                            FrameMetadata.Builder()
                                .setWidth(selector.previewSize!!.width)
                                .setHeight(selector.previewSize!!.height)
                                .setRotation(selector.rotation)
                                .setCameraFacing(selector.facing)
                                .build(),
                            overlay
                        )
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Exception thrown from receiver.", t)
                } finally {
                    camera!!.addCallbackBuffer(data!!.array())
                }
            }
        }
    }

    companion object {
        private const val TAG = "LensEngine"
    }

    init {
        overlay.clear()
    }
}