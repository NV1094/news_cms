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
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import cn.lvdou.vod.R
import cn.lvdou.vod.bean.LoginBean
import cn.lvdou.vod.bean.ShareBean
import cn.lvdou.vod.bean.ShareInfoBean
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import cn.lvdou.vod.utils.SimpleUtils
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.BaseFragment
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.BaseObserver
import cn.lvdou.vod.base.observer.LoadingObserver
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.greenrobot.eventbus.EventBus
import java.io.File

class ShareFragment : BaseFragment(){

    private val ivBack: ImageView by lazy { requireView().findViewById(R.id.ivBack) }
    private val ivCopyLink: ImageView by lazy { requireView().findViewById(R.id.ivCopyLink) }
    private val ivInviteFriend: ImageView by lazy { requireView().findViewById(R.id.ivInviteFriend) }
    private val ivQrcode: ImageView by lazy { requireView().findViewById(R.id.ivQrcode) }
    private val rlRoot: RelativeLayout by lazy { requireView().findViewById(R.id.rlRoot) }
    private val tvCopy: TextView by lazy { requireView().findViewById(R.id.tvCopy) }
    private val tvSharecode: TextView by lazy { requireView().findViewById(R.id.tvSharecode) }

    private var shareInfo: ShareInfoBean? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_share
    }

    override fun initView() {
        super.initView()
        ivBack.visibility = View.GONE
    }

    override fun initLoad() {
        super.initLoad()
        getShareUrl()
    }

    override fun initListener() {
        super.initListener()
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
            copyLink()
        }
        tvCopy.setOnClickListener {
            copyShareCode()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ShareActivity.REQOUEST_SHARE) {
            shareScore()
        }
    }

    private fun getShareUrl() {
        val vodService= Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

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
                            if (share_url.contains("=")) {
                                val shareCode = share_url.split("=")[1]
                                if (shareCode.isNotEmpty())
                                    tvSharecode.text = shareCode
                            }
                            if (!share_logo.isNullOrEmpty() && mActivity != null && !mActivity.isFinishing) {
                                Glide.with(mActivity)
                                        .load(share_logo)
                                        .into(object : CustomTarget<Drawable>() {
                                            override fun onLoadCleared(placeholder: Drawable?) {
                                            }

                                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                                rlRoot.setBackgroundDrawable(resource)
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

    private fun inviteFriend() {
        val progressDialog = ProgressDialog.show(mActivity, "", StringUtils.getString(R.string.loading_msg))
        ThreadUtils.executeBySingle(object : ThreadUtils.Task<File>() {
            override fun doInBackground(): File {
                val bitmap = SimpleUtils.getCacheBitmapFromView(rlRoot)
                return SimpleUtils.saveBitmapToSdCard(mActivity, bitmap)
            }

            override fun onSuccess(file: File?) {
                progressDialog.dismiss()
                file?.let {
                    shareSingleImage(it.absolutePath)
//                    Share2.Builder(mActivity)
//                            .setContentType(ShareContentType.IMAGE)
//                            .setShareFileUri(FileUtil.getFileUri(mActivity, null, file))
//                            .setTitle("Share Image")
//                            .setOnActivityResult(ShareActivity.REQOUEST_SHARE)
//                            .build()
//                            .shareBySystem()
                } ?: ToastUtils.showShort("分享失败，请重试")
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
    public fun shareSingleImage(file: String) {
        val imageUri = Uri.parse(MediaStore.Images.Media.insertImage(activity?.contentResolver, file, null, null))
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_STREAM, imageUri)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "分享到"), ShareActivity.REQOUEST_SHARE);
    }

    private fun copyLink() {
        shareInfo?.run {
            val clipData = ClipData.newPlainText("", share_url)
            val clipbrardManager = mActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipbrardManager.setPrimaryClip(clipData)
            ToastUtils.showShort("已经复制到剪切板")
        }

    }

    private fun copyShareCode() {
        val clipData = ClipData.newPlainText("", tvSharecode.text.toString())
        val clipbrardManager = mActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipbrardManager.setPrimaryClip (clipData)
        ToastUtils.showShort("已经复制到剪切板")
    }

    private fun shareScore() {
        val vodService= Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(
                this,
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

    companion object{
        @JvmStatic
        fun newInstance(): ShareFragment {
            val args = Bundle()
            val fragment = ShareFragment()
            fragment.arguments = args
            return fragment
        }
    }

}
