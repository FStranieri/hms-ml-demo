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
package com.huawei.mlkit.sample.activity.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.entity.GridViewItem
import java.util.*

class GridViewAdapter(
    private val mDataList: ArrayList<GridViewItem>?,
    private val mContext: Context
) : BaseAdapter() {
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
            if (cView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.gridview_item, null)
                holder = ViewHolder()
                holder.imageView = convertView.findViewById(R.id.image_item)
                holder.textView = convertView.findViewById(R.id.text_item)
                holder.imageNew = convertView.findViewById(R.id.icon_new)
                convertView.tag = holder
            } else {
                holder = convertView!!.tag as ViewHolder
            }
            val item = mDataList!![position]
            if (position == 0) {
                holder.imageNew.setBackgroundResource(R.drawable.icon_new)
            } else {
                holder.imageNew.background = null
            }
            holder.imageView.setImageResource(item.resourceId)
            holder.textView.setText(item.stringId)
        } catch (e: Exception) {
            Log.e("GridViewAdapter", e.message!!)
        }
        return convertView!!
    }

    internal class ViewHolder {
        lateinit var imageView: ImageView
        lateinit var imageNew: ImageView
        lateinit var textView: TextView
    }

    companion object {
        private val TAG = GridViewAdapter::class.java.name
    }
}