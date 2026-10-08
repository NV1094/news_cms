package cn.lvdou.vod.ui.notice

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.lvdou.vod.App
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.bean.MessageBean
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.DensityUtils
import cn.lvdou.vod.utils.Retrofit2Utils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.LoadingObserver
import cn.lvdou.vod.utils.MMkvUtils
import com.youxiao.ssp.ad.bean.SSPAd
import com.youxiao.ssp.ad.core.AdClient
import com.youxiao.ssp.ad.listener.AdLoadAdapter

class MessageCenterActivity : BaseActivity(), View.OnClickListener {

    private val linear_Banner: FrameLayout by lazy { findViewById(R.id.linear_Banner) }
    private val rlBack: RelativeLayout by lazy { findViewById(R.id.rlBack) }
    private val rl_msg_center: RelativeLayout by lazy { findViewById(R.id.rl_msg_center) }
    private val rl_system_notice: RelativeLayout by lazy { findViewById(R.id.rl_system_notice) }
    private val rvMsg: RecyclerView by lazy { findViewById(R.id.rvMsg) }

    private val msgAdapter by lazy {
        MsgAdapter(this@MessageCenterActivity)
    }

    override fun getLayoutResID(): Int {
        return R.layout.activity_message_center
    }

    override fun onResume() {
        super.onResume()
        getXTWAd()
    }

    //为了更好的释放资源，在页面销毁时调用release方法释放资源
    override fun onDestroy() {
        super.onDestroy()
        if (App.SdkSwitch){
            adClient?.release()
        }
    }
    /**
     * 信天翁
     * */
    private var adClient: AdClient? = AdClient(this@MessageCenterActivity)
    private fun getXTWAd() {
//        val adClient = AdClient(mActivity)
        val banner = MMkvUtils.Builds().loadStartBean("")?.ads?.sdk_banner
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
        rvMsg.layoutManager = LinearLayoutManager(mActivity)
        rvMsg.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                val paddingTop = DensityUtils.dp2px(application, 5f)
                val paddingLeft = DensityUtils.dp2px(application, 15f)
                outRect.set(paddingLeft, paddingTop, paddingLeft, paddingTop)
            }
        })
        rvMsg.adapter = msgAdapter
        rlBack.setOnClickListener(this)
        rl_msg_center.setOnClickListener(this)
        rl_system_notice.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        getMsgList()
    }

    private fun getMsgList() {
        var vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(mActivity, vodService.getMsgList(),
                object : LoadingObserver<MessageBean>(mActivity) {
                    override fun onSuccess(data: MessageBean) {
                        val list = data.list
                        msgAdapter.setNewData(list)
                    }

                    override fun onError(e: ResponseException) {
                    }

                })
    }


    override fun onClick(v: View?) {
        when (v) {
            rlBack -> {
                finish()
            }
            rl_msg_center -> {

            }
            rl_system_notice -> {
                ToastUtils.showShort("暂无系统公告")
            }
        }
    }

    class MsgAdapter(var activity: Activity) : BaseQuickAdapter<MessageBean.ListBean, BaseViewHolder>(R.layout.item_msg_list) {
        override fun convert(helper: BaseViewHolder, item: MessageBean.ListBean?) {
            item?.run {
                helper.setText(R.id.tv_title,this.title )
                helper.setText(R.id.tv_desc, this.content)
                helper.setText(R.id.tv_time, this.create_date)
                helper.getView<ConstraintLayout>(R.id.total_view).setOnClickListener {
                    MessageDetailActivity.start(activity,this.id.toString())
                }
            }
        }

    }


}
