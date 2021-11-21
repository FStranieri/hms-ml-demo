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

import android.Manifest
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
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.content.ContentUris
import android.content.Context
import android.os.Environment
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.content.Intent
import android.graphics.YuvImage
import android.graphics.BitmapFactory
import android.os.ParcelFileDescriptor
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.content.SharedPreferences
import kotlin.jvm.JvmOverloads
import android.content.res.TypedArray
import android.view.View.MeasureSpec
import android.graphics.Shader
import android.os.Parcelable
import android.os.Parcel
import android.graphics.Xfermode
import android.util.DisplayMetrics
import android.graphics.PorterDuffXfermode
import android.graphics.PorterDuff
import android.graphics.DashPathEffect
import android.widget.GridView
import android.widget.AbsListView
import android.graphics.drawable.Drawable
import android.util.Log
import java.lang.Exception
import java.nio.ByteBuffer

object CommonUtils {
    private const val TAG = "CommonUtils"
    const val PERMISSION_CODE_STORAGE = 1
    const val PERMISSION_CODE_CAMERA = 2
    const val REQUEST_PIC = 3
    const val REQUEST_TAKE_PHOTO_CODE = 4
    const val STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val CAMERA_PERMISSION = Manifest.permission.CAMERA

    /**
     * Handle ByteBuffer
     *
     * @param src High resolution ByteBuffer
     * @param dst Low resolution ByteBuffer
     * @param srcWidth High resolution wide
     * @param srcHeight High resolution height
     * @param dstWidth Low resolution wide
     * @param dstHeight Low resolution height
     */
    fun handleByteBuffer(
        src: ByteBuffer, dst: ByteBuffer,
        srcWidth: Int, srcHeight: Int, dstWidth: Int, dstHeight: Int
    ) {
        var y: Int
        var x: Int
        var srcY: Int
        var srcX: Int
        var srcIndex: Int
        val xrIntFloat = (srcWidth shl 16) / dstWidth + 1
        val yrIntFloat = (srcHeight shl 16) / dstHeight + 1
        val dstUv = dstHeight * dstWidth
        val srcUv = srcHeight * srcWidth
        var dstUvYScanline = 0
        var srcUvYScanline = 0
        var dstYSlice = 0
        var srcYSlice: Int
        var sp: Int
        var dp: Int
        y = 0
        while (y < dstHeight and 7.inv()) {
            srcY = y * yrIntFloat shr 16
            srcYSlice = srcY * srcWidth
            if (y and 1 == 0) {
                dstUvYScanline = dstUv + y / 2 * dstWidth
                srcUvYScanline = srcUv + srcY / 2 * srcWidth
            }
            x = 0
            while (x < dstWidth and 7.inv()) {
                srcX = x * xrIntFloat shr 16
                try {
                    dst.put(x + dstYSlice, src[srcYSlice + srcX])
                } catch (e: Exception) {
                    Log.d(
                        TAG,
                        "nv12_Resize Exception1" + e.message
                    )
                }
                if (y and 1 == 0) {
                    if (x and 1 == 0) {
                        srcIndex = srcX / 2 * 2
                        sp = dstUvYScanline + x
                        dp = srcUvYScanline + srcIndex
                        try {
                            dst.put(sp, src[dp])
                        } catch (e: Exception) {
                            Log.d(
                                TAG,
                                "nv12_Resize Exception2" + e.message
                            )
                        }
                        ++sp
                        ++dp
                        try {
                            dst.put(sp, src[dp])
                        } catch (e: Exception) {
                            Log.d(
                                TAG,
                                "nv12_Resize Exception3" + e.message
                            )
                        }
                    }
                }
                ++x
            }
            dstYSlice += dstWidth
            ++y
        }
    }

    fun dp2px(context: Context, dipValue: Float): Float {
        return dipValue * context.resources.displayMetrics.density + 0.5f
    }
}