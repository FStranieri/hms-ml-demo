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
package com.huawei.mlkit.sample.activity.documentskew

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.BaseActivity
import com.huawei.mlkit.sample.activity.documentskew.DocumentSkewCorretionActivity

class DocumentSkewStartActivity : BaseActivity(), View.OnClickListener {
    var operate_type = 0
    private lateinit var chooseTitles: Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_skew_start)
        findViewById<View>(R.id.rl_upload_picture).setOnClickListener(this)
        findViewById<View>(R.id.back).setOnClickListener(this)
        setStatusBarColor(this, R.color.black)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.rl_upload_picture) {
            showDialog()
        } else if (v.id == R.id.back) {
            finish()
        }
    }

    private fun showDialog() {
        chooseTitles = arrayOf(
            resources.getString(R.string.take_photo),
            resources.getString(R.string.select_from_album)
        )
        val builder = AlertDialog.Builder(this)
        builder.setItems(chooseTitles) { dialogInterface, position ->
            if (position == 0) {
                operate_type = TAKE_PHOTO
                requestPermission(Manifest.permission.CAMERA)
            } else {
                operate_type = SELECT_ALBUM
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        builder.create().show()
    }

    private fun requestPermission(permisssions: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startSuperResolutionActivity()
            return
        }
        if (ContextCompat.checkSelfPermission(this, permisssions)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permisssions), REQUEST_CODE)
        } else {
            startSuperResolutionActivity()
        }
    }

    private fun startSuperResolutionActivity() {
        val intent =
            Intent(this@DocumentSkewStartActivity, DocumentSkewCorretionActivity::class.java)
        intent.putExtra("operate_type", operate_type)
        startActivity(intent)
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
        const val TAKE_PHOTO = 1
        const val SELECT_ALBUM = 2
    }
}