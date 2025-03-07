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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.GridView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.mlplugin.productvisionsearch.MLProductVisionSearchCapture
import com.huawei.hms.mlplugin.productvisionsearch.MLProductVisionSearchCaptureConfig
import com.huawei.hms.mlplugin.productvisionsearch.MLProductVisionSearchCaptureFactory
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.productvisionsearch.MLProductVisionSearch
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.Imagesupersesolution.ImageSuperResolutionStartActivity
import com.huawei.mlkit.sample.activity.`object`.ObjectDetectionActivity
import com.huawei.mlkit.sample.activity.adapter.GridViewAdapter
import com.huawei.mlkit.sample.activity.documentskew.DocumentSkewStartActivity
import com.huawei.mlkit.sample.activity.entity.GridViewItem
import com.huawei.mlkit.sample.activity.fragment.ProductFragment
import com.huawei.mlkit.sample.activity.imageclassfication.ImageClassificationActivity
import com.huawei.mlkit.sample.activity.imageseg.ImageSegmentationActivity
import com.huawei.mlkit.sample.activity.scenedection.SceneStartActivity
import com.huawei.mlkit.sample.activity.table.TableRecognitionStartActivity
import com.huawei.mlkit.sample.util.Constant
import com.huawei.mlkit.sample.views.overlay.GraphicOverlay
import java.util.*

class StartActivity : BaseActivity(), ActivityCompat.OnRequestPermissionsResultCallback,
    View.OnClickListener {
    private lateinit var mGridView: GridView
    private var mDataList: ArrayList<GridViewItem>? = null
    private var graphicOverlay: GraphicOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(this, R.color.logo_background)
        this.setContentView(R.layout.activity_start)
        findViewById<View>(R.id.setting_img).setOnClickListener(this)
        graphicOverlay = findViewById(R.id.fireFaceOverlay)
        initData()
        mGridView = findViewById(R.id.gridview)
        val mAdapter = GridViewAdapter(mDataList, applicationContext)
        mGridView.adapter = mAdapter
        initClickEvent()
        // Set the ApiKey of the application for accessing cloud services.
        setApiKey()
        if (!allPermissionsGranted()) {
            runtimePermissions
        }
    }

    /**
     * Read the ApiKey field in the sample-agconnect-services.json to obtain the API key of the application and set it.
     * For details about how to apply for the sample-agconnect-services.json, see section https://developer.huawei.com/consumer/cn/doc/development/HMS-Guides/ml-add-agc.
     */
    private fun setApiKey() {
        val config = AGConnectServicesConfig.fromContext(application)
        MLApplication.getInstance().apiKey = config.getString(API_KEY)
    }

    private fun initClickEvent() {
        mGridView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                when (position) {
                    0 ->                         //Table Recognition
                        startActivity(
                            Intent(
                                this@StartActivity,
                                TableRecognitionStartActivity::class.java
                            )
                        )
                    1 ->                         // Image Segmentation
                        startActivity(
                            Intent(
                                this@StartActivity,
                                ImageSegmentationActivity::class.java
                            )
                        )
                    2 ->                         // Object detection and tracking
                        startActivity(
                            Intent(this@StartActivity, ObjectDetectionActivity::class.java)
                        )
                    3 ->                         // Image classification
                        startActivity(
                            Intent(
                                this@StartActivity,
                                ImageClassificationActivity::class.java
                            )
                        )
                    4 -> {
                        // Landmark recognition
                        val intent = Intent(this@StartActivity, RemoteDetectionActivity::class.java)
                        intent.putExtra(Constant.MODEL_TYPE, Constant.CLOUD_LANDMARK_DETECTION)
                        startActivity(intent)
                    }
                    5 -> {
                        // Image super resolution
                        val intentIsr = Intent(
                            this@StartActivity,
                            ImageSuperResolutionStartActivity::class.java
                        )
                        intentIsr.putExtra(
                            Constant.SUPER_RESOLUTION_TYPE,
                            Constant.TYPE_IMAGE_SUPER_RESOLUTION
                        )
                        startActivity(intentIsr)
                    }
                    6 -> {
                        // Text image super resolution
                        val intentTsr = Intent(
                            this@StartActivity,
                            ImageSuperResolutionStartActivity::class.java
                        )
                        intentTsr.putExtra(
                            Constant.SUPER_RESOLUTION_TYPE,
                            Constant.TYPE_TEXT_SUPER_RESOLUTION
                        )
                        startActivity(intentTsr)
                    }
                    7 ->                         //Scene
                        startActivity(Intent(this@StartActivity, SceneStartActivity::class.java))
                    8 -> {
                        // Product Visual Search
                        val config: MLProductVisionSearchCaptureConfig =
                            MLProductVisionSearchCaptureConfig.Factory()
                                .setLargestNumOfReturns(16)
                                .setProductFragment<MLProductVisionSearch>(ProductFragment())
                                .setRegion(MLProductVisionSearchCaptureConfig.REGION_DR_CHINA)
                                .create()
                        val capture: MLProductVisionSearchCapture =
                            MLProductVisionSearchCaptureFactory.getInstance().create(config)
                        capture.startCapture(this@StartActivity)
                    }
                    9 ->                         // Document Skew Corretion
                        startActivity(
                            Intent(
                                this@StartActivity,
                                DocumentSkewStartActivity::class.java
                            )
                        )
                    else -> {}
                }
            }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.setting_img) {
            startActivity(Intent(this@StartActivity, SettingActivity::class.java))
        }
    }

    private fun initData() {
        mDataList = ArrayList()
        var item: GridViewItem
        for (i in ICONS.indices) {
            item = GridViewItem(ICONS[i], TITLES[i])
            mDataList!!.add(item)
        }
    }

    private val requiredPermissions: Array<String?>
        private get() = try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.size > 0) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            arrayOfNulls(0)
        }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission)) {
                return false
            }
        }
        return true
    }

    private val runtimePermissions: Unit
        private get() {
            val allNeededPermissions: MutableList<String?> = ArrayList()
            for (permission in requiredPermissions) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission)
                }
            }
            if (!allNeededPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS
                )
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSION_REQUESTS) {
            return
        }
        var isNeedShowDiag = false
        for (i in permissions.indices) {
            if (permissions[i] == Manifest.permission.CAMERA && grantResults[i] != PackageManager.PERMISSION_GRANTED
                || permissions[i] == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[i] != PackageManager.PERMISSION_GRANTED
            ) {
                // If the camera or storage permissions are not authorized, need to pop up an authorization prompt box.
                isNeedShowDiag = true
            }
        }
        if (isNeedShowDiag && !ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CALL_PHONE
            )
        ) {
            val dialog = AlertDialog.Builder(this)
                .setMessage(this.getString(R.string.camera_permission_rationale))
                .setPositiveButton(this.getString(R.string.settings)) { dialog, which ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    // Open the corresponding setting interface according to the package name.
                    intent.data = Uri.parse("package:" + this@StartActivity.packageName)
                    this@StartActivity.startActivityForResult(intent, 200)
                    this@StartActivity.startActivity(intent)
                }
                .setNegativeButton(this.getString(R.string.cancel)) { dialog, which -> finish() }
                .create()
            dialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            if (!allPermissionsGranted()) {
                runtimePermissions
            }
        }
    }

    companion object {
        private const val TAG = "StartActivity"
        const val API_KEY = "client/api_key"
        private const val PERMISSION_REQUESTS = 1
        private val ICONS = intArrayOf(
            R.drawable.icon_table,
            R.drawable.icon_segmentation,
            R.drawable.icon_object,
            R.drawable.icon_classification,
            R.drawable.icon_landmark,
            R.drawable.icon_image_super_resolution,
            R.drawable.icon_text_super_resolution,
            R.drawable.icon_scene_detection,
            R.drawable.icon_shopping,
            R.drawable.icon_documentskew
        )
        private val TITLES = intArrayOf(
            R.string.table,
            R.string.image_segmentation,
            R.string.object_detection,
            R.string.image_classification,
            R.string.landmark,
            R.string.image_super_resolution_s,
            R.string.text_super_resolution_s,
            R.string.scene_detection,
            R.string.photographed_shopping,
            R.string.document_skew
        )

        private fun isPermissionGranted(context: Context, permission: String?): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission!!)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}