package com.zhj.countview

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout

/**
 * Description:
 * 借鉴了https://github.com/rengwuxian/IconCountView
 * Created by zhonghaojie on 16/10/2017.
 */

class IconCountView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private var isPraise: Boolean = false
    private val imgIcon: ImageView
    private val countView: CountView
    private var unPraiseRes: Int = 0
    private var praisedRes: Int = 0
    private val praiseStateChanged: OnPraiseStateChanged? = null

    init {

        val view = LayoutInflater.from(context).inflate(R.layout.layout_praise, this)
        countView = view.findViewById(R.id.count_view)
        imgIcon = view.findViewById(R.id.img_praise)

        val a = context.obtainStyledAttributes(attrs, R.styleable.IconCountView, defStyleAttr, 0)
        val isPraised = a.getBoolean(R.styleable.IconCountView_state, false)
        val count = a.getInt(R.styleable.IconCountView_count, 0).toLong()
        val normalRes = a.getResourceId(R.styleable.IconCountView_normalRes, R.drawable.icon_praise_normal)
        val selectedRes = a.getResourceId(R.styleable.IconCountView_selectedRes, R.drawable.icon_praise_selected)
        setCount(count)
        setIconRes(normalRes, selectedRes)
        setPraised(isPraised)
        a.recycle()

        view.setOnClickListener { praiseChange(!isPraise) }
    }

    fun setIconRes(normalRes: Int, selectedRes: Int) {
        unPraiseRes = normalRes
        praisedRes = selectedRes
        imgIcon.setImageResource(unPraiseRes)
    }

    fun setCount(count: Long) {
        countView.setNum(count)
    }

    fun setPraised(isPraised: Boolean) {
        isPraise = isPraised
        imgIcon.setImageResource(if (isPraise) praisedRes else unPraiseRes)
    }

    private fun praiseChange(isPraised: Boolean) {
        isPraise = isPraised
        //icon变化
        imgIcon.setImageResource(if (isPraised) praisedRes else unPraiseRes)
        animImageView(isPraised)
        //数字变化
        if (isPraised) {
            countView.addNum()
        } else {
            countView.minusNum()
        }
        //接口回调
        praiseStateChanged?.praise(isPraise)
    }

    /**
     * 点赞icon动画
     * @param isPraised
     */
    private fun animImageView(isPraised: Boolean) {
        //图片动画
        val toScale = if (isPraised) 1.2f else 0.9f
        val propertyValuesHolderX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, toScale, 1.0f)
        val propertyValuesHolderY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, toScale, 1.0f)
        val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(imgIcon, propertyValuesHolderX, propertyValuesHolderY)
        objectAnimator.start()
    }

    /**
     * 点赞状态改变监听
     */
    internal interface OnPraiseStateChanged {
        fun praise(isPraised: Boolean)
    }
}
