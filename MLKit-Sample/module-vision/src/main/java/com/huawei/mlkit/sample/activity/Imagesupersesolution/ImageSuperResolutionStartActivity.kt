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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.BaseActivity
import com.huawei.mlkit.sample.activity.textsuperresolution.TextImageSuperResolutionActivity
import com.huawei.mlkit.sample.util.Constant

class ImageSuperResolutionStartActivity : BaseActivity(), View.OnClickListener {
    private var type: String? = Constant.TYPE_IMAGE_SUPER_RESOLUTION
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_super_resolution_start)
        val title = findViewById<TextView>(R.id.title)
        val intent = this.intent
        if (intent != null) {
            type = intent.getStringExtra(Constant.SUPER_RESOLUTION_TYPE)
            if (Constant.TYPE_TEXT_SUPER_RESOLUTION == type) {
                title.setText(R.string.text_super_resolution)
            }
        }
        findViewById<View>(R.id.rl_upload_picture).setOnClickListener(this)
        findViewById<View>(R.id.back).setOnClickListener(this)
        setStatusBarColor(this, R.color.black)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.rl_upload_picture) {
            requestPermission()
        } else if (v.id == R.id.back) {
            finish()
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startSuperResolutionActivity()
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
        } else {
            startSuperResolutionActivity()
        }
    }

    private fun startSuperResolutionActivity() {
        if (Constant.TYPE_IMAGE_SUPER_RESOLUTION == type) {
            startActivity(
                Intent(
                    this@ImageSuperResolutionStartActivity,
                    ImageSuperResolutionActivity::class.java
                )
            )
        } else {
            startActivity(
                Intent(
                    this@ImageSuperResolutionStartActivity,
                    TextImageSuperResolutionActivity::class.java
                )
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSuperResolutionActivity()
            } else {
                Toast.makeText(
                    this,
                    "Permission application failed, you denied the permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}