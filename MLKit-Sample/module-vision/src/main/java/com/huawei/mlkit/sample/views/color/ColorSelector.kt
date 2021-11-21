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
package com.huawei.mlkit.sample.views.color

import android.annotation.SuppressLint
import android.app.Activity
import kotlin.jvm.Synchronized
import kotlin.Throws
import android.hardware.Camera.PictureCallback
import android.hardware.Camera.PreviewCallback
import android.hardware.Camera.CameraInfo
import android.hardware.Camera.AutoFocusCallback
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.mlkit.sample.transactor.LocalObjectTransactor
import com.huawei.mlkit.sample.views.graphic.LocalObjectGraphic
import com.huawei.mlkit.sample.transactor.RemoteLandmarkTransactor
import com.huawei.mlkit.sample.views.graphic.RemoteLandmarkGraphic
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzer
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerSetting
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory
import com.huawei.mlkit.sample.views.graphic.SceneDetectionGraphic
import com.huawei.mlkit.sample.transactor.SceneDetectionTransactor
import android.renderscript.RenderScript
import com.huawei.mlkit.sample.transactor.ImageSegmentationTransactor
import android.util.SparseArray
import android.widget.Toast
import android.renderscript.Allocation
import android.renderscript.ScriptIntrinsicBlur
import com.huawei.mlkit.sample.transactor.StillImageSegmentationTransactor
import com.huawei.mlkit.sample.transactor.LocalImageClassificationTransactor
import com.huawei.mlkit.sample.views.graphic.LocalImageClassificationGraphic
import com.huawei.mlkit.sample.transactor.RemoteImageClassificationTransactor
import com.huawei.mlkit.sample.views.graphic.RemoteImageClassificationGraphic
import com.huawei.mlkit.sample.R
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.content.ContentUris
import android.content.Context
import android.os.Environment
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.content.SharedPreferences
import android.content.res.Configuration
import kotlin.jvm.JvmOverloads
import android.content.res.TypedArray
import android.graphics.*
import android.view.View.MeasureSpec
import android.os.Parcelable
import android.os.Parcel
import android.util.DisplayMetrics
import android.widget.GridView
import android.widget.AbsListView
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.*
import com.huawei.mlkit.sample.util.Constant

class ColorSelector : View {
    /**
     * Color bar fillet rectangle border.
     */
    private val mRect = Rect()

    /**
     * mBitmapForIndicator where to draw on the view.
     */
    private val mIndicatorRect = Rect()

    /**
     * Indicator point color
     */
    private var mIndicatorColor = 0

    /**
     * Paint for view and mBitmapforcolor.
     */
    private var mPaint: Paint? = null

    /**
     * Paint for indicator.
     */
    private var mIndicatorPaint: Paint? = null
    private var mLinearGradient: LinearGradient? = null
    private var mTop = 0
    private var mLeft = 0
    private var mRight = 0
    private var mBottom = 0

    /**
     * Point radius.
     */
    private var mRadius = 0
    private var mBitmapForColor: Bitmap? = null
    private var mBitmapForIndicator: Bitmap? = null
    private var mIsNeedReDrawColorTable = true
    private var mCurrentX = 0
    private var mCurrentY = 0
    private var mColors: IntArray? = null
    private var mCurrentColor = 0
    private var mContext: Context? = null
    private var lastLand = false
    private var lastWidth = 0
    private var isInitialed = false
    private var mColorSelectorChangeListener: OnColorSelectorChangeListener? = null

    constructor(context: Context?) : super(context) {}

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mContext = context
        mBitmapForColor = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        mBitmapForIndicator = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        // setShadowLayer is invalid when hardware acceleration is turned on. Hardware acceleration needs to be turned off.
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mIndicatorPaint = Paint()
        mIndicatorPaint!!.isAntiAlias = true
        mCurrentY = Int.MAX_VALUE
        mCurrentX = mCurrentY
        val array =
            context.theme.obtainStyledAttributes(attrs, R.styleable.ColorSelector, defStyleAttr, 0)
        mIndicatorColor = array.getColor(R.styleable.ColorSelector_indicatorColor, Color.WHITE)
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width: Int
        var height: Int
        width = if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(widthMeasureSpec)
        } else { // Set the width is warp_content in XML.
            this.suggestedMinimumWidth + this.paddingLeft + this.paddingRight
        }
        height = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(heightMeasureSpec)
        } else {
            this.suggestedMinimumHeight + this.paddingTop + this.paddingBottom
        }
        width = Math.max(width, LONG_SIZE)
        height = Math.max(height, SHORT_SIZE)
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mTop = this.paddingTop
        mLeft = this.paddingLeft
        mBottom = this.measuredHeight - this.paddingBottom
        mRight = this.measuredWidth - this.paddingRight
        if (mCurrentX == mCurrentY || mCurrentY == Int.MAX_VALUE) {
            mCurrentX = this.width / 2
            mCurrentY = this.height / 2
        }
        calculateBounds()
        if (mColors == null) {
            setColors(*Constant.COLOR_TABLE)
        } else {
            setColors(*mColors!!)
        }
        createBitmap()
    }

    private fun createBitmap() {
        if (mBitmapForColor != null) {
            if (!mBitmapForColor!!.isRecycled) {
                mBitmapForColor!!.recycle()
                mBitmapForColor = null
            }
        }
        if (mBitmapForIndicator != null) {
            if (!mBitmapForIndicator!!.isRecycled) {
                mBitmapForIndicator!!.recycle()
                mBitmapForIndicator = null
            }
        }
        mBitmapForColor =
            Bitmap.createBitmap(mRect.width(), mRect.height(), Bitmap.Config.ARGB_8888)
        mBitmapForIndicator = Bitmap.createBitmap(mRadius * 2, mRadius * 2, Bitmap.Config.ARGB_8888)
    }

    /**
     * Calculate color bar boundaries.
     */
    private fun calculateBounds() {
        val average = 9
        val height = mBottom - mTop
        val width = mRight - mLeft
        var size = Math.min(width, height)
        if (width <= height) { // Width is smaller than height, recalculate height in the way of 6:1.
            size = width / 6
        }
        val each = size / average
        mRadius = each * 7 / 2
        val top: Int
        val left: Int
        val bottom: Int
        val right: Int
        val offset = each * 3 / 2
        left = mLeft + mRadius
        right = mRight - mRadius
        top = height / 2 - offset
        bottom = height / 2 + offset
        mRect[left, top, right] = bottom
    }

    /**
     * Set the gradient color of the color bar.
     *
     * @param colors color value.
     */
    fun setColors(vararg colors: Int) {
        mLinearGradient = null
        mColors = colors
        mLinearGradient = LinearGradient(
            mRect.left, mRect.top,
            mRect.right, mRect.top,
            colors,
            null,
            Shader.TileMode.CLAMP
        )
        mIsNeedReDrawColorTable = true
        this.invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (mIsNeedReDrawColorTable) {
            createColorTableBitmap()
        }
        canvas.drawBitmap(mBitmapForColor!!, null, mRect, mPaint)
        createIndicatorBitmap()
        if (!isInitialed) {
            lastWidth = width
            isInitialed = true
        }
        // Draw indicator points.
        val scale: Float
        if (lastLand != isLands) {
            lastLand = if (isLands) {
                true
            } else {
                false
            }
            scale = width / (lastWidth * 1.0f)
            mCurrentX = (mCurrentX * scale).toInt()
            lastWidth = width
        }
        mIndicatorRect[mCurrentX - mRadius, mCurrentY - mRadius, mCurrentX + mRadius] =
            mCurrentY + mRadius
        canvas.drawBitmap(mBitmapForIndicator!!, null, mIndicatorRect, mPaint)
    }

    private val isLands: Boolean
        private get() {
            val mConfiguration = mContext!!.resources.configuration
            val ori = mConfiguration.orientation
            return ori == Configuration.ORIENTATION_LANDSCAPE
        }

    private fun createIndicatorBitmap() {
        mIndicatorPaint!!.color = mIndicatorColor
        val radius = 3
        mIndicatorPaint!!.setShadowLayer(radius.toFloat(), 0f, 0f, Color.GRAY)
        val canvas = Canvas(mBitmapForIndicator!!)
        canvas.drawCircle(
            mRadius.toFloat(),
            mRadius.toFloat(),
            (mRadius - radius).toFloat(),
            mIndicatorPaint!!
        )
    }

    private fun createColorTableBitmap() {
        val canvas = Canvas(mBitmapForColor!!)
        val rf = RectF(
            0, 0, mBitmapForColor!!.width
                .toFloat(), mBitmapForColor!!.height.toFloat()
        )
        val radius: Int
        radius = mBitmapForColor!!.height / 2
        mPaint!!.color = Color.BLACK
        canvas.drawRoundRect(rf, radius.toFloat(), radius.toFloat(), mPaint!!)
        mPaint!!.shader = mLinearGradient
        canvas.drawRoundRect(rf, radius.toFloat(), radius.toFloat(), mPaint!!)
        mPaint!!.shader = null
        mIsNeedReDrawColorTable = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val xValue = event.x.toInt()
        if (!inBoundOfColorTable(xValue)) {
            return true
        }
        mCurrentX = xValue
        mCurrentY = this.height / 2
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            if (mColorSelectorChangeListener != null) {
                mColorSelectorChangeListener!!.onStartColorSelect(this)
                calculateColor()
                mColorSelectorChangeListener!!.onColorChanged(this, mCurrentColor)
            }
        } else if (event.actionMasked == MotionEvent.ACTION_UP) {
            if (mColorSelectorChangeListener != null) {
                mColorSelectorChangeListener!!.onStopColorSelect(this)
                calculateColor()
                mColorSelectorChangeListener!!.onColorChanged(this, mCurrentColor)
            }
        } else {
            if (mColorSelectorChangeListener != null) {
                calculateColor()
                mColorSelectorChangeListener!!.onColorChanged(this, mCurrentColor)
            }
        }
        this.invalidate()
        return true
    }

    /**
     * Get the color of the current indicator.
     *
     * @return color value.
     */
    val color: Int
        get() = calculateColor()

    private fun inBoundOfColorTable(xValue: Int): Boolean {
        return xValue > mLeft + mRadius && xValue < mRight - mRadius
    }

    private fun calculateColor(): Int {
        val x: Int
        val y: Int
        y = (mRect.bottom - mRect.top) / 2
        x = if (mCurrentX < mRect.left) {
            1
        } else if (mCurrentX > mRect.right) {
            mBitmapForColor!!.width - 1
        } else {
            mCurrentX - mRect.left
        }
        val pixel = mBitmapForColor!!.getPixel(x, y)
        mCurrentColor = pixelToColor(pixel)
        return mCurrentColor
    }

    private fun pixelToColor(pixel: Int): Int {
        return Color.argb(
            Color.alpha(pixel),
            Color.red(pixel),
            Color.green(pixel),
            Color.blue(pixel)
        )
    }

    fun setOnColorSelectorChangeListener(listener: OnColorSelectorChangeListener?) {
        mColorSelectorChangeListener = listener
    }

    interface OnColorSelectorChangeListener {
        /**
         * Callback when the selected color value changes.
         *
         * @param picker ColorSelector.
         * @param color  color value.
         */
        fun onColorChanged(picker: ColorSelector?, color: Int)

        /**
         * Start color selection.
         *
         * @param picker ColorSelector.
         */
        fun onStartColorSelect(picker: ColorSelector?)

        /**
         * Stop color selection.
         *
         * @param picker ColorSelector.
         */
        fun onStopColorSelect(picker: ColorSelector?)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val parcelable = super.onSaveInstanceState()
        val saveState: MySavedState = MySavedState(parcelable)
        saveState.xValue = mCurrentX
        saveState.yValue = mCurrentY
        saveState.colors = mColors
        saveState.bitmapColorView = mBitmapForColor
        saveState.bitmapIndicatorView = mBitmapForIndicator
        return saveState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is MySavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val ss = state
        super.onRestoreInstanceState(ss.superState)
        mCurrentX = ss.xValue
        mCurrentY = ss.yValue
        mColors = ss.colors
        mBitmapForColor = ss.bitmapColorView
        mBitmapForIndicator = ss.bitmapIndicatorView
        mIsNeedReDrawColorTable = true
    }

    private inner class MySavedState internal constructor(source: Parcelable?) :
        BaseSavedState(source) {
        var xValue = 0
        var yValue = 0
        var colors: IntArray?
        var bitmapColorView: Bitmap? = null
        var bitmapIndicatorView: Bitmap? = null
        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(xValue)
            out.writeInt(yValue)
            out.writeParcelable(bitmapColorView, flags)
            out.writeIntArray(colors)
            if (bitmapIndicatorView != null) {
                out.writeParcelable(bitmapIndicatorView, flags)
            }
        }
    }

    fun initData() {
        mIsNeedReDrawColorTable = true
        requestLayout()
    }

    companion object {
        // By default, the ratio of long edge to short edge is 6:1.
        private const val SHORT_SIZE = 70
        private const val LONG_SIZE = 420
    }
}