package cn.lvdou.vod.ui.pay

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cn.lvdou.vod.R
import cn.lvdou.vod.bean.*
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.ui.expand.ExpandCenterActivity
import cn.lvdou.vod.ui.widget.HitDialog
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import cn.lvdou.vod.utils.UserUtils
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.BaseFragment
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.LoadingObserver
import org.greenrobot.eventbus.EventBus

class VipFragment : BaseFragment() {

    private val proxy_view: View by lazy { requireView().findViewById(R.id.proxy_view) }
    private val rlDay: LinearLayout by lazy { requireView().findViewById(R.id.rlDay) }
    private val rlMonth: LinearLayout by lazy { requireView().findViewById(R.id.rlMonth) }
    private val rlProxy: LinearLayout by lazy { requireView().findViewById(R.id.rlProxy) }
    private val rlPublic: LinearLayout by lazy { requireView().findViewById(R.id.rlPublic) }
    private val rlWeek: LinearLayout by lazy { requireView().findViewById(R.id.rlWeek) }
    private val rlYear: LinearLayout by lazy { requireView().findViewById(R.id.rlYear) }
    private val tvProxyPoints: TextView by lazy { requireView().findViewById(R.id.tvProxyPoints) }
    private val tv_score_day: TextView by lazy { requireView().findViewById(R.id.tv_score_day) }
    private val tv_score_month: TextView by lazy { requireView().findViewById(R.id.tv_score_month) }
    private val tv_score_week: TextView by lazy { requireView().findViewById(R.id.tv_score_week) }
    private val tv_score_year: TextView by lazy { requireView().findViewById(R.id.tv_score_year) }

    override fun getLayoutId(): Int {
        return R.layout.fragment_vip
    }

    override fun initListener() {
        super.initListener()
        rlDay.setOnClickListener {
            upgrade("day", String.format(getString(R.string.day_upgrade_hit_s), tv_score_day.text))
        }
        rlWeek.setOnClickListener {
            upgrade("week", String.format(getString(R.string.week_upgrade_hit_s), tv_score_week.text))
        }
        rlMonth.setOnClickListener {
            upgrade("month", String.format(getString(R.string.month_upgrade_hit_s), tv_score_month.text))
        }
        rlYear.setOnClickListener {
            upgrade("year", String.format(getString(R.string.year_upgrade_hit_s), tv_score_year.text))
        }
        rlProxy.setOnClickListener {
            changeAgents()
        }
        rlPublic.setOnClickListener {
            val intent = Intent(context, ExpandCenterActivity::class.java)
            ActivityUtils.startActivity(intent)
        }
    }

    override fun initLoad() {
        super.initLoad()
        getScoreList()
        getAgentsScore()
    }

    private fun getScoreList() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this@VipFragment, vodService.getScoreList(),
                object : LoadingObserver<ScoreListBean>(mActivity) {
                    override fun onSuccess(data: ScoreListBean) {
                        data.list?.`_$3`?.let {
                            val day = data.list.`_$3`.group_points_day
                            val week = data.list.`_$3`.group_points_week
                            val month = data.list.`_$3`.group_points_month
                            val year = data.list.`_$3`.group_points_year
                            tv_score_day.text = "${day}积分"
                            tv_score_week.text = "${week}积分"
                            tv_score_month.text = "${month}积分"
                            tv_score_year.text = "${year}积分"
                        }
                    }

                    override fun onError(e: ResponseException) {
                    }

                })
    }


    private fun upgrade(price: String, hitMsg: String) {
        Log.d("兑换会员参数", "upgrade: " + price +"参数2" + hitMsg)
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        HitDialog(mActivity)
                .setMessage(hitMsg)
                .setOnHitDialogClickListener(object : HitDialog.OnHitDialogClickListener() {
                    override fun onOkClick(dialog: HitDialog) {
                        super.onOkClick(dialog)
                        RequestManager.execute(this@VipFragment, vodService.upgradeGroup(price, UserUtils.userInfo?.group_id.toString()),
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

    private fun getAgentsScore() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this@VipFragment, vodService.getAgentsScore(),
                object : LoadingObserver<AgentsScoreBean>(mActivity) {
                    override fun onSuccess(data: AgentsScoreBean) {
                        rlProxy.visibility = View.VISIBLE
                        proxy_view.visibility = View.VISIBLE
                        tvProxyPoints.text = "${data.score}积分"
                    }

                    override fun onError(e: ResponseException) {
                    }

                })
    }

    private fun changeAgents() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this@VipFragment, vodService.changeAgents(),
                object : LoadingObserver<ChangeAgentsBean>(mActivity) {
                    override fun onSuccess(data: ChangeAgentsBean) {
                        ToastUtils.showShort(data.msg)
                        EventBus.getDefault().post(LoginBean())
                    }

                    override fun onError(e: ResponseException) {
                    }

                })
    }

}
