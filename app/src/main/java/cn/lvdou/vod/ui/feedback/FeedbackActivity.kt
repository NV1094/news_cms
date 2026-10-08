package cn.lvdou.vod.ui.feedback

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.lvdou.vod.App
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.bean.FeedbackBean
import cn.lvdou.vod.bean.Page
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.ui.login.LoginActivity
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import cn.lvdou.vod.utils.UserUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.BaseObserver
import cn.lvdou.vod.utils.MMkvUtils
import com.blankj.utilcode.util.*
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.youxiao.ssp.ad.bean.SSPAd
import com.youxiao.ssp.ad.core.AdClient
import com.youxiao.ssp.ad.listener.AdLoadAdapter
import com.scwang.smartrefresh.layout.SmartRefreshLayout

class FeedbackActivity : BaseActivity() {
    private val rlBack: RelativeLayout by lazy { findViewById(R.id.rlBack) }
    private val linear_Banner: FrameLayout by lazy { findViewById(R.id.linear_Banner) }
    private val etComment: EditText by lazy { findViewById(R.id.etComment) }
    private val qiupian: TextView by lazy { findViewById(R.id.qiupian) }
    private val jianyi: TextView by lazy { findViewById(R.id.jianyi) }
    private val gegnxing: TextView by lazy { findViewById(R.id.gegnxing) }
    private val tvSubmit: TextView by lazy { findViewById(R.id.tvSubmit) }
    private val refreshLayout: SmartRefreshLayout by lazy { findViewById(R.id.refreshLayout) }
    private val rvFeedback: RecyclerView by lazy { findViewById(R.id.rvFeedback) }

    private var curFeedbackPage = 1

    private var commitMsg = ""
    private var isHave = false

    companion object {
        fun start(context: Context, message: String) {
            val intent = Intent(context, FeedbackActivity::class.java)
            intent.putExtra("msg", message)
            intent.putExtra("isHave", true)
            context.startActivity(intent)
        }

        @JvmStatic
        fun starts(context: Context, message: String) {
            val intent = Intent(context, FeedbackActivity::class.java)
            intent.putExtra("msg", message)
            intent.putExtra("isHave", true)
            context.startActivity(intent)
        }
    }

    private val feedbackAdapter: FeedbackAdapter by lazy {
        FeedbackAdapter()
    }

    override fun getLayoutResID(): Int {
        return R.layout.activity_feedback
    }

    override fun onResume() {
        super.onResume()
//        getxtwAd()
    }

    /**
     * 信天翁
     * */

    private fun getxtwAd() {
        val adClient = AdClient(mActivity)
        val banner = MMkvUtils.Builds().loadStartBean("")?.ads?.sdk_banner
        if (App.SdkSwitch && banner != null && banner.description != null && banner.status == 1){
            adClient.requestBannerAd(linear_Banner, banner.description, object : AdLoadAdapter() {
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
            etComment.setText(commitMsg)
        }
        //留言求片
        qiupian.setOnClickListener{
            etComment.setText("【请添加影视】：")
            qiupian.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp)
            jianyi.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp_s)
            gegnxing.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp_s)
            etComment.requestFocus();
            etComment.setSelection(etComment.text.length);
        }
        //功能建议
        jianyi.setOnClickListener{
            etComment.setText("【功能建议】：")
            jianyi.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp)
            qiupian.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp_s)
            gegnxing.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp_s)
            etComment.requestFocus();
            etComment.setSelection(etComment.text.length);
        }
        //提醒更新
        gegnxing.setOnClickListener{
            etComment.setText("【请更新影视】：")
            gegnxing.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp)
            qiupian.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp_s)
            jianyi.setBackgroundResource(R.drawable.shape_bg_blue_radius_25dp_s)
            etComment.requestFocus();
            etComment.setSelection(etComment.text.length);
        }

        tvSubmit.setOnClickListener {
            val comment = etComment.text.trim().toString()
            if (comment.isEmpty()) {
                ToastUtils.showShort("反馈内容不能为空")
            } else {
                if (UserUtils.isLogin()) {
                    feedback(comment)
                } else {
                    ActivityUtils.startActivity(LoginActivity::class.java)
                }
            }
        }

        refreshLayout.setEnableRefresh(false)
        refreshLayout.setRefreshFooter(ClassicsFooter(mActivity))
        rvFeedback.layoutManager = LinearLayoutManager(mActivity)
        rvFeedback.adapter = feedbackAdapter
    }

    override fun initData() {
        super.initData()
        getFeedbackList()
    }

    override fun initListener() {
        super.initListener()
        refreshLayout.setOnLoadMoreListener {
            curFeedbackPage++
            getFeedbackList()
        }
        rlBack.setOnClickListener {
            finish()
        }
    }

    private fun feedback(commentContent: String) {
        var vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this,
                vodService.feedbak(commentContent),
                object : BaseObserver<String>() {
                    override fun onSuccess(data: String) {
                        ToastUtils.showShort("反馈成功")
                        curFeedbackPage = 1
                        getFeedbackList(true)
                    }

                    override fun onError(e: ResponseException) {

                    }

                })
    }


    private fun getFeedbackList(isFresh: Boolean = false) {
        var vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this,
                vodService.getFeedbackList(curFeedbackPage.toString(), 10.toString()),
                object : BaseObserver<Page<FeedbackBean>>() {
                    override fun onSuccess(data: Page<FeedbackBean>) {
                        if (curFeedbackPage == 1) {
                            if (isFresh)
                                feedbackAdapter.setNewData(data.list)
                            else
                                feedbackAdapter.addData(data.list)
                        }

                        if (curFeedbackPage > 1) {
                            feedbackAdapter.addData(data.list)
                            if (data.list.isEmpty()) {
                                refreshLayout.finishLoadMoreWithNoMoreData()
                            } else {
                                refreshLayout.finishLoadMore(true)
                            }
                        }
                    }

                    override fun onError(e: ResponseException) {
                        if (curFeedbackPage > 1) {
                            refreshLayout.finishLoadMore(false)
                        }
                    }

                })
    }

    private class FeedbackAdapter : BaseQuickAdapter<FeedbackBean, BaseViewHolder>(R.layout.item_feedback) {
        override fun convert(helper: BaseViewHolder, item: FeedbackBean?) {
            helper.let {
                item?.run {
                    it.setText(R.id.tvUser, gbook_name)
                    it.setText(R.id.tvTime, TimeUtils.millis2String(gbook_time * 1000))
                    it.setText(R.id.tvComment, gbook_content)
                    if (gbook_reply.isNotEmpty()) {
                        it.setGone(R.id.llReplay, true)
                        it.setText(R.id.tvReplay, gbook_reply)
                    } else {
                        it.setGone(R.id.llReplay, false)
                    }
                }
            }
        }

    }

}
