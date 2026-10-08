package cn.lvdou.vod.ui.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import cn.lvdou.vod.R
import cn.lvdou.vod.utils.GlassBlur
import cn.lvdou.vod.utils.MMkvUtils

class HitDialog : Dialog {

    private val editSec: EditText by lazy { findViewById(R.id.editSec) }
    private val editSec2: EditText by lazy { findViewById(R.id.editSec2) }
    private val setedit: LinearLayout by lazy { findViewById(R.id.setedit) }
    private val tvCancel: TextView by lazy { findViewById(R.id.tvCancel) }
    private val tvMessage: TextView by lazy { findViewById(R.id.tvMessage) }
    private val tvOk: TextView by lazy { findViewById(R.id.tvOk) }
    private val tvTitle: TextView by lazy { findViewById(R.id.tvTitle) }

    private var onHitDialogClickListener: OnHitDialogClickListener? = null

    constructor(context: Context) : super(context) {
        setContentView(R.layout.dialog_hit)
        // 毛玻璃（liquid glass）：装配对话框内所有 BlurView
        GlassBlur.setupAll(findViewById<View>(android.R.id.content))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setCanceledOnTouchOutside(false)
        setCancelable(false)

        tvOk.setOnClickListener {
            if (tvMessage.visibility == View.VISIBLE) {
                onHitDialogClickListener?.onOkClick(this@HitDialog)
            } else {
                val tou : String = editSec.text.toString()
                val wei : String= editSec2.text.toString()
                onHitDialogClickListener?.onOkClick(this@HitDialog)
//                val time = MMkvUtils.Builds().loadPlayTiaoZhuan()
                 onHitDialogClickListener?.onTz(this@HitDialog , tou, wei)
            }
        }
        tvCancel.setOnClickListener {
            onHitDialogClickListener?.onCancelClick(this@HitDialog)
        }

    }

    fun setTitle(title: String): HitDialog {
        tvTitle.text = title
        return this
    }


    fun setMessage(message: String): HitDialog {
        tvMessage.visibility = View.VISIBLE
//        editSec.visibility = View.GONE
//        editSec2.visibility = View.GONE
        setedit.visibility= View.GONE
        tvMessage.text = message
        return this
    }

    fun setInputView(vodid: Int): HitDialog {
        tvMessage.visibility = View.GONE
//        editSec.visibility = View.VISIBLE
//        editSec2.visibility = View.VISIBLE
        setedit.visibility= View.VISIBLE

        val tou = MMkvUtils.Builds().loadPlayTiaoZhuan(vodid)
            editSec.setText(tou)

        val wei = MMkvUtils.Builds().loadPlayTiaoZhuan2(vodid )
        editSec2.setText(wei)


        return this
    }

    fun setOnHitDialogClickListener(onHitDialogClickListener: OnHitDialogClickListener): HitDialog {
        this.onHitDialogClickListener = onHitDialogClickListener
        return this
    }

    abstract class OnHitDialogClickListener {
        open fun onCancelClick(dialog: HitDialog) {
            dialog.dismiss()
        }

        open fun onOkClick(dialog: HitDialog) {
            if (dialog.setedit.visibility == View.GONE ){
                 dialog.dismiss()
            }

        }

        open fun onTz(dialog: HitDialog,touSpeed:String,weiSpeed:String) {

        }
    }
}
