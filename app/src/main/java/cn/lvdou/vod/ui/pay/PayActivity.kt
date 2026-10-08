package cn.lvdou.vod.ui.pay

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import cn.lvdou.vod.ApiConfig
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.bean.UserInfoBean
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import cn.lvdou.vod.utils.UserUtils
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.BaseObserver
import com.google.android.material.tabs.TabLayout
import org.greenrobot.eventbus.Subscribe
import java.util.*

class PayActivity : BaseActivity() {

    private val iv_task_back: TextView by lazy { findViewById(R.id.iv_task_back) }
    private val rlBack: RelativeLayout by lazy { findViewById(R.id.rlBack) }
    private val tab: TabLayout by lazy { findViewById(R.id.tab) }
    private val tvAvator: ImageView by lazy { findViewById(R.id.tvAvator) }
    private val tvCoin: TextView by lazy { findViewById(R.id.tvCoin) }
    private val tvExpireTime: TextView by lazy { findViewById(R.id.tvExpireTime) }
    private val tvMessage: TextView by lazy { findViewById(R.id.tvMessage) }
    private val tvPoints: TextView by lazy { findViewById(R.id.tvPoints) }
    private val tv_task_title: TextView by lazy { findViewById(R.id.tv_task_title) }
    private val vpPay: ViewPager by lazy { findViewById(R.id.vpPay) }

    override fun getLayoutResID(): Int {
        BarUtils.setStatusBarColor(this, Color.TRANSPARENT)
        BarUtils.setStatusBarLightMode(this, true)
        return R.layout.activity_pay
    }

    override fun onResume() {
        super.onResume()
        val vodService= Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this, vodService.userInfo(), object : BaseObserver<UserInfoBean>() {
            override fun onSuccess(data: UserInfoBean) {
                UserUtils.userInfo = data
                changeUserInfo()
            }

            override fun onError(e: ResponseException) {
            }
        })
    }

    override fun initView() {
        changeUserInfo()
        val type = intent.getIntExtra("type", 0)
        if (type == 0) {
            tv_task_title.setText(R.string.pay_title)
        } else {
            tv_task_title.setText(R.string.vip_upgrade_title)
        }
        vpPay.adapter = PayFragmentPagerAdapter(supportFragmentManager)
        tab.setupWithViewPager(vpPay)
        if (type == 1) {
            vpPay.setCurrentItem(1, true)
        }
    }

    override fun initListener() {
        rlBack.setOnClickListener {
            finish()
        }
        iv_task_back.setOnClickListener {
            finish()
        }
        vpPay.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    tv_task_title.setText(R.string.pay_title)
                } else {
                    tv_task_title.setText(R.string.vip_upgrade_title)
                }
            }

        })
    }

    override fun isUseEventBus(): Boolean {
        return true
    }

    @Subscribe
    @Suppress("UNUSED_PARAMETER") //EventBus 回调签名要求该参数，方法内不使用
    fun onUserInfoChanged(userinfo: UserInfoBean? = null) {
        changeUserInfo()
    }

    @SuppressLint("SetTextI18n")
    private fun changeUserInfo() {
        UserUtils.userInfo?.let {
//            tvMessage.text = "${it.group?.group_name}：${it.user_nick_name}"
            //${it.group?.group_name}：默认会员代码
            tvMessage.text = "${it.user_name}"
            if (it.user_end_time <= 0 || Calendar.getInstance().timeInMillis/1000> it.user_end_time) {
                tvExpireTime.text = "升级会员，享超值服务"
            } else {
                tvExpireTime.text = "会员有效期至：" + TimeUtils.millis2String(it.user_end_time * 1000)
            }
            if (it.user_gold == null || it.user_gold == ""){
                it.user_gold = "0"
            }
            tvCoin.text = StringUtils.getString(R.string.remaining_coin, it.user_gold)
            tvPoints.text = StringUtils.getString(R.string.remaining_points, it.user_points.toString())
            if (it.user_portrait.isNotEmpty()) {
                Glide.with(mActivity)
                        .load(ApiConfig.MOGAI_BASE_URL + "/" + it.user_portrait)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .placeholder(tvAvator.drawable)
                        .dontAnimate()
                        .into(tvAvator)
            } else { //设置默认头像
                Glide.with(mActivity)
                        .load(R.drawable.default_avater)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .placeholder(tvAvator.drawable)
                        .dontAnimate()
                        .into(tvAvator)
            }
        }
    }

    @Suppress("DEPRECATION") //FragmentStatePagerAdapter 已弃用，保留现有双 Tab 行为
    private class PayFragmentPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return if (position == 0) {
                PayFragment()
            } else {
                VipFragment()
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if (position == 0) {
                StringUtils.getString(R.string.pay_hit)
            } else {
                StringUtils.getString(R.string.vip_pay_hit)
            }
        }
    }
}
