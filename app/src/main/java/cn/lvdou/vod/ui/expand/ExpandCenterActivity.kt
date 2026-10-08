package cn.lvdou.vod.ui.expand

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import cn.lvdou.vod.ApiConfig
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.bean.ExpandCenter
import cn.lvdou.vod.bean.UserInfoBean
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.ui.share.ShareActivity
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.BaseObserver
import cn.lvdou.vod.base.observer.LoadingObserver
import cn.lvdou.vod.utils.MMkvUtils


class ExpandCenterActivity : BaseActivity(), View.OnClickListener {
    private val rlBack: RelativeLayout by lazy { findViewById(R.id.rlBack) }
    private val tv_my_expand: TextView by lazy { findViewById(R.id.tv_my_expand) }
    private val rl_share: RelativeLayout by lazy { findViewById(R.id.rl_share) }
    private val tv_nick: TextView by lazy { findViewById(R.id.tv_nick) }
    private val iv_avatar: ImageView by lazy { findViewById(R.id.iv_avatar) }
    private val iv_start_level: ImageView by lazy { findViewById(R.id.iv_start_level) }
    private val iv_end_level: ImageView by lazy { findViewById(R.id.iv_end_level) }
    private val tv_next: TextView by lazy { findViewById(R.id.tv_next) }
    private val tv_count1: TextView by lazy { findViewById(R.id.tv_count1) }
    private val tv_count2: TextView by lazy { findViewById(R.id.tv_count2) }
    private val tv_count3: TextView by lazy { findViewById(R.id.tv_count3) }
    private val tv_count4: TextView by lazy { findViewById(R.id.tv_count4) }
    private val tv_count5: TextView by lazy { findViewById(R.id.tv_count5) }
    private val tv_person2: TextView by lazy { findViewById(R.id.tv_person2) }
    private val tv_person3: TextView by lazy { findViewById(R.id.tv_person3) }
    private val tv_person4: TextView by lazy { findViewById(R.id.tv_person4) }
    private val tv_person5: TextView by lazy { findViewById(R.id.tv_person5) }

    override fun getLayoutResID(): Int {
        return R.layout.activity_expand_center
    }

    override fun initView() {
        super.initView()
        rlBack.setOnClickListener(this)
        tv_my_expand.setOnClickListener(this)
        rl_share.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        getUserInfo()
        getExpandCenter()
    }

    private fun getUserInfo() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this, vodService.userInfo(), object : BaseObserver<UserInfoBean>() {
            override fun onSuccess(data: UserInfoBean) {
                tv_nick.text = data.user_nick_name
                if (data.user_portrait.isNotEmpty()) {
                    Glide.with(mActivity)
                            .load(ApiConfig.MOGAI_BASE_URL + "/" + data.user_portrait)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(iv_avatar)
                } else {
                    Glide.with(mActivity)
                            .load(R.drawable.default_avater)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(iv_avatar)
                }
                when (data.user_level) {
                    "1" -> {
                        iv_start_level.setBackgroundResource(R.drawable.ic_lv1)
                        iv_end_level.setBackgroundResource(R.drawable.ic_lv2)
                        tv_next.text = "距离下一等级还差${data.leave_peoples}人"
                    }
                    "2" -> {
                        iv_start_level.setBackgroundResource(R.drawable.ic_lv2)
                        iv_end_level.setBackgroundResource(R.drawable.ic_lv3)
                        tv_next.text = "距离下一等级还差${data.leave_peoples}人"
                    }
                    "3" -> {
                        iv_start_level.setBackgroundResource(R.drawable.ic_lv3)
                        iv_end_level.setBackgroundResource(R.drawable.ic_lv4)
                        tv_next.text = "距离下一等级还差${data.leave_peoples}人"
                    }
                    "4" -> {
                        iv_start_level.setBackgroundResource(R.drawable.ic_lv4)
                        iv_end_level.setBackgroundResource(R.drawable.ic_lv5)
                        tv_next.text = "距离下一等级还差${data.leave_peoples}人"
                    }
                    "5" -> {
                        iv_start_level.setBackgroundResource(R.drawable.ic_lv4)
                        iv_end_level.setBackgroundResource(R.drawable.ic_lv4)
                        tv_next.text = "已达到最高VIP级别"
                    }
                }
            }

            override fun onError(e: ResponseException) {
                ToastUtils.showShort(e.getErrorMessage())
            }
        })
    }

    private fun getExpandCenter() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(mActivity, vodService.expandCenter(),
                object : LoadingObserver<ExpandCenter>(mActivity) {
                    override fun onSuccess(data: ExpandCenter) {
                        tv_count1.text = "享受每日影片观影${data.v1.view_count}次"

                        tv_person2.text = "推广${data.v2.people_count}人"
                        tv_count2.text = "享受每日影片观影${data.v2.view_count}次"

                        tv_person3.text = "推广${data.v3.people_count}人"
                        tv_count3.text = "享受每日影片观影${data.v3.view_count}次"

                        tv_person4.text = "推广${data.v4.people_count}人"
                        tv_count4.text = "享受每日影片观影${data.v4.view_count}次"

                        tv_person5.text = "推广${data.v5.people_count}人"
                        tv_count5.text = "享受每日影片观影${data.v5.view_count}次"
                    }

                    override fun onError(e: ResponseException) {
                    }

                })
    }

    override fun onClick(v: View) {
        when (v) {
            rlBack -> {
                finish()
            }
            tv_my_expand -> {
                val intent = Intent(this@ExpandCenterActivity, MyExpandActivity::class.java)
                startActivity(intent)
            }
            rl_share -> {
                Toast.makeText(this, "扫码即可下载APP哦~~~", Toast.LENGTH_SHORT).show()
                val intn= Intent(this@ExpandCenterActivity, ShareActivity::class.java)
                intn.putExtra("vom_name","有奖推广活动")
                intn.putExtra("vod_pic","www") //图片
                val description_txt: String
                val description = MMkvUtils.Builds().loadStartBean("")?.ads?.share_description
                if (description == null || description.description.isNullOrEmpty()) {
                    description_txt = "1、普通用户分享成功可获得积分奖励\n2、代理用户分销成功可获得金币奖励"
                } else {
                    description_txt = description.description ?: ""
                }
                intn.putExtra("vod_blurd",description_txt) //注释
                intn.putExtra("vod_class","分享给未安装过的用户、对方注册并打开应用算分享成功") //标签
                startActivity(intn)
            }
        }
    }
}