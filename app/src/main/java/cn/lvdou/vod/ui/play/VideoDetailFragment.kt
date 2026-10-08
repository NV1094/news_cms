//ProgressDialog/getParcelable/convertView/getDrawable 等结构性弃用：保留现有播放详情页行为
@file:Suppress("DEPRECATION")

package cn.lvdou.vod.ui.play

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.lvdou.av.play.AvVideoView
import cn.lvdou.av.play.VideoViewImpt
import cn.lvdou.vod.ApiConfig
import cn.lvdou.vod.App
import cn.lvdou.vod.R
import cn.lvdou.vod.ad.AdWebView
import cn.lvdou.vod.base.BaseFragment
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.observer.BaseObserver
import cn.lvdou.vod.bean.*
import cn.lvdou.vod.jiexi.BackListener
import cn.lvdou.vod.jiexi.JieXiUtils2
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.ui.down.AllDownloadActivity
import cn.lvdou.vod.ui.down.cache.Square
import cn.lvdou.vod.ui.down.cache.SquareViewBinder
import cn.lvdou.vod.ui.feedback.FeedbackActivity
import cn.lvdou.vod.ui.home.MyDividerItemDecoration
import cn.lvdou.vod.ui.login.LoginActivity
import cn.lvdou.vod.ui.share.ShareActivity
import cn.lvdou.vod.ui.widget.HitDialog
import cn.lvdou.vod.ui.widget.HitDialog.OnHitDialogClickListener
import cn.lvdou.vod.utils.*
import cn.lvdou.vod.utils.DensityUtils.dp2px
import cn.lvdou.vod.utils.DensityUtils.getScreenWidth
import cn.lvdou.vod.utils.MMkvUtils.Companion.Builds
import cn.lvdou.vod.utils.decoration.GridItemDecoration
import com.blankj.utilcode.util.*
import com.blankj.utilcode.util.StringUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bytedance.sdk.openadsdk.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.dueeeke.videoplayer.player.VideoViewManager
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.ui.login.LoginActivity.Companion.start
import cn.lvdou.vod.ui.pay.PayActivity
import cn.lvdou.vod.utils.UserUtils.isLogin
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.youxiao.ssp.ad.bean.SSPAd
import com.youxiao.ssp.ad.core.AdClient
import com.youxiao.ssp.ad.listener.AdLoadAdapter
import jaygoo.library.m3u8downloader.control.DownloadPresenter
import jaygoo.library.m3u8downloader.db.table.M3u8DoneInfo
import jaygoo.library.m3u8downloader.db.table.M3u8DownloadingInfo
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import me.drakeet.multitype.MultiTypeAdapter
import org.litepal.LitePalApplication
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


open class VideoDetailFragment : BaseFragment() {

    private val refreshLayout: SmartRefreshLayout by lazy { requireView().findViewById(R.id.refreshLayout) }
    private val rvPlayDetail: RecyclerView by lazy { requireView().findViewById(R.id.rvPlayDetail) }

    private lateinit var mVodBean: VodBean
    private var isParse = false
    private var isCollected: Boolean = false
    private var urlIndex: Int = 0 //播放集
    private var playSourceIndex: Int = 0//播放源
    private var curCommentPage = 1
    private lateinit var vod_play_list: List<PlayFromBean>//播放视频列表
    private var curType = 0//推荐 默认是相似推荐
    private var curSameTypePage = 1
    private var curSameActorPage = 1
    private var curParseIndex = 0//记录上一次解析到的位置，如果出现解析到是视频不能播放的话 自动解析下一条
    private var curFailIndex = -1

    private val recommendAdapter: RecommendAdapter by lazy {
        RecommendAdapter().apply {
            setOnItemClickListener { adapter, _, position ->
                val vodBean = adapter.getItem(position) as VodBean
                playActivity.showNewVideo(vodBean)
            }
        }
    }
    private val commentAdapter: CommentAdapter by lazy {
        CommentAdapter().apply {
            setHeaderAndEmpty(true)
        }
    }
    private val selectionAdapter: SelectionAdapter by lazy {
        SelectionAdapter().apply {
            setOnItemClickListener { adapter, _, position ->
                if (urlIndex != position) {
                    urlIndex = position
                    playActivity.changeSelection(urlIndex, false)
                    notifyDataSetChanged()
                }
            }
        }
    }

    private val headerView: View by lazy {
        View.inflate(mActivity, R.layout.layout_video_detail, null)
    }

    private lateinit var rvLastest: RecyclerView
    private lateinit var tlPlaySource: TabLayout
    private lateinit var playActivity: NewPlayActivity

    var isOrder = true//播放顺序，默认为顺序播放

    override fun getLayoutId(): Int {
        return R.layout.fragment_play_detail
    }

    override fun initView() {
        super.initView()
        playActivity = mActivity as NewPlayActivity
        arguments?.run {
            //wqddg
            mVodBean = getParcelable(VOD_BEAN) ?: VodBean() //原 `?: null as VodBean` 永败转换，改为安全默认
            mVodBeans = mVodBean.vodRemarks.toString()
            urlIndex = getInt(URL_INDEX)
            playSourceIndex = getInt(PLAY_SOURCE_INDEX)
        }

        refreshLayout.setEnableRefresh(false)
        refreshLayout.setRefreshFooter(ClassicsFooter(mActivity))
        rvPlayDetail.layoutManager = LinearLayoutManager(mActivity)
        rvPlayDetail.adapter = commentAdapter
        initHeaderMsg()
        commentAdapter.addHeaderView(headerView)
        getCommentList()
        getSameTypeData()
    }

    override fun onResume() {
        super.onResume()
        getCollectionState()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeCurIndex(urlIndex: Int) {
        this.urlIndex = urlIndex
        selectionAdapter.notifyDataSetChanged()
        scrollCurIndex(rvLastest)
    }

    private val DanMuColor = "#FFFFFFFF"
    var videoViewImpt: VideoViewImpt = VideoViewManager.instance()["pip"] as AvVideoView
    open fun sendDanmaku_wqddg(str: String) {
        if (!StringUtils.isEmpty(str)) {
            videoViewImpt.showDanmaku()
            videoViewImpt.addDanmaku(str, true, DanMuColor)
        } else {
            ToastUtils.showShort("请输入弹幕！")
        }
    }

    // 判断字符串包含数字
    fun testIsNumMethodTwo(str: String?): Boolean {
        var flag = false
        val pattern: Pattern = Pattern.compile("[0-9]+")
        val matcher: Matcher = pattern.matcher(str ?: "")
        if (matcher.find()) {
            flag = true
        }
        return flag
    }

    @SuppressLint("SetTextI18n")
    @Suppress("SENSELESS_COMPARISON", "USELESS_NULLABLE_CHECK") //startBean 非空判断被编译器视为恒真
    private fun initHeaderMsg() {
        rvLastest = headerView.findViewById(R.id.rvLastest)
        tlPlaySource = headerView.findViewById(R.id.tlPlaySource)
        val title = headerView.findViewById<TextView>(R.id.item_tv_playinfo_title)
        val intro = headerView.findViewById<TextView>(R.id.item_tv_playinfo_intro)
        val hits = headerView.findViewById<TextView>(R.id.item_tv_playinfo_hits)
        val score = headerView.findViewById<TextView>(R.id.item_tv_playinfo_score)
        val tvLastest = headerView.findViewById<TextView>(R.id.tvLastest)
        val tvLastests = headerView.findViewById<TextView>(R.id.tvLastests)
        val ivLastest = headerView.findViewById<ImageView>(R.id.iv_lastest)
        val sortVodView = headerView.findViewById<TextView>(R.id.item_svv_playinfo)
        val vodpubdates = headerView.findViewById<TextView>(R.id.vod_pubdates)
        val checkOrder = headerView.findViewById<CheckBox>(R.id.checkOrder)
        val itemTvPlayinfoComment = headerView.findViewById<TextView>(R.id.item_tv_playinfo_comment)
        val ivGovip = headerView.findViewById<LinearLayout>(R.id.iv_go_vip)

        var isVip = false
        if (isLogin() && UserUtils.userInfo?.group_id == 3) {
            isVip = true
        }

        val startBean = Objects.requireNonNull(Builds().loadStartBean(""))
        if (startBean != null && startBean.ads != null) { //保留判空（部分分析器认为 startBean 可空），恒真告警由函数级 Suppress 处理
            val ads = startBean.ads
//            if (ads.sdk_banner == null || ads.sdk_banner.status != 1 || ads.sdk_banner.description.isNullOrEmpty()) {
//                if (ads.player_down == null || ads.player_down.status != 1 || ads.player_down.description.isNullOrEmpty()) {
//                    awvPlayerDown.visibility = View.GONE
//                } else {
//                    awvPlayerDown.visibility = View.VISIBLE
//                    awvPlayerDown.loadHtmlBody(ads.player_down.description)
//                }
//            }

            if (isVip || ads.player_down_isvip == null || ads.player_down_isvip.status != 1 || ads.player_down_isvip.description.isNullOrEmpty()) {
                ivGovip.visibility = View.GONE
            } else {
                if (ads.player_down_isvip.description.contains("||")) {
                    val descriptions = ads.player_down_isvip.description.split("||")
                    headerView.findViewById<TextView>(R.id.isvip_1).text = descriptions[0]
                    headerView.findViewById<TextView>(R.id.isvip_2).text = descriptions[1]
                }
                ivGovip.visibility = View.VISIBLE
            }

            if (ads.liuyan_message == null || ads.liuyan_message.status != 1 || ads.liuyan_message.description.isNullOrEmpty()) {
                headerView.findViewById<LinearLayout>(R.id.iv_liuyan_message).visibility = View.GONE
            } else {
                headerView.findViewById<LinearLayout>(R.id.iv_liuyan_message).visibility =
                    View.VISIBLE
                headerView.findViewById<TextView>(R.id.liuyan_message).text =
                    ads.liuyan_message.description
            }

            if (startBean.app_play_recommend == null || startBean.app_play_recommend != "1") {
                headerView.findViewById<LinearLayout>(R.id.item_tuijian).visibility = View.GONE
            } else {
                headerView.findViewById<LinearLayout>(R.id.item_tuijian).visibility = View.VISIBLE
            }
        } else {
//            awvPlayerDown.visibility = View.GONE
            headerView.findViewById<LinearLayout>(R.id.item_tuijian).visibility = View.GONE
            headerView.findViewById<LinearLayout>(R.id.iv_liuyan_message).visibility = View.GONE
        }


        headerView.findViewById<TextView>(R.id.item_tv_playinfo_score)
            .setOnClickListener {
                score()
            }
        headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect)
            .setOnClickListener {
                if (isLogin()) {
                    if (isCollected) {
                        uncollection()
                    } else {
                        collection()
                    }
                } else {
                    ActivityUtils.startActivity(LoginActivity::class.java)
                }

            }
        headerView.findViewById<TextView>(R.id.tvLastest2)
            .setOnClickListener {
                playActivity.showPlayList()
            }
        headerView.findViewById<LinearLayout>(R.id.item_tv_playinfo_collect3)
            .setOnClickListener {
                if (isLogin()) {
                    if (isCollected) {
                        uncollection()
                    } else {
                        collection()
                    }
                } else {
                    ActivityUtils.startActivity(LoginActivity::class.java)
                }

            }

        headerView.findViewById<LinearLayout>(R.id.tv_av_danmaku2)
            .setOnClickListener {
                if (isLogin()) {
                    CommentDialog(mActivity, "弹幕")
                        .setOnCommentSubmitClickListener(object :
                            CommentDialog.OnCommentSubmitClickListener {
                            override fun onCommentSubmit(comment: String) {
                                sendDanmaku_wqddg(comment)
                                //发送弹幕：委托给播放页，与弹窗发送共用同一套参数（vod_id/at_time/color）
                                playActivity.sendDanmu(comment)
                            }
                        })
                        .show()
                } else {
                    start()
                }
            }
        headerView.findViewById<TextView>(R.id.item_tv_playinfo_download)
            .setOnClickListener {
                if (Builds().loadStartBean("")?.ads?.download?.status != 0) {
                    if (!LoginUtils.checkLogin2(activity)) {
                        HitDialog(context!!).setTitle("提示").setMessage("需登录后开通VIP才可下载，确定登录。")
                            .setOnHitDialogClickListener(object : OnHitDialogClickListener() {
                                override fun onCancelClick(dialog: HitDialog) {
                                    super.onCancelClick(dialog)
                                }

                                override fun onOkClick(dialog: HitDialog) {
                                    super.onOkClick(dialog)
                                    ActivityUtils.startActivity(LoginActivity::class.java)
                                }
                            }).show()
                    } else {
                        if (LoginUtils.checkVIP(activity, "下载需要开通vip是否去开通")) {
                            startCache()
                        }
                    }
                } else {
                    startCache()
                }
            }

        /**
         * 信天翁
         * */

        headerView.findViewById<TextView>(R.id.item_tv_playinfo_feedback)
            .setOnClickListener {
                val sdkFullscreenvideo = Builds().loadStartBean("")!!.ads.sdk_Fullscreenvideo
                if (!App.SdkSwitch || sdkFullscreenvideo == null || sdkFullscreenvideo.status != 1 || sdkFullscreenvideo.description.isNullOrEmpty()) {
                    val message =
                        "视频数据：${mVodBean.vod_name} 播放失败\n视频来源：${playActivity.playFrom.player_info.show}\n视频序号：${playActivity.playList!![urlIndex].name}\n视频地址：${playActivity.playList!![urlIndex].url}"
                    FeedbackActivity.start(mActivity, message)
                } else {
                    HitDialog(context!!).setTitle("提示").setMessage("留言求片需要观看一段广告噢~~~")
                        .setOnHitDialogClickListener(object : OnHitDialogClickListener() {
                            override fun onOkClick(dialog: HitDialog) {
                                super.onOkClick(dialog)
                                val progressDialog =
                                    ProgressDialog.show(mActivity, "", "please waiting...")
                                val adClient = AdClient(mActivity)
                                adClient.requestFullScreenVideoAd(
                                    sdkFullscreenvideo.description,
                                    object : AdLoadAdapter() {
                                        override fun onAdLoad(ad: SSPAd) {
                                            super.onAdLoad(ad)
                                            progressDialog.dismiss()
                                            val message =
                                                "视频数据：${mVodBean.vod_name} 播放失败\n视频来源：${playActivity.playFrom.player_info.show}\n视频序号：${playActivity.playList!![urlIndex].name}\n视频地址：${playActivity.playList!![urlIndex].url}"
                                            FeedbackActivity.start(mActivity, message)
                                        }

                                        override fun onError(i: Int, s: String) {
                                            progressDialog.dismiss()
                                            val message =
                                                "视频数据：${mVodBean.vod_name} 播放失败\n视频来源：${playActivity.playFrom.player_info.show}\n视频序号：${playActivity.playList!![urlIndex].name}\n视频地址：${playActivity.playList!![urlIndex].url}"
                                            FeedbackActivity.start(mActivity, message)
                                        }
                                    })
                            }
                        }).show()
                }
            }
        headerView.findViewById<TextView>(R.id.item_tv_playinfo_share)
            .setOnClickListener {
                if (!isLogin()) {
                    ActivityUtils.startActivity(LoginActivity::class.java)
                } else {
                    val intn = Intent(this.mActivity, ShareActivity::class.java)
                    intn.putExtra("vom_name", mVodBean.vodName)
                    intn.putExtra("vod_pic", mVodBean.vodPic) //图片
                    Log.e("wqddg", mVodBean.toString())
                    intn.putExtra(
                        "vod_class",
                        mVodBean.vod_year + " | " + mVodBean.vod_area + " | " + mVodBean.vod_class
                    ) //标签
                    intn.putExtra("vod_blurd", mVodBean.vodBlurb) //注释
                    startActivity(intn)
                }
            }

        headerView.findViewById<TextView>(R.id.item_tv_playinfo_comment)
            .setOnClickListener {
                if (isLogin()) {
                    CommentDialog(mActivity, "讨论")
                        .setOnCommentSubmitClickListener(object :
                            CommentDialog.OnCommentSubmitClickListener {
                            override fun onCommentSubmit(comment: String) {
                                commitComment(comment)
                            }
                        })
                        .show()
                } else {
                    start()
                }

            }
        headerView.findViewById<LinearLayout>(R.id.iv_go_vipm)
            .setOnClickListener {
                if (!isLogin()) {
                    start()
                } else {
                    val intent = Intent(activity, PayActivity::class.java)
                    intent.putExtra("type", 1)
                    ActivityUtils.startActivity(intent)
                }
            }
        tvLastest.setOnClickListener {
            playActivity.showPlayList()
        }
        tvLastests.setOnClickListener {
            playActivity.showPlayList()
        }
        itemTvPlayinfoComment.text = "${mVodBean.comment_num} 评论"
        title.text = mVodBean.vod_name
        hits.text = "" + mVodBean.vod_hits + "次播放"
        score.text = mVodBean.vod_score + "分"
        sortVodView.text = mVodBean.vod_class.replace(",", " ")
        intro.setOnClickListener {
            playActivity.showSummary()
        }
        ivLastest.setOnClickListener {
            playActivity.showSummary()
        }
        if (mVodBean.vodRemarks.isNotEmpty()) {
            tvLastest.text = mVodBeans
            tvLastests.text = mVodBeans
            if (testIsNumMethodTwo(mVodBeans)) {
                tvLastest.visibility = View.VISIBLE
                tvLastests.visibility = View.VISIBLE
                sortVodView.visibility = View.GONE
            } else {
                tvLastest.visibility = View.GONE
                tvLastests.visibility = View.GONE
                sortVodView.visibility = View.VISIBLE
            }
        } else {

        }
        if (mVodBean.vod_pubdate.isNotEmpty()) {
            vodpubdates.text = mVodBean.vod_pubdate + "上映"
        } else {
            vodpubdates.text = "上映时间：未知"
        }
//        vod_play_list.cl
        vod_play_list = mVodBean.vod_play_list
        rvLastest.layoutManager = LinearLayoutManager(mActivity).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        rvLastest.adapter = selectionAdapter

        if (vod_play_list.isNotEmpty()) {
            for (i in vod_play_list.indices) {
                val playFromBean = vod_play_list[i]
                val playerInfo = playFromBean.player_info
                val urls = playFromBean.urls
                var playSource = playerInfo.show
                if (StringUtils.isEmpty(playSource)) {
                    playSource = "默认"
                }
                if (i == playSourceIndex) {
                    selectionAdapter.addData(urls)
                }

                val tab = tlPlaySource.newTab().setText(playSource)
                tlPlaySource.addTab(tab)
            }
        }

        tlPlaySource.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
                Log.d("", "")
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                Log.d("", "")
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {

                val playFromBean = vod_play_list[tlPlaySource.selectedTabPosition]

                selectionAdapter.setNewData(playFromBean.urls)

                playActivity.changePlaySource(playFromBean, tlPlaySource.selectedTabPosition)
            }
        })

        tlPlaySource.getTabAt(playSourceIndex)?.select()
        scrollCurIndex(rvLastest)
        val rvRecommand = headerView.findViewById<RecyclerView>(R.id.rvRecommand)
        val tvChange = headerView.findViewById<TextView>(R.id.tvChange)
        val tvSameType = headerView.findViewById<TextView>(R.id.tvSameType)
        val tvSameActor = headerView.findViewById<TextView>(R.id.tvSameActor)
        val dividerItemDecoration =
            MyDividerItemDecoration(mActivity, RecyclerView.HORIZONTAL, false)
        dividerItemDecoration.setDrawable(mActivity.resources.getDrawable(R.drawable.divider_image))
        rvRecommand.addItemDecoration(dividerItemDecoration)
//        rvRecommand.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        rvRecommand.layoutManager =
            GridLayoutManager(mActivity, 3, RecyclerView.VERTICAL, false).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return 3
                    }
                }
            }

        rvRecommand.adapter = recommendAdapter
        tvChange.setOnClickListener {
            when (curType) {
                0 -> getSameTypeData()
                1 -> getSameActorData()
            }
        }
        tvSameType.setOnClickListener {
            if (curType != 0) {
                curType = 0
                tvSameType.setTextColor(ColorUtils.getColor(R.color.colorPrimary))
                tvSameActor.setTextColor(ColorUtils.getColor(R.color.gray_999))
                getSameTypeData()
            }
        }

        checkOrder.setOnCheckedChangeListener { _, isChecked ->

            mVodBean.vod_play_list.forEachIndexed { _, playFromBean ->
                playFromBean.urls.reverse()
            }

            selectionAdapter.setNewData(mVodBean.vod_play_list[playActivity.playSourceIndex].urls)
            isOrder = !isChecked
            playActivity.changeSelection(urlIndex, true)

        }

        tvSameActor.setOnClickListener {
            if (curType != 1) {
                curType = 1
                tvSameType.setTextColor(ColorUtils.getColor(R.color.gray_999))
                tvSameActor.setTextColor(ColorUtils.getColor(R.color.colorPrimary))
                getSameActorData()
            }
        }
    }

    //为了更好的释放资源，在页面销毁时调用release方法释放资源
    override fun onDestroy() {
        super.onDestroy()
        AdClient(activity).release()
    }

    private fun scrollCurIndex(rvLastest: RecyclerView) {
        rvLastest.smoothScrollToPosition(urlIndex)
        val mLayoutManager = rvLastest.layoutManager as LinearLayoutManager
        mLayoutManager.scrollToPositionWithOffset(urlIndex, 0)
    }

    override fun initListener() {
        super.initListener()
        refreshLayout.setOnLoadMoreListener {
            curCommentPage++
            getCommentList()
        }
    }

    private fun collection() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this,
            vodService.collect(1.toString(), mVodBean.vod_id.toString(), 2.toString()),
            object : BaseObserver<String>() {
                @SuppressLint("UseCompatLoadingForDrawables")
                override fun onSuccess(data: String) {
                    ToastUtils.showShort("已收藏")
                    val drawable = mActivity.getDrawable(R.drawable.ic_livei22)
                    val drawables = mActivity.getDrawable(R.drawable.ic_lovei)
                    isCollected = true
                    drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    drawables?.setBounds(0, 0, drawables.minimumWidth, drawables.minimumHeight)
                    headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect)
                        .apply {
                            setCompoundDrawables(null, drawable, null, null)
                        }
                    headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect2)
                        .apply {
                            setCompoundDrawables(drawables, null, null, null)
                        }
                    headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect2).text = "已 追 剧"
                }

                override fun onError(e: ResponseException) {

                }

            })
    }

    private fun uncollection() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this,
            vodService.deleteCollect(mVodBean.vod_id.toString(), 2.toString()),
            object : BaseObserver<String>() {
                override fun onSuccess(data: String) {
                    ToastUtils.showShort("取消成功")
                    isCollected = false
                    val drawable = mActivity.getDrawable(R.drawable.ic_livei2)
                    val drawables = mActivity.getDrawable(R.drawable.ic_livei)
                    drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                    drawables?.setBounds(0, 0, drawables.minimumWidth, drawables.minimumHeight)
                    headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect)
                        .apply {
                            setCompoundDrawables(null, drawable, null, null)
                        }
                    headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect2)
                        .apply {
                            setCompoundDrawables(drawables, null, null, null)
                        }
                    headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect2).text = "追 剧"
                }

                override fun onError(e: ResponseException) {
                }

            }
        )
    }

    private fun getCollectionState() {
        if (isLogin()) {
            val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

            RequestManager.execute(this,
                vodService.getCollectList(1.toString(), 100.toString(), 2.toString()),
                object : BaseObserver<Page<CollectionBean>>() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    override fun onSuccess(data: Page<CollectionBean>) {
                        for (bean in data.list) {
                            if (bean.data.id == mVodBean.vod_id) {
                                isCollected = true
                                break
                            }
                        }
                        if (isCollected) {
                            val drawable = mActivity.getDrawable(R.drawable.ic_livei22)
                            val drawables = mActivity.getDrawable(R.drawable.ic_lovei)
                            drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                            drawables?.setBounds(
                                0,
                                0,
                                drawables.minimumWidth,
                                drawables.minimumHeight
                            )
                            headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect)
                                .apply {
                                    setCompoundDrawables(null, drawable, null, null)
                                }
                            headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect2)
                                .apply {
                                    setCompoundDrawables(drawables, null, null, null)
                                }
                            headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect2).text =
                                "已 追 剧"
                        } else {
                            val drawables = mActivity.getDrawable(R.drawable.ic_livei)
                            val drawable = mActivity.getDrawable(R.drawable.ic_livei2)
                            drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                            drawables?.setBounds(
                                0,
                                0,
                                drawables.minimumWidth,
                                drawables.minimumHeight
                            )
                            headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect)
                                .apply {
                                    setCompoundDrawables(null, drawable, null, null)
                                }
                            headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect2)
                                .apply {
                                    setCompoundDrawables(drawables, null, null, null)
                                }
                            headerView.findViewById<TextView>(R.id.item_tv_playinfo_collect2).text =
                                "追 剧"
                        }

                    }

                    override fun onError(e: ResponseException) {

                    }

                })
        }
    }

    private fun score() {
        ScoreDialog(mActivity)
            .setOnScoreSubmitClickListener(object : ScoreDialog.OnScoreSubmitClickListener {
                override fun onScoreSubmit(scoreDialog: ScoreDialog, score: Float) {
                    if (score == 0f) {
                        ToastUtils.showShort("评分不能为空!")
                    } else {
                        scoreDialog.dismiss()
                        val vodService =
                            Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

                        RequestManager.execute(
                            this@VideoDetailFragment,
                            vodService.score(mVodBean.vod_id.toString(), score.toString()),
                            object : BaseObserver<GetScoreBean>() {
                                override fun onSuccess(data: GetScoreBean) {
                                    if (data.score != "0") {
                                        ToastUtils.showShort("评分成功，获得${data.score}积分")
                                    }
                                }

                                override fun onError(e: ResponseException) {
                                }
                            }
                        )
                    }
                }
            })
            .show()
    }

    private fun commitComment(commentContent: String) {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this,
            vodService.comment(commentContent, 1.toString(), mVodBean.vod_id.toString()),
            object : BaseObserver<GetScoreBean>() {
                override fun onSuccess(data: GetScoreBean) {
                    if (data.score == "0") {
                        ToastUtils.showShort("评论成功")
                    } else {
                        ToastUtils.showShort("评论成功,获得${data.score}积分")
                    }
                    curCommentPage = 1
                    getCommentList(true)
                }

                override fun onError(e: ResponseException) {

                }

            })
    }

    private fun replayComment(commentContent: String, commentId: String, commentPid: String) {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this,
            vodService.replayComment(
                commentContent,
                1.toString(),
                mVodBean.vod_id.toString(),
                commentId,
                commentPid
            ),
            object : BaseObserver<String>() {
                override fun onSuccess(data: String) {

                }

                override fun onError(e: ResponseException) {

                }

            })
    }

    private fun getCommentList(isFresh: Boolean = false) {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this,
            vodService.getCommentList(mVodBean.vod_id, 1.toString(), curCommentPage, 10),
            object : BaseObserver<Page<CommentBean>>() {
                override fun onSuccess(data: Page<CommentBean>) {
                    if (curCommentPage == 1) {
                        if (isFresh)
                            commentAdapter.setNewData(data.list)
                        else
                            commentAdapter.addData(data.list)
                    }

                    if (curCommentPage > 1) {
                        commentAdapter.addData(data.list)
                        run {
                            if (data.list.isEmpty()) {
                                refreshLayout.finishLoadMoreWithNoMoreData()
                            } else {
                                refreshLayout.finishLoadMore(true)
                            }
                        }
                    }
                }

                override fun onError(e: ResponseException) {
                    if (curCommentPage > 1) {
                        refreshLayout.finishLoadMore(false)
                    }
                }

            })
    }

    private fun getSameTypeData() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this,
            vodService.getSameTypeList(
                mVodBean.type_id,
                mVodBean.vod_class,
                curSameTypePage,
                3,
                AUtils.sign(),
                AUtils.keyTime()
            ),
            object : BaseObserver<Page<VodBean>>() {
                override fun onSuccess(data: Page<VodBean>) {
                    if (data.list.isNotEmpty()) {
                        curSameTypePage++
                        recommendAdapter.setNewData(data.list)
                    }
                }

                override fun onError(e: ResponseException) {
                }

            })
    }

    private fun getSameActorData() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this,
            vodService.getSameActorList(
                mVodBean.type_id,
                mVodBean.vod_actor,
                curSameActorPage,
                3,
                AUtils.sign(),
                AUtils.keyTime()
            ),
            object : BaseObserver<Page<VodBean>>() {
                override fun onSuccess(data: Page<VodBean>) {
                    if (data.list.isNotEmpty()) {
                        recommendAdapter.setNewData(data.list)
                        curSameActorPage++
                    }
                }

                override fun onError(e: ResponseException) {

                }

            })
    }

    fun changePlaysource(playSourceIndex: Int) {
        tlPlaySource.getTabAt(playSourceIndex)?.select()
    }

    private class CommentAdapter :
        BaseQuickAdapter<CommentBean, BaseViewHolder>(R.layout.item_hot_comment) {
        override fun convert(helper: BaseViewHolder, item: CommentBean?) {
            helper.let {
                item?.run {
                    it.setText(R.id.tvUser, comment_name)
                    it.setText(R.id.tvTime, TimeUtils.millis2String(comment_time * 1000))
                    it.setText(R.id.tvComment, comment_content)
                    val ivAvatar = it.getView<ImageView>(R.id.ivAvatar)
                    if (user_portrait.isNotEmpty()) {
                        Glide.with(helper.convertView)
                            .load(ApiConfig.MOGAI_BASE_URL + "/" + user_portrait)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(ivAvatar)
                    } else {
                        Glide.with(helper.convertView)
                            .load(R.drawable.default_avater)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(ivAvatar)
                    }
                }
            }
        }
    }

    private class RecommendAdapter :
        BaseQuickAdapter<VodBean, BaseViewHolder>(R.layout.item_card_child) {

        override fun convert(helper: BaseViewHolder, item: VodBean) {
            helper.setVisible(R.id.item_tv_card_child_tip, false)
            helper.setText(R.id.item_tv_card_child_title, item.vodName)
            helper.setText(R.id.item_tv_card_child_up_title, item.vodRemarks)
            val img = item.vod_pic

            val icon = helper.getView<ImageView>(R.id.item_iv_card_child_icon)
            val lp = icon.layoutParams
            val perWidth = (getScreenWidth(LitePalApplication.getContext()) - dp2px(
                LitePalApplication.getContext(),
                4f
            )) / 3
            lp.height = (perWidth * 1.4f).toInt()
            icon.layoutParams = lp

            val multiTransformation = MultiTransformation(
                CenterInside(),
                RoundedCornersTransformation(15, 8, RoundedCornersTransformation.CornerType.ALL)
            )

            Glide.with(helper.itemView.context)
                .load(img)
                .thumbnail(1.0f)
                .apply(RequestOptions.bitmapTransform(multiTransformation))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(icon)
        }
    }

    inner class SelectionAdapter :
        BaseQuickAdapter<UrlBean, BaseViewHolder>(R.layout.item_video_source) {
        override fun convert(helper: BaseViewHolder, item: UrlBean) {
            if (mVodBean.type_id == 3) {
                helper.itemView.layoutParams = helper.itemView.layoutParams.apply {
                    width = ViewGroup.MarginLayoutParams.WRAP_CONTENT //ConvertUtils.dp2px(130f)
                    height = ConvertUtils.dp2px(50f)
                }
            } else {
                helper.itemView.layoutParams = helper.itemView.layoutParams.apply {
                    width = ViewGroup.MarginLayoutParams.WRAP_CONTENT//ConvertUtils.dp2px(50f)
                    height = ConvertUtils.dp2px(50f)
                }
            }
            val position = helper.layoutPosition
            val name = item.name.replace("第", "").replace("集", "")
            helper.setText(R.id.tv, name)
            if (position == urlIndex) {
                helper.setTextColor(R.id.tv, ColorUtils.getColor(R.color.userTopBg))
                helper.setVisible(R.id.lvdou_AudioWaveView, true)
                helper.setText(R.id.tv, "    ")
            } else {
                helper.setTextColor(R.id.tv, ColorUtils.getColor(R.color.gray_999))
                helper.setVisible(R.id.lvdou_AudioWaveView, false)
            }
        }
    }

    companion object {

        var mVodBeans = ""

        const val VOD_BEAN = "vodBean"

        const val URL_INDEX = "urlIndex"

        const val PLAY_SOURCE_INDEX = "playInfoIndex"

        fun newInstance(
            vodBean: VodBean,
            urlIndex: Int,
            playSourceIndex: Int
        ): VideoDetailFragment = VideoDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(VOD_BEAN, vodBean)
                putInt(URL_INDEX, urlIndex)
                putInt(PLAY_SOURCE_INDEX, playSourceIndex)
            }
        }
    }


    private fun startCache() {
        val bottomSheetDialog = activity?.let {

            BottomSheetDialog(it)
        }
        val view: View = LayoutInflater.from(activity).inflate(R.layout.cache_all_list_layout, null)
        bottomSheetDialog?.setContentView(view)
        bottomSheetDialog?.window?.findViewById<View>(R.id.design_bottom_sheet)
            ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val cacheItem: ArrayList<Square> = ArrayList()
        if (vod_play_list.isNotEmpty()) {
            val playInfoBean = playActivity.playFrom
            val urlS = playInfoBean.urls
            if (urlS.isNotEmpty()) {
                for (index in urlS.indices) {
                    val urlBean = urlS[index]
                    val square = Square(urlBean.name) {
                        val downloadTitle = "${mVodBean.vodName}\t${urlBean.name}"
                        Log.e("TAG", "" + urlBean.url)
                        if (urlBean.url.contains(".m3u8?") || urlBean.url.endsWith(".m3u8")) {
                            val imageView = it.findViewById<ImageView>(R.id.status_tag)
                            imageView.setVisibility(View.VISIBLE)
                            imageView.setImageResource(R.drawable.ic_cache_down)
                            Toast.makeText(activity, "开始缓存第${urlBean.name}集", Toast.LENGTH_SHORT)
                                .show()
                            // 三个参数 下载地址 标题  封面图片
                            DownloadPresenter.addM3u8Task(
                                activity,
                                urlBean.url,
                                downloadTitle,
                                mVodBean.vod_pic
                            )
                        } else {
                            var iscache = false;
                            if (!isParse) {
                                it.isSelected = true
                                val imageView = it.findViewById<ImageView>(R.id.status_tag)
                                imageView.setVisibility(View.VISIBLE)
                                imageView.setImageResource(R.drawable.ic_cache_down)
                                isParse = true

                                // 链接转换
                                val parse = playInfoBean.player_info.parse2
                                JieXiUtils2.INSTANCE.getPlayUrl(
                                    parse,
                                    urlBean.url,
                                    curParseIndex,
                                    object : BackListener {
                                        override fun onSuccess(url: String, curParseIndex: Int) {
                                            isParse = false
                                            LogUtils.eTag(
                                                "TAG",
                                                "onSuccess: curParseIndex =  $curParseIndex url=${url}"
                                            )
                                            url.let {
                                                if (url.endsWith(".m3u8") || url.contains(".m3u8?")) {
                                                    if (!iscache) {
                                                        iscache = true
                                                        Toast.makeText(
                                                            activity,
                                                            "开始缓存${urlBean.name}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        // 三个参数 下载地址 标题  封面图片
                                                        DownloadPresenter.addM3u8Task(
                                                            activity,
                                                            it,
                                                            downloadTitle,
                                                            mVodBean.vod_pic
                                                        )
                                                    }

                                                } else {
                                                    ToastUtils.showLong("不支持此链接缓存。")
                                                }
                                            }

                                        }

                                        override fun onError() {
                                            isParse = false
                                            ToastUtils.showLong("解析失败，请尝试切换线路缓存")
                                        }

                                        override fun onProgressUpdate(msg: String?) {

                                        }
                                    },
                                    curFailIndex
                                )
                            } else {
                                ToastUtils.showLong("请等待上一个解析完在缓存")
                            }
                        }
                    }

                    square.isSelected = false
                    square.finished = false
                    val info: List<M3u8DownloadingInfo> =
                        DownloadPresenter.getM3u8DownLoading(urlS[index].url)
                    if (info.isNotEmpty()) {
                        //正在下载中
                        square.isSelected = true
                    }
                    val doneInfos: List<M3u8DoneInfo> =
                        DownloadPresenter.getM3u8Done(urlS[index].url)
                    if (doneInfos.isNotEmpty()) {
                        //已下载完成
                        square.isSelected = false
                        square.finished = true
                    }
                    cacheItem.add(square)
                }
                val selectedSet = TreeSet<Int>()
                val multiTypeAdapter = MultiTypeAdapter()
                multiTypeAdapter.register(Square::class.java, SquareViewBinder(selectedSet))
                val cacheItems = ArrayList<Any?>()
                cacheItems.addAll(cacheItem)
                multiTypeAdapter.items = cacheItems
                val allList: RecyclerView = view.findViewById(R.id.all_list)
                val title = view.findViewById<TextView>(R.id.title)
                // 查看下载
                val downCenter = view.findViewById<TextView>(R.id.down_center)
                downCenter.setOnClickListener {
                    //进入下载界面
                    activity?.let { it1 -> AllDownloadActivity.start(it1) }
                    bottomSheetDialog?.dismiss()
                }
                title.text = "缓存剧集"
                val close = view.findViewById<ImageView>(R.id.close)
                val gridLayoutManager = GridLayoutManager(activity, 3)
                gridLayoutManager.orientation = GridLayoutManager.VERTICAL
                allList.addItemDecoration(GridItemDecoration(activity, R.drawable.grid_item_decor))
                allList.layoutManager = gridLayoutManager
                allList.adapter = multiTypeAdapter
                bottomSheetDialog?.show()
                close.setOnClickListener {
                    bottomSheetDialog?.dismiss()
                }
            }
        }
    }

}
