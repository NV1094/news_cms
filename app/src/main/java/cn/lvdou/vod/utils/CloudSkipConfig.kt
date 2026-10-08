package cn.lvdou.vod.utils

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.liuwei.android.upnpcast.util.CastUtils
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

/**
 * 云端全局片头跳过配置
 *
 * 配置地址: https://tv.60s.icu/skip/tv.json
 * JSON 格式: { "播放器名": 30 | "00:00:15", "*": 30 }
 *   - key   : 播放器名称（播放源显示名 player_info.show，trim 后精确匹配；"*" 为兜底）
 *   - value : 纯数字 = 跳过秒数；字符串 "HH:MM:SS" = 时间格式；0 = 不跳过（不生效）
 *
 * 生命周期: fetch() 先读 MMKV 缓存立即生效，网络成功后覆盖缓存并刷新内存；
 *          网络失败静默降级为缓存（首次安装且未拉到则本次不跳过，不阻塞播放）。
 */
object CloudSkipConfig {
    private const val TAG = "CloudSkipConfig"
    private const val CONFIG_URL = "https://tv.60s.icu/skip/tv.json"
    private const val CACHE_KEY = "cloud_skip_config_json"
    private const val FALLBACK_KEY = "*"

    // 播放器名 -> 片头跳过毫秒；@Volatile 保证 OkHttp 线程写入后主线程读取可见
    @Volatile
    private var skipMap: Map<String, Long> = emptyMap()

    /**
     * 拉取云端配置：先读本地缓存立即生效，网络成功后覆盖缓存并刷新内存。
     * 每次打开播放页调用，异步、不阻塞播放。
     */
    fun fetch() {
        // 1. 缓存兜底（同步，立即可用）
        try {
            val cache = MMkvUtils.Buildsx().loadString(CACHE_KEY, "")
            if (!cache.isNullOrEmpty()) {
                applyJson(cache, "cache")
            }
        } catch (e: Exception) {
            Log.d(TAG, "读取缓存失败: $e")
        }

        // 2. 异步拉取云端
        OkHttpUtils.getInstance().getDataAsynFromNet(CONFIG_URL, object : OkHttpUtils.MyNetCall {
            override fun success(call: Call?, response: Response?) {
                try {
                    if (response != null && response.code == 200) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty()) {
                            MMkvUtils.Buildsx().saveString(CACHE_KEY, body)
                            applyJson(body, "network")
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "处理云端响应失败: $e")
                }
            }

            override fun failed(call: Call?, e: IOException?) {
                Log.d(TAG, "拉取失败(沿用缓存/不跳过): $e")
            }
        })
    }

    /**
     * 获取指定播放器的片头跳过毫秒；未配置返回 0（表示不跳过）。
     * 匹配顺序：trim 后精确匹配 → 未命中试 "*" 兜底。
     */
    @JvmStatic
    fun getSkipMs(playerName: String): Long {
        val map = skipMap
        if (map.isEmpty()) return 0L
        val name = playerName.trim()
        val direct = map[name]
        if (direct != null) return direct // 命中（含显式配置 0=不跳过，此时不走兜底）
        return map[FALLBACK_KEY] ?: 0L // 未命中试 "*"，没有则不跳
    }

    private fun applyJson(json: String, from: String) {
        try {
            val root = JsonParser().parse(json)
            if (root == null || !root.isJsonObject) {
                Log.d(TAG, "[$from] 非法 JSON: $json")
                return
            }
            val map = HashMap<String, Long>()
            for ((key, value) in root.asJsonObject.entrySet()) {
                map[key.trim()] = parseTimeMs(value)
            }
            skipMap = map
            Log.d(TAG, "[$from] 生效配置: $map")
        } catch (e: Exception) {
            Log.d(TAG, "[$from] 解析失败: $e")
        }
    }

    /**
     * value 解析：数字=秒；"HH:MM:SS"=时间串；其余/非法=0；负数归 0。
     */
    private fun parseTimeMs(value: JsonElement): Long {
        return try {
            val ms = when {
                value.isJsonNull -> 0L
                value.isJsonPrimitive && value.asJsonPrimitive.isNumber -> value.asLong * 1000L
                value.isJsonPrimitive -> {
                    val s = value.asString.trim()
                    if (s.contains(":")) {
                        CastUtils.getIntTime(s) // HH:MM:SS → 毫秒
                    } else {
                        s.toLongOrNull()?.times(1000L) ?: 0L // 数字串按秒
                    }
                }
                else -> 0L
            }
            if (ms < 0) 0L else ms
        } catch (e: Exception) {
            0L
        }
    }
}
