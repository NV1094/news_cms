package cn.lvdou.vod.ui.play

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import cn.lvdou.vod.R
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ToastUtils

class CommentDialog(val mContext: Context, s: String) : Dialog(mContext,R.style.DefaultDialogStyle) {

    private val etComment: EditText by lazy { findViewById(R.id.etComment) }
    private val tvCancel: TextView by lazy { findViewById(R.id.tvCancel) }
    private val tvCount: TextView by lazy { findViewById(R.id.tvCount) }
    private val tvOk: TextView by lazy { findViewById(R.id.tvOk) }

    private var onCommentSubmitClickListener: OnCommentSubmitClickListener? = null

    init {
        setContentView(R.layout.layout_comment)
    }
    private var wqddgs:String =s
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setCanceledOnTouchOutside(false)
        window!!.attributes = window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.BOTTOM
        }

        tvCancel.setOnClickListener {
            closeSoftInput()
            dismiss()
        }
        etComment.hint="请输入"+wqddgs+"内容"
        etComment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCount.text = "${s?.length ?: 0}/300"
            }
        })

        tvOk.setOnClickListener {
            val comment = etComment.text.trim().toString()
            if (comment.isEmpty()) {
                ToastUtils.showShort(wqddgs+"内容不能为空")
            } else {
                dismiss()
                closeSoftInput()
                onCommentSubmitClickListener?.onCommentSubmit(comment)
            }
        }
        KeyboardUtils.showSoftInput(etComment)
    }

    fun closeSoftInput(){
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(
                InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    fun setOnCommentSubmitClickListener(onCommentSubmitClickListener: OnCommentSubmitClickListener) : CommentDialog{
        this.onCommentSubmitClickListener = onCommentSubmitClickListener
        return this
    }


    interface OnCommentSubmitClickListener {
        fun onCommentSubmit(comment: String)
    }
}
