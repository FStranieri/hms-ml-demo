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
package com.huawei.mlkit.sample.activity.imageseg

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.BaseActivity
import com.huawei.mlkit.sample.activity.adapter.TabFragmentAdapter
import com.huawei.mlkit.sample.activity.fragment.BackgroundChangeFragment
import com.huawei.mlkit.sample.activity.fragment.CaptureImageFragment
import com.huawei.mlkit.sample.activity.fragment.HairImageFragment
import com.huawei.mlkit.sample.activity.fragment.SliceImageFragment

class ImageSegmentationActivity : BaseActivity(), View.OnClickListener {
    private var mFragmentList: MutableList<Fragment> = arrayListOf()
    private lateinit var mBgChangeTv: TextView
    private lateinit var mCaptureImgTv: TextView
    private lateinit var mSliceTv: TextView
    private lateinit var mHairTv: TextView
    private lateinit var mViewPager: ViewPager
    private lateinit var mBgChangeLine: View
    private lateinit var mCaptureImgLine: View
    private lateinit var mSliceLine: View
    private lateinit var mHairLine: View
    private var mAdapter: TabFragmentAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_image_segmentation)
        initView()
        setStatusBar()
        setStatusBarFontColor()
    }

    private fun initView() {
        mBgChangeTv = findViewById(R.id.fragment_one)
        mCaptureImgTv = findViewById(R.id.fragment_two)
        mSliceTv = findViewById(R.id.fragment_three)
        mHairTv = findViewById(R.id.fragment_four)
        mBgChangeLine = findViewById(R.id.line_one)
        mCaptureImgLine = findViewById(R.id.line_two)
        mSliceLine = findViewById(R.id.line_three)
        mHairLine = findViewById(R.id.line_four)
        mViewPager = findViewById(R.id.view_pager)
        findViewById<View>(R.id.back).setOnClickListener(this)
        mBgChangeTv.setOnClickListener(this)
        mCaptureImgTv.setOnClickListener(this)
        mSliceTv.setOnClickListener(this)
        mHairTv.setOnClickListener(this)
        mViewPager.addOnPageChangeListener(PagerChangeListener())
        mFragmentList.add(BackgroundChangeFragment())
        mFragmentList.add(CaptureImageFragment())
        mFragmentList.add(SliceImageFragment())
        mFragmentList.add(HairImageFragment())
        mAdapter = TabFragmentAdapter(this.supportFragmentManager, mFragmentList)
        mViewPager.adapter = mAdapter
        mViewPager.currentItem = 0
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fragment_one -> {
                mViewPager.currentItem = 0
                setBgChangeView()
            }
            R.id.fragment_two -> {
                mViewPager.currentItem = 1
                setCaptureImageView()
            }
            R.id.fragment_three -> {
                mViewPager.currentItem = 2
                setSliceImageView()
            }
            R.id.fragment_four -> {
                mViewPager.currentItem = 3
                setHairImageView()
            }
            R.id.back -> finish()
            else -> {}
        }
    }

    private fun setBgChangeView() {
        mBgChangeTv.setTextColor(this.resources.getColor(R.color.button_background))
        mCaptureImgTv.setTextColor(Color.BLACK)
        mSliceTv.setTextColor(Color.BLACK)
        mHairTv.setTextColor(Color.BLACK)
        mBgChangeLine.visibility = View.VISIBLE
        mCaptureImgLine.visibility = View.GONE
        mSliceLine.visibility = View.GONE
        mHairLine.visibility = View.GONE
    }

    private fun setCaptureImageView() {
        mBgChangeTv.setTextColor(Color.BLACK)
        mCaptureImgTv.setTextColor(this.resources.getColor(R.color.button_background))
        mSliceTv.setTextColor(Color.BLACK)
        mHairTv.setTextColor(Color.BLACK)
        mBgChangeLine.visibility = View.GONE
        mCaptureImgLine.visibility = View.VISIBLE
        mSliceLine.visibility = View.GONE
        mHairLine.visibility = View.GONE
    }

    private fun setSliceImageView() {
        mBgChangeTv.setTextColor(Color.BLACK)
        mCaptureImgTv.setTextColor(Color.BLACK)
        mSliceTv.setTextColor(this.resources.getColor(R.color.button_background))
        mHairTv.setTextColor(Color.BLACK)
        mBgChangeLine.visibility = View.GONE
        mCaptureImgLine.visibility = View.GONE
        mSliceLine.visibility = View.VISIBLE
        mHairLine.visibility = View.GONE
    }

    private fun setHairImageView() {
        mBgChangeTv.setTextColor(Color.BLACK)
        mCaptureImgTv.setTextColor(Color.BLACK)
        mSliceTv.setTextColor(Color.BLACK)
        mHairTv.setTextColor(this.resources.getColor(R.color.button_background))
        mBgChangeLine.visibility = View.GONE
        mCaptureImgLine.visibility = View.GONE
        mSliceLine.visibility = View.GONE
        mHairLine.visibility = View.VISIBLE
    }

    /**
     * Set a ViewPager listening event. When the ViewPager is swiped left or right, the menu bar selected state changes accordingly.
     *
     */
    inner class PagerChangeListener : OnPageChangeListener {
        override fun onPageScrollStateChanged(arg0: Int) {}
        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageSelected(position: Int) {
            when (position) {
                0 -> setBgChangeView()
                1 -> setCaptureImageView()
                2 -> setSliceImageView()
                3 -> setHairImageView()
                else -> {}
            }
        }
    }

    fun onBackPressed(view: View?) {
        finish()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }
}