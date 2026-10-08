package cn.lvdou.vod.ui.expand

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.bean.MyExpand
import cn.lvdou.vod.netservice.VodService
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.LoadingObserver
import cn.lvdou.vod.ui.share.ShareActivity
import cn.lvdou.vod.utils.*
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.scwang.smartrefresh.layout.SmartRefreshLayout

class MyExpandActivity : BaseActivity(), View.OnClickListener, OnLoadMoreListener, OnRefreshListener {
    private val refreshLayout: SmartRefreshLayout by lazy { findViewById(R.id.refreshLayout) }
    private val rvExpand: RecyclerView by lazy { findViewById(R.id.rvExpand) }
    private val rlBack: RelativeLayout by lazy { findViewById(R.id.rlBack) }
    private val rl_share: RelativeLayout by lazy { findViewById(R.id.rl_share) }
    private val tv_total: TextView by lazy { findViewById(R.id.tv_total) }

    private var mPage = 1;
    private var isRefresh = true
    private var mDataList = ArrayList<MyExpand.ListBean>()
    private val msgAdapter by lazy {
        ExpandAdapter(this@MyExpandActivity)
    }

    override fun getLayoutResID(): Int {
        return R.layout.activity_my_expand
    }

    override fun initView() {
        super.initView()
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setOnRefreshListener(this)
        rvExpand.layoutManager = LinearLayoutManager(mActivity)
        rvExpand.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                val paddingLeft = DensityUtils.dp2px(application, 5f)
                outRect.set(paddingLeft, 0, paddingLeft, 0)
            }
        })
        rvExpand.adapter = msgAdapter
        rlBack.setOnClickListener(this)
        rl_share.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        getExpandList()
    }

    private fun getExpandList() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(mActivity, vodService.myExpand(mPage.toString(), "20"),
                object : LoadingObserver<MyExpand>(mActivity) {
                    override fun onSuccess(data: MyExpand) {
                        refreshLayout.finishLoadMore()
                        refreshLayout.finishRefresh()
                        val list = data.list
                        if (list.size < 20) {
                            refreshLayout.setNoMoreData(true)
                        } else {
                            refreshLayout.setNoMoreData(false)
                        }
                        if (isRefresh) {
                            mDataList.clear()
                        }
                        mDataList.addAll(list)
                        msgAdapter.setNewData(mDataList)
                        tv_total.text = data.total.toString() + "人"
                    }

                    override fun onError(e: ResponseException) {
                    }

                })
    }


    override fun onClick(v: View) {
        when (v) {
            rlBack -> {
                finish()
            }
            rl_share -> {
                Toast.makeText(this, "扫码即可下载APP哦~~~", Toast.LENGTH_SHORT).show()
                val intn= Intent(this@MyExpandActivity, ShareActivity::class.java)
                intn.putExtra("vom_name","有奖推广活动")
                intn.putExtra("vod_pic","www") //图片
                val description_txt: String
                val description = MMkvUtils.Builds().loadStartBean("")?.ads?.share_description
                if (description == null || description.description.isNullOrEmpty()) {
                    description_txt = "1、普通用户分享成功可获得积分奖励\n2、代理用户分销成功可获得金币奖励"
                } else {
                    description_txt = description.description ?: ""
                }
                intn.putExtra("vod_blurd",description_txt) //注释
                intn.putExtra("vod_class","分享给未安装过的用户、对方注册并打开应用算分享成功") //标签
                startActivity(intn)
            }
        }
    }

    class ExpandAdapter(var activity: Activity) : BaseQuickAdapter<MyExpand.ListBean, BaseViewHolder>(R.layout.item_my_expand) {
        override fun convert(helper: BaseViewHolder, item: MyExpand.ListBean?) {
            item?.run {
                helper.setText(R.id.tv_name, this.user_nick_name)
                helper.setText(R.id.tv_id, this.user_id.toString())
                helper.setText(R.id.tv_time, DateUtil.getyyyyMMddHHmm(this.user_reg_time * 1000.toLong()))
            }
        }

    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        isRefresh = false
        mPage++
        getExpandList()
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        isRefresh = true
        mPage = 1
        getExpandList()
    }


}