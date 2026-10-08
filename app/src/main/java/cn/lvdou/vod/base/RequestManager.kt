package cn.lvdou.vod.base

import androidx.appcompat.app.AppCompatActivity
import cn.lvdou.vod.bean.BaseResult
import cn.lvdou.vod.base.convert.ExceptionConvert
import com.github.StormWyrm.wanandroid.base.net.convert.ResponseConvert
import cn.lvdou.vod.base.observer.BaseObserver
import com.github.StormWyrm.wanandroid.base.sheduler.IoMainScheduler
import io.reactivex.Observable

object RequestManager {
    @JvmStatic
    fun <T : Any> execute(activity: BaseActivity, observable: Observable<BaseResult<T>>?, observer: BaseObserver<T>) {
        if (observable == null)
            throw RuntimeException("the observable is null")

        observable
            .map(ResponseConvert())
            .onErrorResumeNext(ExceptionConvert<T>())
            .compose(IoMainScheduler())
            .subscribe(observer)

        activity.addDisposable(observer.getDisposable())
    }
    @JvmStatic
    fun <T : Any> execute( observable: Observable<BaseResult<T>>?, observer: BaseObserver<T>) {
        if (observable == null)
            throw RuntimeException("the observable is null")

        observable
                .map(ResponseConvert())
                .onErrorResumeNext(ExceptionConvert<T>())
                .compose(IoMainScheduler())
                .subscribe(observer)


    }
    @JvmStatic
    fun <T : Any> execute(fragment: BaseFragment, observable: Observable<BaseResult<T>>?, observer: BaseObserver<T>) {
        if (observable == null)
            throw RuntimeException("the observable is null")
        observable
                .map(ResponseConvert())
                .onErrorResumeNext(ExceptionConvert<T>())
                .compose(IoMainScheduler())
                .subscribe(observer)
        fragment
    }

    @JvmStatic
    fun <T : Any>  execute(mActivity: AppCompatActivity, sendDanmu: Observable<BaseResult<T>>?, baseObserver: BaseObserver<T>) {
        if (sendDanmu == null)
            throw RuntimeException("the observable is null")

        sendDanmu
                .map(ResponseConvert())
                .onErrorResumeNext(ExceptionConvert<T>())
                .compose(IoMainScheduler())
                .subscribe(baseObserver)

        //修复：原实现最后一行裸 `mActivity` 为空操作，订阅未注册到生命周期，
        //导致 VideoDetailFragment 等处注释里怀疑的"这个函数可能有问题"
        (mActivity as? BaseActivity)?.addDisposable(baseObserver.getDisposable())
    }
}