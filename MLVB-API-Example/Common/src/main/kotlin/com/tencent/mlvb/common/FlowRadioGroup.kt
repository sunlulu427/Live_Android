package com.tencent.mlvb.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RadioGroup
import kotlin.math.max

/**
 * RadioGroup that implements automatic line wrapping
 */
class FlowRadioGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RadioGroup(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        measureChildren(widthMeasureSpec, heightMeasureSpec)

        var maxWidth = 0
        var totalHeight = 0
        var lineWidth = 0
        var maxLineHeight = 0

        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            val params = child.layoutParams as MarginLayoutParams
            val oldHeight = maxLineHeight
            val oldWidth = maxWidth

            val deltaX = child.measuredWidth + params.leftMargin + params.rightMargin
            if (lineWidth + deltaX + paddingLeft + paddingRight > widthSize) {
                maxWidth = max(lineWidth, oldWidth)
                lineWidth = deltaX
                totalHeight += oldHeight
                maxLineHeight = child.measuredHeight + params.topMargin + params.bottomMargin
            } else {
                lineWidth += deltaX
                val deltaY = child.measuredHeight + params.topMargin + params.bottomMargin
                maxLineHeight = max(maxLineHeight, deltaY)
            }

            if (i == count - 1) {
                totalHeight += maxLineHeight
                maxWidth = max(lineWidth, oldWidth)
            }
        }

        maxWidth += paddingLeft + paddingRight
        totalHeight += paddingTop + paddingBottom

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        setMeasuredDimension(
            if (widthMode == MeasureSpec.EXACTLY) widthSize else maxWidth,
            if (heightMode == MeasureSpec.EXACTLY) heightSize else totalHeight
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        var preLeft = paddingLeft
        var preTop = paddingTop
        var maxHeight = 0

        for (i in 0 until count) {
            val child = getChildAt(i)
            val params = child.layoutParams as MarginLayoutParams

            if (preLeft + params.leftMargin + child.measuredWidth + params.rightMargin + paddingRight > (r - l)) {
                preLeft = paddingLeft
                preTop += maxHeight
                maxHeight = child.measuredHeight + params.topMargin + params.bottomMargin
            } else {
                maxHeight = max(maxHeight, child.measuredHeight + params.topMargin + params.bottomMargin)
            }

            val left = preLeft + params.leftMargin
            val top = preTop + params.topMargin
            val right = left + child.measuredWidth
            val bottom = top + child.measuredHeight

            child.layout(left, top, right, bottom)
            preLeft += params.leftMargin + child.measuredWidth + params.rightMargin
        }
    }
}