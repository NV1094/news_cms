package cn.lvdou.vod.ui.notice

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.bean.MessageDetail
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.LoadingObserver

class MessageDetailActivity : BaseActivity(), View.OnClickListener {

    private val rlBack: RelativeLayout by lazy { findViewById(R.id.rlBack) }
    private val tv_desc: TextView by lazy { findViewById(R.id.tv_desc) }
    private val tv_time: TextView by lazy { findViewById(R.id.tv_time) }
    private val tv_title: TextView by lazy { findViewById(R.id.tv_title) }

    override fun getLayoutResID(): Int {
        return R.layout.activity_message_detail
    }

    companion object {
        var mId: String=""

        @JvmStatic
        fun start(activity: Activity, id: String) {
            mId = id
            val intent = Intent(activity, MessageDetailActivity::class.java)
            activity.startActivity(intent)
        }

    }


    override fun initView() {
        super.initView()
        rlBack.setOnClickListener(this)

    }

    override fun initData() {
        super.initData()
        getMsgDetail()
    }

    private fun getMsgDetail() {
        var vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(mActivity, vodService.getMsgDetail(mId),
                object : LoadingObserver<MessageDetail>(mActivity) {
                    override fun onSuccess(data: MessageDetail) {
                        tv_title.text = data.title
                        tv_time.text = data.create_date
                        tv_desc.text = data.content
                    }

                    override fun onError(e: ResponseException) {
                    }

                })
    }


    override fun onClick(v: View?) {
        when (v) {
            rlBack -> {
                finish()
            }
        }
    }
}
