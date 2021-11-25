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
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.util.CommonUtils

class RoundImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(
    context, attrs, defStyleAttr
) {
    private var outerBorderColor = Color.WHITE // Outer border color
    private var outerBorderWidth // Outer border color width
            = 0
    private var innerBorderWidth // Inner border width
            = 0
    private var innerBorderColor = Color.WHITE // Inner border color
    private var xfermode: Xfermode? = null
    private var w = 0
    private var h = 0
    private var radius = 0f
    private var rectF: RectF? = null
    private var paint: Paint? = null
    private var dashPaint: Paint? = null
    private var path: Path? = null
    private var srcPath: Path? = null
    private val screenWidth: Int
        private get() {
            val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels
        }
    private val screenHeight: Int
        private get() {
            val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.heightPixels
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = 14 * screenWidth / 15
        val height = 1 * screenHeight / 2
        val a = Math.min(width, height)
        setMeasuredDimension(a, a)
    }

    private fun init() {
        rectF = RectF()
        paint = Paint()
        dashPaint = Paint()
        path = Path()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        } else {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            srcPath = Path()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.w = w
        this.h = h
    }

    override fun onDraw(canvas: Canvas) {
        init()
        initSrcRectF()
        // Use graphics blending mode to display pictures in a specified area
        canvas.saveLayer(rectF, null, Canvas.ALL_SAVE_FLAG)
        val scaleX = 1.0f * (width - 2 * outerBorderWidth - 2 * innerBorderWidth) / width
        val scaleY = 1.0f * (height - 2 * outerBorderWidth - 2 * innerBorderWidth) / height
        // Scale the canvas so that the content of the image is not covered by borders
        canvas.scale(scaleX, scaleY, width / 2.0f, height / 2.0f)
        super.onDraw(canvas)
        paint!!.reset()
        dashPaint!!.reset()
        path!!.reset()
        path!!.addCircle(width / 2.0f, height / 2.0f, radius, Path.Direction.CCW)
        paint!!.isAntiAlias = true
        paint!!.style = Paint.Style.FILL
        paint!!.xfermode = xfermode
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            canvas.drawPath(path!!, paint!!)
        } else {
            srcPath!!.addRect(rectF!!, Path.Direction.CCW)
            srcPath!!.op(path!!, Path.Op.DIFFERENCE)
            canvas.drawPath(srcPath!!, paint!!)
        }
        paint!!.xfermode = null
        canvas.restore()
        drawBorders(canvas)
    }

    private fun drawBorders(canvas: Canvas) {
        drawCircleBorder(
            canvas,
            outerBorderWidth,
            outerBorderColor,
            radius - outerBorderWidth / 2.0f,
            15
        )
        drawDashCircleBorder(
            canvas,
            outerBorderWidth,
            outerBorderColor,
            radius - outerBorderWidth / 2.0f
        )
        drawCircleBorder(
            canvas,
            innerBorderWidth,
            innerBorderColor,
            radius - outerBorderWidth - innerBorderWidth / 2.0f,
            25
        )
    }

    private fun drawCircleBorder(
        canvas: Canvas,
        borderWidth: Int,
        borderColor: Int,
        radius: Float,
        alpha: Int
    ) {
        initBorderPaint(borderWidth, borderColor)
        paint!!.alpha = alpha
        path!!.addCircle(width / 2.0f, height / 2.0f, radius, Path.Direction.CCW)
        canvas.drawPath(path!!, paint!!)
    }

    private fun drawDashCircleBorder(
        canvas: Canvas,
        borderWidth: Int,
        borderColor: Int,
        radius: Float
    ) {
        dashPaint!!.color = borderColor
        dashPaint!!.strokeWidth = CommonUtils.dp2px(context, 1f)
        dashPaint!!.style = Paint.Style.STROKE
        dashPaint!!.alpha = 20
        dashPaint!!.pathEffect = DashPathEffect(floatArrayOf(35f, 40f), 0F)
        canvas.drawCircle(width / 2.0f, height / 2.0f, radius + borderWidth / 2, dashPaint!!)
    }

    private fun initBorderPaint(borderWidth: Int, borderColor: Int) {
        path!!.reset()
        paint!!.strokeWidth = borderWidth.toFloat()
        paint!!.color = borderColor
        paint!!.style = Paint.Style.STROKE
    }

    private fun initSrcRectF() {
        radius = Math.min(width, height) / 2.0f
        rectF!![width / 2.0f - radius, height / 2.0f - radius, width / 2.0f + radius] =
            height / 2.0f + radius
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView, 0, 0)
        for (i in 0 until typedArray.indexCount) {
            val attr = typedArray.getIndex(i)
            if (attr == R.styleable.RoundImageView_outer_border_width) {
                outerBorderWidth = typedArray.getDimensionPixelSize(
                    attr, CommonUtils.dp2px(
                        context, 25f
                    ).toInt()
                )
            } else if (attr == R.styleable.RoundImageView_outer_border_color) {
                outerBorderColor = typedArray.getColor(attr, outerBorderColor)
            } else if (attr == R.styleable.RoundImageView_inner_border_width) {
                innerBorderWidth = typedArray.getDimensionPixelSize(
                    attr, CommonUtils.dp2px(
                        context, 15f
                    ).toInt()
                )
            } else if (attr == R.styleable.RoundImageView_inner_border_color) {
                innerBorderColor = typedArray.getColor(attr, innerBorderColor)
            }
        }
        typedArray.recycle()
        init()
    }
}