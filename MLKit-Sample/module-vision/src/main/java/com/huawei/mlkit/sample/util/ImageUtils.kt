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

import android.os.Build
import android.content.Context
import android.os.Environment
import android.media.MediaScannerConnection
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.util.Log
import com.huawei.mlkit.sample.callback.ImageUtilCallBack
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ImageUtils(private val context: Context) {
    private var imageUtilCallBack: ImageUtilCallBack? = null
    fun setImageUtilCallBack(imageUtilCallBack: ImageUtilCallBack) {
        this.imageUtilCallBack = imageUtilCallBack
    }

    // Save the picture to the system album and refresh it.
    fun saveToAlbum(bitmap: Bitmap) {
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
        if (file == null) {
            return
        }
        if (imageUtilCallBack != null) {
            try {
                imageUtilCallBack!!.callSavePath(file.canonicalPath)
            } catch (e: IOException) {
                Log.e(TAG, e.message!!)
            }
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

    companion object {
        private const val TAG = "ImageUtils"

        /**
         * Compare the size of the two pictures.
         *
         * @param foregroundBitmap the first bitmap
         * @param backgroundBitmap the second bitmap
         * @return true: same size; false: not.
         */
        fun equalImageSize(foregroundBitmap: Bitmap, backgroundBitmap: Bitmap?): Boolean {
            return backgroundBitmap!!.height == foregroundBitmap.height && backgroundBitmap.width == foregroundBitmap.width
        }

        /**
         * Scale background (background picture) size to foreground (foreground picture) size.
         *
         * @param foregroundBitmap foreground picture
         * @param backgroundBitmap background picture
         * @return A background image that is the same size as the foreground image.
         */
        fun resizeImageToForegroundImage(
            foregroundBitmap: Bitmap,
            backgroundBitmap: Bitmap?
        ): Bitmap? {
            var backgroundBitmap = backgroundBitmap
            val scaleWidth = foregroundBitmap.width
                .toFloat() / backgroundBitmap!!.width
            val scaleHeight = foregroundBitmap.height
                .toFloat() / backgroundBitmap.height
            val matrix = Matrix()
            matrix.postScale(scaleWidth, scaleHeight)
            backgroundBitmap = Bitmap.createBitmap(
                backgroundBitmap,
                0,
                0,
                backgroundBitmap.width,
                backgroundBitmap.height,
                matrix,
                true
            )
            return backgroundBitmap
        }
    }
}