package cn.lvdou.vod.ui.play

import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import com.blankj.utilcode.util.BarUtils

class CastScreenActivity : BaseActivity() {
    override fun getLayoutResID(): Int {
        BarUtils.setStatusBarVisibility(this, false)
        return R.layout.activity_cast_screen
    }
}