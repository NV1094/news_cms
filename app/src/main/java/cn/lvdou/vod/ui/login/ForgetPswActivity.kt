package cn.lvdou.vod.ui.login

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.base.observer.LoadingObserver
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.Retrofit2Utils
import com.blankj.utilcode.util.ToastUtils
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.RequestManager
import java.util.*

class ForgetPswActivity : BaseActivity(), Handler.Callback {

    private val rlBack: RelativeLayout by lazy { findViewById(R.id.rlBack) }
    private val tv_send: TextView by lazy { findViewById(R.id.tv_send) }
    private val btn_confirm: Button by lazy { findViewById(R.id.btn_confirm) }
    private val et_login_e1: EditText by lazy { findViewById(R.id.et_login_e1) }
    private val et_verify2: EditText by lazy { findViewById(R.id.et_verify2) }
    private val et_login_e2: EditText by lazy { findViewById(R.id.et_login_e2) }
    private val et_login_e3: EditText by lazy { findViewById(R.id.et_login_e3) }

    private var mHanlder: Handler? = null
    private val WHAT_COUNT = 1
    private val MAX_NUM = 60
    private var index = MAX_NUM
    private var timer: Timer? = null
    private var task: TimerTask? = null

    companion object{
        fun start(context: Context){
            val intent = Intent(context,ForgetPswActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getLayoutResID() = R.layout.activity_forget_psw

    override fun initView() {
        super.initView()

        mHanlder = Handler(this)

        setLisenter()
    }

    private fun setLisenter() {
        rlBack.setOnClickListener { finish() }
        tv_send.setOnClickListener {
            sendVerifyCode()
        }
        btn_confirm.setOnClickListener {
            val phone = et_login_e1.text.trim().toString()
            val code = et_verify2.text.trim().toString()
            val psw1 = et_login_e2.text.trim().toString()
            val psw2 = et_login_e3.text.trim().toString()

            if (phone.isEmpty()) {
                ToastUtils.showShort(R.string.phone_isempty)
                return@setOnClickListener
            }
            if (code.isEmpty()) {
                ToastUtils.showShort("请输入验证码")
                return@setOnClickListener
            }
            if (psw1.isEmpty() || psw2.isEmpty()) {
                ToastUtils.showShort("请输入密码")
                return@setOnClickListener
            }

            if (psw1 != psw2) {
                ToastUtils.showShort("两次输入的密码不一致")
                return@setOnClickListener
            }
            forgetPsw(phone,code,psw1)
        }
    }

    private fun forgetPsw(phone: String, code: String, psw: String) {
        var vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        startTimer()
        val registerObservable = vodService
                .findpass(phone,psw,psw,code)
        RequestManager.execute(this, registerObservable, object : LoadingObserver<String>(this) {
            override fun onSuccess(data: String) {
                ToastUtils.showShort("成功")
                finish()
            }

            override fun onError(e: ResponseException) {
                ToastUtils.showShort(e.getErrorMessage())
            }
        })
    }

    private fun sendVerifyCode() {
        val phone = et_login_e1.text.trim().toString()
        if (phone.isEmpty()) {
            ToastUtils.showShort(R.string.phone_isempty)
            return
        }
        var vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        startTimer()
        val registerObservable = vodService
                .findpasssms(phone)
        RequestManager.execute(this, registerObservable, object : LoadingObserver<String>(this) {
            override fun onSuccess(data: String) {
                ToastUtils.showShort(R.string.verify_code_success)
            }

            override fun onError(e: ResponseException) {
                cancelTimer()
            }
        })
    }


    private fun startTimer() {
        tv_send.isEnabled = false
        timer = Timer()
        task = object : TimerTask() {
            override fun run() {
                if (index <= 1) {
                    cancelTimer()
                } else {
                    index--
                    mHanlder?.sendMessage(Message.obtain(mHanlder, WHAT_COUNT, index, 0))
                }
            }
        }
        timer?.schedule(task, 0, 1000)
    }

    private fun cancelTimer() {
        index = MAX_NUM
        mHanlder?.sendMessage(Message.obtain(mHanlder, WHAT_COUNT, index, 0))
        task?.cancel()
        timer?.cancel()
    }

    override fun handleMessage(msg: Message): Boolean {
        return when (msg.what) {
            WHAT_COUNT -> {
                if (msg.arg1 == MAX_NUM) {
                    tv_send.isEnabled = true
                    tv_send.text = "发送验证码"
                } else {
                    tv_send.text = "${index}s"
                }
                true
            }
            else -> {
                false
            }
        }
    }
}