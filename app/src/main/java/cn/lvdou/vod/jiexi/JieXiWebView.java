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
import cn.lvdou.vod.utils.AUtils;
import cn.lvdou.vod.utils.AgainstCheatUtil;
import cn.lvdou.vod.utils.MMkvUtils;
import cn.lvdou.vod.utils.OkHttpUtils;
import cn.lvdou.vod.utils.RSAUtils;
import okhttp3.Call;
import okhttp3.Response;

@SuppressLint({"SetJavaScriptEnabled", "ViewConstructor"})
public class JieXiWebView extends WebView {
    private static final String TAG = "JieXi--";
    private int index = 0;
    private final String[] parseUrls;
    private final String url;
    private BackListener mBackListener;
    private int time = 8;//一条线路探8s
    private Timer timer;
    private TimerTask task;
    private final Handler handler = new Handler();
    private Context mContext;
    static Handler mainHanlder = new Handler(Looper.getMainLooper());

    public JieXiWebView(Context context, String parses, String url, int curParseIndex, BackListener backListener) {
        super(context);
        mContext = context;
        if (parses.contains(",")) {
            parseUrls = parses.split(",");
        } else {
            parseUrls = new String[1];
            parseUrls[0] = parses;
        }
        this.url = url;
        if (curParseIndex >= parseUrls.length) {
            backListener.onError();
        } else {
            index = curParseIndex;
            mBackListener = backListener;
            initSetting();
            if (timer == null) {
                timer = new Timer();
                task = new TimerTask() {
                    @Override
                    public void run() {
                        if (mBackListener != null) {
                            mBackListener.onProgressUpdate("正在获取播放地址 " + (8 - time + 1) + "s");
                        }
                        if (--time <= 0) {
                            time = 8;
                            parstNextOrComplete();
                        }
                    }
                };
                timer.schedule(task, 0, 1000);
            }
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

    public void startParse() {

        String realUrl = parseUrls[index] + url + "&apikey="+ AUtils.sign() + "&keytime=" + AUtils.keyTime();
        if (realUrl.contains("json") || realUrl.contains("..")) {
            realUrl = realUrl.replaceFirst("\\.\\.", "\\.");
            Log.d(TAG, "startParse: 通过json解析" + url);
            getJsonResult(realUrl);
        } else {
            Log.d(TAG, "startParse: 通过webview解析" + url);
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
            //正式编译禁止输出
            //Log.i(TAG, "请求的url：" + url);
            if (url.contains(".mp4") || url.contains(".m3u8") || url.contains("/m3u8?") || url.contains(".flv")) {
                //正式编译禁止输出
                // Log.i(TAG, "webview解析到播放地址：" + url);
                if (mBackListener != null) {
                    timer.cancel();
                    timer = null;
                    time = 8;
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

    private void parstNextOrComplete() {
        if (++index < parseUrls.length) {
            startParse();
        } else {
            time = 8;
            timer.cancel();
            task = null;
            timer = null;
            if (mBackListener != null)
                mBackListener.onError();
        }
    }

    private void getJsonResult(String url) {
        OkHttpUtils.getInstance()
                .getDataAsynFromNet(url, new OkHttpUtils.MyNetCall() {
                    @Override
                    public void success(Call call, Response response) throws IOException {
                        boolean isSuccessful = response.isSuccessful();
                        if (isSuccessful) {
                            try {
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
                                                if (timer != null) {
                                                    timer.cancel();
                                                    timer = null;
                                                }
                                                time = 8;
                                                String playUrl = playBean.getUrl();
                                                if (playUrl.startsWith("http") || playUrl.startsWith("https")) {
                                                    playBean.setUrl(playUrl);
                                                } else {
                                                    playBean.setUrl("https:" + playUrl);
                                                }
                                                //正式编译禁止输出
                                                // Log.i(TAG, "json解析到播放地址22：" + url + "playurl==>" + playBean.getUrl());
                                                mBackListener.onSuccess(playBean.getUrl(), index);
                                                mBackListener = null;//获取到解析地址后就不回调了
                                            }
                                        }
                                    });
                                } else {
                                    Log.i(TAG, "json返回值 解析不成功");
                                    time = 0;
                                }
                            } catch (Exception e) {
                                Log.i(TAG, "json解析错误1：" + e);
                                time = 0;
                            }
                        } else {
                            Log.i(TAG, "json解析错误：非200");
                            time = 0;
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
