package cn.lvdou.av

import android.app.Application

/**
 * 通过反射机制在module中获取到Application的Context实例。
 */
class OkApplication {
    var activityThread: Object? = null

    constructor(){
        try {
            val acThreadClass = Class.forName("android.app.ActivityThread")
            acThreadClass ?: return
            var acThreadMethod = acThreadClass.getMethod("currentActivityThread")
            acThreadClass ?: return
            acThreadMethod.isAccessible = true
            activityThread = acThreadMethod.invoke(null) as Object?
            var applicationMethod = activityThread?.`class`?.getMethod("getApplication")
            applicationMethod ?: return
            var app = applicationMethod.invoke(activityThread)
            application = app as Application
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    companion object {

        private var application: Application? = null
        fun get(): Application {
            if (application == null) {
                synchronized(OkApplication::class.java) {
                    if (application == null) {
                        OkApplication()
                    }
                }
            }
            return application!!
        }
    }
}