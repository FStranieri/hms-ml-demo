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
package com.huawei.mlkit.sample.activity.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.huawei.mlkit.sample.R

class AddPictureDialog(context: Context) : Dialog(
    context, R.style.MyDialogStyle
), View.OnClickListener {
    private lateinit var tvTakePicture: TextView
    private lateinit var tvSelectImage: TextView
    private lateinit var tvExtend: TextView
    private var clickListener: ClickListener? = null

    interface ClickListener {
        /**
         * Take picture
         */
        fun takePicture()

        /**
         * Select picture from local
         */
        fun selectImage()

        /**
         * Extension method
         */
        fun doExtend()
    }

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        initViews()
    }

    private fun initViews() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_add_picture, null)
        this.setContentView(view)
        tvTakePicture = view.findViewById(R.id.take_photo)
        tvSelectImage = view.findViewById(R.id.select_image)
        tvExtend = view.findViewById(R.id.extend)
        tvTakePicture.setOnClickListener(this)
        tvSelectImage.setOnClickListener(this)
        tvExtend.setOnClickListener(this)
        setCanceledOnTouchOutside(true)
        val dialogWindow = this.window
        val layoutParams = dialogWindow!!.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.BOTTOM
        dialogWindow.attributes = layoutParams
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    override fun onClick(v: View) {
        dismiss()
        if (clickListener == null) {
            return
        }
        when (v.id) {
            R.id.take_photo -> clickListener!!.takePicture()
            R.id.select_image -> clickListener!!.selectImage()
            R.id.extend -> clickListener!!.doExtend()
            else -> {}
        }
    }
}