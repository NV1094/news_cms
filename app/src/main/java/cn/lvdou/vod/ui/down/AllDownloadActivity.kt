package cn.lvdou.vod.ui.down

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import cn.lvdou.vod.App
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.utils.MMkvUtils
import com.youxiao.ssp.ad.bean.SSPAd
import com.youxiao.ssp.ad.core.AdClient
import com.youxiao.ssp.ad.listener.AdLoadAdapter

import jaygoo.library.m3u8downloader.M3U8Library
import jaygoo.library.m3u8downloader.view.DownloadItemList
import jaygoo.library.m3u8downloader.view.DownloadingItemList
import jaygoo.library.m3u8downloader.view.adapter.DownloadCenterPagerAdpter

import java.util.*

class AllDownloadActivity : BaseActivity() {
    private val tab_vp: ViewPager by lazy { findViewById(R.id.tab_vp) }
    private val tl_down: TabLayout by lazy { findViewById(R.id.tl_down) }
    private val backups: RelativeLayout by lazy { findViewById(R.id.backups) }
    private val linear_Banner: FrameLayout by lazy { findViewById(R.id.linear_Banner) }

    private var downloadingItemList: DownloadingItemList? = null
    private var downloadItemList: DownloadItemList? = null


    override fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = getColor(R.color.transparent)
        }
        val fragments = ArrayList<Fragment>()
        downloadingItemList = DownloadingItemList()
        downloadItemList = DownloadItemList()
        fragments.add(downloadingItemList!!)
        fragments.add(downloadItemList!!)
        val adpter = DownloadCenterPagerAdpter(supportFragmentManager, fragments, this)
        tab_vp.adapter = adpter
        tl_down.setupWithViewPager(tab_vp)
        val intentFilter = IntentFilter(M3U8Library.EVENT_REFRESH)
        registerReceiver(receiver, intentFilter)
        backups.setOnClickListener { finish() }
    }

    override fun initData() {
    }

    private var adClient: AdClient? = AdClient(this@AllDownloadActivity)
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        if (App.SdkSwitch){
            adClient?.release()
        }
    }

    override fun getLayoutResID(): Int {
        return R.layout.activity_all_download
    }

    override fun onResume() {
        super.onResume()
        getXTWAd()
    }

    /**
     * 信天翁
     * */

    private fun getXTWAd() {
//        val adClient = AdClient(mActivity)
        val banner = MMkvUtils.Builds().loadStartBean("")?.ads?.sdk_banner
        if (App.SdkSwitch && banner != null && banner.description != null && banner.status == 1){
            adClient?.requestBannerAd(linear_Banner, banner.description, object : AdLoadAdapter() {
                override fun onAdLoad(ad: SSPAd) {
                    super.onAdLoad(ad)
                    linear_Banner.visibility = View.VISIBLE
                }

                override fun onError(p0: Int, p1: String?) {
                    linear_Banner.visibility = View.GONE
                }
            })
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AllDownloadActivity::class.java)
            context.startActivity(intent)
        }
    }

    var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == M3U8Library.EVENT_REFRESH) {
                if (downloadingItemList != null) {
                    downloadingItemList!!.refreshList()
                }
                if (downloadItemList != null) {
                    downloadItemList!!.refreshList()
                }
            }
        }
    }
}