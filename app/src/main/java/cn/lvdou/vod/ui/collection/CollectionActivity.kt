package cn.lvdou.vod.ui.collection

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.lvdou.vod.App
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.bean.CollectionBean
import cn.lvdou.vod.bean.Page
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.ui.login.LoginActivity
import cn.lvdou.vod.ui.play.PlayActivity
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import cn.lvdou.vod.utils.UserUtils
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.BaseObserver
import cn.lvdou.vod.utils.MMkvUtils
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.youxiao.ssp.ad.bean.SSPAd
import com.youxiao.ssp.ad.core.AdClient
import com.youxiao.ssp.ad.listener.AdLoadAdapter
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import com.scwang.smartrefresh.layout.SmartRefreshLayout


class CollectionActivity : BaseActivity() {
    private val rlBack: RelativeLayout by lazy { findViewById(R.id.rlBack) }
    private val tvEdit: TextView by lazy { findViewById(R.id.tvEdit) }
    private val linear_Banner: FrameLayout by lazy { findViewById(R.id.linear_Banner) }
    private val refreshLayout: SmartRefreshLayout by lazy { findViewById(R.id.refreshLayout) }
    private val rvCollection: RecyclerView by lazy { findViewById(R.id.rvCollection) }
    private val breakLine: View by lazy { findViewById(R.id.breakLine) }
    private val rlEdit: LinearLayout by lazy { findViewById(R.id.rlEdit) }
    private val tvSelect: TextView by lazy { findViewById(R.id.tvSelect) }
    private val tvSelectCount: TextView by lazy { findViewById(R.id.tvSelectCount) }

    private var curCollectionPage = 1
    private var isEditMode = false
    private var isAllSelect : Boolean = false

    private val collectionAdapter: CollectionAdapter by lazy {
        CollectionAdapter().apply {
            setOnItemClickListener { adapter, _, position ->
                val item = adapter.getItem(position) as CollectionBean
                if (isEditMode) {
                    item.isSelect = !item.isSelect
                    if(isAllSelect && !item.isSelect){
                        isAllSelect = false
                        tvSelect.text =  "全选"
                    }
                    adapter.data[position] = item
                    notifyItemChanged(position)
                    changeDeleteNum()
                }else{
                    PlayActivity.startByCollection(item.data.id)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getxtwAd()
    }

    /**
     * 信天翁
     * */

    private fun getxtwAd() {
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
    private var adClient: AdClient? = AdClient(this@CollectionActivity)
    override fun onDestroy() {
        super.onDestroy()
        if (App.SdkSwitch){
            adClient?.release()
        }
    }
    override fun getLayoutResID(): Int {
        return R.layout.activity_collection
    }

    @Suppress("DEPRECATION") //systemUiVisibility/statusBarColor 已弃用（API35），保留现有状态栏行为
    override fun initView() {
        super.initView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = getColor(R.color.transparent)
        }
        refreshLayout.setEnableRefresh(false)
        refreshLayout.setRefreshFooter(ClassicsFooter(mActivity))
        rvCollection.layoutManager = LinearLayoutManager(mActivity)
        rvCollection.adapter = collectionAdapter
    }

    override fun initListener() {
        super.initListener()
        tvSelect.setOnClickListener {
            if(isAllSelect){
                collectionAdapter.data.map {
                    it.isSelect = false
                }
                isAllSelect = false
                tvSelect.text =  "全选"
            }else{
                collectionAdapter.data.map {
                    it.isSelect = true
                }
                isAllSelect = true
                tvSelect.text =  "取消全选"
            }
            collectionAdapter.notifyDataSetChanged()
            changeDeleteNum()

        }
        tvSelectCount.setOnClickListener {
            val ids = getSelectCollection()
            if (ids.isNullOrEmpty()) {
                ToastUtils.showShort("未选择任何数据")
            } else {
                deleteCollection(ids)
            }
        }
        tvEdit.setOnClickListener {
            isEditMode = !isEditMode
            changeEditMode()
        }

        rlBack.setOnClickListener {
            finish()
        }

        refreshLayout.setOnLoadMoreListener {
            curCollectionPage++
            getCollectionList()
        }
    }

    override fun initData() {
        super.initData()
        getCollectionList()
    }

    private fun getCollectionList(isFresh: Boolean = false) {
        var vodService= Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this,
                vodService.getCollectList(curCollectionPage.toString(), 10.toString(), 2.toString()),
                object : BaseObserver<Page<CollectionBean>>() {
                    override fun onSuccess(data: Page<CollectionBean>) {
                        if (curCollectionPage == 1) {
                            if (isFresh)
                                collectionAdapter.setNewData(data.list)
                            else
                                collectionAdapter.addData(data.list)
                        }

                        if (curCollectionPage > 1) {
                            collectionAdapter.addData(data.list)
                            if (data.list.isEmpty()) {
                                refreshLayout.finishLoadMoreWithNoMoreData()
                            } else {
                                refreshLayout.finishLoadMore(true)
                            }
                        }
                    }

                    override fun onError(e: ResponseException) {
                        if (curCollectionPage > 1) {
                            refreshLayout.finishLoadMore(false)
                        }
                    }

                })
    }

    private fun deleteCollection(ids: String) {
        var vodService=Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this,
                vodService.deleteCollect(ids, 2.toString()),
                object : BaseObserver<String>() {
                    override fun onSuccess(data: String) {
                        ToastUtils.showShort("取消成功")
                        isEditMode = false
                        changeEditMode()
                        curCollectionPage = 1
                        getCollectionList(true)
                    }

                    override fun onError(e: ResponseException) {
                    }

                }
        )
    }

    private fun changeEditMode() {
        if (isEditMode) {
            tvEdit.text = "取消"
            breakLine.visibility = View.VISIBLE
            rlEdit.visibility = View.VISIBLE
            collectionAdapter.data.map {
                it.isSelect = false
            }
            isAllSelect = false
            tvSelect.text =  "全选"
            collectionAdapter.notifyDataSetChanged()
        } else {
            tvEdit.text = "管理"
            breakLine.visibility = View.GONE
            rlEdit.visibility = View.GONE
        }
        collectionAdapter.changeEditMode(isEditMode)
    }

    private fun getSelectCount(): Int {
        var count = 0
        collectionAdapter.data.map {
            if (it.isSelect) {
                count++
            }
        }
        return count
    }


    private fun changeDeleteNum(){
        tvSelectCount.text = "删除(${getSelectCount()})"
    }

    private fun getSelectCollection(): String {
        var ids = ""
        collectionAdapter.data.map {
            if (it.isSelect) {
                ids += "${it.data.id},"
            }
        }
        if (ids.isNotEmpty()) {
            return ids.substring(0, ids.length - 1)
        }
        return ids
    }

    class CollectionAdapter(var isEditMode: Boolean = false) : BaseQuickAdapter<CollectionBean, BaseViewHolder>(R.layout.item_collection) {

        override fun convert(helper: BaseViewHolder, item: CollectionBean?) {
            item?.let {
                helper.setText(R.id.tvName, it.data.name)
                helper.setText(R.id.tv_desc, it.data.vod_content)
                helper.setText(R.id.tvType, it.data.type.type_name)
                val ivAvator = helper.getView<ImageView>(R.id.ivImg)
                if (isEditMode) {
                    helper.setChecked(R.id.cb, item.isSelect)
                }

                val mation: MultiTransformation<Bitmap> = MultiTransformation(CenterCrop(), RoundedCornersTransformation(20, 0, RoundedCornersTransformation.CornerType.ALL))
                Glide.with(helper.itemView.context)
                        .load(it.data.pic)
                        .error(R.drawable.error)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .apply(RequestOptions.bitmapTransform(mation))
                        .into(ivAvator)
            }

            if (isEditMode) {
                helper.setGone(R.id.cb, true)
            } else {
                helper.setGone(R.id.cb, false)
            }
        }

        fun changeEditMode(isEditMode: Boolean) {
            this.isEditMode = isEditMode
            notifyDataSetChanged()
        }
    }

    companion object {
        fun start() {
            if (UserUtils.isLogin()) {
                ActivityUtils.startActivity(CollectionActivity::class.java)
            } else {
                ActivityUtils.startActivity(LoginActivity::class.java)
            }
        }
    }
}
