package com.zhj.countview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import java.util.*


/**
 * Description:计数控件
 * 数字增大：新数字是从下往上进入，老数字是从中间往上移除
 * 数字减小：新数字从上往下进入，老数字从中间往下移除
 * Created by zhonghaojie on 2018/9/20.
 */
class CountView : View {

    private var currentNum: Long = 0
    private var newNum: Long = 0
    private var oldList = ArrayList<Int>()//用来存储老数字每个数位上的值
    private var newList = ArrayList<Int>()//用来存储新数字每个数位上的值

    private var topSpace = 60//上下各要预留一定的高度来显示动画效果
    private var PADDING_SPACE = 3//数字之间的间隔
    private val textRect = Rect()//文字绘制区域
    private val digitalRect = Rect()//用来获取单个数字的宽高
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textSize = 36f
    private lateinit var animate: ValueAnimator
    private var animatePercent = 0f
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {


        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CountView)
        textSize = typedArray.getDimensionPixelSize(R.styleable.CountView_textsize, 36).toFloat()
        topSpace = typedArray.getDimensionPixelSize(R.styleable.CountView_topSpace, 60)
        currentNum = typedArray.getInteger(R.styleable.CountView_number,0).toLong()
        initNumber()
        paint.color = Color.parseColor("#b1b1b1")
        paint.textSize = textSize
        paint.getTextBounds("0", 0, 1, digitalRect)
        typedArray.recycle()
        initAnimate()
        postInvalidate()
    }

    private fun initNumber(){
        toDigitals(currentNum,newList)
        toDigitals(currentNum,oldList)
    }

    private fun initAnimate() {
        animate = ValueAnimator.ofFloat(0f, 1f)
        animate.duration = 200
        animate.addUpdateListener {
            animatePercent = it.animatedValue as Float
            invalidate()
        }
        animate.interpolator = LinearInterpolator()
        animate.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                //动画结束后，更新数值
                currentNum = newNum
            }

        })
    }

    fun setNum(num: Long) {
        currentNum = num
        changeCount(0)
    }


    fun addNum() {
        changeCount(1)
    }

    fun minusNum() {
        changeCount(-1)
    }

    fun changeCount(value: Long) {
        if (animate.isRunning) {
            animate.end()
        }
        newNum = value + currentNum
        toDigitals(currentNum, oldList)
        toDigitals(newNum, newList)
        paint.getTextBounds(newNum.toString(), 0, newNum.toString().length, textRect)
        if (newNum != currentNum) {
            animate.start()
        }
    }

    /**
     * 把数字拆成list
     * @param num
     * @param digitalList
     */
    private fun toDigitals(num: Long, digitalList: ArrayList<Int>) {
        var num = num
        digitalList.clear()
        if (num == 0L) {
            digitalList.add(0)
        }
        while (num > 0) {
            digitalList.add(0, ((num % 10).toInt()))
            num /= 10
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            val numHeight = digitalRect.height()
            val numWidth = digitalRect.width() + PADDING_SPACE
            newList.forEachIndexed { index, l ->
                var newDigital = newList[index]
                var oldDigital = -1
                if (index < oldList.size) {
                    oldDigital = oldList[index]
                }
                val x = (numWidth * index).toFloat()
                when {
                //数位上一样的话不做动画
                    oldDigital == newDigital -> it.drawText(newDigital.toString(), paddingLeft + x, paddingTop + (numHeight + topSpace).toFloat(), paint)
                    newNum > currentNum -> {
                        drawUp(it, x, numHeight + 2 * topSpace - animatePercent * topSpace, newDigital.toString(), "in")
                        if (oldDigital != -1) {
                            drawUp(it, x, numHeight + topSpace - animatePercent * topSpace, oldDigital.toString(), "out")
                        }
                    }
                    else -> {
                        drawDown(it, x, 0 + (topSpace + numHeight) * animatePercent, newDigital.toString(), "in")
                        if (oldDigital != -1) {
                            drawDown(it, x, numHeight + topSpace + animatePercent * topSpace, oldDigital.toString(), "out")
                        }
                    }
                }
            }

        }
    }


    /**
     * 数字往下运动
     */
    private fun drawDown(canvas: Canvas, x: Float = 0f, y: Float, num: String, inOrOut: String) {

        if (inOrOut == "in") {
            //透明度从0-255；字大小从0.5 - 1； y坐标从0-topSpace
            paint.textSize = (0.5f + animatePercent * 0.5f) * textSize
            paint.alpha = (animatePercent * 255).toInt()
            canvas.drawText(num, paddingLeft + x, paddingTop + y, paint)
        } else {
            //透明度从255-0；字大小从1-0.5；y坐标从TOP_SPACE-0
            paint.textSize = (1f - 0.5f * animatePercent) * textSize
            paint.alpha = ((1 - animatePercent) * 255).toInt()
            canvas.drawText(num, paddingLeft + x, paddingTop + y, paint)
        }
        paint.alpha = 255
        paint.textSize = textSize
    }

    /**
     * 数字往上运动
     */
    private fun drawUp(canvas: Canvas, x: Float = 0f, y: Float, num: String, inOrOut: String) {

        if (inOrOut == "in") {
            paint.textSize = (0.5f + animatePercent * 0.5f) * textSize
            paint.alpha = (animatePercent * 255).toInt()
            canvas.drawText(num, paddingLeft + x, paddingTop + y, paint)
        } else {
            paint.textSize = (1f - 0.5f * animatePercent) * textSize
            paint.alpha = ((1 - animatePercent) * 255).toInt()
            canvas.drawText(num, paddingLeft + x, paddingTop + y, paint)
        }
        paint.alpha = 255
        paint.textSize = textSize
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        textRect.setEmpty()
        if (digitalRect.height() > topSpace) {
            topSpace = digitalRect.height()
        } else {
            topSpace
        }
        paint.getTextBounds(newNum.toString(), 0, newNum.toString().length, textRect)
        val width = textRect.width() + newList.size * PADDING_SPACE + paddingLeft + paddingRight
        val height = textRect.height() + 2 * topSpace + paddingTop + paddingBottom
        val w = resolveSizeAndState(width, widthMeasureSpec, 0)
        val h = resolveSizeAndState(height, heightMeasureSpec, 0)
        setMeasuredDimension(w, h)
    }

}