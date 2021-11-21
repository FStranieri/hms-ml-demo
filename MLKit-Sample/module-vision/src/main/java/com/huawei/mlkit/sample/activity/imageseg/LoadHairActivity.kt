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

import com.huawei.mlkit.sample.activity.BaseActivity.onCreate
import com.huawei.mlkit.sample.activity.BaseActivity.setStatusBar
import com.huawei.mlkit.sample.activity.BaseActivity.setStatusBarFontColor
import com.huawei.mlkit.sample.activity.adapter.ItemAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import com.huawei.mlkit.sample.R
import android.graphics.Bitmap
import com.google.gson.Gson
import com.huawei.hms.mlsdk.fr.MLFormRecognitionTablesAttribute
import com.huawei.hms.mlsdk.fr.MLFormRecognitionConstant
import com.huawei.mlkit.sample.activity.table.TableRecognitionActivity
import android.content.pm.PackageManager
import jxl.write.WriteException
import kotlin.Throws
import jxl.write.WritableWorkbook
import jxl.Workbook
import jxl.write.WritableSheet
import android.graphics.BitmapFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzer
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzerFactory
import com.google.gson.JsonObject
import com.huawei.hmf.tasks.OnSuccessListener
import android.content.Intent
import com.huawei.hmf.tasks.OnFailureListener
import android.content.DialogInterface
import android.provider.MediaStore
import android.content.ContentValues
import android.app.Activity
import com.huawei.mlkit.sample.activity.`object`.ObjectDetectionActivity
import com.bumptech.glide.Glide
import com.huawei.mlkit.sample.activity.adapter.imgseg.MyGridViewAdapter
import com.huawei.mlkit.sample.activity.adapter.ItemAdapter.ItemHolder
import com.huawei.hms.mlsdk.fr.MLFormRecognitionTablesAttribute.TablesContent.TableAttribute.TableCellAttribute
import com.huawei.hms.mlplugin.productvisionsearch.MLProductVisionSearchCapture.AbstractProductFragment
import com.huawei.mlkit.sample.activity.adapter.BottomSheetAdapter
import com.huawei.mlkit.sample.activity.fragment.ProductFragment
import com.huawei.mlkit.sample.activity.imageseg.LoadHairActivity
import com.huawei.mlkit.sample.activity.imageseg.LoadPhotoActivity
import com.huawei.mlkit.sample.activity.imageseg.StillCutPhotoActivity
import android.renderscript.RenderScript
import android.view.View.OnTouchListener
import android.os.Build
import android.graphics.drawable.BitmapDrawable
import androidx.viewpager.widget.ViewPager
import com.huawei.mlkit.sample.activity.adapter.TabFragmentAdapter
import com.huawei.mlkit.sample.activity.imageseg.ImageSegmentationActivity.PagerChangeListener
import com.huawei.mlkit.sample.activity.fragment.BackgroundChangeFragment
import com.huawei.mlkit.sample.activity.fragment.CaptureImageFragment
import com.huawei.mlkit.sample.activity.fragment.SliceImageFragment
import com.huawei.mlkit.sample.activity.fragment.HairImageFragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.huawei.mlkit.sample.activity.documentskew.DocumentSkewStartActivity
import com.huawei.mlkit.sample.activity.documentskew.DocumentSkewCorretionActivity
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzer
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionResult
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionCoordinateInput
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerSetting
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerFactory
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewDetectResult
import com.huawei.mlkit.sample.activity.scenedection.SceneDectionActivity
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzer
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerSetting
import android.content.pm.PackageInfo
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.util.Log
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import android.util.SparseArray
import android.view.*
import android.widget.*
import com.huawei.mlkit.sample.activity.imageclassfication.ImageClassificationActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting
import com.huawei.mlkit.sample.activity.table.TableRecognitionStartActivity
import com.huawei.mlkit.sample.activity.imageseg.ImageSegmentationActivity
import com.huawei.mlkit.sample.activity.``object`
import com.huawei.mlkit.sample.activity.BaseActivity
import com.huawei.mlkit.sample.transactor.*
import com.huawei.mlkit.sample.util.BitmapUtils
import com.huawei.mlkit.sample.views.color.ColorSelector
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.util.*

class LoadHairActivity : BaseActivity() {
    private var mImageUri: Uri? = null
    private var preview: ImageView? = null
    private var linearObjects: LinearLayout? = null
    private var imageTransactor: ImageTransactor? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var originBitmap: Bitmap? = null
    private var colorvalue = Color.GREEN
    private var imageMaxWidth = 0
    private var imageMaxHeight = 0
    private var colorSelector: ColorSelector? = null
    private var isLandScape = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_load_photo)
        preview = findViewById(R.id.image_preview)
        findViewById<View>(R.id.back).setOnClickListener { finish() }
        initView()
        isLandScape =
            this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    private fun initView() {
        linearObjects = findViewById(R.id.linear_objects)
        val categories: Array<String>
        categories = if (isEngLanguage) {
            CATEGORIES_EN
        } else {
            CATEGORIES
        }
        for (i in categories.indices) {
            val view = LayoutInflater.from(this.applicationContext)
                .inflate(R.layout.layout, linearObjects, false)
            val textView = view.findViewById<TextView>(R.id.text)
            textView.text = categories[i]
            linearObjects.addView(view)
            textView.setOnClickListener {
                val setting = MLImageSegmentationSetting.Factory().setAnalyzerType(
                    MLImageSegmentationSetting.HAIR_SEG
                ).create()
                val transactor = StillImageSegmentationTransactor(
                    setting,
                    originBitmap,
                    preview,
                    i
                )
                transactor.setColor(colorvalue)
                imageTransactor = transactor
                imageTransactor.process(originBitmap, graphicOverlay)
            }
        }
        graphicOverlay = findViewById(R.id.previewOverlay)

        // Color picker settings.
        colorSelector = findViewById(R.id.color_selector)
        colorSelector = findViewById(R.id.color_selector)
        colorSelector.initData()
        colorSelector.setColors(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.WHITE)
        colorSelector.setOnColorSelectorChangeListener(object :
            ColorSelector.OnColorSelectorChangeListener {
            override fun onColorChanged(picker: ColorSelector, color: Int) {
                colorvalue = color
            }

            override fun onStartColorSelect(picker: ColorSelector) {}
            override fun onStopColorSelect(picker: ColorSelector) {}
        })
        colorSelector.post(Runnable { selectLocalImage() })
    }

    private fun selectLocalImage() {
        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        this.startActivityForResult(intent, REQUEST_SLECT_IMAGE)
    }

    val isEngLanguage: Boolean
        get() {
            val locale = Locale.getDefault()
            if (locale != null) {
                val strLan = locale.language
                return strLan != null && "en" == strLan
            }
            return false
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(this.TAG, "requestCode:$requestCode : $resultCode")
        if (requestCode == REQUEST_TAKE_PHOTOR && resultCode == RESULT_OK) {
            loadImage()
        } else if (requestCode == REQUEST_TAKE_PHOTOR && resultCode == RESULT_CANCELED) {
            finish()
        } else if (requestCode == REQUEST_SLECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                mImageUri = data.data
            }
            loadImage()
        } else if (requestCode == REQUEST_SLECT_IMAGE && resultCode == RESULT_CANCELED) {
            finish()
        }
    }

    private fun loadImage() {
        originBitmap = BitmapUtils.loadFromPath(
            this@LoadHairActivity,
            mImageUri,
            maxWidthOfImage,
            maxHeightOfImage
        )
        preview!!.setImageBitmap(originBitmap)
    }

    private val maxWidthOfImage: Int
        private get() {
            if (imageMaxWidth == 0) {
                if (isLandScape) {
                    imageMaxWidth = (preview!!.parent as View).height
                } else {
                    imageMaxWidth = (preview!!.parent as View).width
                }
            }
            return imageMaxWidth
        }
    private val maxHeightOfImage: Int
        private get() {
            if (imageMaxHeight == 0) {
                if (isLandScape) {
                    imageMaxHeight = (preview!!.parent as View).width
                } else {
                    imageMaxHeight = (preview!!.parent as View).height
                }
            }
            return imageMaxHeight
        }

    override fun onDestroy() {
        super.onDestroy()
        BitmapUtils.recycleBitmap(originBitmap)
        mImageUri = null
        if (imageTransactor != null) {
            imageTransactor!!.stop()
            imageTransactor = null
        }
        if (graphicOverlay != null) {
            graphicOverlay!!.clear()
            graphicOverlay = null
        }
    }

    fun onBackPressed(view: View?) {
        finish()
    }

    companion object {
        private const val TAG = "LoadPhotoActivity"
        private val CATEGORIES = arrayOf("背景", "头发")
        private val CATEGORIES_EN = arrayOf("Background", "Hair")
        private const val REQUEST_TAKE_PHOTOR = 1
        private const val REQUEST_SLECT_IMAGE = 2
    }
}