package cn.lvdou.vod.ui.start;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.CacheDiskStaticUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;

import cn.lvdou.vod.Rc4;
import cn.lvdou.vod.base.RequestManager;

import com.blankj.utilcode.util.ToastUtils;
import com.github.StormWyrm.wanandroid.base.sheduler.IoMainScheduler;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.youxiao.ssp.ad.bean.SSPAd;
import com.youxiao.ssp.ad.config.DownloadConfirmPolicy;
import com.youxiao.ssp.ad.core.AdClient;
import com.youxiao.ssp.ad.listener.AdLoadAdapter;
import com.youxiao.ssp.core.SSPSdk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.App;
import cn.lvdou.vod.MainActivity;
import cn.lvdou.vod.R;
import cn.lvdou.vod.ad.AdClickListener;
import cn.lvdou.vod.ad.AdWebView;
import cn.lvdou.vod.base.BaseActivity;
import cn.lvdou.vod.base.exception.ResponseException;
import cn.lvdou.vod.base.observer.BaseObserver;
import cn.lvdou.vod.bean.AppConfigBean;
import cn.lvdou.vod.bean.BaseResult;
import cn.lvdou.vod.bean.CloseSplashEvent;
import cn.lvdou.vod.bean.StartBean;
import cn.lvdou.vod.bean.TitleEvent;
import cn.lvdou.vod.download.SPKey;
import cn.lvdou.vod.entity.AdvEntity;
import cn.lvdou.vod.netservice.StartService;
import cn.lvdou.vod.netservice.VodService;
import cn.lvdou.vod.network.RetryWhen;
import cn.lvdou.vod.ui.login.LoginActivity;
import cn.lvdou.vod.ui.lvdou.TanChu;
import cn.lvdou.vod.ui.widget.HitDialog;
import cn.lvdou.vod.utils.AgainstCheatUtil;
import cn.lvdou.vod.utils.MMkvUtils;
import cn.lvdou.vod.utils.OkHttpUtils;
import cn.lvdou.vod.utils.Retrofit2Utils;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Call;
import okhttp3.Response;

public class StartActivity extends BaseActivity {

    public static final String KEY_START_BEAN = "KEY_START_BEAN";
    private static final String TAG = "start";

    private String ad_str;
    private Disposable disposable;
    private StartService startService;
    private Disposable disposable1;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.awv_start)
    AdWebView webView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_start)
    TextView textView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.iv_image)
    ImageView imageView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_load)
    TextView loadTv;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.start_frame)
    ViewGroup start_frame;

    private AdClient adClient;
    private boolean isInit = false;
    private boolean isInits = false;
    private static final int MAX_TIME = 5;
    private int start_time = MAX_TIME;
    private Handler handler = new Handler();
    private boolean isClosed = false;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setTime(start_time);
            start_time -= 1;
            if (start_time >= 0 && !isClosed) {
                handler.postDelayed(runnable, 1000);
            } else {
                gotoMain();
            }
        }
    };

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_start;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Launcher);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        adClient = new AdClient(getMActivity());
        getApi();
    }

    private void goooo() {
        // goooo() 存在多条调用路径（广告 onError/onAdClick、onResume 重复触发等），
        // 重复 register 会抛 "already registered" 崩溃，此处加注册守卫
        if (!EventBus.getDefault().isRegistered(StartActivity.this)) {
            EventBus.getDefault().register(StartActivity.this);
        }
        getAppConfig();
        BarUtils.setStatusBarVisibility(StartActivity.this, false);
        BarUtils.setNavBarVisibility(StartActivity.this, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("xxxxxxx", "startbean========001onResume");
        if(ApiConfig.MOGAI_BASE_URL != null){
            if (!isInit) getStartData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (adClient != null) {
            adClient.release();
        }
    }

    //动态域名 API域名填写
    private void getApi() {
        OkHttpUtils.getInstance()
                .getDataAsynFromNet(new String(Base64.decode("aHR0cHM6Ly90di42MHMuaWN1L21rZXkudHh0", Base64.DEFAULT)), new OkHttpUtils.MyNetCall() {

                    @Override
                    public void success(Call call, Response response) throws IOException {
                        if(response.code() == 200 && response.body() != null){
                            String str = new String(Base64.decode(getString(R.string.video), Base64.DEFAULT));
                            String data = Rc4.decry_RC4(response.body().string(), str);
                            //Log.i("getApi==========", data);
                            try {
                                JSONObject json = new JSONObject(data);
                                ApiConfig.MOGAI_BASE_URL = json.optString("apiurl");
                                App.S =  json.optString("rc4");
                                if(ApiConfig.MOGAI_BASE_URL != null){
                                    if (!isInit) getStartData();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            fallbackToHome(); //网络失败兜底：进入首页
                        }
                    }

                    @Override
                    public void failed(Call call, IOException e) {
                        fallbackToHome(); //网络失败兜底：进入首页
                    }
                });
    }

    //动态域名 备用域名填写
    private void getApitwo() {
        OkHttpUtils.getInstance()
                .getDataAsynFromNet(new String(Base64.decode("aHR0cHM6Ly90di42MHMuaWN1L21rZXkudHh0", Base64.DEFAULT)), new OkHttpUtils.MyNetCall() {

                    @Override
                    public void success(Call call, Response response) throws IOException {
                        if(response.code() == 200 && response.body() != null){
                            String str = new String(Base64.decode(getString(R.string.video), Base64.DEFAULT));
                            String data = Rc4.decry_RC4(response.body().string(), str);
                            //Log.i("getApi==========", data);
                            try {
                                JSONObject json = new JSONObject(data);
                                ApiConfig.MOGAI_BASE_URLB = json.optString("apiurl");
                                App.S =  json.optString("rc4");
                                if(ApiConfig.MOGAI_BASE_URLB != null){
                                    if (!isInit) getStartData();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void failed(Call call, IOException e) {

                    }
                });
    }

    private void getTabThreeName() {
        VodService vodService = Retrofit2Utils.INSTANCE.createByGson(VodService.class);
        if (AgainstCheatUtil.showWarn(vodService)) {
            return;
        }
        assert vodService != null;
        vodService.getTabThreeName()
                .compose(new IoMainScheduler<>())
                .subscribe(new BaseObserver<BaseResult<String>>(true) {
                    @Override
                    public void onError(@NotNull ResponseException e) {
                        // 网络失败不再退出应用，继续进入首页（原 System.exit(0)）
                        Log.i(TAG, "getTabThreeName onError: " + e.getMessage());
                    }

                    @Override
                    public void onSuccess(BaseResult<String> data) {
                        Log.d("底部TAB", "onSuccess: " + data.getDatas() + "======");
                        if (!data.getData().contains("|")) { //不允许对接开源后台
                            System.exit(0);
                        } else {
                            ApiConfig.LVDOU_TAB = data;
                        }
                    }
                });
    }

    private void getStartData() {
        Log.i("xxxxxxx", "startbean========001");
        if (startService == null) {
            startService = Retrofit2Utils.INSTANCE.createByGson(StartService.class);
        }

        startService.getStartBean()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .retryWhen(new RetryWhen(3, 30))
                .subscribe(new Observer<BaseResult<StartBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.i("xxxxxxx", "disposable========haha111");
                        if (disposable != null && !disposable.isDisposed()) {
                            disposable.dispose();
                            disposable = null;
                        }
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull BaseResult<StartBean> result) {
                        Log.i("xxxxxxx", "startbean========haha111");
                        if (result != null) {
                            Log.i("xxxxxxx", "startbean========111");
                            if (result.isSuccessful()) {
                                Log.i("xxxxxxx", "startbean========222");
                                if (result.getData() != null) {
                                    Log.i("xxxxxxx", "startbean========333");
                                    StartBean startBean = result.getData();
                                    CacheDiskStaticUtils.put(KEY_START_BEAN, startBean);
                                    if (startBean != null) {
                                        StartBean.Ads ads = startBean.getAds();
                                        VodService vodService2 = Retrofit2Utils.INSTANCE.createByGson(VodService.class);
                                        RequestManager.execute(vodService2.getAdv(),
                                                new BaseObserver<AdvEntity>() {
                                                    @Override
                                                    public void onSuccess(AdvEntity data) {
                                                        MMkvUtils.Companion.Builds().saveAdvEntity(data);
                                                    }

                                                    @Override
                                                    public void onError(@NotNull ResponseException e) {

                                                    }
                                                }
                                        );

                                        StartBean.Ads newAds = startBean.getAds();
                                        StartBean.Ads oldAds = new StartBean.Ads();
                                        Gson gson = new Gson();

                                        String indexGet = SPUtils.getInstance().getString(SPKey.AD_INDEX);
                                        StartBean.Ad oldIndex = gson.fromJson(indexGet, StartBean.Ad.class);
                                        String cartoonGet = SPUtils.getInstance().getString(SPKey.AD_CARTOON);
                                        StartBean.Ad oldCartoon = gson.fromJson(cartoonGet, StartBean.Ad.class);
                                        String sitcomGet = SPUtils.getInstance().getString(SPKey.AD_SITCOM);
                                        StartBean.Ad oldSitcom = gson.fromJson(sitcomGet, StartBean.Ad.class);
                                        String vodGet = SPUtils.getInstance().getString(SPKey.AD_VOD);
                                        StartBean.Ad oldVod = gson.fromJson(vodGet, StartBean.Ad.class);
                                        String searcherGet = SPUtils.getInstance().getString(SPKey.AD_SEARCHER);
                                        StartBean.Ad oldSearcher = gson.fromJson(searcherGet, StartBean.Ad.class);
                                        String varietyGet = SPUtils.getInstance().getString(SPKey.AD_VARIETY);
                                        StartBean.Ad oldVariety = gson.fromJson(varietyGet, StartBean.Ad.class);

                                        oldAds.setIndex(oldIndex);
                                        oldAds.setCartoon(oldCartoon);
                                        oldAds.setSitcom(oldSitcom);
                                        oldAds.setVod(oldVod);
                                        oldAds.setSearcher(oldSearcher);
                                        oldAds.setVariety(oldVariety);

                                        MMkvUtils.Companion.Builds().saveStartBean(startBean);
                                        if (newAds != null) {
                                            StartBean.Ad index = newAds.getIndex();
                                            if (newAds.getIndex().getDescription() == null || newAds.getIndex().getDescription().isEmpty()) {
                                                if (oldAds.getIndex() != null && oldAds.getIndex().getDescription() != null && !oldAds.getIndex().getDescription().isEmpty()) {
                                                    index.setDescription(oldAds.getIndex().getDescription());
                                                }
                                            }
                                            String indexStr = gson.toJson(newAds.getIndex(), StartBean.Ad.class);
                                            SPUtils.getInstance().put(SPKey.AD_INDEX, indexStr);

                                            StartBean.Ad cartoon = newAds.getCartoon();
                                            if (newAds.getCartoon().getDescription() == null || newAds.getCartoon().getDescription().isEmpty()) {
                                                if (oldAds.getCartoon() != null && oldAds.getCartoon().getDescription() != null && !oldAds.getCartoon().getDescription().isEmpty()) {
                                                    cartoon.setDescription(oldAds.getCartoon().getDescription());
                                                }
                                            }
                                            String cartoonStr = gson.toJson(newAds.getCartoon(), StartBean.Ad.class);
                                            SPUtils.getInstance().put(SPKey.AD_CARTOON, cartoonStr);

                                            StartBean.Ad sitcom = newAds.getSitcom();
                                            if (newAds.getSitcom().getDescription() == null || newAds.getSitcom().getDescription().isEmpty()) {
                                                if (oldAds.getSitcom() != null && oldAds.getSitcom().getDescription() != null && !oldAds.getSitcom().getDescription().isEmpty()) {
                                                    sitcom.setDescription(oldAds.getSitcom().getDescription());
                                                }
                                            }
                                            String sitcomStr = gson.toJson(newAds.getSitcom(), StartBean.Ad.class);
                                            SPUtils.getInstance().put(SPKey.AD_SITCOM, sitcomStr);

                                            StartBean.Ad vod = newAds.getVod();
                                            if (newAds.getVod().getDescription() == null || newAds.getVod().getDescription().isEmpty()) {
                                                if (oldAds.getVod() != null && oldAds.getVod().getDescription() != null && !oldAds.getVod().getDescription().isEmpty()) {
                                                    vod.setDescription(oldAds.getVod().getDescription());
                                                }
                                            }
                                            String vodStr = gson.toJson(newAds.getVod(), StartBean.Ad.class);
                                            SPUtils.getInstance().put(SPKey.AD_VOD, vodStr);

                                            StartBean.Ad searcher = newAds.getSearcher();
                                            if (newAds.getSearcher().getDescription() == null || newAds.getSearcher().getDescription().isEmpty()) {
                                                if (oldAds.getSearcher() != null && oldAds.getSearcher().getDescription() != null && !oldAds.getSearcher().getDescription().isEmpty()) {
                                                    searcher.setDescription(oldAds.getSearcher().getDescription());
                                                }
                                            }
                                            String searcherStr = gson.toJson(newAds.getSearcher(), StartBean.Ad.class);
                                            SPUtils.getInstance().put(SPKey.AD_SEARCHER, searcherStr);


                                            StartBean.Ad variety = newAds.getVariety();
                                            if (newAds.getVariety().getDescription() == null || newAds.getVariety().getDescription().isEmpty()) {
                                                if (oldAds.getVariety() != null && oldAds.getVariety().getDescription() != null && !oldAds.getVariety().getDescription().isEmpty()) {
                                                    variety.setDescription(oldAds.getVariety().getDescription());
                                                }
                                            }
                                            String varietyStr = gson.toJson(newAds.getVariety(), StartBean.Ad.class);
                                            SPUtils.getInstance().put(SPKey.AD_VARIETY, varietyStr);

                                            if (MMkvUtils.Companion.Builds().loadStartBean("") != null) {
                                                StartBean startBean1 = MMkvUtils.Companion.Builds().loadStartBean("");
                                                assert startBean1 != null;
                                                startBean1.setAds(newAds);
                                                MMkvUtils.Companion.Builds().saveStartBean(startBean1);
                                            }

                                            StartBean.Ad ad = newAds.getStartup_adv();
                                            StartBean.Ad xunfei_appid = newAds.getXunfei_appid();
                                            ApiConfig.LVDOU_LOGIN_TYPE = newAds.getDefine_account().getStatus();
                                            MMkvUtils.Companion.Builds().saveSearchHot(startBean.getSearch_hot());
                                            if (xunfei_appid != null && xunfei_appid.getDescription() != null && xunfei_appid.getStatus() == 1){
                                                ApiConfig.UNFEIYUYIN = 1;
                                                SpeechUtility.createUtility(mActivity, SpeechConstant.APPID + "=" + xunfei_appid.getDescription());
                                            }
                                            if (ad != null) {
                                                ad_str = ad.getDescription();
                                                if (ad.getStatus() == 0) {
                                                    gotoMain();
                                                }
                                            }
                                            SdkStartup(startBean);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        Log.i("xxxxxxx", "startbean========555" + e.getMessage());
                        MMkvUtils.Companion.Builds().saveUrlList(""); //清空保存的域名
                        if (StringUtils.isEmpty(ad_str)) {
                            gotoMain(); //网络失败直接进入首页
                        } else {
                            init();
                        }
                    }

                    @Override
                    public void onComplete() {
                        init();
                    }
                });
    }

    private void SdkStartup(StartBean startBean) {
        if (!App.SdkSwitch || startBean == null) {  //数据异常关闭SDK
            App.SdkSwitch = false; //禁用SDK
            goooo(); //进入首页
        } else {
            StartBean.Ads startBeans = startBean.getAds(); //后台关闭了SDK功能
            if (startBeans.getApplication_id() == null || startBeans.getApplication_id().getDescription() == null || startBeans.getApplication_id().getStatus() == 0) {
                App.SdkSwitch = false; //禁用SDK
                goooo(); //进入首页
            } else {
                initAd(startBeans.getApplication_id().getDescription()); //初始化SDK
                if (startBeans.getSdk_startup().getDescription() == null || startBeans.getSdk_startup().getStatus() == 0) { //关闭开屏
                    goooo(); //进入首页
                } else {
                    if (!MMkvUtils.Companion.Builds().loadTimeoutDATA().equals("")) {
                        adClient.requestSplashAd(start_frame, startBeans.getSdk_startup().getDescription(), new AdLoadAdapter() {
                            @Override
                            public void onError(int i, String s) { //拉取广告失败
                                goooo();  //进入首页
                            }

                            @Override
                            public void onAdClick(SSPAd sspAd) { //开屏被点击？
                                super.onAdClick(sspAd);
                                goooo(); //进入首页
                                gotoMain();
                            }

                            @Override
                            public void onAdDismiss(SSPAd ad) { //拉取成功
                                super.onAdDismiss(ad);
                                gotoMain();
                            }
                        });
                    } else {
                        goooo(); //进入首页
                    }
                }
            }
        }
    }

    //初始化信天翁SDK
    private void initAd(String Application_id) {
        SSPSdk.setReqPermission(true);
        SSPSdk.setDownloadConfirmPolicy(DownloadConfirmPolicy.CONFIRM_NONE);
        //ViVo开屏配置
        SSPSdk.getExtData().setAppName(getString(R.string.app_name));
        SSPSdk.getExtData().setAppDesc(getString(R.string.app_name));
        SSPSdk.getExtData().setEnableHotSplash(true);
        //华为配置
        SSPSdk.getExtData().setEnableUserInfo(true);
        //初始化SDK
        SSPSdk.init(getApplication(), Application_id, "ch_official", true);
    }

    private void stopGet() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable.dispose();
        }
    }

    private void init() {
        if (StringUtils.isEmpty(ad_str)) return;
        webView.setVisibility(View.VISIBLE);
        LogUtils.e(ad_str);
        isInit = true;
        webView.isforceFullScreen(true);
        webView.addAdClickListener(new AdClickListener() {
            @Override
            public void onAdClick(String url) {
                isClosed = true;
                handler.removeCallbacks(runnable);
            }
        });
        webView.loadHtmlBody(ad_str);
    }

    private void cancleImage() {
        ValueAnimator anim = ValueAnimator.ofFloat(1, 0);
        anim.setDuration(500);
        anim.setRepeatCount(0);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (imageView != null) {
                    imageView.setAlpha((Float) animation.getAnimatedValue());
                    loadTv.setAlpha((Float) animation.getAnimatedValue());
                }
                System.out.println("onAnimationUpdate " + (Float) animation.getAnimatedValue());
            }
        });
        anim.start();
    }

    private void setTime(int i) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (textView != null) {
                    textView.setText(StringUtils.getString(R.string.skip, i));
                }
            }
        });
    }

    /** 网络失败兜底：域名获取失败时不再卡在启动页，直接进入首页 */
    private void fallbackToHome() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                if (StringUtils.isEmpty(ApiConfig.MOGAI_BASE_URL)) {
                    gotoMain();
                }
            }
        });
    }

    private void gotoMain() {
        // 网络失败且无任何缓存配置时，写入最小默认配置，保证首页可正常初始化
        if (MMkvUtils.Companion.Builds().loadStartBean("") == null) {
            StartBean def = new StartBean();
            def.setApp_cc_fd_jc("1");
            def.setApp_ui_tab_fun("1");
            def.setApp_theme_ui("1");
            def.setApp_banner_style("1");
            MMkvUtils.Companion.Builds().saveStartBean(def);
        }
        getLogin();
    }

    private void getLogin() {
        isClosed = true;
        handler.removeCallbacks(runnable);
        stopGet();
        String baseUrl = ApiConfig.MOGAI_BASE_URL;
        if (StringUtils.isEmpty(baseUrl)) {
            baseUrl = MMkvUtils.Buildsx().loadUrlList();
        }
        if (!StringUtils.isEmpty(baseUrl)) {
            if (!baseUrl.equals(ApiConfig.MOGAI_BASE_URLB)) { //不保存本地域名
                MMkvUtils.Companion.Builds().saveUrlList(baseUrl); //保存启动成功的域名
            }
            MMkvUtils.Companion.Builds().saveTimeoutDATA(baseUrl); //保存启动成功的域名
        }
        String PASSWD = MMkvUtils.Buildsx().loadAccount2();
        if (PASSWD.length() != 0) {
            ActivityUtils.startActivity(MainActivity.class);
            finish(); //必须保留，不然没法退出程序
        } else {
            ActivityUtils.startActivity(LoginActivity.class);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.tv_start)
    void missAd() {
        getLogin();
    }

    public void getAppConfig() {
        VodService vodService = Retrofit2Utils.INSTANCE.createByGson(VodService.class);

        RequestManager.execute(this, vodService.getPlayAd(2 + ""),
                new BaseObserver<AppConfigBean>() {
                    @Override
                    public void onSuccess(AppConfigBean data) {
                        MMkvUtils.Companion.Builds().saveAppConfigAd(data);
                    }

                    @Override
                    public void onError(@NotNull ResponseException e) {

                    }
                }
        );

        //获取标签状态
        RequestManager.execute(this, vodService.getPlayAd(1 + ""),
                new BaseObserver<AppConfigBean>() {
                    @Override
                    public void onSuccess(AppConfigBean data) {
                        MMkvUtils.Companion.Builds().saveAppConfig(data);
                    }

                    @Override
                    public void onError(@NotNull ResponseException e) {

                    }
                }
        );

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onCloseEvent(CloseSplashEvent event) {
        start_time = MAX_TIME;
        setTime(start_time);
        handler.postDelayed(runnable, 1000);
        cancleImage();
    }

}
