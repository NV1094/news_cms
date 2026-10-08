package cn.lvdou.av.play;

import android.view.View;

public interface ControllerClickListener {
    void onClick(View view);
    void onLongPress(); //长按切速
    void onSingleTapUp(); //长按切速
}
