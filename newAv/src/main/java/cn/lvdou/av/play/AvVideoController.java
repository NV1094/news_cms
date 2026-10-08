package cn.lvdou.av.play;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.CacheDiskStaticUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.dueeeke.videoplayer.controller.GestureVideoController;
import com.dueeeke.videoplayer.controller.MediaPlayerControl;
import com.dueeeke.videoplayer.player.VideoView;
import com.dueeeke.videoplayer.player.VideoViewManager;
import com.dueeeke.videoplayer.util.PlayerUtils;

import cn.lvdou.av.CheckVodTrySeeBean;

import com.xiaweizi.marquee.MarqueeTextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import cn.lvdou.av.R;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import pl.droidsonroids.gif.GifImageView;

import java.util.TimerTask;

import static com.dueeeke.videoplayer.util.PlayerUtils.stringForTime;

public class AvVideoController extends GestureVideoController implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private int isNeedVip;
    private Boolean isClickivPlay = false;
    private int trySeeTime;
    protected ControllerClickListener controllerClickListener;
    protected ControllerClickListenerWZ controllerClickListenerWZ;
    protected ControllerPlayIngLisenter playingLisenter;
    private RelativeLayout rl_fullscreen;
    public static final String KEY_SPEED_INDEX = "KEY_SPEED_INDEX";
    public static final int RECEIVER_TYPE_REPLAY = 1;
    public static final int RECEIVER_TYPE_TIMER = 2;
    private Boolean mReplayByCurProgress = false;
    private ImageView loge;
    private GifImageView gifIMageView;
    private ControllerClickListener controllerClickListenerGKDY;

    public void setControllerClickListenerWZ(ControllerClickListenerWZ controllerClickListenerWZ) {
        this.controllerClickListenerWZ = controllerClickListenerWZ;
    }

    public void setControllerClickListenerGKDY(ControllerClickListener controllerClickListenerGKDY) {
        this.controllerClickListenerGKDY = controllerClickListenerGKDY;
    }

    //    @SuppressWarnings("unused")
    public void setControllerClickListener(ControllerClickListener controllerClickListener) {
        this.controllerClickListener = controllerClickListener;
    }

    public void setControllerPlayIngLisenter(ControllerPlayIngLisenter lisenter) {
        this.playingLisenter = lisenter;
    }

    private static final String KEY_IS_OPEN_DANMAKU = "KEY_IS_OPEN_DANMAKU";

    private static final String TAG = "AvVideoController";
    private Animation mShowAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_av_alpha_in);
    private Animation mHideAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_av_alpha_out);

    protected ImageView iv_bg;
    //顶部
    protected View v_all_bg;
    protected View v_top_bg;
    protected View v_bottom_bg;
    protected ImageView iv_back;
    protected MarqueeTextView tv_title;
    protected TextClock tc_localtime;
    protected ImageView iv_miracast;
    protected ImageView iv_download;
    //中部
    protected ImageView iv_lock;
    protected ImageView iv_pip;
    protected ImageView iv_scale;
    //顶部：横屏/竖屏切换
    protected ImageView iv_av_rotate;
    //底部
    protected ImageView iv_play;
    protected TextView tv_curr_time, tv_total_time, tv_playtime, tv_playtime2;
    protected SeekBar progress, sb_1, sb_2;//使用两个来切换
    protected ImageView iv_fullscreen;
    protected ImageView iv_next;
    protected ImageView iv_danmaku;
    protected TextView tv_danmaku;
    protected TextView tvPlaySource;
    protected TextView tv_speed;
    protected TextView tv_hd;
    private ImageView iv_msch;
    private LinearLayout iv_msc_1;
    protected TextView tv_selected;
    protected TextView tvEndUpdateButtonad;
    private LinearLayout llPay;
    private LinearLayout llUpdate;
    private FrameLayout rlEndUpdate;
    private FrameLayout rlEndPay;
    private TextView tvUpdateTitle;
    private TextView tvPayTitle;
    private TextView tvEndUpdateTitle;
    private TextView tvEndPayTitle;
    private View v_av_top_bg;

    private FrameLayout llSkip;
    private ImageView awvPlayer;
    private ImageView imgTz, imgTzH;
    private TextView tvSkip;
    private TextView tvAvAnnouncement;
    private Button btError, btReplay;

    ///全局View
    protected ImageView iv_replay;
    protected TextView tv_replay;
    //加载进度条
    protected LinearLayout clpb_loading;
    //加载进度条
    protected ConstraintLayout clpb_jiexi;
    protected TextView tvJiexiMsg;
    //是否在滑动进度条
    private boolean mIsDragging;
    //是否显示vip hint
    public boolean isShowHint;
    private Animation animation;
    private VideoViewImpt videoViewImpt;
    private WeakReference<AppCompatActivity> curActivity;

    private boolean isNormal = true;//是否是竖屏
    private boolean isPlayVideoAdv = false;//是否播放视频广告中
    private boolean isPlayStopAd = false;//是否播放暂停广告中

    private LinearLayout speedAdd; //长按切速显示

    public AvVideoController(VideoViewImpt impt, @NonNull Context context) {
        this(context);
        curActivity = new WeakReference<>((AppCompatActivity) context);
        this.videoViewImpt = impt;
    }

    public AvVideoController(@NonNull Context context) {
        this(context, null);
    }

    public AvVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvVideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_av_controller;
    }


    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    //长按切速
    @Override
    public void onLongPress(MotionEvent e) {
        if (controllerClickListener != null) {
            speedAdd.setVisibility(View.VISIBLE);
            controllerClickListener.onLongPress();
        }
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (controllerClickListener != null) {
            speedAdd.setVisibility(View.GONE);
            controllerClickListener.onSingleTapUp();
        }
        return super.onSingleTapUp(e);
    }

    //长按切速结束
    @SuppressLint("CutPasteId")
    @Override
    protected void initView() {
        super.initView();
        Log.e(TAG, "initView");
        setEnableInNormal(true);
        btError = findViewById(R.id.btError);
        btReplay = findViewById(R.id.btReplay);
        loge = findViewById(R.id.loge);
        gifIMageView = findViewById(R.id.progress);
        tvAvAnnouncement = findViewById(R.id.tv_av_announcement);
        v_av_top_bg = findViewById(R.id.v_av_top_bg);
        tvEndUpdateButtonad = findViewById(R.id.tvEndUpdateButtonad);
        tvUpdateTitle = findViewById(R.id.tvUpdateTitle);
        tvJiexiMsg = findViewById(R.id.tvJiexiMsg);
        tvPayTitle = findViewById(R.id.tvPayTitle);
        tvEndPayTitle = findViewById(R.id.tvEndPayTitle);
        tvEndUpdateTitle = findViewById(R.id.tvEndUpdateTitle);
        rlEndUpdate = findViewById(R.id.rlEndUpdate);
        rlEndPay = findViewById(R.id.rlEndPay);
        llPay = findViewById(R.id.llPay);
        llUpdate = findViewById(R.id.llUpdateVip);
        iv_msc_1 = findViewById(R.id.iv_msc_1);
        iv_bg = findViewById(R.id.iv_av_bg);
        llSkip = findViewById(R.id.llSkip);
        tvSkip = findViewById(R.id.tvSkip);
        awvPlayer = findViewById(R.id.awvPlayer);
        imgTz = findViewById(R.id.imgTz);
        imgTzH = findViewById(R.id.imgTzH);
        imgTz.setOnClickListener(this);
        imgTzH.setOnClickListener(this);
        speedAdd = findViewById(R.id.speedAdd);//长按切速
        findViewById(R.id.tvPayButton).setOnClickListener(this);
        findViewById(R.id.tvUpdateButton).setOnClickListener(this);
        findViewById(R.id.tvEndPayButton).setOnClickListener(this);
        findViewById(R.id.tvEndUpdateButton).setOnClickListener(this);
        findViewById(R.id.tvEndUpdateButtonad).setOnClickListener(this);
        //全部背景
        v_all_bg = findViewById(R.id.v_av_all_bg);
        //顶部背景
        v_top_bg = findViewById(R.id.v_av_top_bg);
        //底部背景
        v_bottom_bg = findViewById(R.id.v_av_bottom_bg);
        //-------------顶部
        //返回键
        iv_back = findViewById(R.id.iv_av_back);
        iv_back.setOnClickListener(this);
        findViewById(R.id.iv_av_back1).setOnClickListener(this);
        findViewById(R.id.iv_av_back2).setOnClickListener(this);
        //标题
        tv_title = findViewById(R.id.tv_av_title);
        //系统时间
        tc_localtime = findViewById(R.id.tc_av_localtime);
        //旋转切换（横屏/竖屏）
        iv_av_rotate = findViewById(R.id.iv_av_rotate);
        iv_av_rotate.setOnClickListener(this);
        //投屏
        iv_miracast = findViewById(R.id.iv_av_miracast);
        iv_miracast.setOnClickListener(this);
        //下载
        iv_download = findViewById(R.id.iv_av_download);
        iv_download.setOnClickListener(this);
        //--------------中部
        //锁定
        iv_lock = findViewById(R.id.iv_av_lock);
        iv_lock.setOnClickListener(this);
        //画中画
        iv_pip = findViewById(R.id.iv_av_pip);
        iv_pip.setOnClickListener(this);
        //视频比例
        iv_scale = findViewById(R.id.iv_av_scale);
        iv_scale.setOnClickListener(this);
        //---------------底部
        //播放
        iv_play = findViewById(R.id.iv_av_play);
        iv_play.setOnClickListener(this);
        //当前播放时间
        tv_curr_time = findViewById(R.id.tv_av_curr_time);
        //总播放时间
        tv_total_time = findViewById(R.id.tv_av_total_time);
        //播放时间合集
        tv_playtime = findViewById(R.id.tv_av_playtime);
        tv_playtime2 = findViewById(R.id.tv_av_playtime2);
        //播放进度条
        sb_1 = findViewById(R.id.sb_av_1);
        sb_1.setOnSeekBarChangeListener(null);
        sb_2 = findViewById(R.id.sb_av_2);
        sb_2.setOnSeekBarChangeListener(null);
        progress = sb_1;
        progress.setOnSeekBarChangeListener(this);
        //全屏
        iv_fullscreen = findViewById(R.id.iv_av_fullscreen);
        rl_fullscreen = findViewById(R.id.rl_av_fullscreen);
        rl_fullscreen.setOnClickListener(this);
        //下一集
        iv_next = findViewById(R.id.iv_av_next);
        iv_next.setOnClickListener(this);
        //弹幕开关
        iv_danmaku = findViewById(R.id.iv_av_danmaku);
        iv_danmaku.setOnClickListener(this);
        //弹幕发送按钮
        tvPlaySource = findViewById(R.id.tvPlaySource);
        tvPlaySource.setOnClickListener(this);

        //弹幕发送按钮
        tv_danmaku = findViewById(R.id.tv_av_danmaku);
        tv_danmaku.setOnClickListener(this);

        //初始化弹幕状态
        boolean b = SPUtils.getInstance().getBoolean(KEY_IS_OPEN_DANMAKU, true);
        iv_danmaku.setSelected(b);
        //播放倍数
        tv_speed = findViewById(R.id.tv_av_speed);
        tv_speed.setOnClickListener(this);
        //清晰度
        tv_hd = findViewById(R.id.tv_av_hd);
        tv_hd.setOnClickListener(this);
        //语音输入
        iv_msch = findViewById(R.id.iv_msc);
        iv_msch.setOnClickListener(this);
        //选集
        tv_selected = findViewById(R.id.tv_av_selected);
        tv_selected.setOnClickListener(this);
        //加载进度条
        clpb_loading = findViewById(R.id.clpb_av_loading);
        clpb_loading.setVisibility(View.GONE);
        //加载进度条
        clpb_jiexi = findViewById(R.id.clpb_av_jiexi);
        //重新播放
        iv_replay = findViewById(R.id.iv_av_replay);
        iv_replay.setOnClickListener(this);
        tv_replay = findViewById(R.id.tv_av_replay);

        findViewById(R.id.imgCloseStopHAd).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayStopHAd();
            }
        });
    }

    int bufferTime = 0;
    TimerTask bufferTask;

    private void startBufferTimer() {
        if (bufferTimer != null) {
            bufferTimer.cancel();
        }
        if (bufferTask != null) {
            bufferTask.cancel();
        }
        bufferTimer = new Timer();
        bufferTask = new TimerTask() {
            @Override
            public void run() {
                if (bufferTime >= 8) {
                    bufferTime = 0;
                    bufferTimer.cancel();
                    bufferTimer = null;
                    bufferTask = null;
                    Intent intent = new Intent("cn.whiner.av.AvVideoController");
                    intent.putExtra("type", RECEIVER_TYPE_TIMER);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                    System.out.println("缓存结束====");
                } else {
                    bufferTime++;
                }
            }
        };
        bufferTimer.schedule(bufferTask, 0, 1000);
    }

    /**
     * 拦截手势的双击事件
     *
     * @param e
     * @return
     */
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (!isLocked() && isInPlaybackState()) {
            if (!isPlayStopAd) {
                togglePlay();
            }
        }
        return false;
    }

    //窗口切换
    @Override
    public void setPlayerState(int playerState) {
        super.setPlayerState(playerState);
        if (isLocked()) return;
        switch (playerState) {
            case VideoView.PLAYER_NORMAL:
                isNormal = true;
                Log.e(TAG, "正常窗口");
                //系统时间
                tc_localtime.setVisibility(GONE);
                //下载
                iv_download.setVisibility(GONE);
                //锁定
                iv_lock.setVisibility(GONE);
                //视频比列
                iv_scale.setVisibility(GONE);
                //旋转切换（仅全屏可见）
                iv_av_rotate.setVisibility(GONE);
                //如果不是视频广告播放中
                if (!isPlayVideoAdv) {
                    //画中画
                    iv_pip.setVisibility(VISIBLE);
                    if (animation != null && tvAvAnnouncement.getVisibility() == VISIBLE) {
                        animation.reset();
                        tvAvAnnouncement.startAnimation(animation);
                    }
                    setGestureEnabled(true);
                    iv_back.setVisibility(VISIBLE);
                    //当前播放时间
                    tv_curr_time.setVisibility(VISIBLE);
                    //总时间
                    tv_total_time.setVisibility(VISIBLE);
                    //全屏
                    iv_fullscreen.setVisibility(VISIBLE);
                    rl_fullscreen.setVisibility(VISIBLE);
                    //seekBar
                    sb_1.setVisibility(VISIBLE);
                    sb_1.setOnSeekBarChangeListener(this);
                    sb_2.setVisibility(GONE);
                    sb_2.setOnSeekBarChangeListener(null);
                    imgTz.setVisibility(VISIBLE);
                    imgTzH.setVisibility(GONE);
                    iv_msch.setVisibility(GONE);
                    iv_msc_1.setVisibility(GONE);
                }

                progress = sb_1;
                ///下一集
                iv_next.setVisibility(GONE);
                //时间集合
                tv_playtime.setVisibility(GONE);
                tv_playtime2.setVisibility(GONE);
                //弹幕
                iv_danmaku.setVisibility(GONE);
                tv_danmaku.setVisibility(GONE);
                tvPlaySource.setVisibility(GONE);
                //播放倍数
                tv_speed.setVisibility(GONE);
                //播放清晰度
                tv_hd.setVisibility(GONE);
                //选集
                tv_selected.setVisibility(GONE);
                break;
            case VideoView.PLAYER_FULL_SCREEN:
                isNormal = false;
                Log.e(TAG, "全屏窗口");

                //系统时间
                tc_localtime.setVisibility(VISIBLE);
                //锁定
                iv_lock.setVisibility(VISIBLE);
                //视频比列
                iv_scale.setVisibility(VISIBLE);
                //旋转切换（与锁定/比例同进同出）
                iv_av_rotate.setVisibility(VISIBLE);
                //当前播放时间
                tv_curr_time.setVisibility(GONE);
                //总时间
                tv_total_time.setVisibility(GONE);
                //全屏
                iv_fullscreen.setVisibility(GONE);
                rl_fullscreen.setVisibility(GONE);
                //seekBar
                sb_1.setVisibility(GONE);
                sb_1.setOnSeekBarChangeListener(null);
                //如果不是视频广告播放中
                if (!isPlayVideoAdv) {
                    if (animation != null && tvAvAnnouncement.getVisibility() == VISIBLE) {
                        animation.reset();
                        tvAvAnnouncement.startAnimation(animation);
                    }
                    setGestureEnabled(true);

                    //                //画中画
                    iv_pip.setVisibility(VISIBLE);
                    //seekBar
                    sb_2.setVisibility(VISIBLE);
                    sb_2.setOnSeekBarChangeListener(this);
                    progress = sb_2;
                    ///下一集
                    iv_next.setVisibility(VISIBLE);
                    //时间集合
                    tv_playtime.setVisibility(VISIBLE);
                    tv_playtime2.setVisibility(VISIBLE);
                    //弹幕
                    iv_danmaku.setVisibility(VISIBLE);
                    imgTz.setVisibility(View.GONE);
                    imgTzH.setVisibility(View.VISIBLE);
                    if (xunFeiWitch == 1) {
                        iv_msch.setVisibility(View.VISIBLE);
                    } else {
                        iv_msch.setVisibility(View.GONE);
                    }
                    iv_msc_1.setVisibility(View.VISIBLE);
                }
                if (iv_danmaku.isSelected()) {
                    tv_danmaku.setVisibility(VISIBLE);
                    tvPlaySource.setVisibility(VISIBLE);
                } else {
                    tv_danmaku.setVisibility(GONE);
                    tvPlaySource.setVisibility(GONE);
                }
                //播放倍数
                tv_speed.setVisibility(VISIBLE);
                //播放清晰度
                tv_hd.setVisibility(VISIBLE);
                //选集
                tv_selected.setVisibility(VISIBLE);
                break;
        }
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) v_av_top_bg.getLayoutParams();
//        if (curActivity.get() != null && playerState == VideoView.PLAYER_NORMAL) {
//            layoutParams.topMargin = BarUtils.getStatusBarHeight();
//            layoutParams.height = ConvertUtils.dp2px(45);
//            BarUtils.setStatusBarVisibility(curActivity.get(), true);
//        } else {
//            layoutParams.height = ConvertUtils.dp2px(45);
//            layoutParams.topMargin = ConvertUtils.dp2px(0);
//            BarUtils.setStatusBarVisibility(curActivity.get(), false);
//        }
        layoutParams.height = ConvertUtils.dp2px(45);
        v_av_top_bg.setLayoutParams(layoutParams);
    }

    public void startPlay() {

        if (mControlWrapper != null) {
            if (isShowHint) {
                mControlWrapper.pause();
            } else {
                mControlWrapper.start();
            }
        }
    }

    //播放状态
    @Override
    public void setPlayState(int playState) {
        super.setPlayState(playState);
        switch (playState) {
            case VideoView.STATE_IDLE:
                Log.e(TAG, "初始状态");
                isHdChange = true;
                tv_hd.setText(R.string.av_hd);
                iv_bg.setVisibility(VISIBLE);
                clpb_loading.setVisibility(View.GONE);
                sb_1.setProgress(0);
                sb_1.setSecondaryProgress(0);
                sb_2.setProgress(0);
                sb_2.setSecondaryProgress(0);
                setLocked(false);
                iv_lock.setSelected(false);
                iv_play.setSelected(false);
                //重新播放
                v_all_bg.setVisibility(GONE);
                iv_replay.setVisibility(GONE);
                tv_replay.setVisibility(GONE);
                show();
                break;
            case VideoView.STATE_PLAYING:
                Log.e(TAG, "播放中");
                iv_bg.setVisibility(GONE);
                iv_play.setSelected(true);
                hideJiexi();
                clpb_loading.setVisibility(View.GONE);
                post(mShowProgress);
                break;
            case VideoView.STATE_PAUSED:
                Log.e(TAG, "暂停");
                iv_bg.setVisibility(GONE);
                iv_play.setSelected(false);
                break;
            case VideoView.STATE_PREPARING:
                Log.e(TAG, "准备中");
                startBufferTimer();
                onDanmakuChanged(true);
                iv_bg.setVisibility(GONE);
                clpb_loading.setVisibility(View.VISIBLE);
                //重新播放
                v_all_bg.setVisibility(GONE);
                iv_replay.setVisibility(GONE);
                tv_replay.setVisibility(GONE);
                show();
                break;
            case VideoView.STATE_PREPARED:
                stopBufferTime();
//                hide();
                postDelayed(() -> {
                            mShowing = true;
                            hide();
                        },
                        8000L
                );

                Log.e(TAG, "准备好了");
                //hideJiexi();
                iv_bg.setVisibility(GONE);
                if (playingLisenter != null && !isPlayVideoAdv)
                    playingLisenter.playPrepared();
                break;
            case VideoView.STATE_ERROR:
                Log.e(TAG, "错误");
                iv_bg.setVisibility(GONE);
                clpb_loading.setVisibility(View.GONE);
                //重新播放
                v_all_bg.setVisibility(VISIBLE);
                iv_replay.setVisibility(VISIBLE);
                tv_replay.setVisibility(VISIBLE);
                tv_replay.setText(R.string.av_error);
                show();
                break;
            case VideoView.STATE_BUFFERING:
                Log.e(TAG, "加载中");
                iv_bg.setVisibility(GONE);
                clpb_loading.setVisibility(View.VISIBLE);
                iv_play.setSelected(mControlWrapper.isPlaying());
                break;
            case VideoView.STATE_BUFFERED:
                Log.e(TAG, "加载结束");
                iv_bg.setVisibility(GONE);
                clpb_loading.setVisibility(View.GONE);
                iv_play.setSelected(mControlWrapper.isPlaying());
                break;
            case VideoView.STATE_PLAYBACK_COMPLETED:
                Log.e(TAG, "播放完毕");
                if (isPlayVideoAdv) {
                    isPlayVideoAdv = false;
                } else {
                    iv_bg.setVisibility(VISIBLE);
                    clpb_loading.setVisibility(View.GONE);
                    setLocked(false);
//                mControlWrapper.setLock(false);
                    iv_lock.setSelected(false);
                    iv_play.setSelected(false);
                    //重新播放
                    v_all_bg.setVisibility(VISIBLE);
                    iv_replay.setVisibility(VISIBLE);
                    tv_replay.setVisibility(VISIBLE);
                    tv_replay.setText(R.string.av_replay);
                    hide();
                }
                break;
            default:
                Log.e(TAG, "未知状态---" + playState);
                break;
        }
    }

    //播放横屏广告
    public void startPlayHAdv(String htmlCode) {
        this.isPlayVideoAdv = true;
        findViewById(R.id.layoutHAdv).setVisibility(View.VISIBLE);
        WebView webview = findViewById(R.id.webviewH);
        webview.loadDataWithBaseURL(null, htmlCode, "text/html", "utf-8", null);
    }

    //播放暂停广告
    public void startPlayStopHAd(String htmlCode) {
        isPlayStopAd(true);
        //横屏暂停广告
        WebView stopAd = findViewById(R.id.stopAdH);
        stopAd.setVisibility(View.VISIBLE);
        findViewById(R.id.imgCloseStopHAd).setVisibility(View.VISIBLE);
        stopAd.loadDataWithBaseURL(null, htmlCode, "text/html", "utf-8", null);
    }

    public void stopPlayStopHAd() {
        isPlayStopAd(false);
        findViewById(R.id.stopAdH).setVisibility(View.GONE);
        findViewById(R.id.imgCloseStopHAd).setVisibility(View.GONE);
    }

    public void isPlayStopAd(Boolean is) {
        isPlayStopAd = is;
    }

    public Boolean isPlayStopAd() {
        return isPlayStopAd;
    }

    public void stopPayHAdv() {
        this.isPlayVideoAdv = false;
        findViewById(R.id.layoutHAdv).setVisibility(View.INVISIBLE);
    }

    public void setAdvTime(Long mill) {
        TextView textView = findViewById(R.id.ad_timeH);
        textView.setText((mill / 1000) + "s | vip可关闭该广告");
    }

    //开始视频广告播放
    public void startPlayVideoAdv() {
        this.isPlayVideoAdv = true;
    }

    public boolean getIsPlayVideoAdv() {
        return isPlayVideoAdv;
    }

    public void setIsPlayVideoAdv(boolean msg) {
        this.isPlayVideoAdv = msg;
    }

    //获取视频播放器横竖屏状态
    public boolean getVideoState() {
        return isNormal;
    }

    @Override
    public void show() {
        if (!isPlayVideoAdv)
            show(mDefaultTimeout);
    }

    @Override
    public void hide() {
        if (mShowing) {
            iv_lock.setVisibility(GONE);
            if (!isLocked()) {
                setAllView(false);
            }
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) v_av_top_bg.getLayoutParams();
//            if (curActivity.get() != null && !mControlWrapper.isFullScreen()) {
//                layoutParams.topMargin = BarUtils.getStatusBarHeight();
//                layoutParams.height = ConvertUtils.dp2px(45);
//                BarUtils.setStatusBarVisibility(curActivity.get(), true);
//            } else {
//                layoutParams.height = ConvertUtils.dp2px(45);
//                layoutParams.topMargin = ConvertUtils.dp2px(0);
//            }
            layoutParams.height = ConvertUtils.dp2px(45);
            v_av_top_bg.setLayoutParams(layoutParams);

            //隐藏播放倍数
            if (speedPop != null && speedPop.isShowing()) {
                speedPop.dismiss();
            }
            //隐藏清晰度
            if (hdPop != null && hdPop.isShowing()) {
                hdPop.dismiss();
            }
            mShowing = false;
        }
    }

    public long getDuration() {
        return mControlWrapper.getDuration();
    }

    public float getPercentage() {
//        if (mControlWrapper.getDuration() == 0) {
//            return 100f;
//        }
        float percentage = mControlWrapper.getCurrentPosition() / (mControlWrapper.getDuration() * 1.0f);
        DecimalFormat df = new DecimalFormat("#.00");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);
        System.out.println("进度11：curr=" + mControlWrapper.getCurrentPosition() + " duration=" + mControlWrapper.getDuration() + " percentage=" + percentage + "   >" + df.format(percentage));
        float progress = 0.0F;
        //解决播放点击返回有几率闪退的bug
        try {
            progress = Float.valueOf(df.format(percentage));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return progress;
    }

    public long getCurProgress() {
        return mControlWrapper.getCurrentPosition();
    }

    public void CheckVodTrySeeBean(int freeCount, CheckVodTrySeeBean bean, boolean isVip, int vod_point_pay) {
        rlEndPay.setVisibility(View.GONE);
        int status = bean.getStatus();
        if (isVips == 1) {
            isVip = true;
            isShowHint = true;
            tvEndUpdateButtonad.setVisibility(GONE);
        }

        if (isVip) {
            rlEndUpdate.setVisibility(View.GONE);
            if (isShowHint) {
                isShowHint = false;
                startPlay();
            }
        }

        if (status == 0) {
            isNeedVip = 0;
            trySeeTime = bean.getTrysee();
            llPay.setVisibility(View.GONE);
            llUpdate.setVisibility(View.GONE);
            return;
        }
        if (freeCount > 0) {
            isNeedVip = 0;
            trySeeTime = bean.getTrysee();
            llPay.setVisibility(View.GONE);
            llUpdate.setVisibility(View.GONE);
            return;
        }

        isNeedVip = status;
        trySeeTime = bean.getTrysee();

        if (status == 1) {
            if (isVip) {
                isNeedVip = 0;
                llPay.setVisibility(View.GONE);
                llUpdate.setVisibility(View.GONE);
            } else {
                llPay.setVisibility(View.GONE);
                llUpdate.setVisibility(View.VISIBLE);
                tvUpdateTitle.setText("试看" + trySeeTime + "分钟，观看完整版请 ");
            }
        } else if (status == 2) {
            llPay.setVisibility(View.VISIBLE);
            llUpdate.setVisibility(View.GONE);
//            tvPayTitle.setText("试看" + trySeeTime + "分钟 ，观看完整版请支付1" + vod_point_pay + "积分 ");
//            tvEndPayTitle.setText("试看" + trySeeTime + "分钟 ，观看完整版请支付2" + vod_point_pay + "积分");
            tvPayTitle.setText("本片为付费资源，观看完整版需支付" + vod_point_pay + "积分 ");
            tvEndPayTitle.setText("本片为付费资源，观看完整版需支付 " + vod_point_pay + "积分");
        }
    }

    Timer timer = null;
    Timer bufferTimer = null;

    public void showAd(String gifUrl, String url) {
        llSkip.setVisibility(View.VISIBLE);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (!TextUtils.isEmpty(url))
            llSkip.setOnClickListener(v -> ActivityUtils.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
        if (gifUrl.endsWith(".gif"))
            Glide.with(this).asGif().load(gifUrl).centerCrop().into(awvPlayer);
        else
            Glide.with(this).asBitmap().load(gifUrl).centerCrop().into(awvPlayer);

        final int[] time = {6};
        tvSkip.setText(time[0] + "s");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                post(() -> {
                    if (--time[0] <= 0) {
                        llSkip.setVisibility(View.GONE);
                        timer.cancel();
                        timer = null;
                    } else {
                        tvSkip.setText(time[0] + "s");
                    }
                });
            }
        }, 1000, 1000);
    }

    public void showAnnouncement(String announcement) {
        tvAvAnnouncement.setVisibility(View.VISIBLE);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) tvAvAnnouncement.getLayoutParams();
        layoutParams.width = ConvertUtils.dp2px(1100);
        tvAvAnnouncement.setLayoutParams(layoutParams);
        tvAvAnnouncement.setText(announcement);
        if (animation == null) {
            animation = AnimationUtils.loadAnimation(getContext(), R.anim.recomment_in_left);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    tvAvAnnouncement.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        tvAvAnnouncement.setAnimation(animation);
    }

    private void show(int timeout) {
        if (!mShowing) {
            if (mControlWrapper.isFullScreen()) {
                iv_lock.setVisibility(VISIBLE);
            }
            if (!isLocked()) {
                setAllView(true);
            }

            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) v_av_top_bg.getLayoutParams();
//            if (curActivity.get() != null && !mControlWrapper.isFullScreen()) {
//                layoutParams.topMargin = BarUtils.getStatusBarHeight();
//                layoutParams.height = ConvertUtils.dp2px(45);
//                BarUtils.setStatusBarVisibility(curActivity.get(), true);
//            } else {
//                layoutParams.height = ConvertUtils.dp2px(45);
//                layoutParams.topMargin = ConvertUtils.dp2px(0);
//            }
            layoutParams.height = ConvertUtils.dp2px(45);
            v_av_top_bg.setLayoutParams(layoutParams);
            mShowing = true;
        }
        removeCallbacks(mFadeOut);
        if (timeout != 0) {
            postDelayed(mFadeOut, timeout);
        }
    }

    //设置所有view
    public void setAllView(boolean isShow) {
        autoSetVisibility(v_top_bg, isShow);
        autoSetVisibility(v_bottom_bg, isShow);
        autoSetVisibility(iv_back, isShow);
        autoSetVisibility(tv_title, isShow);
        autoSetVisibility(tc_localtime, isShow);
        autoSetVisibility(iv_miracast, isShow);
        //暂时隐藏下载功能
//      autoSetVisibility(iv_download, isShow);
        autoSetVisibility(iv_lock, isShow);
        autoSetVisibility(iv_pip, isShow);
        autoSetVisibility(iv_scale, isShow);
        autoSetVisibility(iv_av_rotate, isShow);
        autoSetVisibility(iv_play, isShow);
        autoSetVisibility(tv_curr_time, isShow);
        autoSetVisibility(tv_total_time, isShow);
        autoSetVisibility(tv_playtime, isShow);
        autoSetVisibility(tv_playtime2, isShow);
        autoSetVisibility(sb_1, isShow);
        autoSetVisibility(sb_2, isShow);
        autoSetVisibility(iv_fullscreen, isShow);
        autoSetVisibility(rl_fullscreen, isShow);
        autoSetVisibility(iv_next, isShow);
        autoSetVisibility(iv_danmaku, isShow);
        autoSetVisibility(imgTz, isShow);
        autoSetVisibility(imgTzH, isShow);
        autoSetVisibility(iv_msch, isShow);
        autoSetVisibility(iv_msc_1, isShow);
        autoSetVisibility(tv_danmaku, isShow);
        autoSetVisibility(tvPlaySource, isShow);
        autoSetVisibility(tv_speed, isShow);
        autoSetVisibility(tv_hd, isShow);
        autoSetVisibility(tv_selected, isShow);
    }

    //如果view不是被GONE了，显示或隐藏View
    private void autoSetVisibility(View view, boolean isShow) {
        if (view.getVisibility() == GONE) return;
        if (isShow) {
            view.setVisibility(VISIBLE);
            view.startAnimation(mShowAnim);
        } else {
            view.setVisibility(INVISIBLE);
            view.startAnimation(mHideAnim);
        }
    }

    //显示隐藏会员提示
    @SuppressLint("SetTextI18n")
    public void isShowVipHint(boolean isShowHint) {
        if (rlEndUpdate == null) {
            return;
        }

        this.isShowHint = isShowHint;
        tvEndUpdateTitle.setText("该视频需开通VIP才可观看");
        rlEndUpdate.setVisibility(isShowHint ? View.VISIBLE : View.GONE);
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void setProgress(int duration, int position) {
        if (controllerClickListenerWZ != null) {
            controllerClickListenerWZ.onPlayProgress(duration, position);
        }

        if (mControlWrapper == null || mIsDragging) {
            return;
        }
        if (isNeedVip != 0) {
            if (position / 1000f > trySeeTime * 60 || trySeeTime == 888) {
                if (isNeedVip == 1) {
                    if (sdkexcitation.equals("1") && !sdknreward.equals("0")) { //开启激励视频且奖励次数不为0
                        tvEndUpdateButtonad.setVisibility(View.VISIBLE);
                        tvEndUpdateTitle.setText("观看一条广告可获得" + sdknreward + "次观影特权");
                    } else { //仅开启会员通道
                        tvEndUpdateButtonad.setVisibility(View.GONE);
                        tvEndUpdateTitle.setText("试看结束，升级VIP可继续观看");
                    }
                    rlEndPay.setVisibility(View.GONE);
                    rlEndUpdate.setVisibility(View.VISIBLE);
                } else {
                    rlEndPay.setVisibility(View.VISIBLE);
                    rlEndUpdate.setVisibility(View.GONE);
                }
                mControlWrapper.pause();
            }
        }
        if (progress != null) {
            if (duration > 0) {
                progress.setEnabled(true);
                int pos = (int) (position * 1.0 / duration * progress.getMax());
                progress.setProgress(pos);
            } else {
                progress.setEnabled(false);
            }
            int percent = mControlWrapper.getBufferedPercentage();
            if (percent >= 95) { //解决缓冲进度不能100%问题
                progress.setSecondaryProgress(progress.getMax());
            } else {
                progress.setSecondaryProgress(percent * 10);
            }
        }

        if (tv_total_time != null)
            tv_total_time.setText(stringForTime((int) duration));
        if (tv_curr_time != null)
            tv_curr_time.setText(stringForTime((int) position));
        if (tv_playtime != null) {
            String text = stringForTime((int) position);
            String text2 = stringForTime((int) duration);
            tv_playtime.setText(text);
            tv_playtime2.setText(text2);
        }

        if (playingLisenter != null && !isPlayVideoAdv) {
            int total = duration / 1000;
            int cu = position / 1000;
            playingLisenter.onPlayIng(cu, total);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (!b) {
            return;
        }
        long duration = mControlWrapper.getDuration();
        long newPosition = (duration * i) / progress.getMax();
        if (tv_curr_time != null)
            tv_curr_time.setText(stringForTime((int) newPosition));
        if (tv_playtime != null) {
            String text = stringForTime((int) newPosition);
            String text2 = stringForTime((int) duration);
            tv_playtime.setText(text);
            tv_playtime2.setText(text2);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsDragging = true;
        removeCallbacks(mShowProgress);
        removeCallbacks(mFadeOut);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        long duration = mControlWrapper.getDuration();
        long newPosition = (duration * seekBar.getProgress()) / progress.getMax();
        mControlWrapper.seekTo((int) newPosition);
        mIsDragging = false;
        post(mShowProgress);
        show();
    }


    @Override
    public void setMediaPlayer(MediaPlayerControl mediaPlayer) {
        super.setMediaPlayer(mediaPlayer);
    }

    //返回键
    @Override
    public boolean onBackPressed() {
        if (isLocked()) {
            show();
            ToastUtils.showShort(R.string.av_lock_tip);
            return true;
        }

        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity == null) return super.onBackPressed();
        if (mControlWrapper.isFullScreen()) {
            // 任意方向全屏退出后必回竖屏（原 UNSPECIFIED 存在不回竖屏的隐患）
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mControlWrapper.stopFullScreen();
            return true;
        }
        return super.onBackPressed();
    }

    private int isVips;

    public void visibility_wqddg() {
        isVips = 1;
    }

    public void visibility_wqddg(String playImg) {
        showDanmaku();
        if (playImg.equals("msc_start")) {
            if (controllerClickListenerWZ != null) {
                controllerClickListenerWZ.onMsc("duration");
            }
        } else if (!playImg.equals("")) {
            setClickivPlay(true);
            et_danmaku.setText(playImg);
            et_danmaku.requestFocus();
            et_danmaku.setSelection(et_danmaku.getText().length());
            if (!isPlayStopAd) togglePlay(); //如果不是在播放暂停广告的话就自动播放
        }
    }

    @Override
    public void onClick(View view) {
        show();
        int i = view.getId();
        if (view.getId() == R.id.tvEndUpdateButton || view.getId() == R.id.tvUpdateButton) {
            if (mControlWrapper.isFullScreen()) {
                toggleFullScreen();
            }
            if (controllerClickListener != null) controllerClickListener.onClick(view);

        } else if (view.getId() == R.id.tvEndUpdateButtonad) {
            Log.e("TAG", "wqddgs");
            //我们的点击按钮
            controllerClickListenerGKDY.onClick(view);
        } else if (view.getId() == R.id.tvEndPayButton || view.getId() == R.id.tvPayButton) {
            if (mControlWrapper.isFullScreen()) {
                toggleFullScreen();
            }
            if (controllerClickListener != null) controllerClickListener.onClick(view);

        } else if (view.getId() == R.id.iv_av_lock) {//先处理锁屏
            doLockUnlock();
        } else if (i == R.id.iv_av_back) {//返回键
            if (mControlWrapper.isFullScreen()) {
                toggleFullScreen();
            } else {
                //需要加接口
                if (controllerClickListener != null) controllerClickListener.onClick(view);
            }
        } else if (i == R.id.iv_av_back1) {//返回键
            if (mControlWrapper.isFullScreen()) {
                toggleFullScreen();
            } else {
                //需要加接口
                if (controllerClickListener != null) controllerClickListener.onClick(view);
            }
        } else if (i == R.id.iv_av_back2) {//返回键
            if (mControlWrapper.isFullScreen()) {
                toggleFullScreen();
            } else {
                //需要加接口
                if (controllerClickListener != null) controllerClickListener.onClick(view);
            }
        } else if (i == R.id.iv_av_miracast) {//投屏
            if (controllerClickListener != null) {
                hide();
                controllerClickListener.onClick(view);
            }
        } else if (i == R.id.iv_av_download) {//下载
            if (controllerClickListener != null) controllerClickListener.onClick(view);
        } else if (i == R.id.iv_av_pip) {//画中画
            if (controllerClickListener != null) controllerClickListener.onClick(view);
        } else if (i == R.id.iv_av_scale) {//视频比例
            doScale();
        } else if (i == R.id.iv_av_rotate) {//横屏/竖屏切换
            doRotate();
        } else if (i == R.id.iv_av_play) {//播放
            setClickivPlay(true);
            if (!isPlayStopAd)
                togglePlay();
        } else if (i == R.id.rl_av_fullscreen) {//全屏（自适应方向）
            toggleFullScreenSmart();
        } else if (i == R.id.iv_av_next) {//下集
            if (controllerClickListener != null) controllerClickListener.onClick(view);
        } else if (i == R.id.iv_av_danmaku) {//弹幕开关
            onDanmakuChanged(false);
        } else if (i == R.id.tv_av_danmaku) {//显示弹幕发送框
            showDanmaku();
        } else if (i == R.id.btn_pop_danmaku) {//发送弹幕
            sendDanmaku();
            if (controllerClickListener != null) {
                view.setTag(et_danmaku.getText().toString());
                controllerClickListener.onClick(view);
            }
            et_danmaku.setText("");
        } else if (i == R.id.iv_msc_2) {//语音输入
            visibility_wqddg("msc_start");
        } else if (i == R.id.tv_av_speed) {//视频播放速度
            if (controllerClickListener != null) {
                hide();
                view.setTag(curSpeedSelect);
                controllerClickListener.onClick(view);
            }
        } else if (i == R.id.tv_av_hd) {//修复
            if (controllerClickListener != null) controllerClickListener.onClick(view);
        } else if (i == R.id.iv_msc) {//语音输入
            visibility_wqddg("msc_start");
        } else if (i == R.id.tv_av_selected) {//选集
            if (controllerClickListener != null) {
                hide();
                controllerClickListener.onClick(view);
            }
        } else if (i == R.id.iv_av_replay) {//重新播放
            if (mReplayByCurProgress) {
                mControlWrapper.replay(true);
                Intent intent = new Intent("cn.whiner.av.AvVideoController");
                intent.putExtra("type", RECEIVER_TYPE_REPLAY);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                mReplayByCurProgress = false;
                System.out.println("进度6：==");
            } else {
                mControlWrapper.replay(true);
                System.out.println("进度7：==");
            }
        } else if (i == R.id.tvPlaySource) {//选择播放源
            if (controllerClickListener != null) {
                hide();
                controllerClickListener.onClick(view);
            }
        } else if (i == R.id.imgTz || i == R.id.imgTzH) {//跳转播放
            if (controllerClickListener != null) {
                controllerClickListener.onClick(view);
            }
        } else if (i == R.id.CheckBox_1) {//弹幕颜色 洋
            setDanMuColor("#FF7F00", 1);
            CheckBox_2.setChecked(false);
            CheckBox_3.setChecked(false);
            CheckBox_4.setChecked(false);
            CheckBox_5.setChecked(false);
            CheckBox_6.setChecked(false);
        } else if (i == R.id.CheckBox_2) {//弹幕颜色 红
            CheckBox_1.setChecked(false);
            CheckBox_3.setChecked(false);
            CheckBox_4.setChecked(false);
            CheckBox_5.setChecked(false);
            CheckBox_6.setChecked(false);
            setDanMuColor("#ff4444", 2);
        } else if (i == R.id.CheckBox_3) {//弹幕颜色 绿
            CheckBox_1.setChecked(false);
            CheckBox_2.setChecked(false);
            CheckBox_4.setChecked(false);
            CheckBox_5.setChecked(false);
            CheckBox_6.setChecked(false);
            setDanMuColor("#0099cc", 3);
        } else if (i == R.id.CheckBox_4) {//弹幕颜色 蓝
            CheckBox_1.setChecked(false);
            CheckBox_2.setChecked(false);
            CheckBox_3.setChecked(false);
            CheckBox_5.setChecked(false);
            CheckBox_6.setChecked(false);
            setDanMuColor("#BA55D3", 4);
        } else if (i == R.id.CheckBox_5) {//弹幕颜色 黄
            CheckBox_1.setChecked(false);
            CheckBox_2.setChecked(false);
            CheckBox_3.setChecked(false);
            CheckBox_4.setChecked(false);
            CheckBox_6.setChecked(false);
            setDanMuColor("#32CD32", 5);
        } else if (i == R.id.CheckBox_6) {//弹幕颜色 青
            CheckBox_1.setChecked(false);
            CheckBox_2.setChecked(false);
            CheckBox_3.setChecked(false);
            CheckBox_4.setChecked(false);
            CheckBox_5.setChecked(false);
            setDanMuColor("#FFC0CB", 6);
        } else {
            ToastUtils.showShort("未知View" + view.getId());
        }
    }

    /**
     * 横屏/竖屏切换：仅改方向、不改变全屏状态
     * （视图已在 DecorView，播放页 configChanges 不重建，播放不中断）
     */
    private void doRotate() {
        Activity activity = curActivity != null ? curActivity.get() : null;
        if (activity == null || activity.isFinishing()) return;
        if (mControlWrapper == null || !mControlWrapper.isFullScreen()) return; // 按钮仅全屏可见，双保险
        int o = activity.getRequestedOrientation();
        boolean isLandscape = o == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                || o == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        if (isLandscape) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ToastUtils.showShort("竖屏播放");
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            ToastUtils.showShort("横屏播放");
        }
    }

    /**
     * 进入全屏（自适应方向）：竖屏视频→竖屏全屏；横屏视频/尺寸未知→横屏全屏（与原行为一致）
     * 退出：无条件回竖屏小窗（修复手动转横后退出卡横屏的路径）
     */
    private void toggleFullScreenSmart() {
        if (mControlWrapper == null) return;
        if (mControlWrapper.isFullScreen()) {
            stopFullScreen(); // 基类方法：先设 PORTRAIT 再退出全屏
            return;
        }
        int[] size = mControlWrapper.getVideoSize();
        boolean verticalVideo = size != null && size.length >= 2
                && size[0] > 0 && size[1] > size[0];
        if (verticalVideo) {
            // 竖屏视频：仅进入全屏，不旋转（保持竖屏 = 竖屏沉浸全屏）
            mControlWrapper.startFullScreen();
        } else {
            // 横屏视频/尺寸未知：横屏全屏（与原行为一致）
            startFullScreen(); // 基类方法：设 LANDSCAPE + startFullScreen
        }
    }

    //锁定处理
    protected void setDanMuColor(String colorCode, int id) {
        DanMuColor = colorCode;
    }

    //锁定处理
    protected void doLockUnlock() {
        if (isLocked()) {
            setLocked(false);
            setGestureEnabled(true);
            mShowing = false;
            show();
            ToastUtils.showShort(R.string.av_unlocked);
        } else {
            hide();
            setLocked(true);
            setGestureEnabled(false);
            ToastUtils.showShort(R.string.av_locked);
        }
        iv_lock.setSelected(isLocked());
//        setLocked(false);
    }

    //视频比例模式
    private int scale_val = 0;

    protected void doScale() {
        scale_val++;
        if (scale_val >= 6) scale_val = 0;
        switch (scale_val) {
            case 0:
                ToastUtils.showShort("默认");
                mControlWrapper.setScreenScaleType(VideoView.SCREEN_SCALE_DEFAULT);
                break;
            case 1:
                ToastUtils.showShort("16:9");
                mControlWrapper.setScreenScaleType(VideoView.SCREEN_SCALE_16_9);
                break;
            case 2:
                ToastUtils.showShort("4:3");
                mControlWrapper.setScreenScaleType(VideoView.SCREEN_SCALE_4_3);
                break;
            case 3:
                ToastUtils.showShort("填充");
                mControlWrapper.setScreenScaleType(VideoView.SCREEN_SCALE_MATCH_PARENT);
                break;
            case 4:
                ToastUtils.showShort("原始大小");
                mControlWrapper.setScreenScaleType(VideoView.SCREEN_SCALE_ORIGINAL);
                break;
            case 5:
                ToastUtils.showShort("居中裁剪");
                mControlWrapper.setScreenScaleType(VideoView.SCREEN_SCALE_CENTER_CROP);
                break;
            default:
                break;
        }
    }

    public void huaTest() {
        if (!isNormal) {
            toggleFullScreen();
        }
    }

    //弹幕开关
    public void onDanmakuChanged(boolean isFirst) {
        if (videoViewImpt == null) return;
        if (iv_danmaku == null || tv_danmaku == null) return;
        if (isFirst) {
            boolean b = SPUtils.getInstance().getBoolean(KEY_IS_OPEN_DANMAKU, true);
            Log.e(TAG, b + "2");
            //设置相反的状态
            iv_danmaku.setSelected(b);
            if (b) {
                videoViewImpt.showDanmaku();
            } else {
                videoViewImpt.hideDanmaku();
            }
        } else {
            if (!iv_danmaku.isSelected()) {
                ToastUtils.showShort("弹幕开");
                iv_danmaku.setSelected(true);
                videoViewImpt.showDanmaku();
                SPUtils.getInstance().put(KEY_IS_OPEN_DANMAKU, true);
                if (iv_danmaku.getVisibility() == VISIBLE) {
                    tv_danmaku.setVisibility(VISIBLE);
                    iv_msc_1.setVisibility(VISIBLE);
//                    tvPlaySource.setVisibility(VISIBLE);
                }
            } else {
                ToastUtils.showShort("弹幕关");
                iv_danmaku.setSelected(false);
                videoViewImpt.hideDanmaku();
                SPUtils.getInstance().put(KEY_IS_OPEN_DANMAKU, false);
                tv_danmaku.setVisibility(GONE);
                iv_msc_1.setVisibility(GONE);
//                tvPlaySource.setVisibility(GONE);
            }
        }
    }

    //显示弹幕输入框
    private PopupWindow popupWindow;
    private EditText et_danmaku;
    private CheckBox CheckBox_1;
    private CheckBox CheckBox_2;
    private CheckBox CheckBox_3;
    private CheckBox CheckBox_4;
    private CheckBox CheckBox_5;
    private CheckBox CheckBox_6;

    protected void showDanmaku() {
        if (popupWindow == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.pop_layout_danmaku, this, false);
            et_danmaku = view.findViewById(R.id.et_pop_danmaku);
            Button btn_danmaku = view.findViewById(R.id.btn_pop_danmaku);
            btn_danmaku.setOnClickListener(this);
            View iv_msc_2 = view.findViewById(R.id.iv_msc_2);
            iv_msc_2.setOnClickListener(this);
            CheckBox_1 = view.findViewById(R.id.CheckBox_1);
            CheckBox_1.setOnClickListener(this);
            CheckBox_2 = view.findViewById(R.id.CheckBox_2);
            CheckBox_2.setOnClickListener(this);
            CheckBox_3 = view.findViewById(R.id.CheckBox_3);
            CheckBox_3.setOnClickListener(this);
            CheckBox_4 = view.findViewById(R.id.CheckBox_4);
            CheckBox_4.setOnClickListener(this);
            CheckBox_5 = view.findViewById(R.id.CheckBox_5);
            CheckBox_5.setOnClickListener(this);
            CheckBox_6 = view.findViewById(R.id.CheckBox_6);
            CheckBox_6.setOnClickListener(this);
            if (xunFeiWitch != 1) {
                iv_msc_2.setVisibility(GONE);
            }
            popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
            popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            popupWindow.setBackgroundDrawable(new ColorDrawable(0xffdadada));
            popupWindow.setOutsideTouchable(true);
        }
        popupWindow.showAtLocation(this, Gravity.BOTTOM, 0, 0);
    }

    private String DanMuColor = "#FFFFFFFF";

    //发送弹幕---还需要上传到服务器
    private void sendDanmaku() {
        if (videoViewImpt == null) return;
        if (et_danmaku != null) {
            String str = et_danmaku.getText().toString();
            if (!StringUtils.isEmpty(str)) {
                videoViewImpt.addDanmaku(str, true, DanMuColor);
            } else {
                ToastUtils.showShort("请输入弹幕！");
            }
        }
    }
//
//    private void sendDanmaku_wqddg(String str) {
//        if (videoViewImpt == null) return;
//        if (!StringUtils.isEmpty(str)) {
//            videoViewImpt.addDanmaku(str, true);
//        } else {
//            ToastUtils.showShort("请输入弹幕！");
//        }
//    }

    public void setReplayByCurProgress(Boolean replayByCurProgress) {
        mReplayByCurProgress = replayByCurProgress;
    }

    public void setSpeed(String speed) {
        tv_speed.setText(speed);
    }

    private int curSpeedSelect = SPUtils.getInstance().getInt(KEY_SPEED_INDEX, 3);

    public void setSpeedSelect(String speed) {
        switch (speed) {
            case "0.5X":
                videoViewImpt.setVideoSpeed(0.50f);
                tv_speed.setText(getResources().getText(R.string.av_speed_1));
                curSpeedSelect = 5;
                break;
            case "0.75X":
                videoViewImpt.setVideoSpeed(0.75f);
                tv_speed.setText(getResources().getText(R.string.av_speed_2));
                curSpeedSelect = 4;
                break;
            case "1.0X":
                videoViewImpt.setVideoSpeed(1.00f);
                tv_speed.setText(getResources().getText(R.string.av_speed_3));
                curSpeedSelect = 3;
                break;
            case "1.25X":
                videoViewImpt.setVideoSpeed(1.25f);
                tv_speed.setText(getResources().getText(R.string.av_speed_4));
                curSpeedSelect = 2;
                break;
            case "1.5X":
                videoViewImpt.setVideoSpeed(1.50f);
                tv_speed.setText(getResources().getText(R.string.av_speed_5));
                curSpeedSelect = 1;
                break;
            case "2.0X":
                videoViewImpt.setVideoSpeed(2.00f);
                tv_speed.setText(getResources().getText(R.string.av_speed_6));
                curSpeedSelect = 0;
                break;
            default:
                break;
        }
        SPUtils.getInstance().put(KEY_SPEED_INDEX, curSpeedSelect);
    }

    //speed播放速度
    private PopupWindow speedPop;
    private LinearLayout speedPopLayout;
    private TextView tv_speed_1, tv_speed_2, tv_speed_3, tv_speed_4, tv_speed_5, tv_speed_6;
    private TextView oldSpeedTv;
    private OnClickListener speedOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (videoViewImpt == null) return;
            int i = v.getId();
            if (i == R.id.tv_pop_speed_1) {
                videoViewImpt.setVideoSpeed(0.50f);
                tv_speed_1.setTextColor(getResources().getColor(R.color.player_theme_color));
                tv_speed.setText(getResources().getText(R.string.av_speed_1));
                if (oldSpeedTv != null) oldSpeedTv.setTextColor(Color.WHITE);
                oldSpeedTv = tv_speed_1;
            } else if (i == R.id.tv_pop_speed_2) {
                videoViewImpt.setVideoSpeed(0.75f);
                tv_speed_2.setTextColor(getResources().getColor(R.color.player_theme_color));
                tv_speed.setText(getResources().getText(R.string.av_speed_2));
                if (oldSpeedTv != null) oldSpeedTv.setTextColor(Color.WHITE);
                oldSpeedTv = tv_speed_2;
            } else if (i == R.id.tv_pop_speed_3) {
                videoViewImpt.setVideoSpeed(1.00f);
                tv_speed_3.setTextColor(getResources().getColor(R.color.player_theme_color));
                tv_speed.setText(getResources().getText(R.string.av_speed_3));
                if (oldSpeedTv != null) oldSpeedTv.setTextColor(Color.WHITE);
                oldSpeedTv = tv_speed_3;
            } else if (i == R.id.tv_pop_speed_4) {
                videoViewImpt.setVideoSpeed(1.25f);
                tv_speed_4.setTextColor(getResources().getColor(R.color.player_theme_color));
                tv_speed.setText(getResources().getText(R.string.av_speed_4));
                if (oldSpeedTv != null) oldSpeedTv.setTextColor(Color.WHITE);
                oldSpeedTv = tv_speed_4;
            } else if (i == R.id.tv_pop_speed_5) {
                videoViewImpt.setVideoSpeed(1.50f);
                tv_speed_5.setTextColor(getResources().getColor(R.color.player_theme_color));
                tv_speed.setText(getResources().getText(R.string.av_speed_5));
                if (oldSpeedTv != null) oldSpeedTv.setTextColor(Color.WHITE);
                oldSpeedTv = tv_speed_5;
            } else if (i == R.id.tv_pop_speed_6) {
                videoViewImpt.setVideoSpeed(2.00f);
                tv_speed_6.setTextColor(getResources().getColor(R.color.player_theme_color));
                tv_speed.setText(getResources().getText(R.string.av_speed_6));
                if (oldSpeedTv != null) oldSpeedTv.setTextColor(Color.WHITE);
                oldSpeedTv = tv_speed_6;
            }
            speedPop.dismiss();
        }
    };

    private void selectSpeeed() {
        if (speedPop == null || speedPopLayout == null) {
            speedPopLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pop_layout_speed, this, false);
            tv_speed_1 = speedPopLayout.findViewById(R.id.tv_pop_speed_1);
            tv_speed_1.setOnClickListener(speedOnClickListener);
            tv_speed_2 = speedPopLayout.findViewById(R.id.tv_pop_speed_2);
            tv_speed_2.setOnClickListener(speedOnClickListener);
            tv_speed_3 = speedPopLayout.findViewById(R.id.tv_pop_speed_3);
            tv_speed_3.setTextColor(getResources().getColor(R.color.player_theme_color));
            oldSpeedTv = tv_speed_3;
            tv_speed_3.setOnClickListener(speedOnClickListener);
            tv_speed_4 = speedPopLayout.findViewById(R.id.tv_pop_speed_4);
            tv_speed_4.setOnClickListener(speedOnClickListener);
            tv_speed_5 = speedPopLayout.findViewById(R.id.tv_pop_speed_5);
            tv_speed_5.setOnClickListener(speedOnClickListener);
            tv_speed_6 = speedPopLayout.findViewById(R.id.tv_pop_speed_6);
            tv_speed_6.setOnClickListener(speedOnClickListener);
            speedPop = new PopupWindow(speedPopLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
            speedPop.setBackgroundDrawable(new ColorDrawable(getContext().getResources().getColor(android.R.color.transparent)));
            speedPop.setOutsideTouchable(true);
            speedPop.setClippingEnabled(false);
        }
        speedPopLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        speedPop.showAsDropDown(tv_speed, -((speedPopLayout.getMeasuredWidth() - tv_speed.getMeasuredWidth()) / 2),
                -(speedPopLayout.getMeasuredHeight() + tv_speed.getMeasuredHeight()));
    }


    //清晰度
    private PopupWindow hdPop;
    private LinearLayout hdPopLayout;
    private boolean isHdChange = true;
    private int currentIndex = 0;
    private List<String> hdList;
    private OnClickListener rateOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (videoViewImpt == null) return;
            int index = (int) v.getTag();
            if (currentIndex == index) return;
            ((TextView) hdPopLayout.getChildAt(currentIndex)).setTextColor(Color.BLACK);
            ((TextView) hdPopLayout.getChildAt(index)).setTextColor(getResources().getColor(R.color.player_theme_color));
            tv_hd.setText(hdList.get(index));
            videoViewImpt.switchHd(hdList.get(index));
            hdPop.dismiss();
            currentIndex = index;
        }
    };

    private void selectHd() {
        if (videoViewImpt == null) return;
        if (hdPop == null || hdPopLayout == null || isHdChange) {
            isHdChange = false;
            hdPopLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pop_layout_hd, this, false);
            hdPop = new PopupWindow(hdPopLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
            hdPop.setBackgroundDrawable(new ColorDrawable(0xffffffff));
            hdPop.setOutsideTouchable(true);
            hdPop.setClippingEnabled(false);
            hdList = new ArrayList<>();
            LinkedHashMap<String, String> definitionData = videoViewImpt.getHdData();
            if (definitionData == null) return;
            int i = 0;
            for (Map.Entry<String, String> entry : definitionData.entrySet()) {
                LogUtils.d("key:" + entry.getKey() + "   value:" + entry.getValue());
                hdList.add(entry.getKey());
                TextView rateItem = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.pop_item_layout_hd, hdPopLayout, false);
                rateItem.setText(entry.getKey());
                rateItem.setTag(i);
                if (i == currentIndex) {
                    rateItem.setTextColor(getResources().getColor(R.color.player_theme_color));
                }
                rateItem.setOnClickListener(rateOnClickListener);
                hdPopLayout.addView(rateItem);
                i++;
            }
        }
        hdPopLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        hdPop.showAsDropDown(tv_hd, -((hdPopLayout.getMeasuredWidth() - tv_hd.getMeasuredWidth()) / 2),
                -(hdPopLayout.getMeasuredHeight() + tv_hd.getMeasuredHeight()));
    }

    /**
     * 设置标题
     */
    public void setTitle(String title) {
        if (title == null) {
            return;
        }
        tv_title.post(() -> {
            tv_title.setText(title);
            if (title.length() > 10)
                tv_title.startScroll();
        });
    }

    public void showJiexi() {
        play_imgs();
        clpb_jiexi.post(() -> clpb_jiexi.setVisibility(View.VISIBLE));
    }

    public void updateJiexiProgess(String progress) {
        post(() -> tvJiexiMsg.setText(progress));
    }

    public void showPlayErrorView() {
        loge.setVisibility(GONE);
        gifIMageView.setVisibility(GONE);
        btError.setVisibility(View.VISIBLE);
        btReplay.setVisibility(View.VISIBLE);

        btError.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //hideJiexi();
                if (controllerClickListener != null) controllerClickListener.onClick(v);
            }
        });

        btReplay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //hideJiexi();
                if (controllerClickListener != null) controllerClickListener.onClick(v);
            }
        });
    }

    //播放器加载特效远程图片地址
    public static String replay_logo;
    public static String sdknreward;
    public static String sdkexcitation;
    public static int xunFeiWitch;

    public void visibility_wqddg(String playImg, String nreWard, String excitation, int xunFei) {
        replay_logo = playImg;
        sdknreward = nreWard;
        sdkexcitation = excitation;
        xunFeiWitch = xunFei;
    }

    public void play_imgs() {
        Log.d("播放器图片地址1", "iv_replay_logo: " + replay_logo);
        if (replay_logo.equals("0")) { //加载本地图片
            loge.setBackgroundResource(R.drawable.bf_bg_test);
        } else { //加载网络图片
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.bf_bg_test)  //加载错误时显示该图
                    .placeholder(R.drawable.bf_bg_test) //加载时显示该图
                    .override(Target.SIZE_ORIGINAL) //指定大小  Target.SIZE_ORIGINAL代表原始大小
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .skipMemoryCache(true);//禁用内存缓存
            Glide.with(mActivity)
                    .load(replay_logo)
                    .apply(requestOptions)
                    .into(loge);
        }
    }

    public void hideJiexi() {
        clpb_jiexi.post(new Runnable() {
            @Override
            public void run() {
                clpb_jiexi.setVisibility(View.GONE);
                loge.setVisibility(VISIBLE);
                gifIMageView.setVisibility(VISIBLE);
                btError.setVisibility(View.GONE);
                btReplay.setVisibility(View.GONE);
            }
        });
    }

    public void setClickivPlay(Boolean clickivPlay) {
        isClickivPlay = clickivPlay;
    }

    public Boolean getClickivPlay() {
        return isClickivPlay;
    }

    /**
     * 销毁
     */
    public void onDestroy() {
        videoViewImpt = null;
        stopBufferTime();
    }

    private void stopBufferTime() {
        if (bufferTimer != null) {
            bufferTimer.cancel();
            bufferTimer = null;
        }
        if (bufferTask != null) {
            bufferTask.cancel();
            bufferTask = null;
        }
    }
}
