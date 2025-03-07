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
package com.huawei.mlkit.sample.activity.scenedection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.huawei.mlkit.sample.R

class SceneStartActivity : Activity(), View.OnClickListener {
    private lateinit var iv_scan: ImageView
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_scene)
        iv_scan = findViewById(R.id.iv_scan)
        editText = findViewById(R.id.editText)
        iv_scan.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_scan -> {
                val intent = Intent(this, SceneDectionActivity::class.java)
                val confidence = editText.text.toString().trim { it <= ' ' }
                intent.putExtra("confidence", confidence)
                startActivity(intent)
            }
        }
    }
}