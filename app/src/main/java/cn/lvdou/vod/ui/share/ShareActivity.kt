//ProgressDialog/insertImage 等结构性弃用：保留现有分享行为，不升级组件
@file:Suppress("DEPRECATION")

package cn.lvdou.vod.ui.share

import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.ui.login.LoginActivity
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import cn.lvdou.vod.utils.SimpleUtils
import cn.lvdou.vod.utils.UserUtils
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.BaseObserver
import cn.lvdou.vod.base.observer.LoadingObserver
import cn.lvdou.vod.bean.*
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.greenrobot.eventbus.EventBus
import java.io.File

class ShareActivity : BaseActivity() {

    private val VomName: TextView by lazy { findViewById(R.id.VomName) }
    private val ivCopyLink: ImageView by lazy { findViewById(R.id.ivCopyLink) }
    private val ivInviteFriend: LinearLayout by lazy { findViewById(R.id.ivInviteFriend) }
    private val ivQrcode: ImageView by lazy { findViewById(R.id.ivQrcode) }
    private val rlRoot: RelativeLayout by lazy { findViewById(R.id.rlRoot) }
    private val vodBlurd: TextView by lazy { findViewById(R.id.vodBlurd) }
    private val vodPicSlide: ImageView by lazy { findViewById(R.id.vodPicSlide) }
    private val vodTag: TextView by lazy { findViewById(R.id.vodTag) }

    private var shareInfo: ShareInfoBean? = null
    private var vom_name: String = ""
    private var vod_tag: String = ""
    private var vod_pic_slide: String = ""
    private var vod_blurd: String = ""


    override fun getLayoutResID(): Int {
        return R.layout.activity_share2
    }


    override fun initData() {
        super.initData()
        getShareUrl()

    }

    override fun initListener() {
        super.initListener()
//        ivBack.setOnClickListener {
//            finish()
//        }
        ivInviteFriend.setOnClickListener {
            PermissionUtils.permission(PermissionConstants.STORAGE)
                    .callback(object : PermissionUtils.SimpleCallback {
                        override fun onGranted() {
                            inviteFriend()
                        }

                        override fun onDenied() {
                            ToastUtils.showShort("需要开启读写权限后才能分享！")
                        }

                    })
                    .request()

        }
        ivCopyLink.setOnClickListener {
            //复制推广链接
            copyLink()
        }
        vom_name = intent?.getStringExtra("vom_name").toString()
        vod_tag = intent?.getStringExtra("vod_class").toString()
        vod_pic_slide = intent?.getStringExtra("vod_pic").toString()
        vod_blurd = intent?.getStringExtra("vod_blurd").toString()
        VomName.text = vom_name
        vodTag.text = vod_tag
        Log.d("TAGtext", "initListener: "+vod_pic_slide)
        //工具箱
        Glide.with(this).load(vod_pic_slide).placeholder(R.drawable.ic_extension_share_top).into(vodPicSlide)
        vodBlurd.text = vod_blurd
//        tvCopy.setOnClickListener {
//            copyShareCode()
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQOUEST_SHARE) {
            shareScore()
        }
        if (requestCode == -1) {
            if (data != null) {
                vom_name = data.getStringExtra("vom_name") ?: ""
                VomName.text = vom_name
            }


        }
    }

    private fun getShareUrl() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(
                this,
                vodService.getShareInfo(),
                object : LoadingObserver<ShareInfoBean>(mActivity) {
                    override fun onSuccess(data: ShareInfoBean) {
                        shareInfo = data
                        data.run {
                            val bitmap = BarcodeEncoder().encodeBitmap(share_url, BarcodeFormat.QR_CODE,
                                    ConvertUtils.dp2px(125f), ConvertUtils.dp2px(125f))
                            ivQrcode.setImageBitmap(bitmap)
                            if (!share_logo.isNullOrEmpty() && !mActivity.isFinishing) {
                                Glide.with(mActivity)
                                        .load(share_logo)
                                        .into(object : CustomTarget<Drawable>() {
                                            override fun onLoadCleared(placeholder: Drawable?) {
                                            }

                                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                                rlRoot.background = resource
                                            }

                                        })


                            }
                        }
                    }

                    override fun onError(e: ResponseException) {
                    }

                }
        )
    }

    private fun inviteFriend() { //截屏？
        val progressDialog = ProgressDialog.show(mActivity, "", StringUtils.getString(R.string.loading_msg))
        ThreadUtils.executeBySingle(object : ThreadUtils.Task<File>() {
            override fun doInBackground(): File {
                val bitmap = SimpleUtils.getCacheBitmapFromView(rlRoot)
                return SimpleUtils.saveBitmapToSdCard(mActivity, bitmap)

            }

            override fun onSuccess(file: File?) {
                progressDialog.dismiss()
                if (file == null) {
                    ToastUtils.showShort("分享失败，请重试")
                }
                file?.let {
                    shareSingleImage(it)
                }
            }

            override fun onFail(t: Throwable?) {
                progressDialog.dismiss()
                ToastUtils.showShort("分享失败，请重试")
            }

            override fun onCancel() {
                progressDialog.dismiss()
            }
        })
    }

    //分享单张图片
    fun shareSingleImage(file: File) {
        val imageUri = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, file.absolutePath, file.name, null))
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_STREAM, imageUri)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "分享到"), REQOUEST_SHARE);
    }

    private fun copyLink() {
        shareInfo?.run {
            val clipData = ClipData.newPlainText("", share_url)
            val clipbrardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipbrardManager.setPrimaryClip(clipData)
            ToastUtils.showShort("已经复制到剪切板")
        }

    }

    private fun copyShareCode() {
        val clipData = ClipData.newPlainText("", "tvSharecode.text.toString()")
        val clipbrardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipbrardManager.setPrimaryClip(clipData)
        ToastUtils.showShort("已经复制到剪切板")
    }

    private fun shareScore() {
        var vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(
                mActivity,
                vodService.shareScore(),
                object : BaseObserver<ShareBean>() {
                    override fun onSuccess(data: ShareBean) {
                        if (data.score != "0") {
                            ToastUtils.showShort("分享成功，获得${data.score}积分")
                        } else {
                            ToastUtils.showShort("分享成功")
                        }
                        EventBus.getDefault().post(LoginBean())
                    }

                    override fun onError(e: ResponseException) {
                    }
                }
        )
    }

    companion object {
        const val REQOUEST_SHARE = 1
        fun start(mVodBean: VodBean) {
            if (!UserUtils.isLogin()) {
                ActivityUtils.startActivity(LoginActivity::class.java)
            } else {
                var bynit = Bundle()
                bynit.putString("vom_name", mVodBean.vodName)
                ActivityUtils.startActivity(ShareActivity::class.java, bynit)
            }
        }
    }

}
