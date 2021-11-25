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
import com.huawei.mlkit.sample.activity.adapter.imgseg.MyGridViewAdapter
import com.huawei.mlkit.sample.activity.entity.Entity
import com.huawei.mlkit.sample.activity.fragment.BackgroundChangeFragment
import com.huawei.mlkit.sample.activity.imageseg.*
import com.huawei.mlkit.sample.util.Constant
import com.huawei.mlkit.sample.util.SharedPreferencesUtil
import java.util.*

class BackgroundChangeFragment : Fragment() {
    private lateinit var mGridView: GridView
    private var mDataList: ArrayList<Entity>? = null
    private var mAdapter: MyGridViewAdapter? = null
    private lateinit var mTakePhoto: ImageView
    private var mIndex = -1
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bg_change, container, false)
        initData()
        initView(view)
        return view
    }

    private fun initView(view: View) {
        mTakePhoto = view.findViewById(R.id.take_photo)
        mGridView = view.findViewById(R.id.gridview)
        mAdapter = MyGridViewAdapter(mDataList, this.context)
        mGridView.adapter = mAdapter
        mGridView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            mDataList!![position].isSelected = !mDataList!![position].isSelected
            mIndex = -1
            for (i in mDataList!!.indices) {
                if (i != position) {
                    mDataList!![i].isSelected = false
                }
                if (mDataList!![i].isSelected) {
                    mIndex = i
                }
            }
            this@BackgroundChangeFragment.context?.let {
                SharedPreferencesUtil.getInstance(it)?.putIntValue(
                    Constant.VALUE_KEY, mIndex
                )
            }
            /**
             * Notify the adapter that the bound data has changed and the view should be refreshed.
             */
            /**
             * Notify the adapter that the bound data has changed and the view should be refreshed.
             */
            mAdapter!!.notifyDataSetChanged()
        }
        mTakePhoto.setOnClickListener(View.OnClickListener {
            val intent =
                Intent(this@BackgroundChangeFragment.activity, TakePhotoActivity::class.java)
            intent.putExtra(Constant.VALUE_KEY, mIndex)
            this@BackgroundChangeFragment.startActivity(intent)
        })
    }

    private fun initData() {
        mDataList = ArrayList()
        var entity: Entity
        val saveIndex =
            SharedPreferencesUtil.getInstance(requireContext())?.getIntValue(Constant.VALUE_KEY) ?: -1
        mIndex = saveIndex
        if (saveIndex != -1) {
        for (i in Constant.IMAGES.indices) {
            entity = if (i == saveIndex) {
                Entity(
                    Constant.IMAGES[i],
                    true
                )
            } else {
                Entity(
                    Constant.IMAGES[i],
                    false
                )
            }
            mDataList!!.add(entity)
        }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SharedPreferencesUtil.getInstance(requireContext())?.putIntValue(Constant.VALUE_KEY, 0)
    }
}