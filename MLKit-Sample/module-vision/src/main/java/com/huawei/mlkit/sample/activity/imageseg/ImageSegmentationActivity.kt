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

import com.huawei.mlkit.sample.activity.BaseActivity.onCreate
import com.huawei.mlkit.sample.activity.BaseActivity.setStatusBar
import com.huawei.mlkit.sample.activity.BaseActivity.setStatusBarFontColor
import com.huawei.mlkit.sample.activity.adapter.ItemAdapter
import android.widget.TextView
import android.annotation.SuppressLint
import android.os.Bundle
import com.huawei.mlkit.sample.R
import android.graphics.Bitmap
import com.google.gson.Gson
import com.huawei.hms.mlsdk.fr.MLFormRecognitionTablesAttribute
import com.huawei.hms.mlsdk.fr.MLFormRecognitionConstant
import com.huawei.mlkit.sample.activity.table.TableRecognitionActivity
import android.content.pm.PackageManager
import jxl.write.WriteException
import kotlin.Throws
import jxl.write.WritableWorkbook
import jxl.Workbook
import jxl.write.WritableSheet
import android.widget.Toast
import android.graphics.BitmapFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzer
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzerFactory
import com.google.gson.JsonObject
import com.huawei.hmf.tasks.OnSuccessListener
import android.content.Intent
import com.huawei.hmf.tasks.OnFailureListener
import android.content.DialogInterface
import android.provider.MediaStore
import android.content.ContentValues
import android.app.Activity
import android.widget.CompoundButton
import android.widget.ToggleButton
import com.huawei.mlkit.sample.transactor.LocalObjectTransactor
import com.huawei.mlkit.sample.activity.`object`.ObjectDetectionActivity
import com.bumptech.glide.Glide
import com.huawei.mlkit.sample.activity.adapter.imgseg.MyGridViewAdapter
import com.huawei.mlkit.sample.activity.adapter.ItemAdapter.ItemHolder
import com.huawei.hms.mlsdk.fr.MLFormRecognitionTablesAttribute.TablesContent.TableAttribute.TableCellAttribute
import com.huawei.hms.mlplugin.productvisionsearch.MLProductVisionSearchCapture.AbstractProductFragment
import android.widget.GridView
import com.huawei.mlkit.sample.activity.adapter.BottomSheetAdapter
import com.huawei.mlkit.sample.activity.fragment.ProductFragment
import com.huawei.mlkit.sample.activity.imageseg.LoadHairActivity
import com.huawei.mlkit.sample.activity.imageseg.LoadPhotoActivity
import com.huawei.mlkit.sample.activity.imageseg.StillCutPhotoActivity
import android.widget.AdapterView
import android.widget.LinearLayout
import com.huawei.mlkit.sample.transactor.StillImageSegmentationTransactor
import android.widget.ImageButton
import com.huawei.mlkit.sample.transactor.ImageSegmentationTransactor
import android.renderscript.RenderScript
import android.view.View.OnTouchListener
import android.os.Build
import android.widget.RelativeLayout
import android.graphics.drawable.BitmapDrawable
import androidx.viewpager.widget.ViewPager
import com.huawei.mlkit.sample.activity.adapter.TabFragmentAdapter
import com.huawei.mlkit.sample.activity.imageseg.ImageSegmentationActivity.PagerChangeListener
import com.huawei.mlkit.sample.activity.fragment.BackgroundChangeFragment
import com.huawei.mlkit.sample.activity.fragment.CaptureImageFragment
import com.huawei.mlkit.sample.activity.fragment.SliceImageFragment
import com.huawei.mlkit.sample.activity.fragment.HairImageFragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.huawei.mlkit.sample.activity.documentskew.DocumentSkewStartActivity
import com.huawei.mlkit.sample.activity.documentskew.DocumentSkewCorretionActivity
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzer
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionResult
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionCoordinateInput
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerSetting
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerFactory
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewDetectResult
import android.widget.EditText
import com.huawei.mlkit.sample.activity.scenedection.SceneDectionActivity
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzer
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerSetting
import com.huawei.mlkit.sample.transactor.SceneDetectionTransactor
import android.content.pm.PackageInfo
import android.graphics.Color
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import android.util.SparseArray
import android.view.*
import androidx.fragment.app.Fragment
import com.huawei.mlkit.sample.transactor.LocalImageClassificationTransactor
import com.huawei.mlkit.sample.activity.imageclassfication.ImageClassificationActivity
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.mlkit.sample.activity.table.TableRecognitionStartActivity
import com.huawei.mlkit.sample.activity.imageseg.ImageSegmentationActivity
import com.huawei.mlkit.sample.activity.``object`
import com.huawei.mlkit.sample.activity.BaseActivity
import java.util.ArrayList

class ImageSegmentationActivity : BaseActivity(), View.OnClickListener {
    private var mFragmentList: MutableList<Fragment>? = null
    private var mBgChangeTv: TextView? = null
    private var mCaptureImgTv: TextView? = null
    private var mSliceTv: TextView? = null
    private var mHairTv: TextView? = null
    private var mViewPager: ViewPager? = null
    private var mBgChangeLine: View? = null
    private var mCaptureImgLine: View? = null
    private var mSliceLine: View? = null
    private var mHairLine: View? = null
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
        mViewPager.setOnPageChangeListener(PagerChangeListener())
        mFragmentList = ArrayList()
        mFragmentList.add(BackgroundChangeFragment())
        mFragmentList.add(CaptureImageFragment())
        mFragmentList.add(SliceImageFragment())
        mFragmentList.add(HairImageFragment())
        mAdapter = TabFragmentAdapter(this.supportFragmentManager, mFragmentList)
        mViewPager.setAdapter(mAdapter)
        mViewPager.setCurrentItem(0)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fragment_one -> {
                mViewPager!!.currentItem = 0
                setBgChangeView()
            }
            R.id.fragment_two -> {
                mViewPager!!.currentItem = 1
                setCaptureImageView()
            }
            R.id.fragment_three -> {
                mViewPager!!.currentItem = 2
                setSliceImageView()
            }
            R.id.fragment_four -> {
                mViewPager!!.currentItem = 3
                setHairImageView()
            }
            R.id.back -> finish()
            else -> {}
        }
    }

    private fun setBgChangeView() {
        mBgChangeTv!!.setTextColor(this.resources.getColor(R.color.button_background))
        mCaptureImgTv!!.setTextColor(Color.BLACK)
        mSliceTv!!.setTextColor(Color.BLACK)
        mHairTv!!.setTextColor(Color.BLACK)
        mBgChangeLine!!.visibility = View.VISIBLE
        mCaptureImgLine!!.visibility = View.GONE
        mSliceLine!!.visibility = View.GONE
        mHairLine!!.visibility = View.GONE
    }

    private fun setCaptureImageView() {
        mBgChangeTv!!.setTextColor(Color.BLACK)
        mCaptureImgTv!!.setTextColor(this.resources.getColor(R.color.button_background))
        mSliceTv!!.setTextColor(Color.BLACK)
        mHairTv!!.setTextColor(Color.BLACK)
        mBgChangeLine!!.visibility = View.GONE
        mCaptureImgLine!!.visibility = View.VISIBLE
        mSliceLine!!.visibility = View.GONE
        mHairLine!!.visibility = View.GONE
    }

    private fun setSliceImageView() {
        mBgChangeTv!!.setTextColor(Color.BLACK)
        mCaptureImgTv!!.setTextColor(Color.BLACK)
        mSliceTv!!.setTextColor(this.resources.getColor(R.color.button_background))
        mHairTv!!.setTextColor(Color.BLACK)
        mBgChangeLine!!.visibility = View.GONE
        mCaptureImgLine!!.visibility = View.GONE
        mSliceLine!!.visibility = View.VISIBLE
        mHairLine!!.visibility = View.GONE
    }

    private fun setHairImageView() {
        mBgChangeTv!!.setTextColor(Color.BLACK)
        mCaptureImgTv!!.setTextColor(Color.BLACK)
        mSliceTv!!.setTextColor(Color.BLACK)
        mHairTv!!.setTextColor(this.resources.getColor(R.color.button_background))
        mBgChangeLine!!.visibility = View.GONE
        mCaptureImgLine!!.visibility = View.GONE
        mSliceLine!!.visibility = View.GONE
        mHairLine!!.visibility = View.VISIBLE
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