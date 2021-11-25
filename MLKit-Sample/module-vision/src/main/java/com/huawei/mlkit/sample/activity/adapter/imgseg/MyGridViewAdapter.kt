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
package com.huawei.mlkit.sample.activity.adapter.imgseg

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.entity.Entity
import java.util.*

class MyGridViewAdapter(private val mDataList: ArrayList<Entity>?, private val mContext: Context?) :
    BaseAdapter() {
    override fun getCount(): Int {
        return mDataList!!.size
    }

    override fun getItem(position: Int): Any {
        return mDataList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, cView: View?, parent: ViewGroup): View {
        var convertView = cView
        val holder: ViewHolder?
        try {
            if (convertView == null) {
                convertView =
                    LayoutInflater.from(mContext).inflate(R.layout.imageseg_gridview, null)
                holder = ViewHolder()
                holder.imageView = convertView.findViewById(R.id.image)
                holder.selectIcon = convertView.findViewById(R.id.img_select)
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            if (mDataList!![position].isSelected) {
                holder.selectIcon.setBackgroundResource(R.mipmap.seg_selected)
            } else {
                holder.selectIcon.background = null
            }
            val resourceId = mDataList[position].resourceId
            Glide.with(mContext!!)
                .load(resourceId)
                .error(R.drawable.icon_logo)
                .into(holder.imageView)
        } catch (e: Exception) {
            Log.e("MyGridViewAdapter", e.message!!)
        }
        return convertView!!
    }

    internal class ViewHolder {
        lateinit var imageView: ImageView
        lateinit var selectIcon: ImageView
    }

    companion object {
        private val TAG = MyGridViewAdapter::class.java.name
    }
}