package cn.lvdou.vod.ui.push

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.lvdou.vod.App
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.observer.LoadingObserver
import cn.lvdou.vod.bean.Page
import cn.lvdou.vod.bean.PlayLogBean
import cn.lvdou.vod.bean.PlayScoreBean
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.ui.play.PlayActivity
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.MMkvUtils
import cn.lvdou.vod.utils.MMkvUtils.Companion.Builds
import cn.lvdou.vod.utils.MyLinearLayoutManager
import cn.lvdou.vod.utils.Retrofit2Utils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.Gson
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.youxiao.ssp.ad.bean.SSPAd
import com.youxiao.ssp.ad.core.AdClient
import com.youxiao.ssp.ad.listener.AdLoadAdapter
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class PushActivity : BaseActivity() {

    private val breakLine: View by lazy { findViewById(R.id.breakLine) }
    private val linear_Banner: FrameLayout by lazy { findViewById(R.id.linear_Banner) }
    private val refreshLayout: SmartRefreshLayout by lazy { findViewById(R.id.refreshLayout) }
    private val rlBack: RelativeLayout by lazy { findViewById(R.id.rlBack) }
    private val rlEdit: LinearLayout by lazy { findViewById(R.id.rlEdit) }
    private val rvPlayScore: RecyclerView by lazy { findViewById(R.id.rvPlayScore) }
    private val tvEdit: TextView by lazy { findViewById(R.id.tvEdit) }
    private val tvSelect: TextView by lazy { findViewById(R.id.tvSelect) }
    private val tvSelectCount: TextView by lazy { findViewById(R.id.tvSelectCount) }

    private var isEditMode = false
    private var isAllSelect: Boolean = false
    private var curPage: Int = 1

    private val playScoreAdapter: PlayScoreAdapter by lazy {
        PlayScoreAdapter().apply {
            setOnItemClickListener { adapter, _, position ->
                val tempItem: Any = adapter.getItem(position) ?: return@setOnItemClickListener
                val item = tempItem as PlayScoreBean
                if (isEditMode) {
                    item.isSelect = !item.isSelect
                    if (isAllSelect && !item.isSelect) {
                        isAllSelect = false
                        tvSelect.text = "全选"
                    }
                    adapter.data[position] = item
                    notifyItemChanged(position)
                    changeDeleteNum()
                } else {
                    MMkvUtils.Builds().savePlayScore(item)
                    PlayActivity.startByPlayScore(item.vodId)
                }
            }
        }
    }

    override fun getLayoutResID(): Int {
        return R.layout.activity_push
    }

    override fun initView() {
        super.initView()
        rvPlayScore.layoutManager = MyLinearLayoutManager(mActivity)
        rvPlayScore.adapter = playScoreAdapter

        refreshLayout.setDisableContentWhenRefresh(true) //是否在刷新的时候禁止列表的操作
        refreshLayout.setDisableContentWhenLoading(true) //是否在加载的时候禁止列表的操作
        refreshLayout.setEnableLoadMore(true) //是否启用上拉加载功能
        refreshLayout.setEnableRefresh(true) //是否启用上拉加载功能;
        refreshLayout.setEnableAutoLoadMore(true)
        refreshLayout.setOnRefreshListener(OnRefreshListener {
            curPage = 1
            if (curPage == 1) {
                playScoreAdapter.data.clear()
            }
            getPlayScore(curPage, false)
        })
        refreshLayout.setOnLoadMoreListener(OnLoadMoreListener {
            curPage++
            getPlayScore(curPage, true)

        })


    }

    override fun onResume() {
        super.onResume()
        curPage = 1
        if (curPage == 1) {
            playScoreAdapter.data.clear()
        }
        getPlayScore(curPage, false)
        getXTWAd()
    }

    /**
     * 信天翁
     * */

    private fun getXTWAd() {
        val adClient = AdClient(mActivity)
        val banner = Builds().loadStartBean("")?.ads?.sdk_banner
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

    override fun initListener() {
        super.initListener()
        tvSelect.setOnClickListener {
            if (isAllSelect) {
                playScoreAdapter.data.map {
                    it.isSelect = false
                }
                isAllSelect = false
                tvSelect.text = "全选"
            } else {
                playScoreAdapter.data.map {
                    it.isSelect = true
                }
                isAllSelect = true
                tvSelect.text = "取消全选"
            }
            playScoreAdapter.notifyDataSetChanged()
            changeDeleteNum()
        }

        tvSelectCount.setOnClickListener {
            val ids = getSelectCollection()
            if (ids.isNullOrEmpty()) {
                ToastUtils.showShort("未选择任何数据")
            } else {
                deleteCollection(ids)
            }
        }

        tvEdit.setOnClickListener {
            isEditMode = !isEditMode
            changeEditMode()
        }

        rlBack.setOnClickListener {
            setResult(5)
            finish()
        }
    }

    private fun getSelectCollection(): List<String> {
        val ids = ArrayList<String>()
        playScoreAdapter.data.map {
            if (it.isSelect) {
                ids.add(it.id.toString())
            }
        }
        return ids
    }

    private fun changeEditMode() {
        if (isEditMode) {
            tvEdit.text = "取消"
            breakLine.visibility = View.VISIBLE
            rlEdit.visibility = View.VISIBLE
            playScoreAdapter.data.map {
                it.isSelect = false
            }
            isAllSelect = false
            tvSelect.text = "全选"
        } else {
            tvEdit.text = "编辑"
            breakLine.visibility = View.GONE
            rlEdit.visibility = View.GONE
        }
        playScoreAdapter.changeEditMode(isEditMode)
    }

    private fun getSelectCount(): Int {
        var count = 0
        playScoreAdapter.data.map {
            if (it.isSelect) {
                count++
            }
        }
        return count
    }

    private fun changeDeleteNum() {
        tvSelectCount.text = "删除(${getSelectCount()})"
    }

    private fun deleteCollection(ids: List<String>) {
        ids.forEach {

            deletePlayScore(it)
            println(it.toString())
        }

        val data = playScoreAdapter.data as ArrayList<PlayScoreBean>
        var deleteMsg =  ArrayList<PlayScoreBean>()
        for (posi in data.indices) {
            for (positio in ids.indices){
                if (data.get(posi).id.toString() == ids.get(positio)){
                    deleteMsg.add(data.get(posi))
                }
            }
        }
        for (pos in deleteMsg){
            if (playScoreAdapter.data.contains(pos)){
                playScoreAdapter.data.remove(pos)
            }
        }
        playScoreAdapter.setNewData(playScoreAdapter.data)
        isEditMode = false
        changeEditMode()
        changeDeleteNum()
    }


    private fun deletePlayScore(id: String) {
        var vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(
                mActivity,
                vodService.deletePlayLogList(id),
                object : LoadingObserver<String>(mActivity) {
                    override fun onSuccess(data: String) {
                        ToastUtils.showShort("删除成功！")
                    }

                    override fun onError(e: ResponseException) {
                        e.getErrorMessage()
                        ToastUtils.showShort("删除失败！")
                    }

                }
        )
    }

    private fun getPlayScore(page: Int, isLoadMore: Boolean) {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this, vodService.getPushList(page.toString(), "6", "3"),
                object : LoadingObserver<Page<PlayLogBean>>(mActivity) {
                    override fun onSuccess(data: Page<PlayLogBean>) {
                        val playScoreBeans = ArrayList<PlayScoreBean>()
                        val playLogBeans = data.list
                        playLogBeans.forEach {
                            val playScoreBean: PlayScoreBean = PlayScoreBean()
                            playScoreBean.vodName = it.vod_name
                            playScoreBean.vodImgUrl = it.vod_pic
                            playScoreBean.vodscore = it.vod_score
                            playScoreBean.vodarea = it.vod_area
                            playScoreBean.vodclass = it.vod_class
                            playScoreBean.vodcontent = it.vod_content

                            if (it.percent == "NaN") {
                                playScoreBean.percentage = 0.0f
                            } else {
                                try {
                                    playScoreBean.percentage = it.percent.toFloat()
                                } catch (ex: Exception) {
                                }
                            }

                            playScoreBean.typeId = it.type_id;
                            playScoreBean.vodId = it.vod_id.toInt()
                            playScoreBean.id = it.id
                            playScoreBean.isSelect = false
                            playScoreBean.vodSelectedWorks = it.nid.toString()
                            playScoreBean.urlIndex = it.urlIndex
                            playScoreBean.curProgress = it.curProgress
                            playScoreBean.playSourceIndex = it.playSourceIndex

                            var gson: Gson = Gson()
                            var playScoreBeanStr = gson.toJson(playScoreBean).toString();
                            Log.i("playlog", "playScoreBean${playScoreBeanStr}")
                            playScoreBeans.add(playScoreBean)
                            //  Collections.reverse(playScoreBeans)
                        }

                        if (playScoreAdapter.data.size > 0) {
                            playScoreAdapter.addData(playScoreBeans)
                            playScoreAdapter.notifyDataSetChanged()
                        } else {
                            playScoreAdapter.setNewData(playScoreBeans)
                        }

                        Log.i("playlog", "getPlayLogList11${data}")

                        if (isLoadMore) {
                            refreshLayout.finishLoadMore(200)
                        } else {
                            refreshLayout.finishRefresh(200)
                        }
                    }

                    override fun onError(e: ResponseException) {

                    }
                })
    }

    private class PlayScoreAdapter(var isEditMode: Boolean = false) : BaseQuickAdapter<PlayScoreBean, BaseViewHolder>(R.layout.item_play_score_vertical) {
        override fun convert(helper: BaseViewHolder, item: PlayScoreBean?) {
            item?.run {

                val name = if (item.typeId == 3) {
                    "$vodName $vodSelectedWorks"
                } else if (item.typeId == 1) {
                    vodName
                } else {
                    "$vodName ${vodSelectedWorks}"
                }
//                helper.setText(R.id.tvtype, vodType)
                val tvContent = vodcontent
                val voDscore = vodscore
                helper.setText(R.id.tvName, name)
                helper.setText(R.id.tv_score, voDscore)
                helper.setText(R.id.tv_content, tvContent)
                helper.setText(R.id.tvPlayProgress, "观看至${(percentage * 100).toInt()}%")

                if (isEditMode) {
                    helper.setChecked(R.id.cb, isSelect)
                }

                val mation: MultiTransformation<Bitmap> = MultiTransformation(CenterCrop(), RoundedCornersTransformation(20, 0, RoundedCornersTransformation.CornerType.ALL))
                Glide.with(helper.itemView.context)
                        .load(vodImgUrl)
                        .error(R.drawable.error)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .apply(RequestOptions.bitmapTransform(mation))
                        .into(helper.getView<View>(R.id.ivImg) as ImageView)
            }

            if (isEditMode) {
                helper.setGone(R.id.cb, true)
            } else {
                helper.setGone(R.id.cb, false)
            }
        }

        fun changeEditMode(isEditMode: Boolean) {
            this.isEditMode = isEditMode
            notifyDataSetChanged()
        }
    }
}
