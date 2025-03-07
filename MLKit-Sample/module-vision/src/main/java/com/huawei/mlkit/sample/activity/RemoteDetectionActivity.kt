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
package com.huawei.mlkit.sample.activity

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.dialog.AddPictureDialog
import com.huawei.mlkit.sample.transactor.*
import com.huawei.mlkit.sample.util.BitmapUtils
import com.huawei.mlkit.sample.util.Constant
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.lang.ref.WeakReference

class RemoteDetectionActivity : BaseActivity(), View.OnClickListener {
    private lateinit var getImageButton: Button
    private lateinit var preview: ImageView
    private lateinit var title: TextView
    private lateinit var graphicOverlay: GraphicOverlay
    private var selectedMode: String? = Constant.CLOUD_IMAGE_CLASSIFICATION
    var isLandScape = false
    private var imageUri: Uri? = null
    private var maxWidthOfImage: Int? = null
    private var maxHeightOfImage: Int? = null
    private var imageTransactor: ImageTransactor? = null
    private var imageBitmap: Bitmap? = null
    private var progressDialog: Dialog? = null
    private var addPictureDialog: AddPictureDialog? = null
    private val mHandler: Handler = MsgHandler(this)

    private class MsgHandler(mainActivity: RemoteDetectionActivity) : Handler() {
        var mMainActivityWeakReference: WeakReference<RemoteDetectionActivity> = WeakReference(mainActivity)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val mainActivity = mMainActivityWeakReference.get() ?: return
            Log.d(TAG, "msg what :" + msg.what)
            if (msg.what == Constant.GET_DATA_SUCCESS) {
                mainActivity.handleGetDataSuccess()
            } else if (msg.what == Constant.GET_DATA_FAILED) {
                mainActivity.handleGetDataFailed()
            }
        }

    }

    private fun handleGetDataSuccess() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
        mHandler.removeCallbacks(myRunnable)
    }

    private fun handleGetDataFailed() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
        mHandler.removeCallbacks(myRunnable)
        Toast.makeText(this, this.getString(R.string.get_data_failed), Toast.LENGTH_SHORT).show()
    }

    private val myRunnable = Runnable {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
        Toast.makeText(
            this@RemoteDetectionActivity.applicationContext,
            this@RemoteDetectionActivity.getString(R.string.get_data_failed),
            Toast.LENGTH_SHORT
        ).show()
    }
    private val detectRunnable = Runnable { loadImageAndSetTransactor() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = this.intent
        var type: String? = null
        try {
            selectedMode = intent.getStringExtra(Constant.MODEL_TYPE)
            type = intent.getStringExtra(Constant.ADD_PICTURE_TYPE)
        } catch (e: RuntimeException) {
            Log.e(TAG, "Get intent value failed: " + e.message)
        }
        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI)
            if (imageUri != null) {
                maxWidthOfImage = savedInstanceState.getInt(KEY_IMAGE_MAX_WIDTH)
                maxHeightOfImage = savedInstanceState.getInt(KEY_IMAGE_MAX_HEIGHT)
            }
        }
        this.setContentView(R.layout.activity_remote_detection)
        initTitle()
        findViewById<View>(R.id.back).setOnClickListener(this)
        preview = findViewById(R.id.still_preview)
        graphicOverlay = findViewById(R.id.still_overlay)
        getImageButton = findViewById(R.id.getImageButton)
        getImageButton.setOnClickListener(this)
        createImageTransactor()
        createDialog()
        isLandScape =
            this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        setStatusBar()
        if (type == null) {
            selectLocalImage()
        } else if (type == Constant.TYPE_SELECT_IMAGE) {
            selectLocalImage()
        } else {
            startCamera()
        }
    }

    private fun initTitle() {
        title = findViewById(R.id.page_title)
        if (selectedMode == Constant.CLOUD_IMAGE_CLASSIFICATION) {
            title.setText(this.resources.getText(R.string.cloud_classification))
        } else if (selectedMode == Constant.CLOUD_LANDMARK_DETECTION) {
            title.setText(this.resources.getText(R.string.landmark_s))
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onClick(view: View) {
        if (view.id == R.id.getImageButton) {
            this.showDialog()
        } else if (view.id == R.id.back) {
            finish()
        }
    }

    private fun createDialog() {
        addPictureDialog = AddPictureDialog(this)
        val intent = Intent(this@RemoteDetectionActivity, RemoteDetectionActivity::class.java)
        intent.putExtra(Constant.MODEL_TYPE, Constant.CLOUD_IMAGE_CLASSIFICATION)
        addPictureDialog!!.setClickListener(object : AddPictureDialog.ClickListener {
            override fun takePicture() {
                startCamera()
            }

            override fun selectImage() {
                selectLocalImage()
            }

            override fun doExtend() {}
        })
    }

    private fun showDialog() {
        addPictureDialog!!.show()
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

    private fun reloadAndDetectImage() {
        if (preview == null || maxHeightOfImage == null || (maxHeightOfImage == 0
                    && (preview!!.parent as View).height == 0)
        ) {
            mHandler.postDelayed(detectRunnable, DELAY_TIME.toLong())
        } else {
            loadImageAndSetTransactor()
        }
    }

    private fun startCamera() {
        imageUri = null
        preview!!.setImageBitmap(null)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(this.packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri =
                this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            this.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
        }
    }

    private fun selectLocalImage() {
        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        this.startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            reloadAndDetectImage()
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_CANCELED) {
            finish()
        } else if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.data
            }
            reloadAndDetectImage()
        } else if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_CANCELED) {
            finish()
        }
    }

    private fun loadImageAndSetTransactor() {
        if (imageUri == null) {
            return
        }

        showLoadingDialog()
        graphicOverlay.clear()
        mHandler.postDelayed(myRunnable, TIMEOUT.toLong())
        imageBitmap = BitmapUtils.loadFromPath(
            this@RemoteDetectionActivity,
            imageUri!!,
            getMaxWidthOfImage()!!,
            getMaxHeightOfImage()!!
        )
        preview.setImageBitmap(imageBitmap)
        if (imageBitmap != null) {
            imageTransactor!!.process(imageBitmap, graphicOverlay)
        }
    }

    private fun getMaxWidthOfImage(): Int? {
        if (maxWidthOfImage == null || maxWidthOfImage == 0) {
            if (isLandScape) {
                maxWidthOfImage = (preview.parent as View).height
            } else {
                maxWidthOfImage = (preview.parent as View).width
            }
        }
        return maxWidthOfImage
    }

    private fun getMaxHeightOfImage(): Int? {
        if (maxHeightOfImage == null || maxHeightOfImage == 0) {
            if (isLandScape) {
                maxHeightOfImage = (preview.parent as View).width
            } else {
                maxHeightOfImage = (preview.parent as View).height
            }
        }
        return maxHeightOfImage
    }

    private fun createImageTransactor() {
        when (selectedMode) {
            Constant.CLOUD_IMAGE_CLASSIFICATION -> imageTransactor =
                RemoteImageClassificationTransactor(this.applicationContext, mHandler)
            Constant.CLOUD_LANDMARK_DETECTION -> imageTransactor =
                RemoteLandmarkTransactor(mHandler)
            else -> throw IllegalStateException("Unknown selectedMode: " + selectedMode)
        }
        Log.d(TAG, imageTransactor!!.javaClass.name)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (imageTransactor != null) {
            imageTransactor!!.stop()
            imageTransactor = null
        }
        imageUri = null
        if (progressDialog != null) {
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
            progressDialog = null
        }
    }

    private fun showLoadingDialog() {
        if (progressDialog == null) {
            progressDialog = Dialog(this@RemoteDetectionActivity, R.style.progress_dialog)
            progressDialog!!.setContentView(R.layout.dialog)
            progressDialog!!.setCancelable(false)
            progressDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            val msg = progressDialog!!.findViewById<TextView>(R.id.id_tv_loadingmsg)
            msg.text = this.getString(R.string.loading_data)
        }
        progressDialog!!.show()
    }

    companion object {
        private const val TAG = "RemoteDetectionActivity"
        private const val KEY_IMAGE_URI = "KEY_IMAGE_URI"
        private const val KEY_IMAGE_MAX_WIDTH = "KEY_IMAGE_MAX_WIDTH"
        private const val KEY_IMAGE_MAX_HEIGHT = "KEY_IMAGE_MAX_HEIGHT"
        private const val REQUEST_TAKE_PHOTO = 1
        private const val REQUEST_SELECT_IMAGE = 2
        private const val TIMEOUT = 20 * 1000
        private const val DELAY_TIME = 600
    }
}