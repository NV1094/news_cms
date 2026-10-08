//ProgressDialog 等结构性弃用：保留现有签到/展示行为
@file:Suppress("DEPRECATION")

package cn.lvdou.vod.ui.user

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.lvdou.vod.ApiConfig
import cn.lvdou.vod.App
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseFragment
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.observer.BaseObserver
import cn.lvdou.vod.base.observer.LoadingObserver
import cn.lvdou.vod.bean.*
import cn.lvdou.vod.download.SPUtils
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.ui.account.AccountSettingActivity
import cn.lvdou.vod.ui.collection.CollectionActivity
import cn.lvdou.vod.ui.down.AllDownloadActivity
import cn.lvdou.vod.ui.expand.ExpandCenterActivity
import cn.lvdou.vod.ui.feedback.FeedbackActivity
import cn.lvdou.vod.ui.login.LoginActivity
import cn.lvdou.vod.ui.notice.MessageCenterActivity
import cn.lvdou.vod.ui.pay.PayActivity
import cn.lvdou.vod.ui.play.PlayActivity
import cn.lvdou.vod.ui.score.PlayScoreActivity
import cn.lvdou.vod.ui.share.ShareActivity
import cn.lvdou.vod.ui.task.TaskActivity2
import cn.lvdou.vod.ui.withdraw.GoldWithdrawActivity
import cn.lvdou.vod.utils.*
import cn.lvdou.vod.utils.MMkvUtils.Companion.Builds
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bytedance.sdk.openadsdk.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.Gson
import com.youxiao.ssp.ad.bean.SSPAd
import com.youxiao.ssp.ad.core.AdClient
import com.youxiao.ssp.ad.listener.AdLoadAdapter
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

import me.yokeyword.fragmentation.SupportActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UserFragmentC : BaseFragment() {
    private val gonggao: TextView by lazy { requireView().findViewById(R.id.gonggao) }
    private val tv_user_servlce: TextView by lazy { requireView().findViewById(R.id.tv_user_servlce) }
    private val tv_user_sign: TextView by lazy { requireView().findViewById(R.id.tv_user_sign) }
    private val rvPlayScore: RecyclerView by lazy { requireView().findViewById(R.id.rvPlayScore) }
    private val tvLogin: TextView by lazy { requireView().findViewById(R.id.tvLogin) }
    private val tv_user_task: TextView by lazy { requireView().findViewById(R.id.tv_user_task) }
    private val tv_Message_Book: TextView by lazy { requireView().findViewById(R.id.tv_Message_Book) }
    private val shop_home: TextView by lazy { requireView().findViewById(R.id.shop_home) }
    private val tv_user_t2: TextView by lazy { requireView().findViewById(R.id.tv_user_t2) }
    private val tv_user_t3: TextView by lazy { requireView().findViewById(R.id.tv_user_t3) }
    private val tv_user_time: TextView by lazy { requireView().findViewById(R.id.tv_user_time) }
    private val iv_user_plc: ImageView by lazy { requireView().findViewById(R.id.iv_user_plc) }
    private val tv_user_phone: TextView by lazy { requireView().findViewById(R.id.tv_user_phone) }
    private val tv_user_share: TextView by lazy { requireView().findViewById(R.id.tv_user_share) }
    private val llCollect: TextView by lazy { requireView().findViewById(R.id.llCollect) }
    private val llPlayScore: LinearLayout by lazy { requireView().findViewById(R.id.llPlayScore) }
    private val liCache: TextView by lazy { requireView().findViewById(R.id.liCache) }
    private val llNotice: TextView by lazy { requireView().findViewById(R.id.llNotice) }
    private val iv_llNotice: TextView by lazy { requireView().findViewById(R.id.iv_llNotice) }
    private val tv_coin_withdraw: TextView by lazy { requireView().findViewById(R.id.tv_coin_withdraw) }
    private val tv_user_jifen: TextView by lazy { requireView().findViewById(R.id.tv_user_jifen) }
    private val iv_PlayScore: LinearLayout by lazy { requireView().findViewById(R.id.iv_PlayScore) }
    private val awvUser: ImageView by lazy { requireView().findViewById(R.id.awvUser) }

    private val playScoreAdapter: PlayScoreAdapter by lazy {
        PlayScoreAdapter().apply {
            setOnItemClickListener { adapter, _, position ->
                val item = adapter.getItem(position) as PlayScoreBean
                if (!UserUtils.isLogin()) {
                    LoginActivity.start()
                } else {
                    Builds().savePlayScore(item)
                    PlayActivity.startByPlayScoreResult(this@UserFragmentC, item.vodId);
                }
            }
        }
    }

    override var isUseEventBus: Boolean = true
    var isInit: Boolean = false
    var ggtime = ""

    override fun getLayoutId(): Int {
        return R.layout.fragment_userc
    }

    var playVideoReceiver: PlayVideoReceiver = PlayVideoReceiver()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initView() {
        super.initView()
        val filter = IntentFilter()
        //给意图过滤器添加action，就是要监听的广播对应的action
        filter.addAction("android.intent.action.AddPlayScore")
        mActivity.registerReceiver(playVideoReceiver, filter)

        val ad = Builds().loadStartBean("")?.ads
        val userTip = Builds().loadStartBean("")?.document?.notice?.content ?: ""
        if (userTip.isNotEmpty()) {
            gonggao.text = userTip
        }
        if (ad != null) {
            if (ad.service_qq == null || ad.service_qq.status == 0 || ad.service_qq.description.isNullOrEmpty()) {
                tv_user_servlce.visibility = View.GONE
            } else {
                tv_user_servlce.visibility = View.VISIBLE
                tv_user_servlce.setOnClickListener {
                    Log.d("serviceqq.description", "initView: " + ad.service_qq.description)
                    val link = "mqq://im/chat?chat_type=wpa&uin=${ad.service_qq.description}&version=1&src_type=web"
                    Intent(Intent.ACTION_VIEW, Uri.parse(link)).let {
                        if (it.resolveActivity(mActivity.packageManager) != null) {
                            ActivityUtils.startActivity(it)
                        } else {
                            ToastUtils.showShort("未安装QQ!!")
                        }
                    }
                }
            }
        }
        getMsgList()
        getGroupChatList()
        if (getTimeData() != null) {
            if (getTimeData() == Builds().loadUserSignTime()) {
                tv_user_sign.text = "已 签 到"
            }
        }

        rvPlayScore.layoutManager = LinearLayoutManager(activity).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        rvPlayScore.adapter = playScoreAdapter
    }

    /**
     * 信天翁
     * */

    private fun getStartData() {
        val ad = Builds().loadStartBean("")?.ads?.sdk_Insertscreen
        if (!App.SdkSwitch || ad == null || ad.status == 0 || ad.description.isNullOrEmpty()) {
            sign()
        } else {
            val adClient = AdClient(activity)
            val progressDialog = ProgressDialog.show(activity, "", "签到中...")
            adClient.requestInteractionAd(ad.description, object : AdLoadAdapter() {
                override fun onAdLoad(sspAd: SSPAd) {
                    super.onAdShow(sspAd)
                    progressDialog.dismiss()
                    sign()
                }

                override fun onError(p0: Int, p1: String?) {
                    progressDialog.dismiss()
                    sign()
                }
            })
        }
    }

    override fun initListener() {
        super.initListener()

        tvLogin.setOnClickListener {
            LoginActivity.start()
        }
        tv_user_task.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                TaskActivity2.start()
            }
        }

        tv_Message_Book.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                val message = ""
                FeedbackActivity.start(this@UserFragmentC.mActivity, message)
            }
        }

        shop_home.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                val intent = Intent(activity, PayActivity::class.java)
                intent.putExtra("type", 0)
                ActivityUtils.startActivity(intent)
            }
        }
        tv_user_t2.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                val intent = Intent(activity, PayActivity::class.java)
                intent.putExtra("type", 0)
                ActivityUtils.startActivity(intent)
            }
        }
        tv_user_t3.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                val intent = Intent(activity, PayActivity::class.java)
                intent.putExtra("type", 1)
                ActivityUtils.startActivity(intent)
            }
        }

        tv_user_time.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                val intent = Intent(activity, PayActivity::class.java)
                intent.putExtra("type", 1)
                ActivityUtils.startActivity(intent)
            }
        }

        iv_user_plc.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                ActivityUtils.startActivity(AccountSettingActivity::class.java)
            }
        }

        tv_user_phone.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                ActivityUtils.startActivity(AccountSettingActivity::class.java)
            }
        }

        tv_user_share.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                Toast.makeText(activity, "扫码即可下载APP哦~~~", Toast.LENGTH_SHORT).show()
                val intn = Intent(this.mActivity, ShareActivity::class.java)
                intn.putExtra("vom_name", "有奖推广活动")
                intn.putExtra("vod_pic", "www") //图片
                val share_description = Builds().loadStartBean("")?.ads?.share_description
                val share_description_txt = if (share_description == null || share_description.status == 0 || share_description.description.isNullOrEmpty()) {
                    "1、普通用户分享成功可获得积分奖励\n2、代理用户分销成功可获得金币奖励"
                } else {
                    share_description.description
                }
                intn.putExtra("vod_blurd",share_description_txt) //注释
                intn.putExtra("vod_class", "分享给未安装过的用户注册并打开应用算分享成功") //标签
                startActivity(intn)
            }
        }

        tv_user_sign.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                getStartData()
            }
        }

        llCollect.setOnClickListener {
            CollectionActivity.start()
        }

        llPlayScore.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                val intent = Intent(activity, PlayScoreActivity::class.java)
                startActivityForResult(intent, 2)
            }
        }

        liCache.setOnClickListener {
            if (LoginUtils.checkLogin(activity)) {
                activity?.let { it1 -> AllDownloadActivity.start(it1) }
            }

        }
        llNotice.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                val intent = Intent(activity, MessageCenterActivity::class.java)
                startActivity(intent)
                iv_llNotice.visibility = View.GONE
                Builds().saveGongGao(ggtime) //储存被点击的公告时间
            }
        }

        tv_coin_withdraw.setOnClickListener {
            if (!UserUtils.isLogin()) {
                LoginActivity.start()
            } else {
                ActivityUtils.startActivity(GoldWithdrawActivity::class.java)
            }
        }
    }

    @Subscribe
    @Suppress("UNUSED_PARAMETER") //EventBus 回调签名要求该参数
    fun onLoginSucces(data: LoginBean? = null) {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this, vodService.userInfo(), object : BaseObserver<UserInfoBean>() {
            override fun onSuccess(data: UserInfoBean) {
                updateUserInfo(data)
                UserUtils.userInfo = data
                getPlayScore()
                EventBus.getDefault().post(data)//通知改变信息
            }

            override fun onError(e: ResponseException) {
            }
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            UserUtils.userInfo = null
            updateUserInfo()
            getPlayScore()

            if (UserUtils.isLogin()) {
                onLoginSucces()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserInfo()
        getPlayScore()
        getAdsClass()
        if (UserUtils.isLogin()) {
            onLoginSucces()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //从我的页面点击视频播放界面和视频播放记录页面
        if (requestCode == 1 || requestCode == 2) {
            getPlayScore()
        }
    }

    class PlayVideoReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i(javaClass.name.toString(), "onReceive playscore")
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTimeData(): String? {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd") //2021-12-28
        val date = Date() //创建一个date对象保存当前时间
        return simpleDateFormat.format(date) //当前日期
    }
    private fun voDd() {
        val nreWard = Builds().loadStartBean("")!!.ads.sdk_nreward
        if (nreWard != null || nreWard?.status == 1 || !nreWard?.description.isNullOrEmpty()) {
            val preferences: SharedPreferences = mActivity.getSharedPreferences("wqddg", SupportActivity.MODE_PRIVATE)
            val edit = preferences.edit()
            val wqddg: Int = preferences.getInt("wqddg", 0)
            edit.putInt("wqddg", wqddg + nreWard.description.toInt())
            edit.apply()
        }
    }
    private fun sign() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this, vodService.sign(), object : BaseObserver<GetScoreBean>() {
            @SuppressLint("SimpleDateFormat")
            override fun onSuccess(data: GetScoreBean) {
                if (data.score == "0") {
                    ToastUtils.showShort(R.string.sign_success)
                } else {
                    ToastUtils.showShort("签到成功，获得${data.score}积分")
                }
                tv_user_sign.text = "已 签 到"
                val dateStr = getTimeData()
                if (dateStr != null) {
                    Builds().saveUserSignTime(dateStr)
                }
                voDd()
                onLoginSucces()
            }

            override fun onError(e: ResponseException) {
                val dateStr = getTimeData()
                if (dateStr != null) {
                    Builds().saveUserSignTime(dateStr)
                }
                tv_user_sign.text = "已 签 到"
                ToastUtils.showShort(e.getErrorMessage())
            }
        })
    }

    private fun updateUserInfo(data: UserInfoBean? = null) {
        if (UserUtils.isLogin()) {
//            getExpandList()
            tvLogin.visibility = View.GONE
//            tv_user_vip.visibility = View.VISIBLE
//            tv_user_vips.visibility = View.VISIBLE
            tv_user_phone.visibility = View.VISIBLE
            iv_PlayScore.visibility = View.VISIBLE
            tv_user_time.visibility = View.VISIBLE
        } else {
            tvLogin.visibility = View.VISIBLE
//            tv_user_vip.visibility = View.GONE
//            tv_user_vips.visibility = View.GONE
            iv_PlayScore.visibility = View.GONE
            tv_user_phone.visibility = View.INVISIBLE
            tv_user_time.visibility = View.INVISIBLE
//            tv_user_jinbi.text = "0"
            tv_user_jifen.text = "当前积分 0"
//            tv_user_video.text = "0"
        }

        data?.let {
            val isVIp = it.group.group_name.contains("VIP")
            SPUtils.setBoolean(activity, "isVip", isVIp)
            if (it.user_gold == null || it.user_gold == "") {
                it.user_gold = "0"
            }
            tv_user_time.text = "到期时间:  ${DateUtil.getyyyyMMddHHmm(it.user_end_time*1000)}"
            if (!SPUtils.getBoolean(activity,"isVip")){
                tv_user_time.text = "普通会员"
                tv_user_t3.text = "  立即开通>"
            }
            tv_user_phone.text = it.user_name
            Builds().saveUserNickname(data.user_nick_name)
            tv_user_jifen.text = "当前积分 "+"${it.user_points}"
            var user_pic = it.user_portrait
            Log.d("TAGuser_pic", user_pic)
            if (user_pic == null) {
                Log.d("TAGuser_pic2", "$user_pic")
                user_pic = "app/logo_user.png"
            }
            Glide.with(this)
                    .load(ApiConfig.MOGAI_BASE_URL + "/" + user_pic)
                    .error(R.drawable.monkey_avatar)
                    .centerCrop()
                    .override(imageWidthSize, imageHeightSize) //默认淡入淡出动画
                    .transition(DrawableTransitionOptions.withCrossFade()) //缓存策略,跳过内存缓存【此处应该设置为false，否则列表刷新时会闪一下】
                    .skipMemoryCache(false) //缓存策略,硬盘缓存-仅仅缓存最终的图像，即降低分辨率后的（或者是转换后的）
                    .diskCacheStrategy(DiskCacheStrategy.ALL) //设置图片加载的优先级
                    .priority(Priority.HIGH)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(iv_user_plc)
        }
    }

    //用于计算图片的宽高值
    private var imageWidthSize = 0
    private var imageHeightSize = 0

    private fun getAdsClass() {
        val ad = Builds().loadStartBean("")?.ads
        if (ad == null || ad.user_center == null || ad.user_center.status == 0 || ad.user_center.description.isNullOrEmpty() || UserUtils.isLogin()) {
            awvUser.visibility = View.GONE
            rvPlayScore.visibility = View.VISIBLE
        } else {
            awvUser.visibility = View.VISIBLE
            awvUser.setOnClickListener {
                if (!UserUtils.isLogin()) {
                    LoginActivity.start()
                } else {
                    val intent = Intent(activity, ExpandCenterActivity::class.java)
                    ActivityUtils.startActivity(intent)
                }
            }
            rvPlayScore.visibility = View.GONE
            Glide.with(mContext)
                    .load(ad.user_center.description)
                    .into(awvUser)
        }
    }

    //为了更好的释放资源，在页面销毁时调用release方法释放资源
    override fun onDestroy() {
        super.onDestroy()
        if (App.SdkSwitch){
            AdClient(activity).release()
        }
    }

    private fun getPlayScore() {
        val playScoreBeans = ArrayList<PlayScoreBean>()
        if (UserUtils.isLogin()) {
            val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

            RequestManager.execute(this, vodService.getPlayLogList("1", "12"),
                    object : LoadingObserver<Page<PlayLogBean>>(this@UserFragmentC.mActivity) {
                        override fun onSuccess(data: Page<PlayLogBean>) {
                            val playLogBeans = data.list
                            playLogBeans.forEach {
                                val playScoreBean: PlayScoreBean = PlayScoreBean()
                                playScoreBean.vodName = it.vod_name
                                playScoreBean.vodImgUrl = it.vod_pic
                                if (it.percent.equals("NaN")) {
                                    playScoreBean.percentage = 0.0f
                                } else {
                                    try {
                                        playScoreBean.percentage = it.percent.toFloat()
                                    } catch (ex: Exception) {
                                    }
                                }
                                playScoreBean.typeId = it.type_id;
                                playScoreBean.vodId = it.vod_id.toInt();
                                playScoreBean.isSelect = false
                                playScoreBean.vodSelectedWorks = it.nid.toString()
                                playScoreBean.urlIndex = it.urlIndex
                                playScoreBean.curProgress = it.curProgress
                                playScoreBean.playSourceIndex = it.playSourceIndex
                                val gson: Gson = Gson()
                                val playScoreBeanStr = gson.toJson(playScoreBean).toString();
                                Log.i("playlog11", "playScoreBean${playScoreBeanStr}")
                                playScoreBeans.add(playScoreBean)
                                if (playScoreBeans.size > 10) {
                                    playScoreAdapter.setNewData(playScoreBeans.subList(0, 10))
                                } else {
                                    playScoreAdapter.setNewData(playScoreBeans)
                                }

                            }

                            if (data.list.size == 0) {
                                playScoreAdapter.setNewData(playScoreBeans)
                            }
                            Log.i("playlog", "getPlayLogList11${data}");
                        }

                        override fun onError(e: ResponseException) {
                            Log.i("playlog", "getPlayLogList222")

                        }
                    }
            )
        }
    }

    private class PlayScoreAdapter :
            BaseQuickAdapter<PlayScoreBean, BaseViewHolder>(R.layout.item_play_score_horizontal) {
        override fun convert(helper: BaseViewHolder, item: PlayScoreBean?) {
            item?.run {
                val name = if (item.typeId == 3) {
                    "$vodName $vodSelectedWorks"
                } else if (item.typeId == 1) {
                    "$vodName"
                } else {
                    "$vodName ${vodSelectedWorks}"
                }
                helper.setText(R.id.tvName, name)
                helper.setText(R.id.tvPlayProgress, "${(percentage * 100).toInt()}%")
                val mation: MultiTransformation<Bitmap> = MultiTransformation(
                        CenterCrop(),
                        RoundedCornersTransformation(20, 0, RoundedCornersTransformation.CornerType.ALL)
                )
                Glide.with(helper.itemView.context)
                        .load(vodImgUrl)
                        .error(R.drawable.error)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .apply(RequestOptions.bitmapTransform(mation))
                        .into(helper.getView<View>(R.id.ivImg) as ImageView)
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(): UserFragmentC {
            val args = Bundle()
            val fragment = UserFragmentC()
            fragment.arguments = args
            return fragment
        }
    }

    private fun getMsgList() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this, vodService.getMsgList(),
                object : BaseObserver<MessageBean>() {
                    override fun onSuccess(data: MessageBean) {
                        val list = data.list
                        if (list.size > 0 ) {
                            iv_llNotice.text = list.size.toString()
                        }

                        for (i in list.indices) {
                            when (i) {
                                0 -> {
                                    if (Builds().loadGongGao() != list[0].create_date) {
                                        iv_llNotice.visibility = View.VISIBLE
                                    }else{
                                        iv_llNotice.visibility = View.GONE
                                    }
                                    ggtime = list[0].create_date //获得最新一条公告时间
                                }
                            }
                        }
                    }

                    override fun onError(e: ResponseException) {
                        llNotice.visibility = View.GONE
                    }

                })
    }

    //获取群聊列表
    private fun getGroupChatList() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(
                this,
                vodService.groupChat(),
                object : BaseObserver<GroupChatBean>() {
                    override fun onSuccess(data: GroupChatBean) {
                        val list = data.list
                        for (i in list.indices) {
                            when (i) {
                                0 -> {
//                                    item_tv_playinfo_feedback.text = list[0].title
//                                    item_tv_playinfo_feedback.setOnClickListener {
//                                        val intent = Intent(context, X5WebActivity::class.java)
//                                        val bundle = Bundle()
//                                        bundle.putString("url", list[0].url)
//                                        bundle.putString("title", list[0].title)
//                                        intent.putExtras(bundle)
//                                        ActivityUtils.startActivity(intent)
//                                    }
                                }
                                1 -> {
//                                    llPotato.visibility = View.VISIBLE
//                                    line_potato.visibility = View.VISIBLE
//                                    tv_potato.text = list[1].title
//                                    llPotato.setOnClickListener {
//                                        gotoWeb(list[1].url)
//                                    }
                                }
                                2 -> {
//                                    llPlane.visibility = View.VISIBLE
//                                    line_plane.visibility = View.VISIBLE
//                                    tv_plane.text = list[2].title
//                                    llPlane.setOnClickListener {
//                                        gotoWeb(list[2].url)
//                                    }
//                                    iv_user_title.visibility = View.VISIBLE
                                }
                                3 -> {
//                                    if (list[3].title.contains("|")) {
//                                        iv_tuiguang.visibility = View.VISIBLE
//                                        val list1 = (list[3].title).split('|')
//                                        guanggao1.text = list1[0]
//                                        guanggao2.text = list1[1]
//                                        guanggao.setOnClickListener {
//                                            val intent = Intent(context, X5WebActivity::class.java)
//                                            val bundle = Bundle()
//                                            bundle.putString("url", list[3].url)
//                                            bundle.putString("title", list[3].title)
//                                            intent.putExtras(bundle)
//                                            ActivityUtils.startActivity(intent)
//                                        }
//                                    } else {
//                                        iv_tuiguang.visibility = View.GONE
//                                    }
                                }
                            }
                        }
                    }

                    override fun onError(e: ResponseException) {

                    }
                }
        )
    }
}
