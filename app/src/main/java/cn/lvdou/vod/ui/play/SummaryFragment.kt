package cn.lvdou.vod.ui.play

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import cn.lvdou.vod.R
import cn.lvdou.vod.bean.VodBean
import cn.lvdou.vod.base.BaseFragment

class SummaryFragment : BaseFragment() {

    private val ivCloseIntro: ImageView by lazy { requireView().findViewById(R.id.ivCloseIntro) }
    private val tvActor: TextView by lazy { requireView().findViewById(R.id.tvActor) }
    private val tvPlayNumber: TextView by lazy { requireView().findViewById(R.id.tvPlayNumber) }
    private val tvScore: TextView by lazy { requireView().findViewById(R.id.tvScore) }
    private val tvStatus: TextView by lazy { requireView().findViewById(R.id.tvStatus) }
    private val tvSummary: TextView by lazy { requireView().findViewById(R.id.tvSummary) }
    private val tvTitle: TextView by lazy { requireView().findViewById(R.id.tvTitle) }
    private val tvType: TextView by lazy { requireView().findViewById(R.id.tvType) }
    private val tvYear: TextView by lazy { requireView().findViewById(R.id.tvYear) }

    override fun getLayoutId(): Int {
        return R.layout.fragment_summary
    }

    private lateinit var playActivity : NewPlayActivity

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()
        playActivity = mActivity as NewPlayActivity
        val vodBean = arguments?.getParcelable(VOD_BEAN) as? VodBean
        vodBean?.run {
            tvTitle.text = vodName
            tvYear.text = "年代：${vod_year}.${type.typeName}.${vod_area}"
            tvActor.text = "主演：$vod_actor"
            tvType.text = "类型：" + vod_class
            tvStatus.text = "状态：$vodRemarks"
            tvPlayNumber.text = "播放：" + vod_hits + "次"
            tvScore.text = "评分：$vod_score"
            tvSummary.text = "   "+vod_blurb
            ivCloseIntro.setOnClickListener {
                playActivity.hideSummary()
                playActivity.showVideoDetail()
            }
        }
    }

    companion object {
        const val VOD_BEAN = "vodBean"

        @JvmStatic
        fun newInstance(vodBean: VodBean): SummaryFragment = SummaryFragment().apply {
            arguments = Bundle().apply {
                putParcelable(VOD_BEAN, vodBean)
            }
        }
    }
}
