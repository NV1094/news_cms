package cn.lvdou.vod.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import cn.lvdou.vod.ApiConfig
import cn.lvdou.vod.App
import cn.lvdou.vod.MainActivity
import cn.lvdou.vod.R
import cn.lvdou.vod.base.BaseActivity
import cn.lvdou.vod.base.RequestManager
import cn.lvdou.vod.base.exception.ResponseException
import cn.lvdou.vod.base.observer.LoadingObserver
import cn.lvdou.vod.bean.LoginBean
import cn.lvdou.vod.bean.OpenRegister
import cn.lvdou.vod.netservice.VodService
import cn.lvdou.vod.ui.play.X5WebActivity
import cn.lvdou.vod.utils.AgainstCheatUtil
import cn.lvdou.vod.utils.MMkvUtils
import cn.lvdou.vod.utils.MMkvUtils.Companion.Builds
import cn.lvdou.vod.utils.Retrofit2Utils
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import jaygoo.library.m3u8downloader.M3U8Library.context
import org.greenrobot.eventbus.EventBus
import java.util.*

class LoginActivity : BaseActivity(), Handler.Callback {
    private val ivBack: ImageView by lazy { findViewById(R.id.ivBack) }
    private val iv_user_plcs: ImageView by lazy { findViewById(R.id.iv_user_plcs) }
    private val textids: TextView by lazy { findViewById(R.id.textids) }
    private val iv_user: EditText by lazy { findViewById(R.id.iv_user) }
    private val et_verify: EditText by lazy { findViewById(R.id.et_verify) }
    private val iv_pass: EditText by lazy { findViewById(R.id.iv_pass) }
    private val tvForget: TextView by lazy { findViewById(R.id.tvForget) }
    private val iv_checkUserXy: LinearLayout by lazy { findViewById(R.id.iv_checkUserXy) }
    private val checkUserXy: CheckBox by lazy { findViewById(R.id.checkUserXy) }
    private val tv_desc: TextView by lazy { findViewById(R.id.tv_desc) }
    private val btn_login: Button by lazy { findViewById(R.id.btn_login) }
    private val iv_btn_login: TextView by lazy { findViewById(R.id.iv_btn_login) }
    private val iv_btn_login1: LinearLayout by lazy { findViewById(R.id.iv_btn_login1) }
    private val tv_send: Button by lazy { findViewById(R.id.tv_send) }
    private val switch_account: TextView by lazy { findViewById(R.id.switch_account) }

    private var curType = 0 //默认为登录类型
    private var iduser = "" //账号
    private var idpass = "" //密码
    private val WHAT_COUNT = 1
    private var mHanlder: Handler? = null
    private val MAX_NUM = 60
    private var index = MAX_NUM
    private var timer: Timer? = null
    private var task: TimerTask? = null
    private var isOpenRegister: Boolean = true //默认注册方式
    private var define_account: Boolean = true //默认一键登录

    override fun getLayoutResID(): Int {
        return R.layout.activity_login
    }

    //用于计算图片的宽高值
    private var imageWidthSize = 0
    private var imageHeightSize = 0

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()
        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.black))
        checkIsOpenRegister()
        Glide.with(this)
                .load(R.mipmap.ic_launcher_round)
                .error(R.drawable.monkey_avatar)
                .centerCrop()
                .override(imageWidthSize, imageHeightSize) //默认淡入淡出动画
                .transition(DrawableTransitionOptions.withCrossFade()) //缓存策略,跳过内存缓存【此处应该设置为false，否则列表刷新时会闪一下】
                .skipMemoryCache(false) //缓存策略,硬盘缓存-仅仅缓存最终的图像，即降低分辨率后的（或者是转换后的）
                .diskCacheStrategy(DiskCacheStrategy.ALL) //设置图片加载的优先级
                .priority(Priority.HIGH)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(iv_user_plcs)

        mHanlder = Handler(Looper.getMainLooper(), this)
        if (Builds().loadAccount() == "") {
            textids.text = getAndroidID()
        } else {
            textids.text = Builds().loadUserNickname()
        }
        val loginType = Builds().loadStartBean("")?.ads?.define_account
        if (loginType != null && loginType.status == 0) { //仅当后台显式关闭自定义注册时才隐藏入口
            iv_btn_login1.visibility = View.GONE
            iv_btn_login.visibility = View.GONE
        } else {
            iv_btn_login1.visibility = View.VISIBLE
            iv_btn_login.visibility = View.VISIBLE
        }   //还有一种情况、开启了短信验证
        val str = tv_desc.text
        val ssb = SpannableStringBuilder()
        ssb.append(str as CharSequence)
        val start = str.indexOf("《") //第一个出现的位置
        ssb.setSpan(object : ClickableSpan() {
            override fun onClick(v: View) {
                val intent = Intent(context, X5WebActivity::class.java)
                val bundle = Bundle()
                bundle.putString("url", ApiConfig.MOGAI_BASE_URL + "/app/TermsOfService.html")
                bundle.putString("title", "服务条款")
                intent.putExtras(bundle)
                ActivityUtils.startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(context, R.color._xpopup_content_color) //设置文件颜色
                ds.isUnderlineText = false
            }
        }, start, start + 6, 0)

        val end = str.lastIndexOf("《") //最后一个出现的位置
        ssb.setSpan(object : ClickableSpan() {
            override fun onClick(v: View) {
                val intent = Intent(context, X5WebActivity::class.java)
                val bundle = Bundle()
                bundle.putString("url", ApiConfig.MOGAI_BASE_URL + "/app/PrivacyPolicy.html")
                bundle.putString("title", "隐私权政策")
                intent.putExtras(bundle)
                ActivityUtils.startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(context, R.color._xpopup_content_color) //设置文件颜色
                ds.isUnderlineText = false
            }
        }, end, end + 7, 0)
        val middle = str.indexOf("【") //第一个出现的位置
        ssb.setSpan(object : ClickableSpan() {
            override fun onClick(v: View) {
                val intent = Intent(context, X5WebActivity::class.java)
                val bundle = Bundle()
                bundle.putString("url", ApiConfig.MOGAI_BASE_URL + "/app/PlatformStatement.html")
                bundle.putString("title", "平台申明")
                intent.putExtras(bundle)
                ActivityUtils.startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(context, R.color._xpopup_content_color) //设置文件颜色
                ds.isUnderlineText = false
            }
        }, middle, middle + 6, 0)

        tv_desc.highlightColor = Color.TRANSPARENT
        tv_desc.movementMethod = LinkMovementMethod.getInstance()
        tv_desc.setText(ssb, TextView.BufferType.SPANNABLE)
    }

    override fun initListener() {
        super.initListener()
        tvForget.setOnClickListener {
            ForgetPswActivity.start(this)
        }

        ivBack.setOnClickListener {
            ActivityUtils.startActivity(MainActivity::class.java)
        }
        iv_checkUserXy.setOnClickListener {
            checkUserXy.isChecked = true
        }
        switch_account.setOnClickListener {
            define_account = false
            if (switch_account.text == "切换账号") { //用账号登录
                curType = 0 //切换到登录类型
                btn_login.text = "登 录"
                iv_user.hint = "请输入账号"
                switch_account.text = "账号注册"
                iv_user.visibility = View.VISIBLE
                et_verify.visibility = View.GONE
                iv_pass.visibility = View.VISIBLE
                tv_send.visibility = View.GONE
                iv_btn_login.visibility = View.GONE
                btn_login.visibility = View.VISIBLE
                tvForget.visibility = View.VISIBLE
            } else {
                curType = 1
                btn_login.text = "注 册"
                switch_account.text = "切换账号"
                tvForget.visibility = View.GONE
                if (isOpenRegister) { //手机号码注册
                    iv_user.hint = "请输入手机号码"
                    iv_user.visibility = View.VISIBLE
                    et_verify.visibility = View.GONE
                    iv_pass.visibility = View.GONE
                    iv_btn_login.visibility = View.GONE
                    btn_login.visibility = View.GONE
                    tv_send.visibility = View.VISIBLE
                } else { //常规注册
                    iv_user.hint = "请输入账号"
                    tv_send.visibility = View.GONE
                    et_verify.visibility = View.GONE
                    iv_user.visibility = View.VISIBLE
                    iv_pass.visibility = View.VISIBLE
                    btn_login.visibility = View.VISIBLE
                }
            }
            iv_user.setText("")
            iv_pass.setText("")
        }

        btn_login.setOnClickListener {
            if (!checkUserXy.isChecked) {
                ToastUtils.showShort("若不认同本服务协议将无法使用本软件！请勾选用户使用协议")
                return@setOnClickListener
            }

            if (curType == 0) { //登录
                if (define_account) { //一键登录
                    iduser = getAndroidID()
                    idpass = Base64.encodeToString(getAndroidID().toByteArray(), Base64.DEFAULT)
                    register()
                } else { //普通登录
                    iduser = iv_user.text.trim().toString()
                    idpass = iv_pass.text.trim().toString()
                    login()
                }
            } else { //注册
                iduser = iv_user.text.trim().toString()
                idpass = iv_pass.text.trim().toString()
                if (isOpenRegister) { //手机注册
                    registerByCode()
                } else { //普通注册
                    register()
                }
            }
        }

        tv_send.setOnClickListener {
            sendVerifyCode()
        }
    }

    private fun login() {
        if (check()) {
            val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

            val loginObservable = vodService.login(iduser, idpass)
            RequestManager.execute(this, loginObservable, object : LoadingObserver<String>(this) {
                override fun onSuccess(data: String) {
                    Builds().saveAccount(iduser)
                    Builds().saveAccount2(idpass)
                    ToastUtils.showShort(R.string.login_success)
                    EventBus.getDefault().post(LoginBean())
                    ActivityUtils.startActivity(MainActivity::class.java)
                    finish()
                }

                override fun onError(e: ResponseException) {
                    if (define_account) {
                        val str = e.getErrorMessage()
                        val status = str?.contains("请重新") == true
                        if (status) {
                            define_account = false
                            ToastUtils.showShort("一键登录失败！请切换账号登录/注册")
                            curType = 0 //切换到登录类型
                            btn_login.text = "登 录"
                            iv_user.hint = "请输入账号"
                            switch_account.text = "账号注册"
                            iv_user.visibility = View.VISIBLE
                            et_verify.visibility = View.GONE
                            iv_pass.visibility = View.VISIBLE
                            tv_send.visibility = View.GONE
                            iv_btn_login.visibility = View.GONE
                            btn_login.visibility = View.VISIBLE
                            tvForget.visibility = View.VISIBLE
                        }
                    }
                }
            })
        }
    }

    private fun registerByCode() { //手机注册
        if (check()) {
            val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

            val registerObservable = vodService.registerByCode(iduser, idpass, et_verify.text.trim().toString(), "phone", App.CHANNE)
            RequestManager.execute(this, registerObservable, object : LoadingObserver<String>(this) {
                override fun onSuccess(data: String) {
                    curType = 0
                    btn_login.text = "登 录"
                    et_verify.visibility = View.GONE
                    ToastUtils.showShort(R.string.register_success)
                }

                override fun onError(e: ResponseException) {

                }
            })
        }
    }

    private fun register() { //常规注册
        if (check()) {
            val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)


            val registerObservable = vodService.register(iduser, idpass, idpass, App.CHANNE)
            RequestManager.execute(this, registerObservable, object : LoadingObserver<String>(this) {
                override fun onSuccess(data: String) {
                    ToastUtils.showShort(R.string.register_success)
                    btn_login.text = "登 录"
                    curType = 0
                    if (define_account) {
                        login()
                    }
                }

                override fun onError(e: ResponseException) {
                    if (define_account) {
                        val str = e.getErrorMessage()
                        val status = str?.contains("请更换") == true
                        if (status) {
                            ToastUtils.showShort("登录中...")
                            login()
                        }
                    }
                }
            })
        }
    }

    private fun checkIsOpenRegister() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        val registerObservable = vodService.openRegister()
        RequestManager.execute(this, registerObservable, object : LoadingObserver<OpenRegister>(this) {
            override fun onSuccess(data: OpenRegister) {
                isOpenRegister = data.phone == "1"
                if (isOpenRegister) { //开启了短信验证，这里要关闭一键注册
                    define_account = false
                    iv_btn_login1.visibility = View.VISIBLE //短信注册已开启，注册入口必须可见
                    tvForget.visibility = View.VISIBLE
                    iv_user.visibility = View.VISIBLE
                    et_verify.visibility = View.GONE
                    iv_pass.visibility = View.VISIBLE
                    tv_send.visibility = View.GONE
                    btn_login.visibility = View.VISIBLE
                    btn_login.text = "登 录"
                    iv_btn_login.visibility = View.GONE
                } else {
                    val Applogintype = Builds().loadStartBean("")?.app_login_type //startBean 未就绪时不能崩溃
                    if (Applogintype == "0" || Applogintype == null) {
                        define_account = true
                        tvForget.visibility = View.GONE
                        iv_user.visibility = View.GONE
                        et_verify.visibility = View.GONE
                        iv_pass.visibility = View.GONE
                        tv_send.visibility = View.GONE
                        btn_login.text = "设备ID一键登录"
                        btn_login.visibility = View.VISIBLE
                        iv_btn_login.visibility = View.VISIBLE
                    } else {
                        define_account = false
                        tvForget.visibility = View.VISIBLE
                        iv_user.visibility = View.VISIBLE
                        et_verify.visibility = View.GONE
                        iv_pass.visibility = View.VISIBLE
                        tv_send.visibility = View.GONE
                        btn_login.visibility = View.VISIBLE
                        btn_login.text = "登 录"
                        iv_btn_login.visibility = View.GONE
                    }
                }
            }

            override fun onError(e: ResponseException) {
                isOpenRegister = false
                tvForget.visibility = View.VISIBLE
                iv_user.visibility = View.VISIBLE
                et_verify.visibility = View.GONE
                iv_pass.visibility = View.VISIBLE
                tv_send.visibility = View.GONE
                btn_login.visibility = View.VISIBLE
                btn_login.text = "登 录"
                iv_btn_login.visibility = View.GONE
            }
        })
    }

    private fun sendVerifyCode() {
        val phone = iv_user.text.trim().toString()
        if (phone.isEmpty()) {
            ToastUtils.showShort(R.string.phone_isempty)
            return
        }
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)

        startTimer()
        val registerObservable = vodService.sendVerifyCode(phone)
        RequestManager.execute(this, registerObservable, object : LoadingObserver<String>(this) {
            override fun onSuccess(data: String) {
                //保持 tv_send 可见显示倒计时，倒计时结束后可重新获取验证码（短信未收到不再卡死）
                iv_btn_login.visibility = View.GONE
                et_verify.visibility = View.VISIBLE
                iv_pass.visibility = View.VISIBLE
                btn_login.visibility = View.VISIBLE
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

    private fun check(): Boolean {
        val username = iduser
        val password = idpass
//        val repassword = et_login_e3.text.trim().toString()
        val verifyCode = et_verify.text.trim().toString()
        if (StringUtils.isEmpty(username)) {
            ToastUtils.showShort(R.string.phone_isempty)
            return false
        }
        if (StringUtils.isEmpty(password)) {
            ToastUtils.showShort(R.string.password_isempty)
            return false
        }
//        val isPhone = RegexUtils.isMobileSimple(username)
//        if (!isPhone) {
//            ToastUtils.showShort(R.string.phone_format_incorrect)
//            return false
//        }
        if (curType == 1) {
            if (isOpenRegister) {
                if (StringUtils.isEmpty(verifyCode)) {
                    ToastUtils.showShort(R.string.verify_code_isempty)
                    return false
                }
//            } else {
//                if (StringUtils.isEmpty(repassword)) {
//                    ToastUtils.showShort(R.string.repassword_isempty)
//                    return false
//                }
//                if (!password.equals(repassword)) {
//                    ToastUtils.showShort(R.string.repassowrd_not_correct)
//                    return false
//                }
            }
        }
        return true
    }

    companion object {
        fun start() {
            ActivityUtils.startActivity(LoginActivity::class.java, R.anim.slide_in_right, R.anim.no_anim)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun handleMessage(msg: Message): Boolean {
        return when (msg.what) {
            WHAT_COUNT -> {
                if (msg.arg1 == MAX_NUM) {
                    tv_send.isEnabled = true
                    tv_send.text = "获取验证码"
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
