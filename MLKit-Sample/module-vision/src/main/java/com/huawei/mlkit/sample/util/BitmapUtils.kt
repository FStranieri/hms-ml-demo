/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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
import android.hardware.Camera.CameraInfo
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
import android.content.Context
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
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.view.*
import com.huawei.mlkit.sample.camera.FrameMetadata
import java.io.*
import java.lang.Exception
import java.nio.ByteBuffer

object BitmapUtils {
    private const val TAG = "BitmapUtils"

    /**
     * Convert nv21 format byte buffer to bitmap
     *
     * @param data ByteBuffer data
     * @param metadata Frame meta data
     * @return Bitmap object
     */
    fun getBitmap(data: ByteBuffer?, metadata: FrameMetadata?): Bitmap? {
        data!!.rewind()
        val imageBuffer = ByteArray(data.limit())
        data[imageBuffer, 0, imageBuffer.size]
        try {
            val yuvImage = YuvImage(
                imageBuffer, ImageFormat.NV21, metadata.getWidth(),
                metadata.getHeight(), null
            )
            val stream = ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                Rect(0, 0, metadata.getWidth(), metadata.getHeight()),
                80,
                stream
            )
            val bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
            stream.close()
            return rotateBitmap(bitmap, metadata.getRotation(), metadata.getCameraFacing())
        } catch (e: Exception) {
            Log.e(TAG, "Error: " + e.message)
        }
        return null
    }

    fun rotateBitmap(bitmap: Bitmap, rotation: Int, facing: Int): Bitmap {
        val matrix = Matrix()
        var rotationDegree = 0
        if (rotation == MLFrame.SCREEN_SECOND_QUADRANT) {
            rotationDegree = 90
        } else if (rotation == MLFrame.SCREEN_THIRD_QUADRANT) {
            rotationDegree = 180
        } else if (rotation == MLFrame.SCREEN_FOURTH_QUADRANT) {
            rotationDegree = 270
        }
        matrix.postRotate(rotationDegree.toFloat())
        if (facing != CameraInfo.CAMERA_FACING_BACK) {
            matrix.postScale(-1.0f, 1.0f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun recycleBitmap(vararg bitmaps: Bitmap?) {
        for (bitmap in bitmaps) {
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
                bitmap = null
            }
        }
    }

    private fun getImagePath(activity: Activity, uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity.managedQuery(uri, projection, null, null, null)
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }

    fun loadFromPathWithoutZoom(activity: Activity, uri: Uri, width: Int, height: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val path = getImagePath(activity, uri)
        BitmapFactory.decodeFile(path, options)
        val sampleSize = calculateInSampleSize(options, width, height)
        options.inSampleSize = sampleSize
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(path, options)
        return rotateBitmap(bitmap, getRotationAngle(path))
    }

    fun loadFromPath(activity: Activity, uri: Uri, width: Int, height: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val path = getImagePath(activity, uri)
        BitmapFactory.decodeFile(path, options)
        val sampleSize = calculateInSampleSize(options, width, height)
        options.inSampleSize = sampleSize
        options.inJustDecodeBounds = false
        val bitmap = zoomImage(BitmapFactory.decodeFile(path, options), width, height)
        return rotateBitmap(bitmap, getRotationAngle(path))
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            // Calculate height and required height scale.
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            // Calculate width and required width scale.
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            // Take the larger of the values.
            inSampleSize = if (heightRatio > widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    // Scale pictures to screen width.
    private fun zoomImage(imageBitmap: Bitmap, targetWidth: Int, maxHeight: Int): Bitmap {
        val scaleFactor = Math.max(
            imageBitmap.width.toFloat() / targetWidth.toFloat(),
            imageBitmap.height.toFloat() / maxHeight.toFloat()
        )
        return Bitmap.createScaledBitmap(
            imageBitmap,
            (imageBitmap.width / scaleFactor).toInt(),
            (imageBitmap.height / scaleFactor).toInt(),
            true
        )
    }

    /**
     * Get the rotation angle of the photo.
     *
     * @param path photo path.
     * @return angle.
     */
    fun getRotationAngle(path: String?): Int {
        var rotation = 0
        try {
            val exifInterface = ExifInterface(path!!)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to get rotation: " + e.message)
        }
        return rotation
    }

    fun rotateBitmap(bitmap: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Failed to rotate bitmap: " + e.message)
        }
        return result ?: bitmap
    }

    /**
     * Stretch the bitmap based on the given width and height
     *
     * @param origin    Original image
     * @param newWidth  Width of the new bitmap
     * @param newHeight Height of the new bitmap
     * @return new Bitmap
     */
    fun scaleBitmap(origin: Bitmap?, newWidth: Int, newHeight: Int): Bitmap? {
        val scaleWidth: Float
        val scaleHeight: Float
        if (origin == null) {
            return null
        }
        val height = origin.height
        val width = origin.width
        if (height > width) {
            scaleWidth = newWidth.toFloat() / width
            scaleHeight = newHeight.toFloat() / height
        } else {
            scaleWidth = newWidth.toFloat() / height
            scaleHeight = newHeight.toFloat() / width
        }
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
    }

    /**
     * Fusion of two images.
     * @param background background image.
     * @param foreground foreground image.
     * @return return Bitmap
     */
    fun joinBitmap(background: Bitmap?, foreground: Bitmap?): Bitmap? {
        if (background == null || foreground == null) {
            Log.e(TAG, "bitmap is null.")
            return null
        }
        if (background.height != foreground.height || background.width != foreground.width) {
            Log.e(TAG, "bitmap size is not match.")
            return null
        }
        val newmap =
            Bitmap.createBitmap(background.width, background.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newmap)
        canvas.drawBitmap(background, 0f, 0f, null)
        canvas.drawBitmap(foreground, 0f, 0f, null)
        canvas.save()
        canvas.restore()
        return newmap
    }

    fun saveToAlbum(bitmap: Bitmap, context: Context) {
        var file: File? = null
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val root = File(Environment.getExternalStorageDirectory().absoluteFile, context.packageName)
        val dir = File(root, "image")
        if (dir.mkdirs() || dir.isDirectory) {
            file = File(dir, fileName)
        }
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, e.message!!)
        } catch (e: IOException) {
            Log.e(TAG, e.message!!)
        } finally {
            try {
                os?.close()
            } catch (e: IOException) {
                Log.e(TAG, e.message!!)
            }
        }

        // Insert pictures into the system gallery.
        try {
            if (null != file) {
                MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    file.canonicalPath,
                    fileName,
                    null
                )
            }
        } catch (e: IOException) {
            Log.e(TAG, e.message!!)
        }
        if (file == null) {
            return
        }
        // Gallery refresh.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            var path: String? = null
            try {
                path = file.canonicalPath
            } catch (e: IOException) {
                Log.e(TAG, e.message!!)
            }
            MediaScannerConnection.scanFile(
                context, arrayOf(path), null
            ) { path, uri ->
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = uri
                context.sendBroadcast(mediaScanIntent)
            }
        } else {
            val relationDir = file.parent
            val file1 = File(relationDir)
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.fromFile(file1.absoluteFile)
                )
            )
        }
    }

    fun loadBitmapFromView(view: View, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(0, 0, width, height)
        view.draw(canvas)
        return bitmap
    }

    /**
     * Returns a transformation matrix from one reference frame into another. Handles cropping (if
     * maintaining aspect ratio is desired) and rotation.
     *
     * @param srcWidth Width of source frame.
     * @param srcHeight Height of source frame.
     * @param dstWidth Width of destination frame.
     * @param dstHeight Height of destination frame.
     * @param applyRotation Amount of rotation to apply from one frame to another. Must be a multiple
     * of 90.
     * @param maintainAspectRatio If true, will ensure that scaling in x and y remains constant,
     * cropping the image if necessary.
     * @return The transformation fulfilling the desired requirements.
     */
    fun getTransformationMatrix(
        srcWidth: Int,
        srcHeight: Int,
        dstWidth: Int,
        dstHeight: Int,
        applyRotation: Int,
        maintainAspectRatio: Boolean
    ): Matrix {
        val matrix = Matrix()
        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
                Log.w(TAG, "Rotation of %d % 90 != 0  $applyRotation")
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

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            val scaleFactorX = dstWidth / inWidth.toFloat()
            val scaleFactorY = dstHeight / inHeight.toFloat()
            if (maintainAspectRatio) {
                // Scale by minimum factor so that dst is filled completely while
                // maintaining the aspect ratio. Some image may fall off the edge.
                val scaleFactor = Math.max(scaleFactorX, scaleFactorY)
                matrix.postScale(scaleFactor, scaleFactor)
            } else {
                // Scale exactly to fill dst from src.
                matrix.postScale(scaleFactorX, scaleFactorY)
            }
        }
        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f)
        }
        return matrix
    }

    fun tableGetBitmap(context: Context, uri: Uri?): Bitmap? {
        val newOpts = BitmapFactory.Options()
        newOpts.inJustDecodeBounds = true
        getBitmapFromUri(context, uri, newOpts)
        dealBitmapFactoryOption(newOpts)
        return getBitmapFromUri(context, uri, newOpts)
    }

    fun getBitmapFromUri(context: Context, uri: Uri?, opt: BitmapFactory.Options?): Bitmap? {
        if (uri == null) {
            return null
        }
        try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, opt)
            parcelFileDescriptor.close()
            return bitmap
        } catch (e: FileNotFoundException) {
            Log.e("exception", "FileNotFoundException")
        } catch (e: IOException) {
            Log.e("exception", "IOException")
        } catch (e: Exception) {
            Log.e("exception", "Exception")
        }
        return null
    }

    fun dealBitmapFactoryOption(newOpts: BitmapFactory.Options) {
        val w = newOpts.outWidth
        val h = newOpts.outHeight
        var minHOrW = w
        if (w > h) {
            minHOrW = h
        }
        val resizeFlag = true
        val targetSize: Int
        var be = 1
        targetSize = if (resizeFlag) {
            500
        } else {
            3000
        }
        if (minHOrW > targetSize) {
            be = Math.round(minHOrW.toFloat() / targetSize.toFloat())
        }
        newOpts.inSampleSize = be
        newOpts.inJustDecodeBounds = false
        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888
        newOpts.inPurgeable = true
        newOpts.inInputShareable = true
    }
}