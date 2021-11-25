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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import com.huawei.mlkit.sample.R

@SuppressLint("AppCompatCustomView")
class DocumentCorrectImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    private val _pointPaint: Paint
    private val _pointFillPaint: Paint
    private val _linePaint: Paint
    private val _guideLinePaint: Paint
    private var mPoint: Point? = null
    private val mMatrix = FloatArray(9)
    private val mPointLinePath = Path()
    private val LEFT_TOP = 0
    private val RIGHT_TOP = 1
    private val RIGHT_BOTTOM = 2
    private val LEFT_BOTTOM = 3
    private var _scaleX = 0f
    private var _scaleY = 0f
    private var _rectWidth = 0
    private var _rectHeight = 0
    private var _rectTop = 0
    private var _rectLeft = 0
    private val _lineWidth: Float
    private val _pointColor: Int
    private val _pointWidth: Float
    private val _guideLineWidth: Float
    private val _pointFillColor: Int
    private val _pointFillAlpha: Int
    private val _lineColor: Int
    private val _guideLineColor: Int
    lateinit var cropPoints: Array<Point>
        private set

    fun setPoints(mChoosePoints: Array<Point>) {
        if (this.drawable != null) {
            cropPoints = mChoosePoints
            invalidate()
        }
    }

    val isIrRegular: Boolean
        get() {
            if (!isNull(cropPoints)) {
                val left_top = cropPoints[0]
                val right_top = cropPoints[1]
                val right_bottoom = cropPoints[2]
                val left_bottom = cropPoints[3]
                return operater(left_top, right_bottoom, left_bottom.x, left_bottom.y) * operater(
                    left_top,
                    right_bottoom,
                    right_top.x,
                    right_top.y
                ) < 0 &&
                        operater(left_bottom, right_top, left_top.x, left_top.y) * operater(
                    left_bottom,
                    right_top,
                    right_bottoom.x,
                    right_bottoom.y
                ) < 0
            }
            return false
        }

    private fun dp2px(dp: Float): Float {
        val density = resources.displayMetrics.density
        return dp * density
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val drawable = drawable
        imageMatrix.getValues(mMatrix)
        _scaleX = mMatrix[0]
        _scaleY = mMatrix[4]
        if (drawable != null) {
            val intrinsicWidth = drawable.intrinsicWidth
            val intrinsicHeight = drawable.intrinsicHeight
            _rectWidth = Math.round(intrinsicWidth * _scaleX)
            _rectHeight = Math.round(intrinsicHeight * _scaleY)
            _rectTop = (height - _rectHeight) / 2
            _rectLeft = (width - _rectWidth) / 2
        }
        if (isNull(cropPoints)) {
            mPointLinePath.reset()
            val left_top = cropPoints[0]
            val right_top = cropPoints[1]
            val right_bottoom = cropPoints[2]
            val left_bottom = cropPoints[3]
            mPointLinePath.moveTo(getPointX(left_top), getPointY(left_top))
            mPointLinePath.lineTo(getPointX(right_top), getPointY(right_top))
            mPointLinePath.lineTo(getPointX(right_bottoom), getPointY(right_bottoom))
            mPointLinePath.lineTo(getPointX(left_bottom), getPointY(left_bottom))
            mPointLinePath.close()
            val path = mPointLinePath
            if (path != null) {
                canvas.drawPath(path, _linePaint)
            }
            for (point in cropPoints) {
                canvas.drawCircle(getPointX(point), getPointY(point), dp2px(10f), _pointPaint)
                canvas.drawCircle(getPointX(point), getPointY(point), dp2px(10f), _pointFillPaint)
            }
        }
    }

    private fun operater(point1: Point, point2: Point, x: Int, y: Int): Long {
        val point1_x = point1.x.toLong()
        val point1_y = point1.y.toLong()
        val point2_x = point2.x.toLong()
        val point2_y = point2.y.toLong()
        return (x - point1_x) * (point2_y - point1_y) - (y - point1_y) * (point2_x - point1_x)
    }

    private fun getPointX(point: Point): Float {
        return point.x * _scaleX + _rectLeft
    }

    private fun getPointY(point: Point): Float {
        return point.y * _scaleY + _rectTop
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var variable = true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isNull(cropPoints)) {
                    for (point in cropPoints) {
                        val downX = event.x
                        val downY = event.y
                        val pointX = getPointX(point)
                        val pointY = getPointY(point)
                        val distance = Math.sqrt(
                            Math.pow(
                                (downX - pointX).toDouble(),
                                2.0
                            ) + Math.pow((downY - pointY).toDouble(), 2.0)
                        )
                        if (distance < dp2px(14f)) {
                            mPoint = point
                        }
                    }
                }
                if (mPoint == null) variable = false
            }
            MotionEvent.ACTION_MOVE -> {
                val pointType = getPointType(mPoint)
                val x = ((Math.min(
                    Math.max(event.x, _rectLeft.toFloat()),
                    (_rectLeft + _rectWidth).toFloat()
                ) - _rectLeft) / _scaleX).toInt()
                val y = ((Math.min(
                    Math.max(event.y, _rectTop.toFloat()),
                    (_rectTop + _rectHeight).toFloat()
                ) - _rectTop) / _scaleY).toInt()
                if (mPoint != null && pointType != null) {
                    if (pointType == PointType.LEFT_TOP && moveLeftTop(
                            x,
                            y
                        ) || pointType == PointType.RIGHT_TOP && moveRightTop(
                            x,
                            y
                        ) || pointType == PointType.RIGHT_BOTTOM && moveRightBottom(
                            x,
                            y
                        ) || pointType == PointType.LEFT_BOTTOM && moveLeftBottom(x, y)
                    ) {
                        mPoint!!.x = x
                        mPoint!!.y = y
                    }
                }
            }
            MotionEvent.ACTION_UP -> mPoint = null
        }
        invalidate()
        return variable || super.onTouchEvent(event)
    }

    private fun compare(point1: Point, point2: Point, x: Int, y: Int, point3: Point): Boolean {
        return operater(point1, point2, x, y) *
                operater(point1, point2, point3.x, point3.y) <= 0
    }

    private fun moveLeftTop(x: Int, y: Int): Boolean {
        compare(cropPoints[RIGHT_TOP], cropPoints[LEFT_BOTTOM], x, y, cropPoints[RIGHT_BOTTOM])
        compare(cropPoints[RIGHT_TOP], cropPoints[RIGHT_BOTTOM], x, y, cropPoints[LEFT_BOTTOM])
        compare(cropPoints[LEFT_BOTTOM], cropPoints[RIGHT_BOTTOM], x, y, cropPoints[RIGHT_TOP])
        return true
    }

    private fun moveRightTop(x: Int, y: Int): Boolean {
        compare(cropPoints[LEFT_TOP], cropPoints[RIGHT_BOTTOM], x, y, cropPoints[LEFT_BOTTOM])
        compare(cropPoints[LEFT_TOP], cropPoints[LEFT_BOTTOM], x, y, cropPoints[RIGHT_BOTTOM])
        compare(cropPoints[LEFT_BOTTOM], cropPoints[RIGHT_BOTTOM], x, y, cropPoints[LEFT_TOP])
        return true
    }

    private fun moveRightBottom(x: Int, y: Int): Boolean {
        compare(cropPoints[RIGHT_TOP], cropPoints[LEFT_BOTTOM], x, y, cropPoints[LEFT_TOP])
        compare(cropPoints[LEFT_TOP], cropPoints[RIGHT_TOP], x, y, cropPoints[LEFT_BOTTOM])
        compare(cropPoints[LEFT_TOP], cropPoints[LEFT_BOTTOM], x, y, cropPoints[RIGHT_TOP])
        return true
    }

    private fun moveLeftBottom(x: Int, y: Int): Boolean {
        compare(cropPoints[LEFT_TOP], cropPoints[RIGHT_BOTTOM], x, y, cropPoints[RIGHT_TOP])
        compare(cropPoints[LEFT_TOP], cropPoints[RIGHT_TOP], x, y, cropPoints[RIGHT_BOTTOM])
        compare(cropPoints[RIGHT_TOP], cropPoints[RIGHT_BOTTOM], x, y, cropPoints[LEFT_TOP])
        return true
    }

    private fun getPointType(point: Point?): PointType? {
        var type: PointType? = null
        if (point != null) {
            if (isNull(cropPoints)) {
                for (i in cropPoints.indices) {
                    if (point === cropPoints[i]) {
                        type = PointType.values()[i]
                    }
                }
            }
        }
        return type
    }

    fun isNull(points: Array<Point>?): Boolean {
        return points != null && points.size == 4
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DocumentCorrectImageView)
        _lineColor = typedArray.getColor(R.styleable.DocumentCorrectImageView_LineColor, -0xff0001)
        _lineWidth =
            typedArray.getDimension(R.styleable.DocumentCorrectImageView_LineWidth, dp2px(1f))
        _pointColor =
            typedArray.getColor(R.styleable.DocumentCorrectImageView_PointColor, -0xff0001)
        _pointWidth =
            typedArray.getDimension(R.styleable.DocumentCorrectImageView_PointWidth, dp2px(1f))
        _guideLineWidth = typedArray.getDimension(
            R.styleable.DocumentCorrectImageView_GuideLineWidth,
            dp2px(0.5f)
        )
        _guideLineColor =
            typedArray.getColor(R.styleable.DocumentCorrectImageView_GuideLineColor, Color.WHITE)
        _pointFillColor =
            typedArray.getColor(R.styleable.DocumentCorrectImageView_PointFillColor, Color.WHITE)
        _pointFillAlpha = Math.min(
            Math.max(
                0,
                typedArray.getInt(R.styleable.DocumentCorrectImageView_PointFillAlpha, 175)
            ), 255
        )
        typedArray.recycle()
        _pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        _pointPaint.color = _pointColor
        _pointPaint.strokeWidth = _pointWidth
        _pointPaint.style = Paint.Style.STROKE
        _pointFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        _pointFillPaint.color = _pointFillColor
        _pointFillPaint.style = Paint.Style.FILL
        _pointFillPaint.alpha = _pointFillAlpha
        _linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        _linePaint.color = _lineColor
        _linePaint.strokeWidth = _lineWidth
        _linePaint.style = Paint.Style.STROKE
        _guideLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        _guideLinePaint.color = _guideLineColor
        _guideLinePaint.style = Paint.Style.FILL
        _guideLinePaint.strokeWidth = _guideLineWidth
    }
}