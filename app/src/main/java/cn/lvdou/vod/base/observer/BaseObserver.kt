package cn.lvdou.vod.base.observer

import com.blankj.utilcode.util.ToastUtils
import cn.lvdou.vod.base.exception.ExceptionHandler
import cn.lvdou.vod.base.exception.ResponseException
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class BaseObserver<E : Any>(private val showErrorTip: Boolean = true) : Observer<E> {

    private lateinit var disposable: Disposable

    override fun onSubscribe(d: Disposable) {
        disposable = d
    }

    override fun onNext(data: E) {
        try {
            onSuccess(data)
        } catch (t: Throwable) {
            //兑底：如 ResponseConvert 在 data==null 时返回 "" 强转，消费方取字段会抛 ClassCastException，
            //原实现会直接崩溃，这里统一收敛到 onError 流程
            onError(t)
        }
    }

    override fun onError(e: Throwable) {
        //安全转型：非 ResponseException 统一走 ExceptionHandler 归一化，避免二次 ClassCastException
        val responseException: ResponseException =
            e as? ResponseException ?: ExceptionHandler.handle(e)
        if (showErrorTip) {
            ToastUtils.showShort(responseException.getErrorMessage())
        }
        onError(responseException)
    }

    override fun onComplete() {

    }

    fun getDisposable() = disposable

    abstract fun onSuccess(data: E)

    abstract fun onError(e: ResponseException)
}