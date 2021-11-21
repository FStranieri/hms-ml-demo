/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */
package com.huawei.mlkit.sample.util

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
import android.os.Environment
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.renderscript.ScriptIntrinsicYuvToRGB
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

class CropBitMap(private val targetHeight: Int, private val targetWidth: Int) {
    private val output: Bitmap
    fun getCropBitmap(input: Bitmap): Bitmap {
        val srcL: Int
        val srcR: Int
        val srcT: Int
        val srcB: Int
        val dstL: Int
        val dstR: Int
        val dstT: Int
        val dstB: Int
        val w = input.width
        val h = input.height
        if (targetWidth > w) { // padding
            srcL = 0
            srcR = w
            dstL = (targetWidth - w) / 2
            dstR = dstL + w
        } else { // cropping
            dstL = 0
            dstR = targetWidth
            srcL = (w - targetWidth) / 2
            srcR = srcL + targetWidth
        }
        if (targetHeight > h) { // padding
            srcT = 0
            srcB = h
            dstT = (targetHeight - h) / 2
            dstB = dstT + h
        } else { // cropping
            dstT = 0
            dstB = targetHeight
            srcT = (h - targetHeight) / 2
            srcB = srcT + targetHeight
        }
        val src = Rect(srcL, srcT, srcR, srcB)
        val dst = Rect(dstL, dstT, dstR, dstB)
        Canvas(output).drawBitmap(input, src, dst, null)
        return output
    }

    init {
        output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
    }
}