package cn.lvdou.vod.ui.play


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.lvdou.vod.R
import cn.lvdou.vod.ad.AdWebView
import cn.lvdou.vod.bean.UrlBean
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.SnackbarUtils.dismiss
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import cn.lvdou.vod.base.BaseFragment
import cn.lvdou.vod.bean.VodBean
import cn.lvdou.vod.utils.MMkvUtils
import com.blankj.utilcode.util.CacheDiskStaticUtils.getParcelable

class PlayListFragment : BaseFragment() {

    private val awvPlayerDown: AdWebView by lazy { requireView().findViewById(R.id.awvPlayerDown) }
    private val ivClose: ImageView by lazy { requireView().findViewById(R.id.ivClose) }
    private val rvSelectWorks: RecyclerView by lazy { requireView().findViewById(R.id.rvSelectWorks) }
    private val tvLastest2: TextView by lazy { requireView().findViewById(R.id.tvLastest2) }

    private var spanCount = 2
    private var urlIndex = 0
    private lateinit var playActivity: NewPlayActivity

    private val selectionAdapter: SelectionAdapter by lazy {
        SelectionAdapter().apply {
            setOnItemClickListener { adapter, view, position ->
                if (urlIndex != position) {
                    urlIndex = position
                    playActivity.changeSelection(position,false)
                    notifyDataSetChanged()
                    dismiss()
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_play_list
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()
        playActivity = mActivity as NewPlayActivity

        arguments?.run {
            spanCount = getInt("SPAN_COUNT")
        }

        rvSelectWorks.layoutManager = GridLayoutManager(mActivity, spanCount, RecyclerView.VERTICAL, false).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return spanCount
                }
            }
        }
        rvSelectWorks.adapter = selectionAdapter
        val ad = MMkvUtils.Companion.Builds().loadStartBean("")?.ads?.player_down
        if (ad == null || ad.status == 0 || ad.description.isNullOrEmpty()) {
            awvPlayerDown.visibility = View.GONE
        } else {
            awvPlayerDown.visibility = View.VISIBLE
            awvPlayerDown.loadHtmlBody(ad.description)
        }
        val s = VideoDetailFragment.mVodBeans
        tvLastest2.text = s
    }

    override fun initListener() {
        super.initListener()
        ivClose.setOnClickListener {
            playActivity.hidePlayList()
            playActivity.showVideoDetail()
        }
    }

    fun showPlayList(playList : List<UrlBean>,urlIndex : Int){
        this.urlIndex = urlIndex
        selectionAdapter.setNewData(playList)
    }


    companion object {
        @JvmStatic
        fun newInstance(spanCount: Int): PlayListFragment = PlayListFragment().apply {
            arguments = Bundle().apply {
                putInt("SPAN_COUNT", spanCount)
            }
        }
    }

    inner class SelectionAdapter : BaseQuickAdapter<UrlBean, BaseViewHolder>(R.layout.item_play_list) {

        override fun convert(helper: BaseViewHolder, item: UrlBean) {
            if(spanCount == 2){
                helper.itemView.layoutParams =  helper.itemView.layoutParams.apply {
                    width = ConvertUtils.dp2px(130f)
                    height = ConvertUtils.dp2px(50f)
                }
            }else{
                helper.itemView.layoutParams =  helper.itemView.layoutParams.apply {
                    width = ConvertUtils.dp2px(50f)
                    height = ConvertUtils.dp2px(50f)
                }
            }
            val position = helper.layoutPosition
            if(position == urlIndex){
                helper.setTextColor(R.id.tv, ColorUtils.getColor(R.color.userTopBg))
            }else{
                helper.setTextColor(R.id.tv, ColorUtils.getColor(R.color.gray_999))
            }
            var name = item.name.replace("第","").replace("集","")
            helper.setText(R.id.tv, name)
        }
    }
}
