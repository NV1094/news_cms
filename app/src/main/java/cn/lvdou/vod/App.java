package cn.lvdou.vod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.dpuntu.downloader.DownloadManager;
import com.dueeeke.videoplayer.player.VideoViewConfig;
import com.dueeeke.videoplayer.player.VideoViewManager;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.orhanobut.hawk.Hawk;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.tencent.mmkv.MMKV;
import com.tencent.smtt.sdk.QbSdk;
import com.umeng.commonsdk.UMConfigure;

import org.litepal.LitePal;
import org.xutils.x;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Objects;

import cn.lvdou.av.play.MyIjkPlayerFactory;
import cn.lvdou.vod.base.BaseApplication;
import cn.lvdou.vod.utils.MMkvUtils;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import jaygoo.library.m3u8downloader.M3U8Library;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.graphics.Typeface;

public class App extends BaseApplication {

    @SuppressLint("StaticFieldLeak")
    private static App vocApp;
    private static final String TAG = "App";
    public static boolean SdkSwitch = true; //开启SDK
    private static WeakReference<App> weakReference;

    public static String S;

    public static App getInstance() {
        return weakReference.get();
    }

    public static App getApplication() {
        return vocApp;
    }

    static {
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context);
            }
        });
    }

    //public static String K2 = "";
    public static String KEY = "lvdoutv-1.0.0"; //接口密钥
    public static String CHANNE = "PZMOoBzHy5AFH2LTH9j";
    public static String MUNG_KEY = "61c2e011e0f9bb492ba583d2"; //友盟KEY
    //public static String OkHttpUrlB = "9ce0e220854c969c8b8eb0106ca48dbc079cbaf0bb58f1"; //备用域名
    //public static String OkHttpUrl = "9ce0e220854c969c8b8eb0106ca48dbc079cbaf0bb58f1"; //对接域名

//    public void urlCode() {
//        String secret_key = MUNG_KEY + TAG;
//        String K1 = Rc4.MD(secret_key);
//        if (K1 != null) {
//            K2 = K1.substring(
//                    Integer.parseInt(new String(Base64.decode("Mw==", Base64.DEFAULT))),
//                    Integer.parseInt(new String(Base64.decode("MTM=", Base64.DEFAULT))));
//            String K3 = Rc4.decry_RC4(OkHttpUrl, K2); //主域名
//            ApiConfig.MOGAI_BASE_URLB = Rc4.decry_RC4(OkHttpUrlB, K2);
//            if (!Rc4.decry_RC4(OkHttpUrl, K2).contains("http")) { //不是否域名
//                System.exit(0);
//            } else { //是域名
//                if (K3.contains(".txt")) { //动态域名
//                    getUrl(K3);
//                } else { //常规域名
//                    ApiConfig.MOGAI_BASE_URL = K3;
//                }
//            }
//        }
//    }

//   public void getUrl(String OkHttpClients) {
//       OkHttpClient okHttpClient = new OkHttpClient();
//       final Request request = new Request.Builder()
//               .url(OkHttpClients)
//               .build();
//       Call call = okHttpClient.newCall(request);
//       call.enqueue(new Callback() {

//           @Override
//           public void onFailure(@NonNull Call call, @NonNull IOException e) {
//               Log.d("你好", "请求失败啊啊啊啊啊啊啊");
//           }

//           @Override
//           public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//               String ApiList = Objects.requireNonNull(response.body()).string();
//               if (ApiList.contains("||")) {
//                   ApiList = getSubString(ApiList, "||", "||"); //取中间文本
//               }
//               if (ApiList.substring(0, 4).contains("http")) {
//                   ApiConfig.MOGAI_BASE_URL = ApiList;
//               }
//               Log.d("请求成功", "请求失败啊啊啊啊啊啊啊");
//           }
//       });
//   }

    /**
     * 取两个文本之间的文本值
     *
     * @param text  源文本 比如：欲取全文本为 12345
     * @param left  文本前面
     * @param right 后面文本
     * @return 返回 String
     */
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public static String getSubString(String text, String left, String right) {
        String result;
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MMKV.initialize(this);

        // 进程被系统回收后从最近任务恢复时，StartActivity 不会重新走动态域名获取，
        // ApiConfig.MOGAI_BASE_URL 会是 null 导致 Retrofit2Utils 初始化崩溃。
        // 这里在应用启动时优先从本地持久化恢复上次成功的域名。
        if (ApiConfig.MOGAI_BASE_URL == null || ApiConfig.MOGAI_BASE_URL.isEmpty()) {
            String savedUrl = MMkvUtils.Buildsx().loadUrlList();
            if (savedUrl != null && !savedUrl.isEmpty()) {
                ApiConfig.MOGAI_BASE_URL = savedUrl;
            }
        }

        if (SdkSwitch) {
            ApiConfig.SdkSwitchm = true;
        }
        try {
            setRxJavaErrorHandler();
        } catch (Exception e) {
        }
        LitePal.initialize(this);
        UMConfigure.init(this, MUNG_KEY, CHANNE, UMConfigure.DEVICE_TYPE_PHONE, "");
        weakReference = new WeakReference<>(this);
        vocApp = this; //xUtils 初始化
        x.Ext.init(this);
        x.Ext.setDebug(true);//是否输出Debug日志
        Hawk.init(this).build();
        DownloadManager.initDownloader(vocApp);
        QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.i(getClass().getName(), "initX5Environment onCoreInitFinished");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                Log.i(getClass().getName(), "initX5Environment onViewInitFinished");
            }
        });
        //播放器配置，注意：此为全局配置，按需开启
        VideoViewManager.setConfig(VideoViewConfig.newBuilder()
                .setPlayerFactory(MyIjkPlayerFactory.create())
                .build());
        M3U8Library.init(this);
    }

    public static int getSrceenWidth() {
        return ((WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

//        public void setTypeface(){
//        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/danmaku.ttf");
//        try
//        {
//            Field field = Typeface.class.getDeclaredField("MONOSPACE");
//            field.setAccessible(true);
//            field.set(null, typeFace);
//        }catch (NoSuchFieldException e)
//        {
//            e.printStackTrace();
//        }
//        catch (IllegalAccessException e)
//        {
//            e.printStackTrace();
//        }
//    }
// 页面刷新代码 static 代码段可以防止内存泄露
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                //  layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);//全局设置主题颜色
                return new ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setDrawableSize(20);
            }
        });
    }

    /**
     * RxJava2 当取消订阅后(dispose())，RxJava抛出的异常后续无法接收(此时后台线程仍在跑，可能会抛出IO等异常),全部由RxJavaPlugin接收，需要提前设置ErrorHandler
     * 详情：http://engineering.rallyhealth.com/mobile/rxjava/reactive/2017/03/15/migrating-to-rxjava-2.html#Error Handling
     */
    private void setRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.d(TAG, "RxJavaPlugins throw test");
            }
        });
    }
}
