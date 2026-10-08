package cn.lvdou.vod.ui.play

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cn.lvdou.av.CheckVodTrySeeBean
import cn.lvdou.av.MyPlayerManager
import cn.lvdou.av.play.*
import cn.lvdou.av.play.AvVideoController.RECEIVER_TYPE_REPLAY
import cn.lvdou.av.play.AvVideoController.RECEIVER_TYPE_TIMER
import cn.lvdou.vod.ApiConfig
import cn.lvdou.vod.App
import cn.lvdou.vod.R
import cn.lvdou.vod.ad.AdWebView
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.observer.BaseObserver
import cn.lvdou.vod.bean.*
import cn.lvdou.vod.entity.AdvEntity
import cn.lvdou.vod.jiexi.BackListener
import cn.lvdou.vod.jiexi.JieXiUtils2
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.pip.PIPManager
import cn.lvdou.vod.ui.dlan.DlanListPop
import cn.lvdou.vod.ui.feedback.FeedbackActivity
import cn.lvdou.vod.ui.login.LoginActivity
import cn.lvdou.vod.ui.pay.PayActivity
import cn.lvdou.vod.ui.widget.AdControlView
import cn.lvdou.vod.ui.widget.HitDialog
import cn.lvdou.vod.utils.*
import cn.lvdou.vod.utils.MMkvUtils.Companion.Builds
import com.blankj.utilcode.util.*
import com.blankj.utilcode.util.StringUtils
import com.dueeeke.videocontroller.component.GestureView
import com.dueeeke.videoplayer.player.VideoView
import com.dueeeke.videoplayer.player.VideoViewManager
import com.github.StormWyrm.wanandroid.base.net.observer.PlayLoadingObserver
import com.iflytek.cloud.ErrorCode
import com.iflytek.cloud.InitListener
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.ui.RecognizerDialog
import com.iflytek.cloud.ui.RecognizerDialogListener
import com.liuwei.android.upnpcast.NLUpnpCastManager
import com.liuwei.android.upnpcast.device.CastDevice
import com.liuwei.android.upnpcast.util.CastUtils
import com.lxj.xpopup.XPopup
import com.youxiao.ssp.ad.bean.SSPAd
import com.youxiao.ssp.ad.core.AdClient
import com.youxiao.ssp.ad.listener.AdLoadAdapter
import com.youxiao.ssp.ad.listener.RewardVideoAdAdapter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class NewPlayActivity : BaseActivity(), OnSpeedItemClickListener {
    private val TAG = "NewPlayActivity"
    private val frameLayout: FrameLayout by lazy { findViewById(R.id.frameLayout) }
    private val imgCloseStopAd: ImageView by lazy { findViewById(R.id.imgCloseStopAd) }
    private val stopdH: AdWebView by lazy { findViewById(R.id.stopdH) }
    private val webviewV: AdWebView by lazy { findViewById(R.id.webviewV) }
    private val layoutAdv: ConstraintLayout by lazy { findViewById(R.id.layoutAdv) }
    private val awvPlayerDown: AdWebView by lazy { findViewById(R.id.awvPlayerDown) }
    private val item_frame: FrameLayout by lazy { findViewById(R.id.item_frame) }
    private val ad_layout: FrameLayout by lazy { findViewById(R.id.ad_layout) }
    private lateinit var controller: AvVideoController
    private lateinit var mVodBean: VodBean
    private var isShowPlayProgress = false
    private var isCanPlayAd2 = true
    open var curPlayUrl: String = ""
    private var isAllowCastScreen: Boolean = false//根据视频的类型，来判断是否可以投屏
    private var curParseIndex = 0//记录上一次解析到的位置，如果出现解析到是视频不能播放的话 自动解析下一条
    private var curFailIndex = -1
    private var isPlay = false//是否正在播放
    open var playSourceIndex = 0//播放源位置
    private var urlIndex = 0//当前播放的到哪一集
    private lateinit var playFormList: List<PlayFromBean>
    open lateinit var playFrom: PlayFromBean //当前播放播放源信息
    open var playList: List<UrlBean>? = null//当前播放列表
    private var playScoreInfo: PlayScoreBean? = null
    private var isParseSuccess = false
    private var isSeekToHistory: Boolean = false

    private var curProgressHistory: Long = 0
    private var vodDuration: Long = 0
    private var videoNetProgress: Long = 0L
    private var pipManager: PIPManager? = null
    private var mIsShowing: Boolean? = false
    private var IsSwitchkernel: Boolean = false
    private var lastTime_getdanmu: Long = 0
    private var player_pause_ad: Boolean = false
    private val adClient = AdClient(this@NewPlayActivity)

    private fun mscLoum(content: String?) {
        controller.visibility_wqddg(content)
    }

    private fun getDanmu(atTime: Long) {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        if (AgainstCheatUtil.showWarn(vodService)) {
            loadingDanmu = false
            return
        }
        val void_id = mVodBean.vod_id.toString() + urlIndex.toString()
        val void_id_int = void_id.toInt()
        RequestManager.execute(this,
            vodService.getDanMuList(
                void_id_int, 100, 0, atTime
            ),
            object : BaseObserver<DanMuBean>(false) {
                override fun onSuccess(data: DanMuBean) {
                    videoView.removeAllDanmakus()
                    videoView.closes()
                    loadingDanmu = false
                    //过滤脏数据：服务端历史数据 at_time 全为 2147483647(INT_MAX)
                    //若不过滤，弹幕会被排到24天后(所有人看不到)，且 lastTime 永久大于播放进度导致拉取停摆
                    val validList = data.list?.filter { it.at_time > 0 && it.at_time < 2147483647L }
                    if (!validList.isNullOrEmpty()) {
                        for (danmuBean in validList) {
                            videoView.addDanmaku(
                                danmuBean.content,
                                danmuBean.color,
                                danmuBean.at_time - videoView.currentPosition
                            )
                        }
                        lastTime_getdanmu = validList.last().at_time
                    } else {
                        //没有有效弹幕就延时30秒获取
                        lastTime_getdanmu += 30000
                    }
                }

                override fun onError(e: ResponseException) {
                    loadingDanmu = false
                    Log.e("TAG", e.getErrorMessage() ?: "")
                }
            })
    }

    /**
     * 信天翁
     * */

    private fun getStartData() {
        val ads = Builds().loadStartBean("")!!.ads
        if (!App.SdkSwitch || ads.sdk_excitation == null || ads.sdk_excitation.status == 0 || ads.sdk_excitation.description.isNullOrEmpty()) {
            controller.visibility_wqddg()
        } else {
            val nreWard: String =
                if (ads.sdk_nreward == null || ads.sdk_nreward.status == 0 || ads.sdk_nreward.description.isNullOrEmpty()) {
                    "1"
                } else {
                    ads.sdk_nreward.description
                }
            val progressDialog = ProgressDialog.show(mActivity, null, "奖励领取中...")
            val preferences: SharedPreferences =
                mActivity.getSharedPreferences("wqddg", MODE_PRIVATE)
            val edit = preferences.edit()
            adClient.requestRewardAd(
                ads.sdk_excitation.description,
                object : RewardVideoAdAdapter() {
                    override fun onReward(type: Int) {
                        super.onReward(type)
                        edit.putInt("wqddg", nreWard.toInt())
                        edit.apply()
                    }

                    override fun loadRewardAdSuc(var1: SSPAd?) {
                        edit.putInt("wqddg", nreWard.toInt())
                        edit.apply()
                        progressDialog.dismiss()
                    }

                    override fun rewardVideoClosed() {
                        edit.putInt("wqddg", nreWard.toInt())
                        edit.apply()
                        parseData()
                    }

                    override fun loadRewardAdFail(var1: String) {
                        progressDialog.dismiss()
                        edit.putInt("wqddg", 1)
                        edit.apply()
                    }
                })
        }
    }

    private val onJiexiResultListener = object : BackListener {
        override fun onSuccess(url: String?, curParseIndex: Int) {
            if (isSuccess) {
                Log.d(TAG, "====ParseonFail  \n url=$url")
                return
            } else {
                Log.d(TAG, "====ParseonSuccess  \n url=$url")
            }
            curFailIndex = curParseIndex
            if (url == null || url.isEmpty()) {
                println("===修复onSuccess")
                runOnUiThread {
                    chengeNextLine()
                }
                return
            }
            url.let {
                if (!isPlay) {
                    Log.d(TAG, "====ParseonSuccess  play（） url=$it")
                    play(it)
                    curPlayUrl = it
                    isPlay = true
                }
            }
        }

        override fun onError() {
            Log.e("解析错误", "1111111111111111")
            controller.updateJiexiProgess("获取资源失败,请换来源或者联系客服解决！")
            controller.showPlayErrorView()

        }

        override fun onProgressUpdate(msg: String?) {
            controller.updateJiexiProgess(msg)
        }
    }
    private var videoDetailFragment: VideoDetailFragment? = null
    private var summaryFragment: SummaryFragment? = null
    private var playListFragment: PlayListFragment? = null
    private var isParsed: Boolean = false
    private var isLandscape = false//当前是否为横屏
    private var timerCount: MyCount? = null
    private lateinit var videoView: AvVideoView
    override fun getLayoutResID(): Int {
        return R.layout.activity_new_play
    }

    //是否正在获取弹幕
    var loadingDanmu: Boolean = false
    override fun initView() {
        super.initView()
        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.player_status_color))
        videoView = VideoViewManager.instance().get("pip") as AvVideoView
        controller = AvVideoController(videoView, this)
        //添加手势
        val gestureControlView = GestureView(this)
        controller.addControlComponent(gestureControlView)
        pipManager = PIPManager.getInstance()
        videoView.setVideoController(controller)
        if (pipManager?.isStartFloatWindow!!) {
            mIsShowing = true
            pipManager?.stopFloatWindow()
            controller.setPlayerState(videoView.currentPlayerState)
            controller.setPlayState(videoView.currentPlayState)
            mVodBean = pipManager?.vodBean as VodBean
            isShowPlayProgress = pipManager?.getShouldProgress!!

        } else {
            mIsShowing = false
            mVodBean = intent.getParcelableExtra<VodBean>(PlayActivity.KEY_VOD) as VodBean
            isShowPlayProgress = intent.getBooleanExtra(PlayActivity.KEY_SHOW_PROGRESS, false)
            pipManager?.actClass = NewPlayActivity::class.java
        }
        frameLayout.addView(videoView)
        registerReceiver()
        play_img_url()
    }

    override fun initListener() {
        super.initListener()
        imgCloseStopAd.setOnClickListener {
            stopdH.visibility = View.GONE
            imgCloseStopAd.visibility = View.GONE
            controller.isPlayStopAd(false)
        }

        videoView.setOnSeekListeners { currentPos, newPos ->
            //后退播放，重新加载弹幕
            if (newPos < currentPos) {
                videoView.removeAllDanmakus()
                loadingDanmu = false
                videoView.closes()
                lastTime_getdanmu = 0
            } else {
                lastTime_getdanmu = newPos.coerceAtLeast(lastTime_getdanmu);
            }
        }

        controller.setControllerClickListenerGKDY(//长按切速新加的
            object : ControllerClickListener {
                override fun onClick(it: View) {
                    getStartData()
                }

                override fun onLongPress() {
                    TODO("Not yet implemented")
                }

                override fun onSingleTapUp() {
                    TODO("Not yet implemented")
                }
            }
        )
//        controller.setControllerClickListenerGKDY { //原来的
//                getStartData()
//        }

        controller.setControllerClickListenerWZ(object : ControllerClickListenerWZ {
            override fun onClick(view: View?) {
                TODO("Not yet implemented")
            }

            override fun onPlayProgress(duration: Int, position: Int) {
                //当播放进度距离最后一条弹幕小于10秒请求弹幕，也就是说，弹幕播放完毕后才会再次请求
                if (!loadingDanmu && (lastTime_getdanmu - position < 1000 * 10) && videoView.isDanmuEnable) {
                    loadingDanmu = true //防止频繁请求
                    if (lastTime_getdanmu > 0) {
                        lastTime_getdanmu = position.toLong().coerceAtLeast(lastTime_getdanmu)
                    }
                    val attime = if (lastTime_getdanmu > 0) lastTime_getdanmu + 1 else 0
                    getDanmu(attime)
                }
            }


            override fun onMsc(content: String?) {
                val permissions = arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                )
                player_pause_ad = true
                if (Build.VERSION.SDK_INT >= 23) {
                    //判断是否有权限 第一个参数 上下文 第二个参数具体的权限
                    val i: Int = ContextCompat.checkSelfPermission(
                        this@NewPlayActivity,
                        Manifest.permission.RECORD_AUDIO
                    )
                    //如果返回结果为0 则权限已经拥有
                    if (i == PackageManager.PERMISSION_GRANTED) {
                        changeIntoText()
                    } else {
                        Permissions.checkPermission(this@NewPlayActivity, permissions)
                    }
                } else {
                    changeIntoText()
                }
            }

            override fun onSendDanmu(content: String?, textcolor: String?) {
                //修复：原 TODO() 运行即抛 NotImplementedError
                content?.takeIf { it.isNotEmpty() }?.let { sendDanmu(it) }
            }

            override fun isLogin(): Boolean {
                return UserUtils.isLogin()
            }

        })

        controller.setControllerPlayIngLisenter(object : ControllerPlayIngLisenter {
            override fun onPlayIng(position: Int, total: Int) {
                val wei = CastUtils.getIntTime(Builds().loadPlayTiaoZhuan2(mVodBean.vod_id))
                val tou = CastUtils.getIntTime(Builds().loadPlayTiaoZhuan(mVodBean.vod_id))
                if (total == position && total != 0) {
                    playNext()
                }
                if (wei < total * 1000 - wei && total != 0 && wei != 0L && wei + tou < total * 1000) {
                    if ((total - position) * 1000 <= wei) {
                        Toast.makeText(this@NewPlayActivity, "自动跳过片尾。", Toast.LENGTH_SHORT).show()
                        playNext()
                    }
                }
            }

            override fun playPrepared() {
                // prepared()
            }

        })

        controller.setControllerClickListener(
            object : ControllerClickListener { //长按切速
                override fun onClick(it: View) { //长按切速
                    when (it.id) {
                        //解析错误提交错误信息
                        R.id.btError -> {
                            val message =
                                "视频《${mVodBean.vod_name}》播放失败\n播放源：${playFrom.player_info.show}\n视频序列：${playList!![urlIndex].name}"
//                            FeedbackActivity.start(this, message)
                            FeedbackActivity.start(this@NewPlayActivity, message)
                        }
                        //播放失败换源
                        R.id.btReplay -> {
                            // parseData()
                            if (playSourceIndex + 1 > playFormList.count() - 1) {
                                Toast.makeText(this@NewPlayActivity, "没有其他播放源了", Toast.LENGTH_SHORT).show()
                                return
                            }


                            val playFromBean = playFormList[playSourceIndex + 1]
                            changePlaySource(playFromBean)
                        }
//仿外部换源
//                            showPlaySourceDialog()
//旧版bug换源
//                        {
//                            // parseData()
//                            if (playSourceIndex + 1 > playFormList.count() - 1) {
////                            if (this.playSourceIndex + 1 > playFormList.count() - 1) {
////                                Toast.makeText(this, "没有其他播放源了.", Toast.LENGTH_SHORT).show()
//                                Toast.makeText(
//                                    this@NewPlayActivity,
//                                    "没有其他播放源了.",
//                                    Toast.LENGTH_SHORT
//                                ).show()
////                                return@setControllerClickListener
//                            }
//
//                            val playFromBean = playFormList[playSourceIndex + 1]
//                            changePlaySource(playFromBean)
//                        }

                        R.id.tv_av_hd ->
                            chengeNextLine()
                        R.id.iv_av_back, R.id.iv_av_back1, R.id.iv_av_back2 -> {
                            Log.i("bds", "back===========")
                            finish()
                            Builds().savePlayScore(null)
                            playScoreInfo = null
                            savePlayRecord(true)
                            setResult(3)
                        }
                        R.id.imgTz, R.id.imgTzH -> {
                            HitDialog(mActivity).setTitle("跳过片头/尾").setInputView(mVodBean.vod_id)
                                .setOnHitDialogClickListener(object :
                                    HitDialog.OnHitDialogClickListener() {
                                    override fun onCancelClick(dialog: HitDialog) {
                                        super.onCancelClick(dialog)
                                    }

                                    override fun onTz(dialog: HitDialog, tou: String, wei: String) {
                                        super.onTz(dialog, tou, wei)
                                        val patt = "\\d\\d:\\d\\d:\\d\\d".toRegex()
                                        if (patt.matches(tou)) {
                                            Builds().savePlayTiaoZhuan(tou, mVodBean.vod_id)
                                        } else {
                                            Toast.makeText(
                                                this@NewPlayActivity,
                                                "请输入正确时间格式：00:00:00",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return
                                        }
                                        if (patt.matches(wei)) {
                                            Builds().savePlayTiaoZhuan2(wei, mVodBean.vod_id)
                                        } else {
                                            Toast.makeText(
                                                this@NewPlayActivity,
                                                "请输入正确时间格式：00:00:00",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return
                                        }
                                        dialog.dismiss()
//                                         videoView.seekTo(speed)
                                    }
                                }).show()
                        }
                        //画中画
                        R.id.iv_av_pip -> {
                            if (Builds().loadStartBean("")?.ads?.pictureinpicture?.status == 1) {
                                if (isPlay) {
                                    if (!LoginUtils.checkLogin2(mActivity)) {
                                        HitDialog(mActivity).setTitle("提示")
                                            .setMessage("需登录后开通VIP才可使用画中画，确定登录。")
                                            .setOnHitDialogClickListener(object :
                                                HitDialog.OnHitDialogClickListener() {
                                                override fun onCancelClick(dialog: HitDialog) {
                                                    super.onCancelClick(dialog)
                                                }

                                                override fun onOkClick(dialog: HitDialog) {
                                                    super.onOkClick(dialog)
                                                    ActivityUtils.startActivity(LoginActivity::class.java)
                                                }
                                            }).show()
                                    } else {
                                        if (LoginUtils.checkVIP(
                                                this@NewPlayActivity,
                                                "画中画需要开通vip,是否去开通"
                                            )
                                        ) {
                                            requePer {
                                                Log.e("画中画", "开启")
                                                controller.huaTest()
                                                pipManager?.vodBean = mVodBean
                                                pipManager?.setShouldProgress(isShowPlayProgress)
                                                pipManager?.startFloatWindow(getPipMsgBean())
                                                pipManager?.resume()
                                                finish()
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(mActivity, "等待播放地址获取完成在尝试。", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } else {
                                if (isPlay) {
                                    requePer {
                                        Log.e("画中画", "开启")
                                        controller.huaTest()
                                        pipManager?.vodBean = mVodBean
                                        pipManager?.setShouldProgress(isShowPlayProgress)
                                        pipManager?.startFloatWindow(getPipMsgBean());
                                        pipManager?.resume();
                                        finish()
                                    }
                                } else {
                                    Toast.makeText(mActivity, "等待播放地址获取完成在尝试。", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                        R.id.iv_av_next ->
                            playNext()
                        R.id.tv_av_speed ->
                            showSpeedListDialog(it.tag as Int)
                        R.id.tv_av_selected ->
                            showPlayListDialog()
                        R.id.tvPlaySource ->
                            showPlaySourceDialog()
                        R.id.iv_av_miracast -> {
                            if (Builds().loadStartBean("")?.ads?.projection?.status != 0) {
                                if (!LoginUtils.checkLogin2(mActivity)) {
                                    HitDialog(mActivity!!).setTitle("提示")
                                        .setMessage("需登录后开通VIP才可投屏，确定登录。")
                                        .setOnHitDialogClickListener(object :
                                            HitDialog.OnHitDialogClickListener() {
                                            override fun onCancelClick(dialog: HitDialog) {
                                                super.onCancelClick(dialog)
                                            }

                                            override fun onOkClick(dialog: HitDialog) {
                                                super.onOkClick(dialog)
                                                ActivityUtils.startActivity(LoginActivity::class.java)
                                            }
                                        }).show()
                                } else {
                                    if (LoginUtils.checkVIP(
                                            this@NewPlayActivity,
                                            "投屏需要开通vip,是否去开通"
                                        )
                                    ) {
                                        showCastScreenDialog()
                                    }
                                }
                            } else {
                                showCastScreenDialog()
                            }
                        }


                        R.id.tvPayButton, R.id.tvEndPayButton -> {
                            payPlay()
                        }
                        R.id.tvUpdateButton, R.id.tvEndUpdateButton -> {
                            updateVip()
                        }
                        R.id.btn_pop_danmaku -> {
                            val s = it.tag as String
                            sendDanmu(s)
                        }
                    }
                }

                override fun onLongPress() {
                    videoView.setVideoSpeed(3.0f)
                }

                override fun onSingleTapUp() {
                    when (Speeds) {
                        "0.5X" -> {
                            videoView.setVideoSpeed(0.50f)
                        }
                        "0.75X" -> {
                            videoView.setVideoSpeed(0.75f)
                        }
                        "1.0X" -> {
                            videoView.setVideoSpeed(1.00f)
                        }
                        "1.25X" -> {
                            videoView.setVideoSpeed(1.25f)
                        }
                        "1.5X" -> {
                            videoView.setVideoSpeed(1.50f)
                        }
                        "2.0X" -> {
                            videoView.setVideoSpeed(2.00f)
                        }
                        else -> {
                            videoView.setVideoSpeed(1.00f)
                        }
                    }
                }
            }
        )

        videoView.setOnStateChangeListener(object : VideoView.OnStateChangeListener {
            override fun onPlayStateChanged(playState: Int) {
                Log.e("测试", "当前播放状态=${playState}")
                if (playState == VideoView.STATE_PLAYBACK_COMPLETED) {
                    Log.e("测试", "STATE_PLAYBACK_COMPLETED")
                    val percentage = getPercentage(curProgressHistory, vodDuration)
                    println("进度9：=" + controller.percentage + "  2:" + playScoreInfo?.curProgress + " 3=" + curProgressHistory + " 4=" + percentage)
                    if (percentage <= 0.01f || percentage >= 0.99f) {
                        println("进度5：==")
                        playNext()
                    } else {
                        println("进度1：==" + curProgressHistory)
                        controller.setReplayByCurProgress(true)
                    }
                } else if (playState == VideoView.STATE_PREPARED) {
                    prepared()
                } else if (playState == VideoView.STATE_PAUSED) {
                    if (controller.clickivPlay && !player_pause_ad) {
                        //暂停显示广告
                        if (Builds().loadStartBean("")?.ads?.player_pause?.status == 1) {
                            val ad = Builds().loadStartBean("")?.ads?.sdk_Insertscreen
                            if (!App.SdkSwitch || ad == null || ad.status == 0 || ad.description.isNullOrEmpty()) {
                                if (isLandscape) { //原来的广告
                                    controller.startPlayStopHAd(Builds().loadStartBean("")?.ads?.player_pause?.description)
                                } else {
                                    stopdH.visibility = View.VISIBLE
                                    imgCloseStopAd.visibility = View.VISIBLE
                                    stopdH.loadDataWithBaseURL(
                                        null,
                                        Builds().loadStartBean("")?.ads?.player_pause?.description ?: "",
                                        "text/html",
                                        "utf-8",
                                        null
                                    )
                                    controller.isPlayStopAd(true)
                                }
                            } else {
                                getStartData(ad.description)
                            }
                        }
                    }
                } else if (playState == VideoView.STATE_PLAYING) {
                    //隐藏暂停广告
                    stopdH.visibility = View.GONE
                    imgCloseStopAd.visibility = View.GONE
                    controller.stopPlayStopHAd()
                } else if (playState == VideoView.STATE_ERROR) {
                    LogUtils.d("=====问题 video OnError")
                    controller.setReplayByCurProgress(true)
                    isSeekToHistory = true
                    curParseIndex++
                    isCanPlayAd2 = false
                    parseData()
                }
            }

            /**
             * 信天翁
             * */

            private fun getStartData(description: String) {
                //无需判断SDK类型
                //val adClient = AdClient(mActivity)
                adClient.requestInteractionAd(description, object : AdLoadAdapter() {
                    override fun onAdShow(sspAd: SSPAd) {
                        super.onAdShow(sspAd)
                    }

                    override fun onError(p0: Int, p1: String?) {
                    }
                })
            }

            override fun onPlayerStateChanged(playerState: Int) {
                val pausead = Builds().loadStartBean("")?.ads?.player_pause
                //播放暂停广告
                if (playerState == VideoView.PLAYER_NORMAL) {
                    isLandscape = false
                    if (controller.isPlayStopAd()) {
                        stopdH.visibility = View.VISIBLE
                        imgCloseStopAd.visibility = View.VISIBLE
                        stopdH.loadDataWithBaseURL(
                            null,
                            pausead?.description ?: "",
                            "text/html",
                            "utf-8",
                            null
                        )
                        controller.stopPlayStopHAd()
                        controller.isPlayStopAd(true)
                    }
                } else if (playerState == VideoView.PLAYER_FULL_SCREEN) {
                    isLandscape = true
                    if (controller.isPlayStopAd()) {
                        stopdH.visibility = View.GONE
                        imgCloseStopAd.visibility = View.GONE
                        controller.startPlayStopHAd(pausead?.description)
                    }
                }
            }
        })
    }

    var advData: AdvEntity? = Builds().loadAdvEntity("")
    var advControl: AdControlView? = null
    override fun initData() {
        super.initData()
        // 拉取云端全局片头跳过配置（异步，先读缓存立即可用，不阻塞播放）
        CloudSkipConfig.fetch()
        getVideoDetail()
    }

    /**
     * 视频准备完成
     */
    private fun prepared() {
        isParseSuccess = true
        vodDuration = controller.duration
        if (isShowPlayProgress) {
            if (vodDuration > playScoreInfo?.curProgress ?: 0) {
                videoView.seekTo(playScoreInfo?.curProgress ?: 0)
            }
            println("进度3：==" + playScoreInfo?.curProgress)
            isShowPlayProgress = false
        } else {
            if (isSeekToHistory) {
                videoView.seekTo(curProgressHistory)
                println("进度2：==" + curProgressHistory)
            } else {
                //跳过片头：优先手动设置，其次云端按播放器名称的全局配置
                val wei = CastUtils.getIntTime(Builds().loadPlayTiaoZhuan2(mVodBean.vod_id))
                var tou = CastUtils.getIntTime(Builds().loadPlayTiaoZhuan(mVodBean.vod_id))
                if (videoNetProgress == 0L) {
                    if (tou == 0L && this::playFrom.isInitialized) {
                        // 未手动设置时，按当前播放器名称取云端配置（未命中试 "*" 兜底）
                        tou = CloudSkipConfig.getSkipMs(playFrom.player_info?.show ?: "")
                        if (tou != 0L) {
                            Log.d("CloudSkipConfig", "命中云端片头配置: ${playFrom.player_info?.show} -> ${tou}ms")
                        }
                    }
                    if (vodDuration > tou && tou != 0L && tou + wei < vodDuration) {
                        Toast.makeText(this@NewPlayActivity, "自动跳过片头。", Toast.LENGTH_SHORT).show()
                        videoView.seekTo(tou)
                    }

                } else {
                    if (vodDuration > videoNetProgress) {
                        videoView.seekTo(videoNetProgress)
                    }
                }
                println("进度4：== videoNetProgress=" + videoNetProgress)
            }
        }

        println("进度12：==$vodDuration")
        when (SPUtils.getInstance().getInt(AvVideoController.KEY_SPEED_INDEX, 3)) {
            0 -> {
                videoView.speed = 2f
                controller.setSpeed("2.00")
            }
            1 -> {
                videoView.speed = 1.5f
                controller.setSpeed("1.50")
            }
            2 -> {
                videoView.speed = 1.25f
                controller.setSpeed("1.25")
            }
            3 -> {
                videoView.speed = 1f
                controller.setSpeed("1.00")
            }
            4 -> {
                videoView.speed = 0.75f
                controller.setSpeed("0.75")
            }
            5 -> {
                videoView.speed = 0.5f
                controller.setSpeed("0.50")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        videoView.resume()
        if (isParsed) {
            checkVodTrySee()
        }
    }

    override fun onResume() {
        super.onResume()
        checkVodTrySee()
        pipManager?.resume()
    }

    override fun onPause() {
        pipManager?.pause()
        super.onPause()

    }


    override fun onDestroy() {
        super.onDestroy()
        JieXiUtils2.INSTANCE.stopGet()
        if (pipManager != null && !pipManager?.isStartFloatWindow!!) {
            videoView.release()
        }
        pipManager?.reset()
        controller.onDestroy()
        timerCount?.cancel()
        lbm?.unregisterReceiver(localReceiver)
        cancelTimer()
        if (App.SdkSwitch) {
            adClient.release()
        }
    }

    override fun onBackPressedSupport() {
        if (pipManager != null && pipManager?.onBackPress()!!) {
            return
        }
        if (!videoView.onBackPressed()) {
            try {
                recordPlay()//播放记录可能为空
            } catch (e: Exception) {
            } finally {
                super.onBackPressedSupport()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            controller.clickivPlay = false
            if (controller.videoState) {
                Builds().savePlayScore(null)
                playScoreInfo = null
                savePlayRecord(true)
                setResult(3)
                controller.huaTest()
                finish()
            } else {
                controller.huaTest()
            }
            false
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    fun getPipMsgBean(): PipMsgBean {
        val voidid = mVodBean.vod_id.toString()
        val vodSelectedWorks = this@NewPlayActivity.playList!![urlIndex].name
        var playSource = ""
        if (mVodBean.vod_play_list != null) {
            val playFromBean = mVodBean.vod_play_list!!.get(playSourceIndex)
            val playerInfo = playFromBean.player_info
            val urls = playFromBean.urls
            playSource = playerInfo.show
        }
        if (StringUtils.isEmpty(playSource)) {
            playSource = "默认"
        }
        var percentage = controller.percentage.toString()
        if (percentage == "NaN") {
            percentage = "0.0"
        }
        val urlIndex = this@NewPlayActivity.urlIndex

        var pipMsgBean = PipMsgBean()
        pipMsgBean.voidid = voidid
        pipMsgBean.vodSelectedWorks = vodSelectedWorks
        pipMsgBean.playSource = playSource
        pipMsgBean.percentage = percentage
        pipMsgBean.urlIndex = urlIndex
        pipMsgBean.curPregress = curProgressHistory
        pipMsgBean.playSourceIndex = playSourceIndex
        return pipMsgBean
    }

    fun showSummary() {
        if (summaryFragment == null) {
            summaryFragment = SummaryFragment.newInstance(mVodBean)
            supportFragmentManager.beginTransaction()
                .add(R.id.flContainer, summaryFragment!!)
                .commitAllowingStateLoss()
        } else {
            supportFragmentManager.beginTransaction()
                .show(summaryFragment!!)
                .commitAllowingStateLoss()
        }
    }

    fun hideSummary() {
        supportFragmentManager.beginTransaction()
            .hide(summaryFragment!!)
            .commitAllowingStateLoss()
    }

    fun showVideoDetail() {
        if (videoDetailFragment == null) {
            videoDetailFragment =
                VideoDetailFragment.newInstance(mVodBean, urlIndex, playSourceIndex)
            supportFragmentManager.beginTransaction()
                .add(R.id.flContainer, videoDetailFragment!!)
                .commitNowAllowingStateLoss()
        } else {
            videoDetailFragment?.changeCurIndex(urlIndex)
            supportFragmentManager.beginTransaction()
                .show(videoDetailFragment!!)
                .commitNowAllowingStateLoss()
        }
    }

    fun showPlayList() {
        if (playListFragment == null) {
            val spanCount = if (mVodBean.type_id == 3) {
                2
            } else {
                5
            }
            playListFragment = PlayListFragment.newInstance(spanCount).apply {
                if (playList != null) {
                    showPlayList(playList!!, urlIndex)
                }
            }

            supportFragmentManager.beginTransaction()
                .add(R.id.flContainer, playListFragment!!)
                .commitAllowingStateLoss()
        } else {
            playListFragment?.run {
                if (playList != null) {
                    showPlayList(playList!!, urlIndex)
                }
            }
            supportFragmentManager.beginTransaction()
                .show(playListFragment!!)
                .commitAllowingStateLoss()
        }
    }

    fun hidePlayList() {
        supportFragmentManager.beginTransaction()
            .hide(playListFragment!!)
            .commitAllowingStateLoss()
    }

    fun showNewVideo(vodBean: VodBean) {
        savePlayRecord(false)
        curProgressHistory = 0
        videoNetProgress = 0
        recordPlay()//缓存上一个视频的进度
        Builds().savePlayScore(null)
        playScoreInfo = null
        mVodBean = vodBean
        supportFragmentManager.beginTransaction()
            .remove(videoDetailFragment!!)
            .commitNowAllowingStateLoss()
        videoDetailFragment = null

        if (summaryFragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(summaryFragment!!)
                .commitNowAllowingStateLoss()
            summaryFragment = null

        }

        if (playListFragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(playListFragment!!)
                .commitNowAllowingStateLoss()
            playListFragment = null
        }

        videoView.release()
        controller.setTitle(mVodBean.vodName)
        getVideoDetail()
    }

    fun changeSelection(position: Int, isNeedOrder: Boolean) {
        urlIndex = position//更改当前选集
        loadingDanmu = false
        videoView.closes()
        lastTime_getdanmu = 0;
        curProgressHistory = 0
        videoNetProgress = 0
        isSeekToHistory = false
        curFailIndex = -1
        this.curParseIndex = 0
        LogUtils.d("=====问题 changeSelection")
        parseData()
    }

    fun changeVideoUrlIndex(position: Int = -1) {

        videoDetailFragment?.changeCurIndex(urlIndex)
        this.curParseIndex = 0
        curFailIndex = -1
    }

    fun changePlaySource(playFromBean: PlayFromBean, playSourceIndex: Int) {

        this.playFrom = playFromBean
        this.playList = playFrom.urls


        if (urlIndex > playFrom.urls.count() - 1) {
            urlIndex = playFrom.urls.count() - 1
            changeVideoUrlIndex(playFrom.urls.count())
        }
        this.playSourceIndex = playSourceIndex
        this.curParseIndex = 0
        curFailIndex = -1
        LogUtils.d("=====问题 changePlaySource")
        parseData()
        videoDetailFragment?.changePlaysource(playSourceIndex)
    }

    fun changePlaySource(playFromBean: PlayFromBean) {

        this.playFrom = playFromBean
        this.playList = playFrom.urls

        this.playSourceIndex++
        if (this.playSourceIndex > playFormList.count() - 1) {
            Toast.makeText(this, "没有其他播放源了.", Toast.LENGTH_SHORT).show()
            return
        }
        if (urlIndex > playFrom.urls.count() - 1) {
            Toast.makeText(this, "该源没有当前集数，自动切换下一个播放源.", Toast.LENGTH_SHORT).show()
            changePlaySource(playFrom)
            return
        }
        this.curParseIndex = 0
        curFailIndex = -1
        LogUtils.d("=====问题 changePlaySource")
        parseData()
        videoDetailFragment?.changePlaysource(playSourceIndex)
    }

    fun castScreen(device: CastDevice) {
        if (isParseSuccess && curPlayUrl.isNotEmpty()) {
            val vodUrl = if (curPlayUrl.startsWith("//")) {
                "https:$curPlayUrl"
            } else {
                curPlayUrl
            }
            NLUpnpCastManager.getInstance().connect(device);
            Intent(this, CastScreenActivity2::class.java).apply {
                putExtra("vod", mVodBean)
                putExtra("playSourceIndex", playSourceIndex)
                putExtra("urlIndex", urlIndex)
                putExtra("vodurl", vodUrl)
                putExtra("vodLong", controller.duration)
                println("vodurl+=${vodUrl}")
                val newPlayFromList = ArrayList<PlayFromBean>()
                playFormList.map {
                    newPlayFromList.add(it)
                }
                putParcelableArrayListExtra("playFormList", newPlayFromList)
                ActivityUtils.startActivity(this)
            }
        } else {
            runOnUiThread {
                ToastUtils.showShort("正在解析中...")
            }
        }
    }

    private fun changeTitle() {
        var title = mVodBean.vod_name
        if (mVodBean.type_id == 2) {
            if (playList != null) {
                title += " ${playList!![urlIndex].name}"
            }
        }
        controller.setTitle(title)
    }

    private fun chengeNextLine() {
        curParseIndex++
        parseData()
    }

    private fun chengeNextLineFromHead() {
        isCanPlayAd2 = false
        curParseIndex = 0
        curFailIndex = -1
        LogUtils.d("=====问题 chengeNextLineFromHead")
        parseData()
    }

    private fun showSpeedListDialog(pos: Int) {
        SpeedListDialog(mActivity, this, pos).show()
    }

    private fun showPlayListDialog() {
        if (playList != null) {
            PlayListDialog(mActivity, urlIndex, playList!!)
                .show()
        }
    }

    private fun showPlaySourceDialog() {
        PlaySourceDialog(mActivity, playSourceIndex, playFormList)
            .show()
    }

    //    private fun showCastScreenDialog()
//    {
//        val dialogHeight = if (isLandscape) {
//            -1
//        } else {
//            videoView.height
//        }
//        ProjectionActivity.start(this, 333)
//    }
    private fun showCastScreenDialog() {
        isAllowCastScreen = true
        if (isAllowCastScreen) {
            val danListPop = DlanListPop(this@NewPlayActivity) {
                castScreen(it)
            }

            XPopup.Builder(this@NewPlayActivity)
                .asCustom(danListPop)
                .show()
        } else {
            runOnUiThread {
                ToastUtils.showShort("暂无投屏权限请升级会员或购买")
            }
        }
    }

    private fun play_img_url() {
        val playImg: String //!App.SdkSwitch ||
        val nreWard: String
        val excitation: String
        val ad = Builds().loadStartBean("")?.ads
        if (ad != null) {
            if (ad.play_logo == null || ad.play_logo.status == 0 || ad.play_logo.description.isNullOrEmpty()) {
                playImg = "0"
            } else {
                playImg = ad.play_logo.description
            }
            if (ad.sdk_nreward == null || ad.sdk_nreward.status == 0 || ad.sdk_nreward.description.isNullOrEmpty()) {
                nreWard = "0"
            } else {
                nreWard = ad.sdk_nreward.description
            }
            if (ad.sdk_excitation == null || ad.sdk_excitation.status == 0) {
                excitation = "0"
            } else {
                excitation = ad.sdk_excitation.status.toString()
            }
        } else {
            playImg = "0"
            nreWard = "0"
            excitation = "0"
        }
        controller.visibility_wqddg(playImg, nreWard, excitation, ApiConfig.UNFEIYUYIN)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getVideoDetail() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        RequestManager.execute(this,
            vodService.getVideoDetail(mVodBean.vod_id, 10, AUtils.sign(), AUtils.keyTime()),
            object : PlayLoadingObserver<VodBean>(mActivity) {
                override fun onSuccess(data: VodBean) {
                    mVodBean = data
                    curParseIndex = 0
                    curFailIndex = -1
                    playScoreInfo = Builds().loadPlayScore("")
                    urlIndex = playScoreInfo?.urlIndex ?: 0//获取播放记录中的选集
                    playSourceIndex = playScoreInfo?.playSourceIndex ?: 0//获取播放源
                    val playInfo = data.playInfo
                    pipManager = PIPManager.getInstance()
                    if (mIsShowing!!) {
                        urlIndex = pipManager?.pipMsg?.urlIndex ?: 0//获取播放记录中的选集
                        playSourceIndex = pipManager?.pipMsg?.playSourceIndex ?: 0//获取播放源
                        videoNetProgress = pipManager?.pipMsg?.curPregress ?: 0
                        curProgressHistory = videoNetProgress
                    } else {
                        if (playInfo != null) {
                            playSourceIndex = playInfo.playSourceIndex
                            urlIndex = playInfo.urlIndex
                            videoNetProgress = playInfo.curProgress
                            curProgressHistory = videoNetProgress
                        }
                    }
                    playFormList = data.vod_play_list
                    if (playSourceIndex + 1 > playFormList.count()) {
                        playSourceIndex = 0
                    }
                    if (data.vod_play_list.isNullOrEmpty() || data.vod_play_url.isEmpty() || data.getVod_play_from()
                            .equals("no")
                    ) {
                        HitDialog(this@NewPlayActivity)
                            .setTitle(StringUtils.getString(R.string.tip))
                            .setMessage("无播放地址，联系客服添加")
                            .setOnHitDialogClickListener(object :
                                HitDialog.OnHitDialogClickListener() {
                                override fun onCancelClick(dialog: HitDialog) {
                                    super.onCancelClick(dialog)
                                    finish()
                                }

                                override fun onOkClick(dialog: HitDialog) {
                                    super.onOkClick(dialog)
                                    finish()
                                }
                            })
                            .show()
                        return
                    }
                    if (data.vod_play_list != null) {
                        playFrom = data.vod_play_list!![playSourceIndex]
                    }
                    playList = playFrom.urls
                    LogUtils.d("=====问题 getVideoDetail")
                    if (!mIsShowing!!) {
                        mIsShowing = false
                        parseData()
                    } else {
                        isParseSuccess = true
                        isPlay = true
                    }
                    showVideoDetail()
                }

                override fun onError(e: ResponseException) {
                    finish()
                }

            })
    }

    /**
     * 信天翁
     * */

    private fun getAdv(url: String) {
        if (timerCount != null) {
            timerCount?.cancel()
            if (controller.videoState) {
                findViewById<ConstraintLayout>(R.id.layoutAdv).visibility = View.INVISIBLE
            } else {
                controller.stopPayHAdv()
            }
            timerCount = null
        }
        getAdvSdk()
        releaseAdVideo()
        //如果是会员直接播放视频,或者是不需要播放广告(已经播放过了)
        if (!isCanPlayAd2) {
            play2(url)
            return
        }

        if (LoginUtils.checkLogin2(mActivity)) {
            if (cn.lvdou.vod.download.SPUtils.getBoolean(mActivity, "isVip")) {
                //开始播放视频
                play2(url)
                return
            }
        }

        val code = advData?.html?.code
        if (advData?.ad_select.isNullOrEmpty() || advData?.ad_select.isNullOrBlank()) {
            play2(url)
            return
        }

        if (advData?.ad_select == "html") {
            val timeout = (advData?.html?.timeout)
            if (timeout?.equals(0)!!) {
                play2(url)
                return
            }

            if (controller.videoState) {
                layoutAdv.visibility = View.VISIBLE
                webviewV.loadDataWithBaseURL(null, code ?: "", "text/html", "utf-8", null)
            } else {
                controller.startPlayHAdv(code)
            }

            timerCount = MyCount(
                    mActivity,
                    (timeout * 1000).toLong(),
                    1000L,
                    object : MyCount.timerFinsihLisenter {
                        override fun onFinish() {
//                  倒计时结束，开始播放视频
                            if (controller.videoState) {
                                findViewById<ConstraintLayout>(R.id.layoutAdv).visibility =
                                        View.INVISIBLE
                            } else {
                                controller.stopPayHAdv()
                            }
                            play2(url)
                        }

                        override fun onTicks(mill: Long) {
                            if (controller.videoState) {
                                findViewById<TextView>(R.id.ad_timeV).text =
                                        "${mill / 1000}s | vip可关闭该广告"
                            } else {
                                controller.setAdvTime(mill)
                            }

                        }
                    })
            timerCount?.start()
        } else if (advData?.ad_select == "video") {
            val click_urls = advData?.video?.click_url?.split("|")?.toTypedArray()
            val vodurls = advData?.video?.url?.split("|")?.toTypedArray()
            if (click_urls?.size == 0 || vodurls?.size == 0 || !vodurls?.size?.equals(click_urls?.size)!!) {
                play2(url)
                return
            }
            val ra = (0..(click_urls?.size!! - 1 ?: 0)).random()
            advControl = AdControlView(mActivity)
            advControl?.setListener(object : AdControlView.AdControlListener {
                override fun onAdClick() {
                    val intent = Intent()
                    intent.setAction(Intent.ACTION_VIEW)
                    //跳转到外部链接的测试地址
                    intent.setData(Uri.parse(click_urls?.get(ra) ?: ""))
                    if (intent.resolveActivity(packageManager) != null) {
                        val componentName = intent.resolveActivity(packageManager)
                        Log.e("componentName=", "=================" + componentName)
                        startActivity(Intent.createChooser(intent, "请选择浏览器"))
                    } else {
                        ToastUtils.showShort("未安装浏览器")
                    }
                }

                override fun onSkipAd() {
                    //点击跳过
                }
            })
            runOnUiThread {
                //正式数据替换这个视频广告链接
                val testVideoUrl = vodurls.get(ra)
                videoView.setUrl(testVideoUrl)
                if (isSuccess) {
                    videoView.setOnStateChangeListener(object : VideoView.OnStateChangeListener {
                        override fun onPlayStateChanged(playState: Int) {
                            if (playState == VideoView.STATE_PLAYBACK_COMPLETED) {
                                Log.e("播放广告", "结束了")
                                releaseAdVideo()
                                play2(url)
                            } else if (playState == VideoView.STATE_ERROR) {
                                releaseAdVideo()
                                play2(url)
                            }
                        }

                        override fun onPlayerStateChanged(playerState: Int) {
                        }

                    })
                    controller.startPlayVideoAdv()
                    controller.setAllView(false)
                    controller.addControlComponent(advControl)
                    videoView.setVideoController(controller)
                    VideoViewManager.instance().setPlayOnMobileNetwork(true)
                    if (!controller.isShowHint) {
                        videoView.start()
                        controller.startPlay()
                    }
                }
            }
        } else { //既然来到这里，哪肯定是开启的
            val preferences: SharedPreferences =
                    mActivity.getSharedPreferences("lvdous", MODE_PRIVATE)
            val edit = preferences.edit()
            var lvdous: Int = preferences.getInt("lvdous", 0)
            if (lvdous > 0) {
                lvdous -= 1
                edit.putInt("lvdous", lvdous)
                edit.apply()
                play2(url)
                return
            }

            /**
             * 信天翁
             * */

            var isVip = false
            if (UserUtils.isLogin() && UserUtils.userInfo?.group_id == 3) {
                isVip = true
            }

            val timeout = (advData?.sdkad?.timeout)
            val ad = Builds().loadStartBean("")?.ads?.sdk_Insertscreen
            if (isVip || !App.SdkSwitch || ad == null || ad.status != 1 || ad.description.isNullOrEmpty()) { //未开启插屏
                play2(url)
            } else {
                //val adClient = AdClient(mActivity)
                adClient.requestInteractionAd(ad.description, object : AdLoadAdapter() {
                    override fun onAdLoad(sspAd: SSPAd) {
                        super.onAdShow(sspAd)
                        if (timeout != null && timeout > 0) { //如果奖励次数是0
                            edit.putInt("lvdous", timeout)
                            edit.apply()
                        }
                        play2(url)
                    }

                    override fun onError(p0: Int, p1: String?) {
                        play2(url)
                    }
                })
            }
        }
    }

    private fun getAdvSdk() {
        val startBean = Objects.requireNonNull(Builds().loadStartBean(""))

        if (startBean != null && startBean.ads != null) {
            val ads = startBean.ads
            if (!App.SdkSwitch || ads.sdk_banner == null || ads.sdk_banner.status != 1 || ads.sdk_banner.description.isNullOrEmpty()) {
                if (ads.player_down != null && ads.player_down.status == 1 && !ads.player_down.description.isNullOrEmpty()) {
                    awvPlayerDown.loadHtmlBody(ads.player_down.description)
                    awvPlayerDown.visibility = View.VISIBLE
                }
            } else {
                adClient.requestBannerAd(
                    item_frame,
                    ads.sdk_banner.description,
                    object : AdLoadAdapter() {
                        override fun onAdLoad(ad: SSPAd) {
                            super.onAdLoad(ad)
                            item_frame.visibility = View.VISIBLE
                        }

                        override fun onError(var1: Int, var2: String) {
                            if (ads.player_down != null && ads.player_down.status == 1 && !ads.player_down.description.isNullOrEmpty()) {
                                awvPlayerDown.loadHtmlBody(ads.player_down.description)
                                awvPlayerDown.visibility = View.VISIBLE
                            }
                        }
                    })
            }

            if (!App.SdkSwitch || ads.sdk_informationflow == null || ads.sdk_informationflow.status != 1 || ads.sdk_informationflow.description.isNullOrEmpty()) {
                ad_layout.visibility = View.GONE
            } else {
                //val adClient = AdClient(mActivity)
                adClient.requestExpressAd(
                    ads.sdk_informationflow.description,
                    object : AdLoadAdapter() {
                        override fun onAdLoad(ad: SSPAd) {
                            super.onAdLoad(ad)
                            ad_layout.removeAllViews()
                            ad_layout.addView(ad.view)
                            ad_layout.visibility = View.VISIBLE
                        }
                    })
            }
        } else {
            ad_layout.visibility = View.GONE
            item_frame.visibility = View.GONE
        }
    }

    /**
     * 释放视频广告
     */
    private fun releaseAdVideo() {
        if (advData?.ad_select == "video" && advControl != null) {
            controller.removeControlComponent(advControl)
            videoView.release()
            controller.setAllView(true)
        }
    }

    private fun play2(url: String) {
        isCanPlayAd2 = true
        val head = playFrom.player_info.headers ?: ""
        val issetheadstr = playFrom.player_info.issethead ?: ""
        val issetheadlist = issetheadstr.split("||")
        val heads = head.split("||")
        val headers: MutableMap<String, String>? = HashMap()
        var isSetHead = false
        issetheadlist.forEach {
            if (url.matches(it.toRegex())) {
                isSetHead = true
                return@forEach
            }
        }
        if (isSetHead) {
            heads.forEach {
                val kv = it.split("=>")
                if (kv.size == 2) {
                    println(kv.get(0) + "1234567321" + kv.get(1))
                    headers?.put(kv.get(0), kv.get(1))
                }

            }
        }

        var url = url
        println(url + "1234567321")
        if (url.startsWith("//")) {
            url = "https:$url"
        }

        if (playFrom.player_info.kernel ?: "0" != "0") {
            videoView = MyPlayerManager.checktFactory(playFrom.player_info.kernel ?: "1", videoView)
        }

        videoView.setUrl(url, headers)
        videoView.clearDisappearingChildren();
        controller.setTitle(mVodBean.vodName + "   " + (playList?.get(urlIndex)?.name ?: ""))
        VideoViewManager.instance().setPlayOnMobileNetwork(true)
        if (!controller.isShowHint) {
            videoView.start()
            controller.startPlay()
        }
    }

    private fun checkVodTrySee() {
        if (playList == null) {
            return
        }
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        if (AgainstCheatUtil.showWarn(vodService)) {
            return
        }
        RequestManager.execute(mActivity,
            vodService.checkVodTrySee(
                mVodBean.vod_id.toString(),
                1.toString(),
                playList!![urlIndex].nid.toString()
            ),
            object : BaseObserver<CheckVodTrySeeBean>() {
                override fun onSuccess(data: CheckVodTrySeeBean) {
                    var isVip = false
                    if (UserUtils.isLogin() && UserUtils.userInfo?.group_id == 3) {
                        isVip = true
                    }
                    val preferences: SharedPreferences =
                        mActivity.getSharedPreferences("wqddg", MODE_PRIVATE)
                    val edit = preferences.edit()
                    var wqddg: Int = 0
                    wqddg = preferences.getInt("wqddg", 0)
                    if (wqddg > 0) {
                        isVip = true
                        wqddg -= 1
                        edit.putInt("wqddg", wqddg)
                        edit.commit()
                    }
                    val status = data.status
                    isAllowCastScreen = status == 0 || (isVip && status == 1)
                    controller.CheckVodTrySeeBean(
                        data.user_video,
                        data,
                        isVip,
                        mVodBean.vod_points_play
                    )
                }

                override fun onError(e: ResponseException) {
                    isAllowCastScreen = false
                }
            })
    }

    private fun parseData() {
        IsSwitchkernel = false
        checekPlayVip()
        LogUtils.d("=====问题 parseData")
        if (isPlay) {
            videoView.release()
        }
        isParseSuccess = false
        isPlay = false
        // showPlayerAd()
        showAnnouncement()
        // 开始解析地址
        val features = playFrom.player_info.features?.split(",")
        var parse = playFrom.player_info.parse2
        if (Builds().loadJieXiApi() != "") {
            parse = Builds().loadJieXiApi() + "," + parse
        }
        var url = ""
        if (playList != null) {
            url = playList!![urlIndex].url
        }
        LogUtils.d("", "====Parse start url=" + url + "  parse=" + parse)
        checkVodTrySee()
        changeTitle()
        var isStraightChain = false
        features?.forEach {
            if (url.matches(it.toRegex())) {
                isStraightChain = true
                return@forEach
            }
        }
        if (isStraightChain) {
            isPlay = true
            curPlayUrl = url
            play(url)
        } else {
            isSuccess = false
            controller.showJiexi()
            LogUtils.d("", "====Parse start url=" + "正常进这？？" + parse)
            JieXiUtils2.INSTANCE.getPlayUrl(
                parse,
                url,
                curParseIndex,
                onJiexiResultListener,
                curFailIndex
            )
        }
    }

    private fun checekPlayVip(): Boolean {
        if (mVodBean.type.type_name.contains(regex = "[Vv][Ii][Pp]".toRegex())) {
            if (cn.lvdou.vod.download.SPUtils.getBoolean(this@NewPlayActivity, "isVip")) {
                controller.isShowVipHint(false)
                return true
            } else {
                controller.isShowVipHint(true)
                return false
            }
        }
        return false
    }

    private fun showPlayerAd() {
        Builds().loadAppConfigAd("")?.let {
            if (it.img.isNotEmpty()) {
                controller.showAd(it.img, it.url)
            }
        }
    }

    private fun showAnnouncement() {
        runOnUiThread {
            Builds().loadStartBean("")?.document?.roll_notice?.let {
                if (it.content.isNotEmpty() && it.status == "1") {
                    controller.showAnnouncement(it.content)
                }
            }
        }
    }

    private var isSuccess = false
    private fun play(url: String) {
        LogUtils.d("---play----测试11111111111111")
        if (isPlay) {
            videoView.release()
        }
        isSuccess = true
        startTimer()
        controller.hideJiexi()
        LogUtils.d("", "====Parse play url=$url")
        println("====Parse play url=$url")
        videoView.post {
            getAdv(url)
        }
    }

    //将当前记录缓存下来
    private fun recordPlay() {
        if (playScoreInfo == null) {
            playScoreInfo = PlayScoreBean().apply {
                vodId = mVodBean.vod_id
                typeId = mVodBean.type_id
                vodName = mVodBean.vod_name
                vodImgUrl = mVodBean.vod_pic
                percentage = controller.percentage
                curProgress = controller.curProgress
                playSourceIndex = this@NewPlayActivity.playSourceIndex
                if (playList != null) {
                    urlIndex = this@NewPlayActivity.urlIndex
                    vodSelectedWorks = this@NewPlayActivity.playList!![urlIndex].name
                }
                save()
            }
        } else {
            playScoreInfo?.run {
                percentage = controller.percentage
                curProgress = controller.curProgress
                playSourceIndex = this@NewPlayActivity.playSourceIndex
                if (playList != null) {
                    urlIndex = this@NewPlayActivity.urlIndex
                    vodSelectedWorks = this@NewPlayActivity.playList!![urlIndex].name
                }
                saveOrUpdate("vodId = ?", mVodBean.vod_id.toString())
            }
        }
    }


    private fun savePlayRecord(isClose: Boolean) {
        val curProgress = controller.curProgress
        if (curProgress == 0L) {
            if (isClose) {
                finish()
            }
            return
        }
        if (curProgress != 0L) {
            curProgressHistory = curProgress
        }

        val voidid = mVodBean.vod_id.toString()
        Log.e(TAG, "======voidid===$voidid")

        var percentage = controller.percentage.toString()
        if (percentage == "NaN") {
            percentage = "0.0"
        }

        println("进度 ---savePlayRecord---  curProgress=" + curProgress)

        if (this@NewPlayActivity.playList.isNullOrEmpty() && isClose) {
            finish()
            return
        }

        val urlIndex = this@NewPlayActivity.urlIndex
        val vodSelectedWorks = this@NewPlayActivity.playList!![urlIndex].name
        var playSource = ""
        if (mVodBean.vod_play_list != null) {
            val playFromBean = mVodBean.vod_play_list!!.get(playSourceIndex)
            val playerInfo = playFromBean.player_info
            val urls = playFromBean.urls
            playSource = playerInfo.show
        }
        if (StringUtils.isEmpty(playSource)) {
            playSource = "默认"
        }

        if (UserUtils.isLogin()) {
            val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
            if (AgainstCheatUtil.showWarn(vodService)) {
                return
            }
            Log.d(
                TAG,
                "voidid=${voidid}  vodSelectedWorks=${vodSelectedWorks}  playSource=${playSource}  percentage=${percentage} curProgress=${curProgress}"
            )
            var urlIndex_temp = urlIndex
            if (!videoDetailFragment?.isOrder!!) {
                urlIndex_temp = playList!!.size - 1 - urlIndex
            }
            RequestManager.execute(this,
                vodService.addPlayLog(
                    voidid,
                    vodSelectedWorks,
                    playSource.toString(),
                    percentage,
                    urlIndex_temp.toString(),
                    curProgress.toString(),
                    playSourceIndex.toString()
                ),
                object : BaseObserver<UserVideo>() {
                    override fun onSuccess(data: UserVideo) {
                        Log.i("play", "addPlayLogsucess")
                        val intent = Intent("android.intent.action.AddPlayScore")
                        sendBroadcast(intent)
                        if (isClose) {
                            finish()
                        }
                    }

                    override fun onError(e: ResponseException) {
                        Log.i("play", "addPlayfaied")
                        if (isClose) {
                            finish()
                        }
                    }
                })
            println("watchVideoLong==$watchVideoLong")
            if (watchVideoLong != 0) {
                RequestManager.execute(this, vodService.addWatchTime(watchVideoLong),
                    object : BaseObserver<GetScoreBean>() {
                        override fun onSuccess(data: GetScoreBean) {
                            if (data.score != "0") {
                                runOnUiThread {
                                    ToastUtils.showShort("已观影30分钟，获得${data.score}积分")
                                }
                            }
                        }

                        override fun onError(e: ResponseException) {
                            println("watchVideoLong==  onError")
                        }
                    })
            }
        } else {
            if (isClose) {
                finish()
            }
        }
    }

    //播放下一集
    private fun playNext() {
        curProgressHistory = 0
        isSeekToHistory = false
        videoNetProgress = 0
        if (++urlIndex >= playFrom.urls.size) {
            urlIndex = 0
        }
        changeVideoUrlIndex()
        parseData()
    }

    //升级vip
    private fun updateVip() {
        if (!UserUtils.isLogin()) {
            LoginActivity.start()
        } else {
            if (UserUtils.userInfo?.group_id != 3) {
                val intent = Intent(mActivity, PayActivity::class.java)
                intent.putExtra("type", 1)
                ActivityUtils.startActivity(intent)
            } else {
                checkVodTrySee()
            }
        }
    }

    //付费观看
    private fun payPlay() {
        if (!UserUtils.isLogin()) {
            LoginActivity.start()
        } else {
            if (playList == null) {
                return
            }
            val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
            if (AgainstCheatUtil.showWarn(vodService)) {
                return
            }
            RequestManager.execute(
                mActivity,
                vodService.buyVideo(
                    4.toString(),
                    mVodBean.vod_id.toString(),
                    playFrom.sid.toString(),
                    playList!![urlIndex].nid.toString(),
                    1.toString(),
                    1.toString()
                ),
                object : BaseObserver<String>() {
                    override fun onSuccess(data: String) {
                        runOnUiThread {
                            ToastUtils.showShort("购买成功！")
                        }
                        checkVodTrySee()
                    }

                    override fun onError(e: ResponseException) {
                        Log.d("购买失败", e.getErrorMessage() ?: "")
                    }

                }
            )
        }
    }

    private var lbm: LocalBroadcastManager? = null
    private val localReceiver = LocalReceiver(this@NewPlayActivity)

    private fun registerReceiver() {
        lbm = LocalBroadcastManager.getInstance(this@NewPlayActivity)
        lbm!!.registerReceiver(localReceiver, IntentFilter("cn.whiner.newAv.AvVideoController"))//av
    }

    class LocalReceiver(act: NewPlayActivity) : BroadcastReceiver() {

        private var act = act

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == "cn.whiner.newAv.AvVideoController") {//av
                when (intent.getIntExtra("type", RECEIVER_TYPE_TIMER)) {
                    RECEIVER_TYPE_REPLAY -> {
                        act.isSeekToHistory = true
                    }
                    RECEIVER_TYPE_TIMER -> {
                        val isFromHead = intent.getBooleanExtra("isFromHead", false)
                        if (isFromHead) {
                            act.chengeNextLineFromHead()
                        } else {
                            act.chengeNextLine()
                        }
                    }
                }
            }
        }
    }

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var watchVideoLong = 0
    private var index = 0

    private fun startTimer() {
        if (timer == null && timerTask == null) {
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    if (index == 0) {
                        index++
                    } else {
                        savePlayRecord(false)
                        watchVideoLong += 60
                    }
                }
            }
            timer!!.schedule(timerTask, 0, 1000 * 30)
        }
    }

    private fun cancelTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        if (timerTask != null) {
            timerTask!!.cancel()
            timerTask = null
        }
    }

    var Speeds = "0"
    override fun onSpeedItemClick(speed: String) {
        Speeds = speed
        controller.setSpeedSelect(speed)
    }

    private fun changeIntoText() {
        // ②初始化有交互动画的语音识别器
        val iatDialog = RecognizerDialog(this@NewPlayActivity, mInitListener)
        iatDialog.setParameter(SpeechConstant.ASR_PTT, "0")
        iatDialog.setParameter(SpeechConstant.RESULT_TYPE, "plain")
        iatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn")
        iatDialog.setParameter(SpeechConstant.ACCENT, "mandarin")
        iatDialog.setListener(object : RecognizerDialogListener {
            override fun onResult(p0: com.iflytek.cloud.RecognizerResult?, p1: Boolean) {
                mscLoum(p0?.resultString)
                player_pause_ad = false
            }

            override fun onError(p0: SpeechError?) {
                mscLoum("")
                player_pause_ad = false
                p0?.getPlainDescription(true)
            }
        })
        iatDialog.show()
    }

    private val mInitListener = InitListener { code ->
        Log.d(TAG, "SpeechRecognizer init() code = $code")
        if (code != ErrorCode.SUCCESS) {
            Toast.makeText(this@NewPlayActivity, "初始化失败，错误码：$code", Toast.LENGTH_SHORT).show()
        }
    }

    //发送给服务端的弹幕颜色（与本地展示一致的 #RRGGBB 格式）
    private val SEND_DANMU_COLOR = "#FF7F00"

    //自带上传 操作：播放器弹窗与详情页"点我发弹幕"共用，
    //保证 vod_id 拼接 / at_time / color 与弹幕列表拉取(getDanmu)使用完全一致的参数
    fun sendDanmu(content: String) {
        if (content.isEmpty()) {
            return
        }
        //弹幕接口在 needLogin 白名单中，未登录时不带身份 Cookie，服务端不会入库
        if (!UserUtils.isLogin()) {
            ToastUtils.showShort("请先登录后再发送弹幕")
            LoginActivity.start()
            return
        }
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        if (AgainstCheatUtil.showWarn(vodService)) {
            return;
        }
        val vodId = mVodBean.vod_id.toString() + urlIndex
        val atTime = videoView.currentPosition.toString()
        Log.d("DanmakuSend", "send content=$content vod_id=$vodId at_time=$atTime color=$SEND_DANMU_COLOR")
        RequestManager.execute(
            mActivity,
            vodService.sendDanmu(
                content,
                vodId,
                atTime,
                SEND_DANMU_COLOR
            ),
            object : BaseObserver<GetScoreBean>(false) {
                override fun onSuccess(data: GetScoreBean) {
                    val score = data.score
                    Log.d("DanmakuSend", "success score=$score")
                    runOnUiThread {
                        //score==0 也给出明确反馈，消除"静默失败"盲区
                        if (!score.isNullOrEmpty() && score != "0") {
                            ToastUtils.showShort("发送弹幕成功，获得${score}积分")
                        } else {
                            ToastUtils.showShort("弹幕发送成功")
                        }
                    }
                }

                override fun onError(e: ResponseException) {
                    Log.d("DanmakuSend", "error msg=${e.getErrorMessage()}")
                    runOnUiThread {
                        ToastUtils.showShort(e.getErrorMessage())
                    }
                }
            })
    }

    fun getPercentage(curPosition: Long, duration: Long): Float {
        val percentage: Float = curPosition / (duration * 1.0f)
        val df = DecimalFormat("#.00")
        val dfs = DecimalFormatSymbols()
        dfs.decimalSeparator = '.'
        df.decimalFormatSymbols = dfs
        return java.lang.Float.valueOf(df.format(percentage.toDouble()))
    }

    /**
     * CountDownTimer 实现倒计时
     */
    internal class MyCount : CountDownTimer {

        private var mView: View
        private var context: Activity
        private var lisenter: timerFinsihLisenter

        constructor(
            context: Activity,
            millisInFuture: Long,
            countDownInterval: Long,
            lisenter: timerFinsihLisenter
        ) : super(millisInFuture, countDownInterval) {
            this.mView = context.window.decorView
            this.context = context
            this.lisenter = lisenter
        }

        override fun onFinish() {
            context.runOnUiThread {
                lisenter.onFinish()
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            context.runOnUiThread {
                lisenter.onTicks(millisUntilFinished)
            }
        }

        interface timerFinsihLisenter {
            fun onFinish()
            fun onTicks(mill: Long)
        }
    }
}


