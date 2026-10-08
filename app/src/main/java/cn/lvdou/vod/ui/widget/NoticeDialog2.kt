package cn.lvdou.vod.ui.widget

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import cn.lvdou.vod.R
import cn.lvdou.vod.utils.GlassBlur
import cn.lvdou.vod.utils.UrlUtils
import com.blankj.utilcode.util.ActivityUtils.startActivity


class NoticeDialog2(context: Context, val msg: String) : Dialog(context, R.style.DefaultDialogStyle) {

    private val tvNotice: TextView by lazy { findViewById(R.id.tvNotice) }
    private val tv_close: TextView by lazy { findViewById(R.id.tv_close) }

    init {
        setContentView(R.layout.dialog_notice_tip2)
        // 毛玻璃（liquid glass）：装配对话框内所有 BlurView
        GlassBlur.setupAll(findViewById<View>(android.R.id.content))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window!!.attributes?.apply {
            // 公告错位修复：根布局含 16dp 边距，内容固定 300dp，
            // 窗口若固定 300dp 会被压成 268dp 而内容仍按 300dp 排布 →
            // 玻璃面板与内容错位（标题/按钮偏右 16dp、右侧被裁）。
            // 改为随内容自适应（300 + 16*2 = 332dp），面板完整居中。
            width = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
        }

        setCanceledOnTouchOutside(false)
        setCancelable(false)

        if (msg.contains("$")) {

            var index = msg.indexOfFirst { it == '$' }
            var index2 = msg.indexOfLast { it == '$' }-1
            val newMsg: String = msg.replace("$", "")

            if (index < 0 || index2 < 0) {
                return
            }


            val span = SpannableString(newMsg)
            span.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(UrlUtils.converKeywordLoadOrSearch(newMsg.substring(index, index2))))
                    startActivity(browserIntent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isAntiAlias = true
                    ds.isUnderlineText = false
                }
            }, index, index2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

            tvNotice.movementMethod = LinkMovementMethod.getInstance()
            tvNotice.append(newMsg)

            tvNotice.text = span
        } else {
            tvNotice.text = msg
        }
//        tvNotice.setMovementMethod(LinkMovementMethod.getInstance())
        tv_close.setOnClickListener {
            dismiss()
        }
    }


}
