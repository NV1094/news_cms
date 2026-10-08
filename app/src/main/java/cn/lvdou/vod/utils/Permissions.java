package cn.lvdou.vod.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

/*
createName : 程序员keven
createTime : 2022/2/21
*/
public class Permissions {
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void checkPermission(Activity activity , String[] permission){
        String[] noPermission = new String[permission.length];
        int j = 0 ;
        for (int i = 0; i < permission.length; i++) {
            if (activity.checkSelfPermission(permission[i]) == PackageManager.PERMISSION_DENIED){
                if (permission[i] != null){
                    requestPermisson(new String[]{permission[i]} ,  activity );
                }
            }
        }
    }
    private static void requestPermisson(String[] s , Activity activity) {
        ActivityCompat.requestPermissions(activity , s  , 100);
    }
}
