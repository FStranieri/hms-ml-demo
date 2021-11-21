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

class SharedPreferencesUtil(context: Context) {
    private val mPreferences: SharedPreferences
    private val mEditor: SharedPreferences.Editor
    fun putIntValue(key: String?, value: Int) {
        mEditor.putInt(key, value)
        mEditor.commit()
    }

    fun getIntValue(key: String?): Int {
        return mPreferences.getInt(key, -1)
    }

    companion object {
        const val TAG = "SharedPreferencesUtil"
        private var mSharedPreferencesUtil: SharedPreferencesUtil? = null
        fun getInstance(context: Context): SharedPreferencesUtil? {
            if (mSharedPreferencesUtil == null) {
                synchronized(SharedPreferencesUtil::class.java) {
                    if (mSharedPreferencesUtil == null) {
                        mSharedPreferencesUtil = SharedPreferencesUtil(context)
                    }
                }
            }
            return mSharedPreferencesUtil
        }
    }

    init {
        mPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        mEditor = mPreferences.edit()
    }
}