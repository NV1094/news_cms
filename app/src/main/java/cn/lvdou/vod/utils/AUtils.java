package cn.lvdou.vod.utils;

import android.text.format.Time;
import android.util.Log;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;

import cn.lvdou.vod.App;

/**
 * @author joychang
 * @Description 应用工具类
 */
public class AUtils {

    public static String sign() {
        return getSign(new Date());
    }

    public static Date dateTime() {
        Date dateTime = new Date();
        return dateTime;
    }

    public static String keyTime() {
        return String.valueOf(dateTime().getTime() / 1000);
    }

    public static String getSign(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String key = App.KEY;
        // 获取当前年
        String year = String.valueOf(cal.get(Calendar.YEAR));
        // 获取当前小时
        String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        // 获取当前分钟
        String minute = String.valueOf(cal.get(Calendar.MINUTE));
        if (hour.length() < 2) {
            hour = "0" + hour;
        }
        if (minute.length() < 2) {
            minute = "0" + minute;
        }
        Log.d("Datesss", md5(year + ":" + hour + ":" + year + ":" + minute + ":" + key));
        return md5(year + ":" + hour + ":" + year + ":" + minute + ":" + key);
    }

    /**
     * 获取当前时间
     *
     * @return 时间字符串 24小时制
     * @author drowtram
     */
    public static String getStringTime(String type) {
        Time t = new Time();
        t.setToNow(); // 取得系统时间。
        String hour = t.hour < 10 ? "0" + (t.hour) : t.hour + ""; // 默认24小时制
        String minute = t.minute < 10 ? "0" + (t.minute) : t.minute + "";
        return hour + type + minute;
    }

    public static String md5(String string) {
        String hashedPwd;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            byte b[] = md.digest();
            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //32位
            hashedPwd = buf.toString();
            return hashedPwd;
            // 16位
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
