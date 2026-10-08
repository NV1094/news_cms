package cn.lvdou.vod.ui.lvdou

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import cn.lvdou.vod.App
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.ui.login.LoginActivity
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import cn.lvdou.vod.utils.UserUtils
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.BaseObserver
import cn.lvdou.vod.utils.MMkvUtils
import cn.lvdou.vod.utils.MMkvUtils.Companion.Builds
import com.blankj.utilcode.util.*
import com.youxiao.ssp.ad.bean.SSPAd
import com.youxiao.ssp.ad.core.AdClient
import com.youxiao.ssp.ad.listener.AdLoadAdapter

class Dustomapi : BaseActivity() {

    private val etComment: EditText by lazy { findViewById(R.id.etComment) }
    private val gegnxing: TextView by lazy { findViewById(R.id.gegnxing) }
    private val jianyi: TextView by lazy { findViewById(R.id.jianyi) }
    private val linear_Banner: FrameLayout by lazy { findViewById(R.id.linear_Banner) }
    private val qiupian: TextView by lazy { findViewById(R.id.qiupian) }
    private val rlBack: RelativeLayout by lazy { findViewById(R.id.rlBack) }
    private val tvSubmit: TextView by lazy { findViewById(R.id.tvSubmit) }

    private var commitMsg = ""
    private var isHave = false

    companion object {
        fun start(context: Context, message: String) {
            val intent = Intent(context, Dustomapi::class.java)
            intent.putExtra("msg", message)
            intent.putExtra("isHave", true)
            context.startActivity(intent)
        }

        @JvmStatic
        fun starts(context: Context, message: String) {
            val intent = Intent(context, Dustomapi::class.java)
            intent.putExtra("msg", message)
            intent.putExtra("isHave", true)
            context.startActivity(intent)
        }
    }

    override fun getLayoutResID(): Int {
        return R.layout.layout_dusimapi
    }

    override fun onResume() {
        super.onResume()
        getxtwAd()
    }

    private var adClient: AdClient? = AdClient(this@Dustomapi)
    override fun onDestroy() {
        super.onDestroy()
        if (App.SdkSwitch){
            adClient?.release()
        }
    }
    /**
     * 信天翁
     * */

    private fun getxtwAd() {
//        val adClient = AdClient(mActivity)
        val banner = Builds().loadStartBean("")?.ads?.sdk_banner
        if (App.SdkSwitch && banner != null && banner.description != null && banner.status == 1){
            adClient?.requestBannerAd(linear_Banner, banner.description, object : AdLoadAdapter() {
                override fun onAdLoad(ad: SSPAd) {
                    super.onAdLoad(ad)
                    linear_Banner.visibility = View.VISIBLE
                }

                override fun onError(p0: Int, p1: String?) {
                    linear_Banner.visibility = View.GONE
                }
            })
        }
    }

    override fun initView() {
        super.initView()
        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.black))
        isHave = intent.getBooleanExtra("isHave", false)
        if (isHave) {
            commitMsg = intent.getStringExtra("msg") ?: ""
            commitMsg.let {
                etComment.setText(commitMsg)
            }
        }else{
            etComment.setText(Builds().loadJieXiApi())
            etComment.requestFocus();
            etComment.setSelection(etComment.text.length);
        }
        //留言求片
        qiupian.setOnClickListener{
            etComment.setText("")
            qiupian.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp)
            jianyi.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp_s)
            gegnxing.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp_s)
            etComment.requestFocus();
            etComment.setSelection(etComment.text.length)
            ToastUtils.showShort("开发中")
        }
        //功能建议
        jianyi.setOnClickListener{
            etComment.setText("")
            jianyi.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp)
            qiupian.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp_s)
            gegnxing.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp_s)
            etComment.requestFocus();
            etComment.setSelection(etComment.text.length)
            ToastUtils.showShort("开发中")
        }
        //提醒更新
        gegnxing.setOnClickListener{
            etComment.setText("")
            gegnxing.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp)
            qiupian.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp_s)
            jianyi.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp_s)
            etComment.requestFocus()
            etComment.setSelection(etComment.text.length)
            ToastUtils.showShort("开发中")
        }

        tvSubmit.setOnClickListener {
            val comment = etComment.text.trim().toString()
            feedback(comment)
        }
    }

    override fun initData() {
        super.initData()
    }

    override fun initListener() {
        super.initListener()
        rlBack.setOnClickListener {
            finish()
        }
    }

    private fun feedback(commentContent: String) {
        ToastUtils.showShort("保存成功")
        Builds().saveJieXiApi(commentContent)
    }
}
