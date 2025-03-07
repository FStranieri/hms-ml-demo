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
package com.huawei.mlkit.sample.activity.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.imageseg.StillCutPhotoActivity

class CaptureImageFragment : Fragment() {
    private lateinit var mLoadPhoto: ImageView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_capture_image, container, false)
        mLoadPhoto = view.findViewById(R.id.center_image_slice)
        mLoadPhoto.setOnClickListener {
            this@CaptureImageFragment.startActivity(
                Intent(this@CaptureImageFragment.context, StillCutPhotoActivity::class.java)
            )
        }
        return view
    }
}