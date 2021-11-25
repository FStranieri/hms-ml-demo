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

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.util.Constant

class SettingActivity : BaseActivity(), View.OnClickListener {
    private lateinit var mVersion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_setting)
        mVersion = findViewById(R.id.version)
        mVersion.text = versionName
        findViewById<View>(R.id.back).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.back -> finish()
            else -> {}
        }
    }

    /**
     * get App versionName
     *
     * @return version name
     */
    private val versionName: String
        get() {
            val packageManager = this.packageManager
            try {
                val packageInfo = packageManager.getPackageInfo(this.packageName, 0)
                return packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(
                    TAG,
                    "Failed to get package version: " + e.message
                )
            }
            return Constant.DEFAULT_VERSION
        }

    companion object {
        private const val TAG = "SettingActivity"
    }
}