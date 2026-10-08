package cn.lvdou.vod.utils

import android.util.Log
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 智能 DNS（零服务器方案）：解除 DNS 污染类网络访问限制。
 *
 * 解析顺序：
 *  1) 公共 DoH —— 端点直接用 IP 访问（223.5.5.5 / 1.1.1.1），不依赖被污染的本地 DNS；
 *     命中后备用 IP 池（FALLBACK_IPS）优先于解析结果返回，OkHttp 会按序尝试；
 *  2) DoH 全部失败 → 30 秒冷却 → 回退系统 DNS（行为等同现状，最差不劣化）。
 *
 * 边界：仅覆盖 DNS 污染/劫持；深度 IP/SNI 封锁需中转出口（见计划方案 B）。
 */
object SmartDns : Dns {

    private const val TAG = "SmartDns"

    /** DoH 端点（IP 形式，规避对端点域名自身的解析依赖） */
    private val DOH_ENDPOINTS = listOf(
        "https://223.5.5.5/resolve", // 阿里 DoH（标准 JSON）
        "https://1.1.1.1/dns-query"  // Cloudflare DoH（Accept: application/dns-json）
    )

    /** 备用直连 IP 池：按序排在 DoH 结果之前返回（默认空，可按需内置备用接入点） */
    private val FALLBACK_IPS: List<String> = emptyList()

    private const val CACHE_TTL_MS = 60_000L      // 解析结果缓存
    private const val FAIL_COOLDOWN_MS = 30_000L  // DoH 全失败后的冷却，期间直接走系统 DNS

    private data class CacheEntry(val ips: List<InetAddress>, val at: Long)

    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val failedAt = ConcurrentHashMap<String, Long>()

    private val dohClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(3L, TimeUnit.SECONDS)
            .readTimeout(3L, TimeUnit.SECONDS)
            .build() // 端点为 IP 字面量，不经过 SmartDns，无循环风险
    }

    override fun lookup(hostname: String): List<InetAddress> {
        // IP 字面量（IPv4/IPv6）无需解析
        if (hostname.isNotEmpty() && hostname.none { it.isLetter() }) {
            return listOf(InetAddress.getByName(hostname))
        }

        // DoH 失败冷却期内：直接系统解析
        failedAt[hostname]?.let {
            if (System.currentTimeMillis() - it < FAIL_COOLDOWN_MS) {
                return systemLookup(hostname)
            }
        }
        // 解析结果缓存
        cache[hostname]?.let {
            if (System.currentTimeMillis() - it.at < CACHE_TTL_MS) {
                return it.ips
            }
        }

        val dohIps = dohLookup(hostname)
        if (dohIps.isNotEmpty()) {
            val extra = FALLBACK_IPS.mapNotNull { ip ->
                runCatching { InetAddress.getByName(ip) }.getOrNull()
            }
            val ips = (extra + dohIps).distinctBy { it.hostAddress }
            cache[hostname] = CacheEntry(ips, System.currentTimeMillis())
            Log.d(TAG, "$hostname -> [${ips.joinToString { it.hostAddress ?: it.hostName }}] (DoH)")
            return ips
        }

        // DoH 全失败：记录冷却并回退系统 DNS
        failedAt[hostname] = System.currentTimeMillis()
        Log.w(TAG, "$hostname DoH 不可用，回退系统 DNS")
        return systemLookup(hostname)
    }

    private fun systemLookup(hostname: String): List<InetAddress> {
        val ips: List<InetAddress> = Dns.SYSTEM.lookup(hostname)
        Log.d(TAG, "$hostname -> [${ips.joinToString { it.hostAddress ?: it.hostName }}] (system)")
        return ips
    }

    private fun dohLookup(hostname: String): List<InetAddress> {
        for (endpoint in DOH_ENDPOINTS) {
            val ips = dohOnce(endpoint, hostname)
            if (ips != null && ips.isNotEmpty()) {
                return ips
            }
        }
        return emptyList()
    }

    private fun dohOnce(endpoint: String, hostname: String): List<InetAddress>? {
        return try {
            val url = "$endpoint?name=$hostname&type=A"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/dns-json") // Cloudflare 必需；阿里忽略该头
                .build()
            dohClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val bodyStr = response.body?.string() ?: return null
                val json = JSONObject(bodyStr)
                if (json.optInt("Status", -1) != 0) return null
                val answer = json.optJSONArray("Answer") ?: return null
                val result = ArrayList<InetAddress>()
                for (i in 0 until answer.length()) {
                    val item = answer.optJSONObject(i) ?: continue
                    if (item.optInt("type", 0) != 1) continue // 只取 A 记录
                    val data = item.optString("data", "")
                    if (data.isEmpty()) continue
                    runCatching { result.add(InetAddress.getByName(data)) }
                }
                if (result.isEmpty()) null else result
            }
        } catch (t: Throwable) {
            Log.w(TAG, "DoH $endpoint 查询 $hostname 失败: $t")
            null
        }
    }
}
