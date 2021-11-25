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
package com.huawei.mlkit.sample.activity.imageseg

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Pair
import android.view.*
import android.widget.*
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationAnalyzer
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.BaseActivity
import com.huawei.mlkit.sample.callback.ImageSegmentationResultCallBack
import com.huawei.mlkit.sample.util.BitmapUtils
import com.huawei.mlkit.sample.util.ImageUtils
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.io.IOException

class StillCutPhotoActivity : BaseActivity(), ImageSegmentationResultCallBack {
    private lateinit var relativeLayoutLoadPhoto: RelativeLayout
    private lateinit var relativeLayoutCut: RelativeLayout
    private lateinit var relativeLayoutBackgrounds: RelativeLayout
    private lateinit var relativeLayoutSave: RelativeLayout
    private var graphicOverlay: GraphicOverlay? = null
    private lateinit var preview: ImageView
    private var imageUri: Uri? = null
    private var imgBackgroundUri: Uri? = null
    private var originBitmap: Bitmap? = null
    private var backgroundBitmap: Bitmap? = null
    private var maxWidthOfImage: Int? = null
    private var maxHeightOfImage: Int? = null
    var isLandScape = false
    private val REQUEST_CHOOSE_ORIGINPIC = 2001
    private val REQUEST_CHOOSE_BACKGROUND = 2002
    private var processedImage: Bitmap? = null

    // Portrait foreground image.
    private var foreground: Bitmap? = null
    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        this.setContentView(R.layout.activity_still_cut)
        preview = findViewById(R.id.previewPane)
        findViewById<View>(R.id.back).setOnClickListener { finish() }
        isLandScape =
            this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        initView()
        initAction()
    }

    private fun initView() {
        relativeLayoutLoadPhoto = findViewById(R.id.relativate_chooseImg)
        relativeLayoutCut = findViewById(R.id.relativate_cut)
        relativeLayoutBackgrounds = findViewById(R.id.relativate_backgrounds)
        relativeLayoutSave = findViewById(R.id.relativate_save)
        preview = findViewById(R.id.previewPane)
        graphicOverlay = findViewById(R.id.previewOverlay)
    }

    private fun initAction() {
        relativeLayoutLoadPhoto.setOnClickListener { selectLocalImage(REQUEST_CHOOSE_ORIGINPIC) }

        // Outline the edge.
        relativeLayoutCut.setOnClickListener {
            if (imageUri == null) {
                Toast.makeText(
                    this@StillCutPhotoActivity.applicationContext,
                    R.string.please_select_picture,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                createImageTransactor()
                Toast.makeText(
                    this@StillCutPhotoActivity.applicationContext,
                    R.string.cut_success,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Replace background.
        relativeLayoutBackgrounds.setOnClickListener {
            if (imageUri == null) {
                Toast.makeText(
                    this@StillCutPhotoActivity.applicationContext,
                    R.string.please_select_picture,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                selectLocalImage(REQUEST_CHOOSE_BACKGROUND)
            }
        }

        // Save the processed picture.
        relativeLayoutSave.setOnClickListener {
            if (processedImage == null) {
                Toast.makeText(
                    this@StillCutPhotoActivity.applicationContext,
                    R.string.no_pic_neededSave,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val imageUtils = ImageUtils(this@StillCutPhotoActivity.applicationContext)
                imageUtils.saveToAlbum(processedImage!!)
                Toast.makeText(
                    this@StillCutPhotoActivity.applicationContext,
                    R.string.save_success,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        relativeLayoutSave.post { selectLocalImage(REQUEST_CHOOSE_ORIGINPIC) }
    }

    private fun selectLocalImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        this.startActivityForResult(intent, requestCode)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHOOSE_ORIGINPIC && resultCode == RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data!!.data
            loadOriginImage()
        } else if (requestCode == REQUEST_CHOOSE_BACKGROUND) {
            if (data == null) {
                Toast.makeText(
                    this.applicationContext,
                    R.string.please_select_picture,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                imgBackgroundUri = data.data
                loadOriginImage()
                val targetedSize = targetSize
                backgroundBitmap = BitmapUtils.loadFromPath(
                    this@StillCutPhotoActivity,
                    imgBackgroundUri!!,
                    targetedSize.first,
                    targetedSize.second
                )
                changeBackground(backgroundBitmap)
            }
        }
    }

    private fun changeBackground(backgroundBitmap: Bitmap?) {
        if (isChosen(foreground) && isChosen(backgroundBitmap)) {
            val drawable = BitmapDrawable(backgroundBitmap)
            preview.isDrawingCacheEnabled = true
            preview.background = drawable
            preview.setImageBitmap(foreground)
            processedImage = Bitmap.createBitmap(preview.drawingCache)
            preview.isDrawingCacheEnabled = false
        } else {
            Toast.makeText(
                this.applicationContext,
                R.string.please_select_picture,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
    }

    private var analyzer: MLImageSegmentationAnalyzer? = null
    private fun createImageTransactor() {
        val setting = MLImageSegmentationSetting.Factory().setAnalyzerType(
            MLImageSegmentationSetting.BODY_SEG
        ).create()
        analyzer = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(setting)
        if (isChosen(originBitmap)) {
            val mlFrame = MLFrame.Creator().setBitmap(originBitmap).create()
            val task = analyzer!!.asyncAnalyseFrame(mlFrame)
            task.addOnSuccessListener { mlImageSegmentationResults -> // Transacting logic for segment success.
                if (mlImageSegmentationResults != null) {
                    foreground = mlImageSegmentationResults.getForeground()
                    preview.setImageBitmap(foreground)
                    processedImage = (preview.drawable as BitmapDrawable).bitmap
                } else {
                    displayFailure()
                }
            }.addOnFailureListener(OnFailureListener { // Transacting logic for segment failure.
                displayFailure()
                return@OnFailureListener
            })
        } else {
            Toast.makeText(
                this.applicationContext,
                R.string.please_select_picture,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
    }

    private fun displayFailure() {
        Toast.makeText(this.applicationContext, "Fail", Toast.LENGTH_SHORT).show()
    }

    private fun isChosen(bitmap: Bitmap?): Boolean {
        return if (bitmap == null) {
            false
        } else {
            true
        }
    }

    private fun loadOriginImage() {
        if (imageUri == null) {
            return
        }
        // Clear the overlay first.
        graphicOverlay!!.clear()
        val targetedSize = targetSize
        val targetWidth = targetedSize.first
        val targetHeight = targetedSize.second
        originBitmap = BitmapUtils.loadFromPath(
            this@StillCutPhotoActivity,
            imageUri!!,
            targetWidth,
            targetHeight
        )
        // Determine how much to scale down the image.
        Log.i(
            "imageSlicer",
            "resized image size width:" + originBitmap?.width + ",height: " + originBitmap?.height
        )
        preview.setImageBitmap(originBitmap)
    }

    // Returns max width of image.
    private fun getMaxWidthOfImage(): Int {
        if (maxWidthOfImage == null) {
            maxWidthOfImage = if (isLandScape) {
                (preview.parent as View).height
            } else {
                (preview.parent as View).width
            }
        }

        return maxWidthOfImage!!
    }

    // Returns max height of image.
    private fun getMaxHeightOfImage(): Int {
        if (maxHeightOfImage == null) {
            maxHeightOfImage = if (isLandScape) {
                (preview.parent as View).width
            } else {
                (preview.parent as View).height
            }
        }
        return maxHeightOfImage!!
    }

    // Gets the targeted size(width / height).
    private val targetSize: Pair<Int, Int>
        private get() {
            val targetWidth: Int
            val targetHeight: Int
            val maxWidth = getMaxWidthOfImage()
            val maxHeight = getMaxHeightOfImage()
            targetWidth = if (isLandScape) maxHeight else maxWidth
            targetHeight = if (isLandScape) maxWidth else maxHeight
            Log.i(TAG, "height:$targetHeight,width:$targetWidth")
            return Pair(targetWidth, targetHeight)
        }

    public override fun onDestroy() {
        super.onDestroy()
        if (analyzer != null) {
            try {
                analyzer!!.stop()
            } catch (e: IOException) {
                Log.e(TAG, "Stop analyzer failed: " + e.message)
            }
        }
        imageUri = null
        imgBackgroundUri = null
        BitmapUtils.recycleBitmap(originBitmap, backgroundBitmap, foreground, processedImage)
        graphicOverlay?.clear()
        graphicOverlay = null
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_IMAGE_URI, imageUri)
        if (maxWidthOfImage != null) {
            outState.putInt(KEY_IMAGE_MAX_WIDTH, maxWidthOfImage!!)
        }
        if (maxHeightOfImage != null) {
            outState.putInt(KEY_IMAGE_MAX_HEIGHT, maxHeightOfImage!!)
        }
    }

    public override fun onResume() {
        super.onResume()
    }

    override fun callResultBitmap(bitmap: Bitmap?) {
        processedImage = bitmap
    }

    companion object {
        private const val TAG = "CaptureImageFragment"
        private const val REQUEST_TAKE_PHOTOR = 2003
        private const val KEY_IMAGE_URI = "KEY_IMAGE_URI"
        private const val KEY_IMAGE_MAX_WIDTH = "KEY_IMAGE_MAX_WIDTH"
        private const val KEY_IMAGE_MAX_HEIGHT = "KEY_IMAGE_MAX_HEIGHT"
    }
}