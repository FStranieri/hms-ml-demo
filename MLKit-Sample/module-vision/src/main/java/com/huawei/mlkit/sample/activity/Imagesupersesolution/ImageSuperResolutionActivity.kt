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
package com.huawei.mlkit.sample.activity.Imagesupersesolution

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.imagesuperresolution.MLImageSuperResolutionAnalyzer
import com.huawei.hms.mlsdk.imagesuperresolution.MLImageSuperResolutionAnalyzerFactory
import com.huawei.hms.mlsdk.imagesuperresolution.MLImageSuperResolutionAnalyzerSetting
import com.huawei.hms.mlsdk.imagesuperresolution.MLImageSuperResolutionResult
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.BaseActivity
import com.huawei.mlkit.sample.util.BitmapUtils
import java.util.*

class ImageSuperResolutionActivity : BaseActivity(), View.OnClickListener {
    private lateinit var desImageView: ImageView
    private lateinit var srcImageView: ImageView
    private lateinit var tvImageSize: TextView
    private lateinit var adjustImgButton: ImageButton
    private var srcBitmap: Bitmap? = null
    private var desBitmap: Bitmap? = null
    private var imageUri: Uri? = null
    private var selectItem = INDEX_1X
    private var analyzer: MLImageSuperResolutionAnalyzer? = null
    private lateinit var rlScale1X: RelativeLayout
    private lateinit var rlScale3X: RelativeLayout
    private lateinit var rlScaleOriginal: RelativeLayout
    private lateinit var rlHelp: RelativeLayout
    private lateinit var dialog: AlertDialog
    private var strWidth: String? = null
    private var strHeight: String? = null
    private val imageViewList: MutableList<ImageView> = ArrayList()
    private var isShow = true
    private var isSwitch = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_super_resolution)
        setStatusBarColor(this, R.color.black)
        strWidth = resources.getString(R.string.isr_image_width)
        strHeight = resources.getString(R.string.isr_image_height)
        analyzer = createAnalyzer()
        adjustImgButton = findViewById(R.id.adjust)
        srcImageView = findViewById(R.id.src_image)
        desImageView = findViewById(R.id.des_image)
        tvImageSize = findViewById(R.id.image_size_info)
        rlScale1X = findViewById(R.id.rl_1x)
        rlScale3X = findViewById(R.id.rl_3x)
        rlScaleOriginal = findViewById(R.id.rl_original)
        imageViewList.add(findViewById<View>(R.id.ic_1x) as ImageView)
        imageViewList.add(findViewById<View>(R.id.ic_3x) as ImageView)
        imageViewList.add(findViewById<View>(R.id.ic_original) as ImageView)
        rlHelp = findViewById(R.id.rl_help)
        adjustImgButton.setOnClickListener(this)
        rlScaleOriginal.setOnClickListener(this)
        rlScale1X.setOnClickListener(this)
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
        content.setText(R.string.isr_tips_content)
        val mBtn_cancel = v.findViewById<Button>(R.id.dialog_btn_cancel)
        dialog = builder.create()
        dialog.show()
        dialog.window!!.setContentView(v)
        mBtn_cancel.setOnClickListener { dialog.dismiss() }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.rl_1x) {
            onClickItem(INDEX_1X)
        } else if (v.id == R.id.rl_3x) {
            onClickItem(INDEX_3X)
        } else if (v.id == R.id.rl_original) {
            onClickItem(INDEX_ORIGINAL)
        } else if (v.id == R.id.back) {
            finish()
        } else if (v.id == R.id.rl_chooseImg) {
            selectLocalImage()
        } else if (v.id == R.id.rl_help) {
            showTipsDialog()
        } else if (v.id == R.id.adjust) {
            showScaleChooseViews()
        }
    }

    private fun showScaleChooseViews() {
        if (!isShow) {
            rlScale1X.visibility = View.VISIBLE
            rlScale3X.visibility = View.VISIBLE
            rlScaleOriginal.visibility = View.VISIBLE
            isShow = true
        } else {
            rlScaleOriginal.visibility = View.GONE
            rlScale1X.visibility = View.GONE
            rlScale3X.visibility = View.GONE
            isShow = false
        }
    }

    private fun onClickItem(index: Int) {
        isSwitch = selectItem != index
        selectItem = index
        resetSelectItem(selectItem)
        reloadAndDetectImage(false, isSwitch)
    }

    private fun createAnalyzer(): MLImageSuperResolutionAnalyzer {
        return if (selectItem == INDEX_1X) {
            MLImageSuperResolutionAnalyzerFactory.getInstance().imageSuperResolutionAnalyzer
        } else {
            val setting: MLImageSuperResolutionAnalyzerSetting =
                MLImageSuperResolutionAnalyzerSetting.Factory()
                    .setScale(MLImageSuperResolutionAnalyzerSetting.ISR_SCALE_3X)
                    .create()
            MLImageSuperResolutionAnalyzerFactory.getInstance()
                .getImageSuperResolutionAnalyzer(setting)
        }
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
            reloadAndDetectImage(true, false)
        } else if (resultCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_CANCELED) {
            finish()
        }
    }

    private fun reloadAndDetectImage(isReload: Boolean, isSwitch: Boolean) {
        if (isReload) {
            imageUri?.let {
                srcBitmap =
                    BitmapUtils.loadFromPathWithoutZoom(this, it, IMAGE_MAX_SIZE, IMAGE_MAX_SIZE)
                setImage(srcImageView, srcBitmap)
            } ?: return
        }
        srcBitmap?.let {
            if (selectItem == INDEX_ORIGINAL) {
                setImage(desImageView, srcBitmap)
                setImageSizeInfo(it.width, it.height)
                return
            }
        } ?: return

        if (isSwitch) {
            // The analyzer only supports a single instance.
            // If you want to switch to a different scale, you need to release the model and recreate it.
            analyzer?.stop()
            analyzer = createAnalyzer()
        }
        // Create an MLFrame by using the bitmap.
        val frame = MLFrame.fromBitmap(srcBitmap)
        val task: Task<MLImageSuperResolutionResult>? = analyzer?.asyncAnalyseFrame(frame)
        task?.addOnSuccessListener { result -> // Recognition success.
            desBitmap = result.getBitmap()
            setImage(desImageView, desBitmap)
            setImageSizeInfo(desBitmap!!.width, desBitmap!!.height)
        }?.addOnFailureListener { e -> // Recognition failure.
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
        runOnUiThread { tvImageSize.text = resultBuilder.toString() }
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
            analyzer?.stop()
        }
    }

    companion object {
        private const val TAG = "SuperResolutionActivity"
        private const val STR_PX = "px"
        private const val IMAGE_MAX_SIZE = 1024
        private const val REQUEST_SELECT_IMAGE = 1000
        private const val INDEX_1X = 0
        private const val INDEX_3X = 1
        private const val INDEX_ORIGINAL = 2
    }
}