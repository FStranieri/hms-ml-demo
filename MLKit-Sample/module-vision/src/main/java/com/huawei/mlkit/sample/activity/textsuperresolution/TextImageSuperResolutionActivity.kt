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
package com.huawei.mlkit.sample.activity.textsuperresolution

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
import android.app.Dialog
import com.huawei.mlkit.sample.transactor.LocalObjectTransactor
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
import com.huawei.mlkit.sample.transactor.StillImageSegmentationTransactor
import com.huawei.mlkit.sample.transactor.ImageSegmentationTransactor
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
import com.huawei.mlkit.sample.transactor.SceneDetectionTransactor
import android.content.pm.PackageInfo
import android.net.Uri
import android.util.Log
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.huawei.mlkit.sample.transactor.LocalImageClassificationTransactor
import com.huawei.mlkit.sample.activity.imageclassfication.ImageClassificationActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.mlkit.sample.activity.table.TableRecognitionStartActivity
import com.huawei.mlkit.sample.activity.imageseg.ImageSegmentationActivity
import com.huawei.mlkit.sample.activity.``object`
import com.huawei.mlkit.sample.activity.BaseActivity
import com.huawei.mlkit.sample.util.BitmapUtils
import java.lang.StringBuilder
import java.util.ArrayList

class TextImageSuperResolutionActivity : BaseActivity(), View.OnClickListener {
    private var desImageView: ImageViewTouch? = null
    private var srcImageView: ImageView? = null
    private var tvImageSize: TextView? = null
    private var adjustImgButton: ImageButton? = null
    private var srcBitmap: Bitmap? = null
    private var desBitmap: Bitmap? = null
    private var dialog: Dialog? = null
    private var imageUri: Uri? = null
    private var selectItem = INDEX_3X
    private var analyzer: MLTextImageSuperResolutionAnalyzer? = null
    private var rlScale3X: RelativeLayout? = null
    private var rlScaleOriginal: RelativeLayout? = null
    private var rlHelp: RelativeLayout? = null
    private var strWidth: String? = null
    private var strHeight: String? = null
    private val imageViewList: MutableList<ImageView> = ArrayList()
    private var isShow = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_super_resolution)
        setStatusBarColor(this, R.color.black)
        strWidth = resources.getString(R.string.isr_image_width)
        strHeight = resources.getString(R.string.isr_image_height)
        analyzer = createAnalyzer()
        adjustImgButton = findViewById(R.id.adjust)
        srcImageView = findViewById(R.id.src_image)
        desImageView = findViewById<ImageViewTouch>(R.id.des_image)
        tvImageSize = findViewById(R.id.image_size_info)
        rlScale3X = findViewById(R.id.rl_3x)
        rlScaleOriginal = findViewById(R.id.rl_original)
        imageViewList.add(findViewById<View>(R.id.ic_3x) as ImageView)
        imageViewList.add(findViewById<View>(R.id.ic_original) as ImageView)
        rlHelp = findViewById(R.id.rl_help)
        adjustImgButton.setOnClickListener(this)
        rlScaleOriginal.setOnClickListener(this)
        rlScale3X.setOnClickListener(this)
        findViewById<View>(R.id.back).setOnClickListener(this)
        findViewById<View>(R.id.rl_chooseImg).setOnClickListener(this)
        rlHelp.setOnClickListener(this)
        selectLocalImage()
    }

    private fun showTipsDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val v = inflater.inflate(R.layout.dialog_layout, null)
        val content = v.findViewById<TextView>(R.id.dialog_content)
        content.setText(R.string.tsr_tips_content)
        val mBtn_cancel = v.findViewById<Button>(R.id.dialog_btn_cancel)
        dialog = builder.create()
        dialog.show()
        dialog.getWindow()!!.setContentView(v)
        mBtn_cancel.setOnClickListener { dialog.dismiss() }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.rl_3x) {
            onClickItem(INDEX_3X)
        } else if (v.id == R.id.rl_original) {
            onClickItem(INDEX_ORIGINAL)
        } else if (v.id == R.id.back) {
            finish()
        } else if (v.id == R.id.rl_chooseImg) {
            selectLocalImage()
        } else if (v.id == R.id.adjust) {
            showScaleChooseViews()
        } else if (v.id == R.id.rl_help) {
            showTipsDialog()
        }
    }

    private fun showScaleChooseViews() {
        if (!isShow) {
            rlScale3X!!.visibility = View.VISIBLE
            rlScaleOriginal!!.visibility = View.VISIBLE
            isShow = true
        } else {
            rlScaleOriginal!!.visibility = View.GONE
            rlScale3X!!.visibility = View.GONE
            isShow = false
        }
    }

    private fun onClickItem(index: Int) {
        selectItem = index
        resetSelectItem(selectItem)
        reloadAndDetectImage(false)
    }

    private fun createAnalyzer(): MLTextImageSuperResolutionAnalyzer {
        return MLTextImageSuperResolutionAnalyzerFactory.getInstance()
            .getTextImageSuperResolutionAnalyzer()
    }

    private fun selectLocalImage() {
        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    private fun resetSelectItem(position: Int) {
        for (i in imageViewList.indices) {
            if (i == position) {
                imageViewList[i].setBackgroundResource(R.drawable.ic_circle_selected)
            } else {
                imageViewList[i].setBackgroundResource(R.drawable.ic_circle)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.data
            }
            reloadAndDetectImage(true)
        } else if (resultCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_CANCELED) {
            finish()
        }
    }

    private fun reloadAndDetectImage(isReload: Boolean) {
        if (isReload) {
            if (imageUri == null) {
                return
            }
            srcBitmap =
                BitmapUtils.loadFromPathWithoutZoom(this, imageUri, IMAGE_MAX_SIZE, IMAGE_MAX_SIZE)
            setImage(srcImageView, srcBitmap)
        }
        if (selectItem == INDEX_ORIGINAL) {
            setImage(desImageView, srcBitmap)
            setImageSizeInfo(srcBitmap!!.width, srcBitmap!!.height)
            return
        }
        // Create an MLFrame by using the bitmap.
        val frame = MLFrame.fromBitmap(srcBitmap)
        val task: Task<MLTextImageSuperResolution> = analyzer.asyncAnalyseFrame(frame)
        task.addOnSuccessListener { result -> // Recognition success.
            desBitmap = result.getBitmap()
            setImage(desImageView, desBitmap)
            setImageSizeInfo(desBitmap!!.width, desBitmap!!.height)
        }.addOnFailureListener { e -> // Recognition failure.
            Log.e(TAG, "Failed." + e.message)
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setImageSizeInfo(width: Int, height: Int) {
        val resultBuilder = StringBuilder()
        resultBuilder.append(strWidth)
            .append(width)
            .append(STR_PX)
            .append("     ")
            .append(strHeight)
            .append(height)
            .append(STR_PX)
        runOnUiThread { tvImageSize!!.text = resultBuilder.toString() }
    }

    private fun setImage(imageView: ImageView?, bitmap: Bitmap?) {
        runOnUiThread { imageView!!.setImageBitmap(bitmap) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (srcBitmap != null) {
            srcBitmap!!.recycle()
        }
        if (desBitmap != null) {
            desBitmap!!.recycle()
        }
        if (analyzer != null) {
            analyzer.stop()
        }
    }

    companion object {
        private const val TAG = "SuperResolutionActivity"
        private const val STR_PX = "px"
        private const val IMAGE_MAX_SIZE = 1024
        private const val REQUEST_SELECT_IMAGE = 1000
        private const val INDEX_3X = 0
        private const val INDEX_ORIGINAL = 1
    }
}