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
import android.widget.ImageView
import android.widget.TextView
import cn.lvdou.vod.R
import cn.lvdou.vod.utils.GlassBlur
import cn.lvdou.vod.utils.UrlUtils
import com.blankj.utilcode.util.ActivityUtils.startActivity


class NoticeDialog(context: Context, val msg: String) : Dialog(context, R.style.DefaultDialogStyle) {

    private val ivClose: ImageView by lazy { findViewById(R.id.ivClose) }
    private val tvNotice: TextView by lazy { findViewById(R.id.tvNotice) }

    init {
        setContentView(R.layout.dialog_notice_tip)
        // 毛玻璃（liquid glass）：装配对话框内所有 BlurView
        GlassBlur.setupAll(findViewById<View>(android.R.id.content))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window!!.attributes?.apply {
            // 同公告弹窗（NoticeDialog2）的错位修复：根布局 320dp + 16dp 边距 = 352dp，
            // 窗口固定 300dp 会让内容溢出被裁切、关闭按钮偏移。改为随内容自适应。
            width = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
        }

        setCanceledOnTouchOutside(false)
        setCancelable(false)


        val index = msg.indexOf("www")
        val index2 = msg.indexOf("com") + 3

        val span = SpannableString(msg)
        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(UrlUtils.converKeywordLoadOrSearch(msg.substring(index, index2))))
                startActivity(browserIntent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isAntiAlias = true
                ds.isUnderlineText = false
            }
        }, index, index2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        tvNotice.setMovementMethod(LinkMovementMethod.getInstance())
        tvNotice.append(msg)

        tvNotice.text = span
//        tvNotice.setMovementMethod(LinkMovementMethod.getInstance())
        ivClose.setOnClickListener {
            dismiss()
        }
    }


}
