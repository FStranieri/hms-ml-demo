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

object Constant {
    const val GET_DATA_SUCCESS = 100
    const val GET_DATA_FAILED = 101
    const val CAMERA_FACING = "facing"
    const val CLOUD_IMAGE_CLASSIFICATION = "Cloud Classification"
    const val CLOUD_LANDMARK_DETECTION = "Landmark"
    const val MODEL_TYPE = "model_type"
    const val ADD_PICTURE_TYPE = "picture_type"
    const val TYPE_TAKE_PHOTO = "take photo"
    const val TYPE_SELECT_IMAGE = "select image"
    const val DEFAULT_VERSION = "1.0.3.300"
    const val SUPER_RESOLUTION_TYPE = "super_resolution_type"
    const val TYPE_IMAGE_SUPER_RESOLUTION = "type_image"
    const val TYPE_TEXT_SUPER_RESOLUTION = "type_text"
    var IMAGES = intArrayOf(
        R.mipmap.img_001, R.mipmap.img_002, R.mipmap.img_003, R.mipmap.img_004,
        R.mipmap.img_005, R.mipmap.img_006, R.mipmap.img_007, R.mipmap.img_008, R.mipmap.img_009
    )
    var COLOR_TABLE = intArrayOf(
        Color.rgb(255, 0, 0),
        Color.rgb(255, 255, 0),
        Color.rgb(0, 255, 0),
        Color.rgb(0, 255, 255),
        Color.rgb(0, 0, 255),
        Color.rgb(255, 0, 255),
        Color.rgb(255, 0, 0)
    )

    /**
     * Number of the background image used in the background replacement.
     */
    const val VALUE_KEY = "index_value"
}