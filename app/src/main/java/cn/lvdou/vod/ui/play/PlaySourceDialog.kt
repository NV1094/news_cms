package cn.lvdou.vod.ui.play

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.lvdou.vod.R
import cn.lvdou.vod.bean.PlayFromBean
import com.blankj.utilcode.util.ColorUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.lang.ref.WeakReference
class PlaySourceDialog(context: Context, var playSourceIndex: Int, val playList: List<PlayFromBean>) : Dialog(context,R.style.PlayListDialogStyle) {

    private val rvSelectWorks: RecyclerView by lazy { findViewById(R.id.rvSelectWorks) }

    private var playActivity: WeakReference<NewPlayActivity> = WeakReference(context as NewPlayActivity)
    private var spanCount = 4

    private val selectionAdapter: SelectionAdapter by lazy {
        SelectionAdapter(playList).apply {
            setOnItemClickListener { adapter, view, position ->
                val playFromBean = adapter.getItem(position) as PlayFromBean
                if (playSourceIndex != position) {
                    playSourceIndex = position
                    playActivity.get()?.run {
                        changePlaySource(playFromBean,position)
                    }
                    notifyDataSetChanged()
                    dismiss()
                }
            }
        }
    }

    init {
        setContentView(R.layout.dialog_play_list)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window!!.attributes = window!!.attributes.apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
        }

        rvSelectWorks.layoutManager = LinearLayoutManager(context)
        rvSelectWorks.adapter = selectionAdapter

    }


    inner class SelectionAdapter(data: List<PlayFromBean>) : BaseQuickAdapter<PlayFromBean, BaseViewHolder>(R.layout.item_dialog_play_source, data) {
        override fun convert(helper: BaseViewHolder, item: PlayFromBean) {
            val position = helper.layoutPosition
            if (position == playSourceIndex) {
                helper.setTextColor(R.id.tv, ColorUtils.getColor(R.color.userTopBg))
            } else {
                helper.setTextColor(R.id.tv, ColorUtils.getColor(R.color.white))
            }
            helper.setText(R.id.tv, item.player_info.show)
        }
    }
}
