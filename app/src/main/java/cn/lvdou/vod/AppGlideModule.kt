package cn.lvdou.vod

import android.content.Context
import android.util.Log
import cn.lvdou.vod.utils.SmartDns
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * Glide 网络栈改造（修复 WiFi 下列表图片不加载）：
 *  P1：图片加载改走 OkHttp —— client 接入 SmartDns，图片域名同样走公共 DoH 解析（修复 DNS 污染），
 *      DoH 只取 A 记录 = IPv4 优先（规避 WiFi 常见的 IPv6 不通陷阱）；
 *  P2：应用级拦截器 —— http 首试升级 https（防明文被 WiFi 中间人阻断），
 *      非 2xx 或连接失败自动回退原 http（不支持 443 的图床也不至于变坏）。
 *
 * 实现要点：用 registerComponents + OkHttpUrlLoader.Factory 显式注册（覆盖 okhttp3-integration
 * 库模块的默认 client），而非 GlideBuilder.okHttpClient（本项目 glide 4.9.0 无此方法）。
 */
@GlideModule
class AppGlideModuleImpl : AppGlideModule() {

    private val imageClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .dns(SmartDns)                      // 图片域名走 DoH（与 API 流量一致）
            .proxy(Proxy.NO_PROXY)              // 与其它客户端策略一致：不走系统代理
            .addInterceptor(SecureUrlUpgradeInterceptor())
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(15L, TimeUnit.SECONDS)
            .build()
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        if (!BuildConfig.DEBUG) {
            builder.setLogLevel(Log.ERROR) // 正式包收敛 Glide 日志噪音
        }
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // 显式替换 Glide 的 GlideUrl→InputStream 加载器为我们的 OkHttp client
        // （SmartDns DoH + http→https 防劫持），覆盖全部图片请求：列表/详情/banner/专题
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(imageClient)
        )
    }
}

/**
 * http → https 首试升级 + 原地址回退（仅应用级拦截，范围 = 本 Glide client 的图片请求）。
 * https 首试 2xx → 直接用；非 2xx → 关闭响应后回退原 http；
 * https 连接异常（IOException）→ 回退原 http（回退也失败则原样抛出 = 等同旧行为）。
 */
private class SecureUrlUpgradeInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        if (url.isHttps || url.scheme != "http") {
            return chain.proceed(request)
        }
        val secureRequest = request.newBuilder()
            .url(url.newBuilder().scheme("https").build())
            .build()
        return try {
            val response = chain.proceed(secureRequest)
            if (response.code in 200..299) {
                response
            } else {
                Log.d("SecureUrlUpgrade", "https 首试非2xx(${response.code})，回退 http: ${url.encodedPath}")
                response.close()
                chain.proceed(request)
            }
        } catch (e: IOException) {
            Log.d("SecureUrlUpgrade", "https 首试失败，回退 http: ${url.encodedPath} ($e)")
            chain.proceed(request)
        }
    }
}
