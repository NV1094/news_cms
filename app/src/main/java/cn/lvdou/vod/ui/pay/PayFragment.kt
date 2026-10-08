package cn.lvdou.vod.ui.pay

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import cn.lvdou.vod.ApiConfig
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseFragment
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.observer.BaseObserver
import cn.lvdou.vod.base.observer.LoadingObserver
import cn.lvdou.vod.bean.*
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.ui.browser.BrowserActivity
import cn.lvdou.vod.ui.widget.HitDialog
import cn.lvdou.vod.ui.widget.PayDialog
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.MMkvUtils
import cn.lvdou.vod.utils.Retrofit2Utils
import cn.lvdou.vod.utils.UserUtils
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.utils.MMkvUtils.Companion.Builds
import com.github.StormWyrm.wanandroid.base.sheduler.IoMainScheduler
import jaygoo.library.m3u8downloader.M3U8Library
import org.greenrobot.eventbus.EventBus
import java.text.DecimalFormat
import java.util.*


class PayFragment : BaseFragment() {

    private val Rechargeentry: TextView by lazy { requireView().findViewById(R.id.Rechargeentry) }
    private val etCardPassword: EditText by lazy { requireView().findViewById(R.id.etCardPassword) }
    private val etMoney: EditText by lazy { requireView().findViewById(R.id.etMoney) }
    private val iv_play_km: LinearLayout by lazy { requireView().findViewById(R.id.iv_play_km) }
    private val iv_play_s: LinearLayout by lazy { requireView().findViewById(R.id.iv_play_s) }
    private val month_money: TextView by lazy { requireView().findViewById(R.id.month_money) }
    private val month_price: LinearLayout by lazy { requireView().findViewById(R.id.month_price) }
    private val play_title: LinearLayout by lazy { requireView().findViewById(R.id.play_title) }
    private val play_title1: LinearLayout by lazy { requireView().findViewById(R.id.play_title1) }
    private val tvCard: TextView by lazy { requireView().findViewById(R.id.tvCard) }
    private val tvHit: TextView by lazy { requireView().findViewById(R.id.tvHit) }
    private val tvMoney: TextView by lazy { requireView().findViewById(R.id.tvMoney) }
    private val tvOnlinePay: TextView by lazy { requireView().findViewById(R.id.tvOnlinePay) }
    private val vip_tv_month_message: TextView by lazy { requireView().findViewById(R.id.vip_tv_month_message) }
    private val vip_tv_week_message: TextView by lazy { requireView().findViewById(R.id.vip_tv_week_message) }
    private val vip_tv_year_message: TextView by lazy { requireView().findViewById(R.id.vip_tv_year_message) }
    private val week_money: TextView by lazy { requireView().findViewById(R.id.week_money) }
    private val week_price: LinearLayout by lazy { requireView().findViewById(R.id.week_price) }
    private val year_money: TextView by lazy { requireView().findViewById(R.id.year_money) }
    private val year_price: LinearLayout by lazy { requireView().findViewById(R.id.year_price) }

    private var orderCode: String? = null
    private var packaged: String = ""
    private var tv_user_t1_type = 0
    override fun getLayoutId(): Int {
        return R.layout.fragment_pay
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initListener() {
        super.initListener()
        tvMoney.setOnClickListener {
            pointPurchase(etMoney.text.trim().toString())
        }
        tvCard.setOnClickListener {
            cardBuy()
        }
        week_price.setOnClickListener {
            week_price.background = ContextCompat.getDrawable(M3U8Library.context, R.drawable.bg_recharge_amount3)
            month_price.background = ContextCompat.getDrawable(M3U8Library.context, R.drawable.bg_recharge_amount2)
            year_price.background = ContextCompat.getDrawable(M3U8Library.context, R.drawable.bg_recharge_amount2)
            packaged = "week"
            pointPurchase(week_money.text as String)
        }
        month_price.setOnClickListener {
            week_price.background = ContextCompat.getDrawable(M3U8Library.context, R.drawable.bg_recharge_amount2)
            month_price.background = ContextCompat.getDrawable(M3U8Library.context, R.drawable.bg_recharge_amount3)
            year_price.background = ContextCompat.getDrawable(M3U8Library.context, R.drawable.bg_recharge_amount2)
            packaged = "month"
            pointPurchase(month_money.text as String)
        }
        year_price.setOnClickListener {
            week_price.background = ContextCompat.getDrawable(M3U8Library.context, R.drawable.bg_recharge_amount2)
            month_price.background = ContextCompat.getDrawable(M3U8Library.context, R.drawable.bg_recharge_amount2)
            year_price.background = ContextCompat.getDrawable(M3U8Library.context, R.drawable.bg_recharge_amount3)
            packaged = "year"
            pointPurchase(year_money.text as String)
        }
    }

    override fun initLoad() {
        super.initLoad()
        getPayTip()
        //Kotlin requireNotNull：返回非空 StartBean（null 时与原 Objects.requireNonNull 一致抛异常）
        val startBean = requireNotNull(Builds().loadStartBean(""))
        if (startBean.online_recharge == null || startBean.online_recharge == "0") { //关闭在线充值
            iv_play_s.visibility = View.GONE
        } else {
            iv_play_s.visibility = View.VISIBLE
        }

        if (startBean.vip_purchase == null || startBean.vip_purchase == "0") { //关闭套餐购买
            play_title.visibility = View.GONE
            play_title1.visibility = View.GONE
        } else {
            play_title1.visibility = View.VISIBLE
            play_title.visibility = View.VISIBLE
            getScoreList()
        }

        val ad = Builds().loadStartBean("")?.ads?.rechargeentry
        if (!ad?.description.isNullOrEmpty()) {
            Rechargeentry.text = ad?.description
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PAY) {
            checkOrder()
        }
    }

    private fun getPayTip() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        if (AgainstCheatUtil.showWarn(vodService)) {
            return
        }
        vodService.payTip()
                .compose(IoMainScheduler())
                .subscribe(object : BaseObserver<BaseResult<PayTipBean>>() {
                    override fun onSuccess(data: BaseResult<PayTipBean>) {
                        if (data.isSuccessful) {
                            if (data.data.url == null || data.data.url == "") {
                                iv_play_km.visibility = View.GONE
                            } else {
                                iv_play_km.visibility = View.VISIBLE
                            }
                            Log.d("TAGdata.data.url", "onSuccess: " + data.data.url)
                            tvHit.text = data.msg
                            tvOnlinePay.setOnClickListener {
                                ActivityUtils.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.data.url)))
                            }
                        }
                    }

                    override fun onError(e: ResponseException) {
                        iv_play_km.visibility = View.GONE
                    }

                })

    }

    private fun pointPurchase(money: String) {
        if (TextUtils.isEmpty(money)) {
            ToastUtils.showShort("未获取到金额")
            return
        }
        try {
            money.toInt()
        } catch (e: Exception) {
            ToastUtils.showShort("输入金额必须为数字")
            return
        }
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        if (AgainstCheatUtil.showWarn(vodService)) {
            return
        }
        RequestManager.execute(this, vodService.pointPurchase(money), object : LoadingObserver<PointPurchseBean>(mActivity) {
            override fun onSuccess(data: PointPurchseBean) {
                orderCode = data.order_code//记录order_code
                PayDialog(mActivity, data)
                        .setOnPayDialogClickListener(object : PayDialog.OnPayDialogClickListener() {
                            override fun onPayTypeClick(dialog: PayDialog, payment: String) {
                                super.onPayTypeClick(dialog, payment)
                                val payUrl = "${ApiConfig.MOGAI_BASE_URL + ApiConfig.PAY}?payment=${payment}&order_code=${data.order_code}"
                                Intent(context, BrowserActivity::class.java).apply {
                                    putExtra("url", payUrl)
                                    startActivityForResult(this, REQUEST_PAY)
                                }
                            }
                        })
                        .show()
            }

            override fun onError(e: ResponseException) {
            }

        })
    }

    private fun getScoreList() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        if (AgainstCheatUtil.showWarn(vodService)) {
            return
        }
        RequestManager.execute(this@PayFragment, vodService.getScoreList(),
                object : LoadingObserver<ScoreListBean>(mActivity) {
                    @SuppressLint("SetTextI18n")
                    override fun onSuccess(data: ScoreListBean) {
                        data.list?.`_$3`?.let {
                            val week = data.list.`_$3`.group_points_week / 10
                            val month = data.list.`_$3`.group_points_month / 10
                            val year = data.list.`_$3`.group_points_year / 10

                            vip_tv_week_message.text = "低至" + DecimalFormat("0.00").format((week.toFloat() / 7.toFloat()).toDouble()) + "元/天"
                            vip_tv_month_message.text = "低至" + DecimalFormat("0.00").format((month.toFloat() / 30.toFloat()).toDouble()) + "元/天"
                            vip_tv_year_message.text = "低至" + DecimalFormat("0.00").format((year.toFloat() / 360.toFloat()).toDouble()) + "元/天"

                            week_money.text = "$week"
                            month_money.text = "$month"
                            year_money.text = "$year"
                        }
                    }

                    override fun onError(e: ResponseException) {
                    }

                })
    }

    private fun checkOrder() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        if (AgainstCheatUtil.showWarn(vodService)) {
            return
        }
        orderCode?.let {
            RequestManager.execute(
                    this@PayFragment,
                    vodService.order(it),
                    object : BaseObserver<OrderBean>() {
                        override fun onSuccess(data: OrderBean) {
                            orderCode = null //更新状态后 重置ordercode
                            if (data.order_status == 0) {
                                ToastUtils.showShort("未支付")
                            } else {
                                EventBus.getDefault().post(LoginBean())
                                if (packaged == "") {
                                    ToastUtils.showShort("支付成功")
                                } else {
                                    upgrade(packaged, "支付成功！是否兑换会员时间？")
                                }

                            }
                        }

                        override fun onError(e: ResponseException) {
                            orderCode = null
                        }

                    }

            )
        }

    }

    private fun cardBuy() {
        val card = etCardPassword.text.trim().toString()
        if (TextUtils.isEmpty(card)) {
            ToastUtils.showShort("卡密不能为空")
            return
        }
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        if (AgainstCheatUtil.showWarn(vodService)) {
            return
        }
        RequestManager.execute(this, vodService.cardBuy(card), object : LoadingObserver<CardBuyBean>(mActivity) {
            override fun onSuccess(data: CardBuyBean) {
                ToastUtils.showShort(data.msg)
                EventBus.getDefault().post(LoginBean())
            }

            override fun onError(e: ResponseException) {
            }

        })
    }

    companion object {
        const val REQUEST_PAY = 0
    }

    private fun upgrade(price: String, hitMsg: String) {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        if (AgainstCheatUtil.showWarn(vodService)) {
            return
        }
        HitDialog(mActivity)
                .setMessage(hitMsg)
                .setOnHitDialogClickListener(object : HitDialog.OnHitDialogClickListener() {
                    override fun onOkClick(dialog: HitDialog) {
                        super.onOkClick(dialog)
                        RequestManager.execute(this@PayFragment, vodService.upgradeGroup(price, UserUtils.userInfo?.group_id.toString()),
                                object : LoadingObserver<CardBuyBean>(mActivity) {
                                    override fun onSuccess(data: CardBuyBean) {
                                        ToastUtils.showShort(data.msg)
                                        EventBus.getDefault().post(LoginBean())
                                    }

                                    override fun onError(e: ResponseException) {
                                    }

                                })
                    }
                })
                .show()
    }

}
