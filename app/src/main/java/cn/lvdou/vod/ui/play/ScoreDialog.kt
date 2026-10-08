package cn.lvdou.vod.ui.play

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import cn.lvdou.vod.R
import cn.lvdou.vod.utils.GlassBlur
import com.hedgehog.ratingbar.RatingBar

class ScoreDialog(context: Context) : Dialog(context,R.style.DefaultDialogStyle) {

    private val ratingbar: RatingBar by lazy { findViewById(R.id.ratingbar) }
    private val tvSubmit: TextView by lazy { findViewById(R.id.tvSubmit) }

    private var onScoreSubmitClickListener: OnScoreSubmitClickListener? = null
    private var score : Float  = 0f

    init {
        setContentView(R.layout.layout_score)
        // 毛玻璃（liquid glass）：装配对话框内所有 BlurView
        GlassBlur.setupAll(findViewById<View>(android.R.id.content))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window!!.attributes = window!!.attributes?.apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        ratingbar.setOnRatingChangeListener { ratingCount ->
            score = ratingCount
        }

        tvSubmit.setOnClickListener {
            onScoreSubmitClickListener?.run {
                onScoreSubmit(this@ScoreDialog,score)
            }
        }
    }

    fun setOnScoreSubmitClickListener(onScoreSubmitClickListener: OnScoreSubmitClickListener) : ScoreDialog{
        this.onScoreSubmitClickListener = onScoreSubmitClickListener
        return this
    }


    interface OnScoreSubmitClickListener {
        fun onScoreSubmit(scoreDialog : ScoreDialog,score: Float)
    }
}
