package cn.lvdou.vod.ui.withdraw

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.bean.*
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import cn.lvdou.vod.utils.UserUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.BaseObserver
import cn.lvdou.vod.base.observer.LoadingObserver
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class GoldWithdrawActivity : BaseActivity() {

    private val etAccount: EditText by lazy { findViewById(R.id.etAccount) }
    private val etMoney: EditText by lazy { findViewById(R.id.etMoney) }
    private val etName: EditText by lazy { findViewById(R.id.etName) }
    private val refreshLayout: SmartRefreshLayout by lazy { findViewById(R.id.refreshLayout) }
    private val rlBack: RelativeLayout by lazy { findViewById(R.id.rlBack) }
    private val rvRecord: RecyclerView by lazy { findViewById(R.id.rvRecord) }
    private val spinner: Spinner by lazy { findViewById(R.id.spinner) }
    private val tvCoin: TextView by lazy { findViewById(R.id.tvCoin) }
    private val tvFinish: TextView by lazy { findViewById(R.id.tvFinish) }
    private val tvPoints: TextView by lazy { findViewById(R.id.tvPoints) }
    private val tvWithdrawHit: TextView by lazy { findViewById(R.id.tvWithdrawHit) }

    private val array = arrayListOf("支付宝", "微信")
    private var curPayType = 1// 1 支付宝 2微信
    private var curRecordIndex = 1
    private val recordAdapter: RecordAdapter by lazy {
        RecordAdapter()
    }

    override fun getLayoutResID(): Int {
        return R.layout.activity_coin_withdraw
    }

    override fun initView() {
        super.initView()
        onUserInfoChanged()

        rvRecord.layoutManager = LinearLayoutManager(mActivity)
        rvRecord.adapter = recordAdapter
        refreshLayout.setEnableRefresh(false)
        refreshLayout.setRefreshFooter(ClassicsFooter(mActivity))

        val adapter = ArrayAdapter<String>(mActivity, android.R.layout.simple_dropdown_item_1line, array)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                curPayType = position + 1
            }

        }
        spinner.setSelection(0)
    }

    override fun initListener() {
        super.initListener()
        rlBack.setOnClickListener {
            finish()
        }
        tvFinish.setOnClickListener {
            withdraw()
        }
        refreshLayout.setOnLoadMoreListener {
            curRecordIndex++
            getRecordData()
        }
    }

    override fun initData() {
        super.initData()
        getGlodTip()
        getRecordData()
    }

    override fun isUseEventBus(): Boolean {
        return true
    }

    @Subscribe
    fun onUserInfoChanged(userinfo: UserInfoBean? = null) {
        UserUtils.userInfo?.let {
            tvPoints.text = it.user_points.toString()
            tvCoin.text = it.user_gold
        }
    }

    private fun getGlodTip() {
        var vodService= Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this, vodService.goldTip(),
                object : LoadingObserver<GoldTipBean>(mActivity) {
                    override fun onSuccess(data: GoldTipBean) {
                        tvWithdrawHit.text = data.info
                    }

                    override fun onError(e: ResponseException) {

                    }

                })
    }

    private fun refreshRecordData() {
        curPayType = 1
        recordAdapter.setNewData(null)
        getRecordData()
    }

    private fun getRecordData() {
        var vodService=Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this, vodService.getGoldWithdrawRecord(curRecordIndex.toString(), 10.toString()),
                object : BaseObserver<Page<GoldWithdrawRecordBean>>() {
                    override fun onSuccess(data: Page<GoldWithdrawRecordBean>) {
                        recordAdapter.addData(data.list)
                        if (curRecordIndex > 1) {
                            if (data.list.isEmpty()) {
                                refreshLayout.finishLoadMoreWithNoMoreData()
                            } else {
                                refreshLayout.finishLoadMore(true)
                            }
                        }
                    }

                    override fun onError(e: ResponseException) {
                        if (curRecordIndex > 1) {
                            refreshLayout.finishLoadMore(false)
                        }
                    }

                })

    }

    private fun withdraw() {
        val accout = etAccount.text.trim().toString()
        val name = etName.text.trim().toString()
        val money = etMoney.text.trim().toString()
        if (accout.isNullOrEmpty()) {
            ToastUtils.showShort("收款账号不能为空！")
            return
        }

        if (name.isNullOrEmpty()) {
            ToastUtils.showShort("收款姓名不能为空！")
            return
        }

        if (money.isNullOrEmpty()) {
            ToastUtils.showShort("提现金额不能为空！")
            return
        }
        var vodService=Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this, vodService.goldWithdrawApply(money, curPayType.toString(), accout, name),
                object : LoadingObserver<GoldWithdrawBean>(mActivity) {
                    override fun onSuccess(data: GoldWithdrawBean) {
                        ToastUtils.showShort(data.info)
                        EventBus.getDefault().post(LoginBean())
                        refreshRecordData()
                    }

                    override fun onError(e: ResponseException) {
                    }

                })
    }



    private class RecordAdapter : BaseQuickAdapter<GoldWithdrawRecordBean, BaseViewHolder>(R.layout.item_withdraw) {

        override fun convert(helper: BaseViewHolder, item: GoldWithdrawRecordBean?) {
            item?.let {
                helper.setText(R.id.tvPoints, it.gold_num.toString())
                helper.setText(R.id.tvMoney, it.num)
                var status = when (it.status) {
                    0 -> "提现中"
                    1 -> "提现成功"
                    2 -> "提现失败"
                    else -> "未知"
                }
                helper.setText(R.id.tvStatus, status)
                helper.setText(R.id.tvTime, TimeUtils.millis2String(it.created_time*1000))
            }

        }

    }
}
