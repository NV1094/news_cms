package cn.lvdou.vod.horizontal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.blankj.utilcode.util.BarUtils.getStatusBarHeight

/**
 *@author : yjz
 *@des : 状态栏
 */
class StatusBarView : View {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
                ImageView.getDefaultSize(0, widthMeasureSpec),
                ImageView.getDefaultSize(0, heightMeasureSpec)
        )
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getStatusBarHeight(), MeasureSpec.EXACTLY)
        )
    }
}

