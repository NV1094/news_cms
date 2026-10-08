package cn.lvdou.vod.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

public class ScreenBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        switch (action) {
            case Intent.ACTION_SCREEN_ON:
                Log.e("哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈", "开锁");
                break;
            case Intent.ACTION_SCREEN_OFF:
                Log.e("哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈", "锁屏");
                EventBus.getDefault().post("锁屏");
                break;
            case Intent.ACTION_USER_PRESENT:
                Log.e("哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈", "解锁");
                break;
        }
    }
}
