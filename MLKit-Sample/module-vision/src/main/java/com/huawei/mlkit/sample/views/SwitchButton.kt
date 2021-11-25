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
package com.huawei.mlkit.sample.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.huawei.mlkit.sample.R

/**
 * Custom switch controls
 *
 * @since 2019-12-26
 */
class SwitchButton(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var mSwitchIcon: Bitmap? = null
    private var mSwitchIconWidth = 0
    private var mSwitchIconXPosition = 0
    private var mSwitchButtonCurrentState = false
    private var mPaint: Paint? = null
    private var mListener: OnSwitchButtonStateChangeListener? = null
    fun setCurrentState(currentState: Boolean) {
        mSwitchButtonCurrentState = currentState
    }

    private fun initView() {
        mSwitchIcon = BitmapFactory.decodeResource(this.resources, R.drawable.swich_slider_new)
        mSwitchIconWidth = mSwitchIcon!!.width
        mPaint = Paint()
        mPaint!!.style = Paint.Style.FILL
        mPaint!!.isAntiAlias = true
        mPaint!!.strokeWidth = 2f
        // init value
        mSwitchIconXPosition = 0
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mSwitchIconWidth * 2, mSwitchIconWidth)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val re3 = RectF(0F, 0F, (mSwitchIconWidth * 2).toFloat(), mSwitchIconWidth.toFloat())
        if (mSwitchButtonCurrentState) {
            mPaint!!.color = this.resources.getColor(R.color.button_background)
            mSwitchIconXPosition = mSwitchIconWidth - 1
        } else {
            mPaint!!.color = this.resources.getColor(R.color.white)
            mSwitchIconXPosition = 0
        }
        canvas.drawRoundRect(re3, mSwitchIconWidth / 2.0f, mSwitchIconWidth / 2.0f, mPaint!!)
        canvas.drawBitmap(mSwitchIcon!!, mSwitchIconXPosition.toFloat(), 1.5f, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            mSwitchButtonCurrentState = !mSwitchButtonCurrentState
            mListener!!.onSwitchButtonStateChange(mSwitchButtonCurrentState)
        }
        this.invalidate()
        return true
    }

    /**
     * Set up listener
     *
     * @param listener listener
     */
    fun setOnSwitchButtonStateChangeListener(listener: OnSwitchButtonStateChangeListener?) {
        mListener = listener
    }

    interface OnSwitchButtonStateChangeListener {
        /**
         * Switch state change callback method
         * @param state state
         */
        fun onSwitchButtonStateChange(state: Boolean)
    }

    init {
        initView()
    }
}