package cn.lvdou.vod.utils

import android.app.ActivityManager
import android.content.Context
import android.widget.Toast
import cn.lvdou.vod.App


object AgainstCheatUtil {

    @JvmStatic
    fun showWarn(obj: Any?): Boolean {
        if (obj == null) {
            val activityManager =  App.getApplication().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appTaskList = activityManager.appTasks
            for (appTask in appTaskList) {
                appTask.finishAndRemoveTask()
            }
            Toast.makeText(App.getApplication(), "请关闭代理或者VPN，APP已自动退出", Toast.LENGTH_LONG).show()
            return true
        }
        return false
    }
}