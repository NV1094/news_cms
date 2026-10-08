package cn.lvdou.vod.ui.account

import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.bean.LoginBean
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import com.blankj.utilcode.util.ToastUtils
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.observer.LoadingObserver
import org.greenrobot.eventbus.EventBus

class ChangeNicknameActivity : BaseActivity() {
    private val rlBack: RelativeLayout by lazy { findViewById(R.id.rlBack) }
    private val tvFinish: Button by lazy { findViewById(R.id.tvFinish) }
    private val etNickname: EditText by lazy { findViewById(R.id.etNickname) }

    override fun getLayoutResID(): Int {
        return R.layout.activity_change_nickname
    }

    override fun initListener() {
        super.initListener()
        rlBack.setOnClickListener {
            finish()
        }
        tvFinish.setOnClickListener {
            changeNickname()
        }
    }

    private fun changeNickname() {
        val newNickName = etNickname.text.trim().toString()
        if(newNickName.isEmpty()){
            ToastUtils.showShort(R.string.new_nickname_empty)
            return
        }
        var vodService= Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        RequestManager.execute(this, vodService.changeNickname(newNickName), object : LoadingObserver<String>(mActivity) {
            override fun onSuccess(data: String) {
                ToastUtils.showShort(R.string.change_nickname_success)
                EventBus.getDefault().post(LoginBean())
                finish()
            }

            override fun onError(e: ResponseException) {
            }

        })
    }

}
