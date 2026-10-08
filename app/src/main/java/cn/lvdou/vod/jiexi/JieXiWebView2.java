package cn.lvdou.vod.jiexi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import cn.lvdou.av.play.*;
import java.io.IOException;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;

import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.App;
import cn.lvdou.vod.Rc4;
import cn.lvdou.vod.bean.JieXiPlayBean;
import cn.lvdou.vod.utils.MMkvUtils;
import cn.lvdou.vod.utils.OkHttpUtils;
import cn.lvdou.vod.utils.AUtils;
import okhttp3.Call;
import okhttp3.Response;

@SuppressLint("SetJavaScriptEnabled")
public class JieXiWebView2 extends WebView {
    private static final String TAG = "JieXi--";
    private int index = 0;
    private int size = 0;
    private BackListener mBackListener;
    private Handler handler = new Handler();
    private Context mContext;
    static Handler mainHanlder = new Handler(Looper.getMainLooper());
    private boolean mIsEnd = false;

    public JieXiWebView2(Context context, String parses, BackListener backListener) {
        super(context);
        mContext = context;
        if (parses == null || parses.isEmpty()) {
            System.out.println("----- parses 空空");
            backListener.onError();
        } else {
            mBackListener = backListener;
            initSetting();
        }

        if (timer == null) {
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    if (mBackListener != null) {
                        mBackListener.onProgressUpdate("正在努力加载中" + (8 - time + 1) + "s");
                    }
                    if (--time <= 0) {
                        time = 8;
                        if (timer != null) {
                            timer.cancel();
                        }
                        task = null;
                        timer = null;
                        if (mBackListener != null) {
                            LogUtils.d("", "====Parse jiexi6 url=");
                            mBackListener.onSuccess("", index);
                        }
                    }
                }
            };
            timer.schedule(task, 0, 1000);
        }

    }

    @Override
    public void destroy() {
        super.destroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        mBackListener = null;
    }

    private int time = 8;//一条线路探8s
    private Timer timer;
    private TimerTask task;

    public void startParse(String realUrl, int curIndex, int curSize, boolean isEnd) {
        mIsEnd = isEnd;
        System.out.println("----- mIsEnd" + mIsEnd + "  curIndex=" + curIndex);
        index = curIndex;
        size = curSize;
        if (realUrl.contains("..") || realUrl.contains("...")) {
            if (realUrl.contains("...")) {
                realUrl = realUrl.replaceFirst("\\.\\.\\.", "\\.");
                LogUtils.d("", "====Parse jiexi realUrl=" + realUrl);
                getJsonResult(realUrl, true);
            }
            if (realUrl.contains("..")) {
                realUrl = realUrl.replaceFirst("\\.\\.", "\\.");
                LogUtils.d("", "====Parse jiexi realUrl2=" + realUrl);
                getJsonResult(realUrl, false);
            }
        } else {
            String finalRealUrl = realUrl;
            handler.post(() -> loadUrl(finalRealUrl));
        }
    }

    private void initSetting() {
        setClickable(true);
        setWebViewClient(webViewClient);
        WebSettings webSetting = getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        webSetting.setSupportZoom(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(true);
        // setAppCacheEnabled was removed from the SDK (API 33+); app cache is disabled by default
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting.setGeolocationEnabled(true);
        webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSetting.setSupportMultipleWindows(true);
    }

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            Log.i(TAG, "请求的url：" + url);
            if (url.contains(".mp4") || url.contains(".m3u8") || url.contains("/m3u8?") || url.contains(".flv")) {
                if (mBackListener != null) {
                    LogUtils.d("", "====Parse jiexi1 realUrl=" + url);
                    mBackListener.onSuccess(url, index);
                    mBackListener = null;//获取到解析地址后就不回调了
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //super.onReceivedSslError(view, handler, error);
            handler.proceed();// 接受所有网站的证书
        }
    };

    /**
     * @param isOnlyUrl true 就是 直接返回 链接，不是 Data格式
     */
    private void getJsonResult(String url, boolean isOnlyUrl) {
        Log.d("TAGurl", "getJsonResult: " + url);
        OkHttpUtils.getInstance()
                .getDataAsynFromNet(url, new OkHttpUtils.MyNetCall() {
                    @Override
                    public void success(Call call, Response response) throws IOException {
                        boolean isSuccessful = response.isSuccessful();
                        if (isSuccessful) {
                            try {
                                if (isOnlyUrl) { //解析接口带两个点不会走这里
                                    if (mBackListener != null) {
                                        String jiexi_url = Objects.requireNonNull(response.body()).string();
                                        LogUtils.d("", "====Parse jiexi2 url=" + jiexi_url);
                                        mBackListener.onSuccess(jiexi_url, index);
                                        mBackListener = null;//获取到解析地址后就不回调了
                                    }
                                } else { //一般情况JSON我们走这里
                                    JieXiPlayBean playBean = GsonUtils.fromJson(Objects.requireNonNull(response.body()).string(), JieXiPlayBean.class);
                                    if (playBean.getEncryption() == 1) {
                                        String apiKeys = Objects.requireNonNull(MMkvUtils.Companion.Builds().loadAdvEntity("")).getJiexi_key();
                                        String[] apikey = ApiConfig.MOGAI_BASE_URL.split("//");
                                        playBean.setUrl(Rc4.decry_RC4(playBean.getUrl(), apikey[1]+apiKeys));
                                    }
                                    if (playBean.getCode().equals("200")) {
                                        mainHanlder.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (mBackListener != null) {
                                                    if (playBean.getUrl() == null) {
                                                        if (mBackListener != null) {
                                                            mBackListener.onError();
                                                        }
                                                        return;
                                                    }

                                                    if (playBean.getUrl().endsWith(".m3u8") && !playBean.getUrl().startsWith("http")) {
                                                        String playUrl = playBean.getUrl();
                                                        if (playUrl.startsWith("http") || playUrl.startsWith("https")) {
                                                            playBean.setUrl(playUrl);
                                                        } else {
                                                            playBean.setUrl("http:" + playUrl);
                                                        }
                                                    }
                                                    //正式编译禁止输出
                                                    if (mBackListener != null) {
                                                        mBackListener.onSuccess(playBean.getUrl(), index);
                                                        mBackListener = null;//获取到解析地址后就不回调了
                                                    }
                                                }
                                            }
                                        });
                                    } else {
                                        Log.i(TAG, "json返回值 解析不成功");
                                        if (mBackListener != null && mIsEnd) {
                                            mBackListener.onError();
                                        }
                                        if (mBackListener != null) {
                                            LogUtils.d("", "====Parse jiexi3 url=");
                                            mBackListener.onSuccess("", index);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.i(TAG, "json解析错误2：" + e);
                                if (mBackListener != null && mIsEnd) {
                                    mBackListener.onError();
                                }
                                if (mBackListener != null) {
                                    LogUtils.d("", "====Parse jiexi4 url=");
                                    mBackListener.onSuccess("", index);
                                }
                            }
                        } else {
                            Log.i(TAG, "json解析错误：非200");
                            if (mBackListener != null && mIsEnd) {
                                mBackListener.onError();
                            }
                            if (mBackListener != null) {
                                LogUtils.d("", "====Parse jiexi5 url=");
                                mBackListener.onSuccess("", index);
                            }
                        }
                    }

                    @Override
                    public void failed(Call call, IOException e) {
                        Log.i(TAG, "json解析错误：" + e);
                    }
                });
    }

    private boolean isWifiProxy() {
        final boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress;
        int proxyPort;
        if (IS_ICS_OR_LATER) {
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
        } else {
            proxyAddress = android.net.Proxy.getHost(App.getInstance().getContext());
            proxyPort = android.net.Proxy.getPort(App.getInstance().getContext());
        }
        return (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
    }

    /**
     * 检测是否正在使用VPN，如果在使用返回true,反之返回flase
     */
    public static boolean isVpnUsed() {
        try {
            Enumeration niList = NetworkInterface.getNetworkInterfaces();
            if (niList != null) {
                for (Object f : Collections.list(niList)) {
                    NetworkInterface intf = (NetworkInterface) f;
                    if (!intf.isUp() || intf.getInterfaceAddresses().size() == 0) {
                        continue;
                    }
                    Log.d("-----", "isVpnUsed() NetworkInterface Name: " + intf.getName());
                    if ("tun0".equals(intf.getName()) || "ppp0".equals(intf.getName())) {
                        return true; // The VPN is up
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

}
