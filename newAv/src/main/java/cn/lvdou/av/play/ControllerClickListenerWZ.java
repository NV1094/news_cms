package cn.lvdou.av.play;

import android.view.View;

public interface ControllerClickListenerWZ {
    void onClick(View view);
    void onPlayProgress(int duration, int position);

    void onSendDanmu(String content,String textcolor);

    void onMsc(String content);

    boolean isLogin();

}
