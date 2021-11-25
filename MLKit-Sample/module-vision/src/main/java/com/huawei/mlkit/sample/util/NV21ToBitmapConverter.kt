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
package com.huawei.mlkit.sample.util

import android.content.Context
import android.graphics.*
import android.renderscript.*
import com.huawei.mlkit.sample.camera.FrameMetadata
import java.nio.ByteBuffer
import java.util.*

class NV21ToBitmapConverter(context: Context?) {
    private val renderScript: RenderScript
    private val yuvToRgbIntrinsic: ScriptIntrinsicYuvToRGB
    private var yuvType: Type.Builder? = null
    private var rgbaType: Type.Builder? = null
    private var `in`: Allocation? = null
    private var out: Allocation? = null
    private var applicationContext: Context? = null
    private var mWidth = -1
    private var mHeight = -1
    private var length = -1
    fun getApplicationContext(): Context? {
        checkNotNull(applicationContext) { "initial must be called first" }
        return applicationContext
    }

    fun convertYUVtoRGB(yuvData: ByteArray, width: Int, height: Int): Bitmap {
        if (yuvType == null) {
            yuvType = Type.Builder(renderScript, Element.U8(renderScript)).setX(yuvData.size)
            `in` = Allocation.createTyped(renderScript, yuvType!!.create(), Allocation.USAGE_SCRIPT)
            rgbaType =
                Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(width).setY(height)
            out = Allocation.createTyped(renderScript, rgbaType!!.create(), Allocation.USAGE_SCRIPT)
        }
        `in`!!.copyFrom(yuvData)
        yuvToRgbIntrinsic.setInput(`in`)
        yuvToRgbIntrinsic.forEach(out)
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        out!!.copyTo(result)
        return result
    }

    /**
     * Returns a transformation matrix from one reference frame into another.
     * Handles cropping (if maintaining aspect ratio is desired) and rotation.
     *
     * @param srcWidth Width of source frame.
     * @param srcHeight Height of source frame.
     * @param dstWidth Width of destination frame.
     * @param dstHeight Height of destination frame.
     * @param applyRotation Amount of rotation to apply from one frame to another.
     * Must be a multiple of 90.
     * @param flipHorizontal should flip horizontally
     * @param flipVertical should flip vertically
     * @param maintainAspectRatio If true, will ensure that scaling in x and y remains constant,
     * cropping the image if necessary.
     * @return The transformation fulfilling the desired requirements.
     */
    fun getTransformationMatrix(
        srcWidth: Int, srcHeight: Int, dstWidth: Int,
        dstHeight: Int, applyRotation: Int, flipHorizontal: Boolean, flipVertical: Boolean,
        maintainAspectRatio: Boolean
    ): Matrix {
        val matrix = Matrix()
        if (applyRotation < 360) {
            require(applyRotation % 90 == 0) {
                String.format(
                    Locale.ENGLISH,
                    "Rotation of %d",
                    applyRotation
                )
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f)

            // Rotate around origin.
            matrix.postRotate(applyRotation.toFloat())
        }

        // Account for the already applied rotation, if any, and then determine how
        // much scaling is needed for each axis.
        val transpose = (Math.abs(applyRotation) + 90) % 180 == 0
        val inWidth = if (transpose) srcHeight else srcWidth
        val inHeight = if (transpose) srcWidth else srcHeight
        val flipHorizontalFactor = if (flipHorizontal) -1 else 1
        val flipVerticalFactor = if (flipVertical) -1 else 1

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            val scaleFactorX = flipHorizontalFactor * dstWidth / inWidth.toFloat()
            val scaleFactorY = flipVerticalFactor * dstHeight / inHeight.toFloat()
            if (maintainAspectRatio) {
                // Scale by minimum factor so that dst is filled completely while
                // maintaining the aspect ratio. Some image may fall off the edge.
                val scaleFactor = Math.max(Math.abs(scaleFactorX), Math.abs(scaleFactorY))
                matrix.postScale(scaleFactor, scaleFactor)
            } else {
                // Scale exactly to fill dst from src.
                matrix.postScale(scaleFactorX, scaleFactorY)
            }
        }
        if (applyRotation < 360) {
            // Translate back from origin centered reference to destination frame.
            val dx = dstWidth / 2.0f
            val dy = dstHeight / 2.0f
            matrix.postTranslate(dx, dy)

            // if postScale fail, nothing happen
            matrix.postScale(flipHorizontalFactor.toFloat(), flipVerticalFactor.toFloat(), dx, dy)
        }
        return matrix
    }

    fun convert(
        bytes: ByteArray,
        srcWidth: Int,
        srcHeight: Int,
        destWidth: Int,
        destHeight: Int,
        rotation: Int
    ): Bitmap {
        // when width or height changed, recreate yuvType, rgbType etc
        recreateIfNeed(bytes, srcWidth, srcHeight, rotation)
        val target = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(target)
        val source = convertYUVtoRGB(bytes, srcWidth, srcHeight)
        val matrix = getTransformationMatrix(
            srcWidth,
            srcHeight,
            destWidth,
            destHeight,
            rotation,
            true,
            false,
            false
        )
        canvas.drawBitmap(source, matrix, null)
        return target
    }

    fun getBitmap(data: ByteBuffer?, metadata: FrameMetadata?): Bitmap {
        val target: Bitmap
        val bytes = data!!.array()
        val width = metadata!!.width
        val height = metadata.height
        val rotation = metadata.rotation * 90
        var matrix = Matrix()
        // when width or height changed, recreate yuvType, rgbType etc
        recreateIfNeed(
            bytes,
            metadata.width,
            metadata.height,
            metadata.rotation * 90
        )
        val source = convertYUVtoRGB(bytes, width, height)
        // final Canvas canvas;
        if (rotation == 0 || rotation == 180) {
            target = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            matrix = if (metadata.cameraFacing == 0) {
                getTransformationMatrix(width, height, width, height, rotation, false, false, false)
            } else {
                getTransformationMatrix(width, height, width, height, rotation, true, false, false)
            }
        } else {
            target = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
            matrix = if (metadata.cameraFacing == 0) {
                getTransformationMatrix(width, height, height, width, rotation, false, false, false)
            } else {
                getTransformationMatrix(width, height, height, width, rotation, true, false, false)
            }
        }
        val canvas = Canvas(target)
        canvas.drawBitmap(source, matrix, null)
        return target
    }

    private fun recreateIfNeed(bytes: ByteArray, srcWidth: Int, srcHeight: Int, rotation: Int) {
        if (mWidth == srcWidth && mHeight == srcHeight && length == bytes.size) {
            return
        }
        mWidth = srcWidth
        mHeight = srcHeight
        length = bytes.size
        yuvType = null
        rgbaType = null
    }

    init {
        requireNotNull(context) { "context can't be null" }
        val appContext = context.applicationContext
        if (appContext == null) {
            applicationContext = context
        } else {
            applicationContext = appContext
        }
        renderScript = RenderScript.create(applicationContext)
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript))
    }
}