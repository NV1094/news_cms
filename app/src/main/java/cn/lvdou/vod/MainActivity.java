package cn.lvdou.vod;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.github.StormWyrm.wanandroid.base.sheduler.IoMainScheduler;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.youxiao.ssp.ad.bean.SSPAd;
import com.youxiao.ssp.ad.bean.SSPContentItem;
import com.youxiao.ssp.ad.core.AdClient;
import com.youxiao.ssp.ad.listener.AdLoadAdapter;
import com.youxiao.ssp.ad.listener.IContentPageListener;
import com.youxiao.ssp.ad.listener.IContentVideoListener;
import com.youxiao.ssp.core.SSPSdk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import cn.lvdou.vod.base.BaseActivity;
import cn.lvdou.vod.base.BaseMainFragment;
import cn.lvdou.vod.base.RequestManager;
import cn.lvdou.vod.base.exception.ResponseException;
import cn.lvdou.vod.base.observer.BaseObserver;
import cn.lvdou.vod.bean.AppUpdateBean;
import cn.lvdou.vod.bean.BaseResult;
import cn.lvdou.vod.bean.OpenRecommendEvent;
import cn.lvdou.vod.bean.PipMsgBean;
import cn.lvdou.vod.bean.StartBean;
import cn.lvdou.vod.bean.TitleEvent;
import cn.lvdou.vod.bean.UserVideo;
import cn.lvdou.vod.netservice.VodService;
import cn.lvdou.vod.pip.PIPManager;
import cn.lvdou.vod.receiver.ScreenBroadCastReceiver;
import cn.lvdou.vod.ui.home.HomeFragment;
import cn.lvdou.vod.ui.home.HomeFragmentB;
import cn.lvdou.vod.ui.home.HomeFragmentC;
import cn.lvdou.vod.ui.home.HomeFragmentD;
import cn.lvdou.vod.ui.home.Wqddg_Gao;
import cn.lvdou.vod.ui.live.LiveFragment;
import cn.lvdou.vod.ui.push.PushActivity;
import cn.lvdou.vod.ui.rank.RankFragment;
import cn.lvdou.vod.ui.rank.RankFragmentB;
import cn.lvdou.vod.ui.rank.RankFragmentC;
import cn.lvdou.vod.ui.specialtopic.SpecialtTopicFragment;
import cn.lvdou.vod.ui.user.UserFragment;
import cn.lvdou.vod.ui.user.UserFragmentB;
import cn.lvdou.vod.ui.user.UserFragmentC;
import cn.lvdou.vod.ui.user.UserFragmentD;
import cn.lvdou.vod.ui.user.UserFragmentE;
import cn.lvdou.vod.ui.user.UserFragmentF;
import cn.lvdou.vod.ui.widget.AppUpdateDialog;
import cn.lvdou.vod.ui.widget.NoticeDialog2;
import cn.lvdou.vod.utils.AgainstCheatUtil;
import cn.lvdou.vod.utils.MMkvUtils;
import cn.lvdou.vod.utils.Retrofit2Utils;
import cn.lvdou.vod.utils.UserUtils;
import me.yokeyword.fragmentation.SupportFragment;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener, BaseMainFragment.OnBackToFirstListener {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.bnv_main)
    BottomNavigationView bnv_main;

    public static final int HOME = 0;
    public static final int TOPIC = 1;
    public static final int RANK = 2;
    public static final int LIVE = 3;
    public static final int USER = 4;
    private final SupportFragment[] mFragments = new SupportFragment[6];
    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    List<String> mPermissionList = new ArrayList<>();
    private static final int PERMISSION_REQUEST = 1;
    private ScreenBroadCastReceiver screenBroadCastReceiver;
    private NotificationManager manager;
    private Notification notification;
    private StartBean startBean;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //原始
        getTabThreeName();
        EventBus.getDefault().register(this);
        BarUtils.setStatusBarColor(this, Color.TRANSPARENT);
        // 液体玻璃导航栏背景由布局 glass_bottom_bar_bg 提供，不再在运行时覆盖
        screenBroadCastReceiver = new ScreenBroadCastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenBroadCastReceiver, filter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(getColor(R.color.transparent));
        }

        //初始化
        SupportFragment firstFragment = findFragment(HomeFragment.class);
        startBean = MMkvUtils.Companion.Builds().loadStartBean("");
        if (startBean == null) {
            // 网络失败且无缓存配置：写入最小默认配置，保证可进入首页
            startBean = new StartBean();
            startBean.setApp_cc_fd_jc("1");
            startBean.setApp_ui_tab_fun("1");
            startBean.setApp_theme_ui("1");
            startBean.setApp_banner_style("1");
        }
        if (startBean.getApp_cc_fd_jc() == null || startBean.getApp_ui_tab_fun() == null || startBean.getApp_theme_ui() == null || startBean.getApp_banner_style() == null) { //确定为盗版后
            Intent var2 = new Intent("android.intent.action.VIEW", Uri.parse(ApiConfig.MOGAI_BASE_URL + "?url=" + ApiConfig.MOGAI_BASE_URL));
            ActivityUtils.startActivity(var2);
            System.exit(0);
        }

        //sdkad.getSdk_Insertscreens().getStatus() == 1

        if (firstFragment == null) {
            String uihometopbg = startBean.getUi_home_topbg();
            if (uihometopbg != null && uihometopbg.equals("1")) { //控制BAN边距
                mFragments[HOME] = HomeFragment.newInstance();
            } else {
                if (ApiConfig.SdkSwitchm) { //是否授权主题切换
                    if (!MMkvUtils.Companion.Builds().loadThemeStyle().equals("")) { //本地是否设置样式
                        if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("1")) {
                            mFragments[HOME] = HomeFragmentB.newInstance();
                        } else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("2")) {
                            mFragments[HOME] = HomeFragmentB.newInstance();
                        } else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("3")) {
                            mFragments[HOME] = HomeFragmentD.newInstance();
                        }else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("4")) {
                            mFragments[HOME] = HomeFragmentD.newInstance();
                        }else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("5")) {
                            mFragments[HOME] = HomeFragmentB.newInstance();
                        } else {
                            mFragments[HOME] = HomeFragmentC.newInstance();
                        }
                    } else {
                        switch (startBean.getApp_theme_ui()) {
                            case "1":
                                mFragments[HOME] = HomeFragmentB.newInstance();
                                break;
                            case "2":
                                mFragments[HOME] = HomeFragmentB.newInstance();
                                break;
                            case "3":
                                mFragments[HOME] = HomeFragmentD.newInstance();
                                break;
                            case "4":
                                mFragments[HOME] = HomeFragmentD.newInstance();
                                break;
                            case "5":
                                mFragments[HOME] = HomeFragmentB.newInstance();
                                break;
                            default:
                                mFragments[HOME] = HomeFragmentC.newInstance();
                                break;
                        }
                    }
                } else {
                    mFragments[HOME] = HomeFragment.newInstance();
                }
            }

            if (startBean.getApp_ui_tab_fun() != null && startBean.getApp_ui_tab_fun().equals("1")) {
                mFragments[TOPIC] = SpecialtTopicFragment.newInstance();
            } else {
                mFragments[TOPIC] = LiveFragment.newInstance();
            }

            mFragments[LIVE] = Wqddg_Gao.newInstance();


            if (ApiConfig.SdkSwitchm) { //是否授权主题切换
                if (!MMkvUtils.Companion.Builds().loadThemeStyle().equals("")) { //本地是否设置样式
                    if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("1")) {
                        mFragments[RANK] = RankFragmentB.newInstance();
                    } else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("2")) {
                        mFragments[RANK] = RankFragmentB.newInstance();
                    } else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("3")) {
                        mFragments[RANK] = RankFragment.newInstance();
                    }else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("4")) {
                        mFragments[RANK] = RankFragment.newInstance();
                    }else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("5")) {
                        mFragments[RANK] = RankFragmentB.newInstance();
                    } else {
                        mFragments[RANK] = RankFragmentC.newInstance();
                    }
                } else {

                    switch (startBean.getApp_theme_ui()) {
                        case "1":
                            mFragments[RANK] = RankFragmentB.newInstance();
                            break;
                        case "2":
                            mFragments[RANK] = RankFragmentB.newInstance();
                            break;
                        case "3":
                            mFragments[RANK] = RankFragment.newInstance();
                            break;
                        case "4":
                            mFragments[RANK] = RankFragment.newInstance();
                            break;
                        case "5":
                            mFragments[RANK] = RankFragmentB.newInstance();
                            break;
                        default:
                            mFragments[RANK] = RankFragmentC.newInstance();
                            break;
                    }
                }
            } else {
                mFragments[RANK] = RankFragment.newInstance();
            }


            if (ApiConfig.SdkSwitchm) { //是否授权主题切换
                if (!MMkvUtils.Companion.Builds().loadThemeStyle().equals("")) { //本地是否设置样式
                    if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("1")) {
                        mFragments[USER] = UserFragmentB.newInstance();
                    } else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("2")) {
                        mFragments[USER] = UserFragmentC.newInstance();
                    } else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("3")) {
                        mFragments[USER] = UserFragmentD.newInstance();
                    }else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("4")) {
                        mFragments[USER] = UserFragmentE.newInstance();
                    }else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("5")) {
                        mFragments[USER] = UserFragmentF.newInstance();
                    } else {
                        mFragments[USER] = UserFragment.newInstance();
                    }
                } else {

                    switch (startBean.getApp_theme_ui()) {
                        case "1":
                            mFragments[USER] = UserFragmentB.newInstance();
                            break;
                        case "2":
                            mFragments[USER] = UserFragmentC.newInstance();
                            break;
                        case "3":
                            mFragments[USER] = UserFragmentD.newInstance();
                            break;
                        case "4":
                            mFragments[USER] = UserFragmentE.newInstance();
                            break;
                        case "5":
                            mFragments[USER] = UserFragmentF.newInstance();
                            break;
                        default:
                            mFragments[USER] = UserFragment.newInstance();
                            break;
                    }
                }
            } else {
                mFragments[USER] = UserFragmentE.newInstance();
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            showFragment(R.id.fl_main_container, mFragments[USER], transaction);
            showFragment(R.id.fl_main_container, mFragments[TOPIC], transaction);
            showFragment(R.id.fl_main_container, mFragments[RANK], transaction);
            showFragment(R.id.fl_main_container, mFragments[LIVE], transaction);
            showFragment(R.id.fl_main_container, mFragments[HOME], transaction);
            transaction.commit();
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题
            // 这里我们需要拿到mFragments的引用
            mFragments[HOME] = firstFragment;

            mFragments[LIVE] = Wqddg_Gao.newInstance();

            if (startBean.getApp_ui_tab_fun() != null && startBean.getApp_ui_tab_fun().equals("1")) {
                mFragments[TOPIC] = findFragment(SpecialtTopicFragment.class);
            } else { //第三个按钮功能
                mFragments[TOPIC] = findFragment(LiveFragment.class);
            }

             //有0、1、2三个状态
            if (ApiConfig.SdkSwitchm) { //是否授权主题切换
                if (!MMkvUtils.Companion.Builds().loadThemeStyle().equals("")) {
                    if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("1")) {
                        mFragments[RANK] = findFragment(RankFragmentB.class);
                    } else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("2")) {
                        mFragments[RANK] = findFragment(RankFragmentB.class);
                    } else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("3")) {
                        mFragments[RANK] = findFragment(RankFragment.class);
                    }else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("4")) {
                        mFragments[RANK] = findFragment(RankFragment.class);
                    }else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("5")) {
                        mFragments[RANK] = findFragment(RankFragmentB.class);
                    } else {
                        mFragments[RANK] = findFragment(RankFragmentC.class);
                    }
                } else {

                    switch (startBean.getApp_theme_ui()) {
                        case "1":
                            mFragments[RANK] = findFragment(RankFragmentB.class);
                            break;
                        case "2":
                            mFragments[RANK] = findFragment(RankFragmentB.class);
                            break;
                        case "3":
                            mFragments[RANK] = findFragment(RankFragment.class);
                            break;
                        case "4":
                            mFragments[RANK] = findFragment(RankFragment.class);
                            break;
                        case "5":
                            mFragments[RANK] = findFragment(RankFragmentB.class);
                            break;
                        default:
                            mFragments[RANK] = findFragment(RankFragmentC.class);
                            break;
                    }
                }
            } else {
                mFragments[RANK] = findFragment(RankFragment.class);
            }
            mFragments[TOPIC] = findFragment(SpecialtTopicFragment.class);
            if (ApiConfig.SdkSwitchm) { //是否授权主题切换
                if (!MMkvUtils.Companion.Builds().loadThemeStyle().equals("")) {
                    if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("1")) {
                        mFragments[USER] = findFragment(UserFragmentB.class);
                    } else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("2")) {
                        mFragments[USER] = findFragment(UserFragmentC.class);
                    } else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("3")) {
                        mFragments[USER] = findFragment(UserFragmentD.class);
                    }else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("4")) {
                        mFragments[USER] = findFragment(UserFragmentE.class);
                    }else if (MMkvUtils.Companion.Builds().loadThemeStyle().equals("5")) {
                        mFragments[USER] = findFragment(UserFragmentF.class);
                    } else {
                        mFragments[USER] = findFragment(UserFragment.class);
                    }
                } else {

                    switch (startBean.getApp_theme_ui()) {
                        case "1":
                            mFragments[USER] = findFragment(UserFragmentB.class);
                            break;
                        case "2":
                            mFragments[USER] = findFragment(UserFragmentC.class);
                            break;
                        case "3":
                            mFragments[USER] = findFragment(UserFragmentD.class);
                            break;
                        case "4":
                            mFragments[USER] = findFragment(UserFragmentE.class);
                            break;
                        case "5":
                            mFragments[USER] = findFragment(UserFragmentF.class);
                            break;
                        default:
                            mFragments[USER] = findFragment(UserFragment.class);
                            break;
                    }
                }
            } else {
                mFragments[USER] = findFragment(UserFragmentE.class);
            }
        }
        initPermission();
        getPush();
        initView();
        showNotice();
        checkVersion();

        /**
         * 信天翁
         * */
        if (startBean.getApp_home_ads() != null && startBean.getApp_home_ads().equals("1")) {
            if (App.SdkSwitch && startBean.getAds() != null) {
                StartBean.Ads sdkad = startBean.getAds();
                if (sdkad.getSdk_Insertscreen() != null && sdkad.getSdk_Insertscreen().getStatus() == 1 && sdkad.getSdk_Insertscreen().getDescription() != null) { //未开启插屏
                    AdClient adClient = new AdClient(mActivity);
                    adClient.requestInteractionAd(sdkad.getSdk_Insertscreen().getDescription(), new AdLoadAdapter() {
                        @Override
                        public void onAdShow(SSPAd sspAd) {
                            super.onAdShow(sspAd);
                        }

                        @Override
                        public void onAdDismiss(SSPAd var1){
                            adClient.release();
                        }
                    });
                }
            }
        }
    }

    AdClient adClient = new AdClient(mActivity);

    public void getPush() {
        if (App.SdkSwitch && startBean.getPushS() != null) {
            if (!startBean.getPushS().equals("0")) {
                manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("lvdou", getString(R.string.app_name),
                            NotificationManager.IMPORTANCE_HIGH);
                    manager.createNotificationChannel(channel);
                }
                Intent intent = new Intent(this, PushActivity.class);
                @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
                notification = new NotificationCompat.Builder(this, "lvdou")
                        .setContentTitle("每日推荐")
                        .setContentText("为您推荐" + startBean.getPushS() + "部视频，点击查看>>")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();
                manager.notify(1, notification);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(screenBroadCastReceiver);
        adClient.release();
    }

    private void initPermission() {
        mPermissionList.clear();
        /**
         * 判断哪些权限未授予
         */
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        /**
         * 判断是否为空
         */
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了

        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST);
        }
    }

    /**
     * 响应授权
     * 这里不管用户是否拒绝，都进入首页，不再重复申请权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:
                break;
            default:
                break;
        }
    }

    private void addAllFragment(SupportFragment[] mFragments, FragmentTransaction transaction, int layoutId) {
        for (SupportFragment fragment : mFragments) {
            if (fragment != null) {
                transaction.add(layoutId, fragment);
            }
        }
    }

    private void showFragment(int layoutId, SupportFragment fragment, FragmentTransaction transaction) {
        if (fragment != null) {
            transaction.add(layoutId, fragment);
        }
    }

    private void hideAllFragment(SupportFragment[] mFragments, FragmentTransaction transaction) {
        for (SupportFragment fragment : mFragments) {
            if (fragment != null) {
                transaction.hide(fragment);
            }
        }
    }

    @Override
    protected void initView() {
        bnv_main.setItemIconTintList(null);
        bnv_main.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackToFirstFragment() {
        bnv_main.setSelectedItemId(R.id.navigation_main_home);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_main_home:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    getWindow().setStatusBarColor(getColor(R.color.transparent));
                }
                showHideFragment(mFragments[HOME]);
                return true;

            case R.id.navigation_main_topic:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    getWindow().setStatusBarColor(getColor(R.color.transparent));
                }
                EventBus.getDefault().post("隐藏播放历史");
                showHideFragment(mFragments[TOPIC]);
                return true;

            case R.id.navigation_main_rank:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    getWindow().setStatusBarColor(getColor(R.color.transparent));
                }
                EventBus.getDefault().post("隐藏播放历史");
                showHideFragment(mFragments[RANK]);
                return true;

            case R.id.navigation_main_live:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    getWindow().setStatusBarColor(getColor(R.color.transparent));
                }
                if (!App.SdkSwitch) {
                    EventBus.getDefault().post("隐藏播放历史");
                    showHideFragment(mFragments[LIVE]);
                } else {
                    getgao();
                }
                return true;
            case R.id.navigation_main_user:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    getWindow().setStatusBarColor(getColor(R.color.transparent));
                }
                EventBus.getDefault().post("隐藏播放历史");
                showHideFragment(mFragments[USER]);
                mFragments[USER].setUserVisibleHint(true);
                return true;
            default:
                return false;
        }
    }

    String a = "";
    int b = 0;
    int c;
    long d;

    private void getgao() {
        //获取短视频内容Fragment页面
        Fragment contentFragment = SSPSdk.getContent();
        //加载短视频内容页面
        getSupportFragmentManager().beginTransaction().add(R.id.fl_main_container, contentFragment).commitAllowingStateLoss();
        //增加内容页面监听器
        SSPSdk.setContentPageListener(new IContentPageListener() {
            @Override
            public void onPageEnter(SSPContentItem sspContentItem) {
                //页面进入
//                a =sspContentItem.a;
//                b=sspContentItem.b;
//                c=sspContentItem.c;
//                d=sspContentItem.d;
            }

            @Override
            public void onPageResume(SSPContentItem sspContentItem) {
                //页面恢复可见
                sspContentItem.a = a;
                sspContentItem.b = b;
                sspContentItem.c = c;
                sspContentItem.d = d;
            }

            @Override
            public void onPagePause(SSPContentItem sspContentItem) {
                //页面不可见
                a = sspContentItem.a;
                b = sspContentItem.b;
                c = sspContentItem.c;
                d = sspContentItem.d;
            }

            @Override
            public void onPageLeave(SSPContentItem sspContentItem) {
                //页面离开
                a = sspContentItem.a;
                b = sspContentItem.b;
                c = sspContentItem.c;
                d = sspContentItem.d;
            }
        });
        SSPSdk.setContentVideoListener(new IContentVideoListener() {
            @Override
            public void onVideoPlayStart(SSPContentItem sspContentItem) {

            }

            @Override
            public void onVideoPlayPaused(SSPContentItem sspContentItem) {

            }

            @Override
            public void onVideoPlayResume(SSPContentItem sspContentItem) {

            }

            @Override
            public void onVideoPlayCompleted(SSPContentItem sspContentItem) {

            }

            @Override
            public void onVideoPlayError(SSPContentItem sspContentItem, int i, int i1) {

            }
        });

    }

    private void showNotice() {
        if (startBean != null) {
            StartBean.Document document = startBean.getDocument();
            if (document != null) {
                StartBean.Register registerd = document.getRegisterd();
                if (registerd != null && registerd.getStatus().equals("1")) {
                    new NoticeDialog2(mActivity, registerd.getContent()).show();
                }
            }
        }
    }

    private void checkVersion() {
        VodService vodService = Retrofit2Utils.INSTANCE.createByGson(VodService.class);

        assert vodService != null;
        vodService
                .checkVersion("v" + AppUtils.getAppVersionName(), 1 + "")
                .compose(new IoMainScheduler<>())
                .subscribe(new BaseObserver<BaseResult<AppUpdateBean>>(true) {
                    @Override
                    public void onError(@NotNull ResponseException e) {

                    }

                    @Override
                    public void onSuccess(BaseResult<AppUpdateBean> data) {
                        if (data.getData() != null) {
                            new AppUpdateDialog(mActivity, data.getData())
                                    .show();
                        }
                    }
                });

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onOpenRecommendEvent(OpenRecommendEvent event) {
        showHideFragment(mFragments[HOME]);
        bnv_main.setSelectedItemId(R.id.navigation_main_home);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void SCREEN_OFF(String msg) {
        if (msg.equals("锁屏")) {
            Log.e("哈哈哈哈", "接收到锁屏信息");
            PipMsgBean pipMsg = PIPManager.getInstance().getPipMsg();
            if (pipMsg != null) {
                PIPManager.getInstance().stopPlay();
            }
        } else if (msg.equals("解锁")) {
            PipMsgBean pipMsg = PIPManager.getInstance().getPipMsg();
            if (pipMsg != null) {
                PIPManager.getInstance().startPlay();
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void pipRecode(PipMsgBean pipMsgBean) {
        if (UserUtils.isLogin()) {
            VodService vodService = Retrofit2Utils.INSTANCE.createByGson(VodService.class);

            Log.d("画中画记录", "voidid=${voidid}  vodSelectedWorks=${vodSelectedWorks}  playSource=${playSource}  percentage=${percentage} curProgress=${curProgress}");
            BaseActivity activity;
            RequestManager.execute(MainActivity.this, vodService.addPlayLog(pipMsgBean.getVoidid(), pipMsgBean.getVodSelectedWorks(), pipMsgBean.getPlaySource(), pipMsgBean.getPercentage(), pipMsgBean.getUrlIndex() + "", pipMsgBean.getCurPregress() + "", pipMsgBean.getPlaySourceIndex() + ""),
                    new BaseObserver<UserVideo>() {
                        @Override
                        public void onError(@NotNull ResponseException e) {
                            PIPManager.getInstance().stopFloatWindow();
                            PIPManager.getInstance().reset();
                        }

                        @Override
                        public void onSuccess(UserVideo data) {
                            PIPManager.getInstance().stopFloatWindow();
                            PIPManager.getInstance().reset();
                        }
                    });
        } else {
            PIPManager.getInstance().stopFloatWindow();
            PIPManager.getInstance().reset();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (System.currentTimeMillis() - startTime <= 5000) {
                super.onBackPressedSupport();
                android.os.Process.killProcess(android.os.Process.myPid());
            } else {
                startTime = System.currentTimeMillis();
                ToastUtils.showShort("再按一次退出程序");
            }
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    private Long startTime = 0L;

    private void getTabThreeName() {
        VodService vodService = Retrofit2Utils.INSTANCE.createByGson(VodService.class);

        assert vodService != null;
        vodService.getTabThreeName()
                .compose(new IoMainScheduler<>())
                .subscribe(new BaseObserver<BaseResult<String>>(true) {
                    @Override
                    public void onError(@NotNull ResponseException e) {
                    }

                    @Override
                    public void onSuccess(BaseResult<String> data) {
                        String tab_name;
                        String ui_faxian;
                        String ui_zhuanji;
                        if (data.getDatas() != null) {
                            Log.d("底部TAB", "onSuccess: " + data.getDatas() + "======");
                            boolean status = data.getDatas().contains("|");
                            if (status) { //插件数据
                                String str = data.getDatas();
                                String[] strArr = str.split("\\|");
                                tab_name = strArr[0]; //按钮名称
                                ui_faxian = strArr[1]; //发现开关
                                ui_zhuanji = strArr[2]; //专辑开关
                            } else {  //开源后台赋值
                                tab_name = data.getDatas();
                                ui_faxian = "1"; //打开
                                ui_zhuanji = "0"; //关闭
                            }
                        } else {
                            tab_name = "发现";
                            ui_faxian = "1";
                            ui_zhuanji = "0";
                        }
                        if (tab_name != null) {
                            ((BottomNavigationView) findViewById(R.id.bnv_main)).getMenu().findItem(R.id.navigation_main_topic).setTitle(tab_name);
                            EventBus.getDefault().postSticky(new TitleEvent(tab_name));
                        }

                        if (ui_faxian.equals("0")) { //关闭发现
                            ((BottomNavigationView) findViewById(R.id.bnv_main)).getMenu().removeItem(R.id.navigation_main_live);
                        }
                        if (ui_zhuanji.equals("0")) { //关闭专辑
                            ((BottomNavigationView) findViewById(R.id.bnv_main)).getMenu().removeItem(R.id.navigation_main_topic);
                        }
                    }
                });
    }
}
