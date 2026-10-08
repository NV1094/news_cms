package cn.lvdou.vod.ui.widget

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.FileProvider
import cn.lvdou.vod.R
import cn.lvdou.vod.bean.AppUpdateBean
import cn.lvdou.vod.utils.AppUpdateUtils
import cn.lvdou.vod.utils.GlassBlur
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ScreenUtils
import java.io.File


class AppUpdateDialog(context: Context, val data: AppUpdateBean) : Dialog(context, R.style.DefaultDialogStyle) {

    private val download_pb: ProgressBar by lazy { findViewById(R.id.download_pb) }
    private val update_ui_ProgressBar: TextView by lazy { findViewById(R.id.update_ui_ProgressBar) }
    private val update_ui_btn_cancel: TextView by lazy { findViewById(R.id.update_ui_btn_cancel) }
    private val update_ui_btn_close: TextView by lazy { findViewById(R.id.update_ui_btn_close) }
    private val update_ui_btn_confirm: Button by lazy { findViewById(R.id.update_ui_btn_confirm) }
    private val update_ui_text_content: TextView by lazy { findViewById(R.id.update_ui_text_content) }
    private val update_ui_text_title: TextView by lazy { findViewById(R.id.update_ui_text_title) }

    init {
        setContentView(R.layout.dialog_app_update_tip)
        // 毛玻璃（liquid glass）：装配对话框内所有 BlurView
        GlassBlur.setupAll(findViewById<View>(android.R.id.content))
    }

    private val mHandler = Handler()
    private var isDownLoading = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        window!!.attributes?.apply {
            gravity = Gravity.CENTER
            // width = (ScreenUtils.getScreenWidth() * 0.8).toInt()
            width = (ScreenUtils.getScreenWidth() * 0.98).toInt()
        }

        update_ui_text_content.text = data.summary
        update_ui_text_title.text = data.version + " 版本更新"

        Log.d("更新方式", "onCreate: "+data.type)
        update_ui_btn_close.setOnClickListener {
            dismiss()
        }
        if (data.type == 2) {
            update_ui_btn_cancel.text = "暂不升级"
            update_ui_btn_cancel.setOnClickListener {
                dismiss()
            }
        }else{
            update_ui_btn_cancel.text = "外部更新"
            println("data.url" + data.url)
            update_ui_btn_cancel.setOnClickListener {
                Intent(Intent.ACTION_VIEW, Uri.parse(data.url)).run {
                    ActivityUtils.startActivity(this)
                }
//            dismiss();
            }
        }

        update_ui_btn_confirm.setOnClickListener {
            download_pb.visibility = View.VISIBLE
            update_ui_ProgressBar.visibility = View.VISIBLE
            if (isDownLoading && update_ui_ProgressBar.text.toString() != "下载失败") {
                return@setOnClickListener
            } else if (!isDownLoading || update_ui_ProgressBar.text.toString() == "下载失败") {
                AppUpdateUtils.startDownload2(context, data.url2)
                AppUpdateUtils.setOnDownloadingLisenter(object : AppUpdateUtils.DowloadingLisenter {
                    @SuppressLint("SetTextI18n")
                    override fun downloading(progress: String) {
                        Log.e("APK下载进度", progress)

                        isDownLoading = true
                        mHandler.post {
                            val num: String = progress //后台返回数据 即报错0.001
                            val numToDouble = num.toDouble() // 先转换成double类型
                            download_pb.progress = numToDouble.toInt()
                            update_ui_ProgressBar.text = "$progress%"
                        }
                    }

                    override fun downloadSuc(path: File) {
                        Log.e("APK下载结果", "成功path=${path}")
                        val intent = Intent()
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.action = Intent.ACTION_VIEW
                        //判读版本是否在7.0以上
                        if (Build.VERSION.SDK_INT >= 24) {
                            //provider authorities
                            val apkUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", path)
                            //Granting Temporary Permissions to a URI
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                        } else {
                            intent.setDataAndType(Uri.fromFile(path), "application/vnd.android.package-archive")
                        }
                        context.startActivity(intent)
                    }

                    override fun downloadFail() {
                        isDownLoading = false
                        Log.e("APK下载结果", "失败")
                        update_ui_ProgressBar.text = "下载失败"
                    }

                })
            }
        }
    }
}
