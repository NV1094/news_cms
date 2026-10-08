package cn.lvdou.vod.pip;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dueeeke.videoplayer.controller.ControlWrapper;
import com.dueeeke.videoplayer.controller.IControlComponent;
import com.dueeeke.videoplayer.player.VideoView;

import cn.lvdou.av.R;

public class PipControlView extends FrameLayout implements IControlComponent, View.OnClickListener {

    private ControlWrapper mControlWrapper;

    private ImageView mPlay;
    private ImageView mClose;
    private ProgressBar mLoading;

    public PipControlView(@NonNull Context context) {
        super(context);
    }

    public PipControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PipControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_float_controller, this, true);
        mPlay = findViewById(R.id.start_play);
        mLoading = findViewById(R.id.loading);
        mClose = findViewById(R.id.btn_close);
        mClose.setOnClickListener(this);
        mPlay.setOnClickListener(this);
        findViewById(R.id.btn_skip).setOnClickListener(this);
    }
    private boolean isCanOpen = false;
    public void setIsCanOpen(boolean is){
        isCanOpen = is;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_close) {
            PIPManager.getInstance().closePip();
        } else if (id == R.id.start_play) {
            mControlWrapper.togglePlay();
        } else if (id == R.id.btn_skip) {
            Log.e("画中画","点击全屏1");
            if (PIPManager.getInstance().getActClass() != null) {
                if (!isCanOpen){
                    Log.e("画中画","点击全屏2");
                    return;
                }
//                synchronized (PipControlView.this) {
                    Log.e("画中画","点击全屏4");
                    Intent intent = new Intent(getContext(), PIPManager.getInstance().getActClass());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    getContext().startActivity(intent);
                    isCanOpen = false;
//                }
            }else {
                Log.e("画中画","点击全屏3");
            }
        }
    }

    @Override
    public void attach(@NonNull ControlWrapper controlWrapper) {
        mControlWrapper = controlWrapper;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {
        if (isVisible) {
            if (mPlay.getVisibility() == VISIBLE)
                return;
            mPlay.setVisibility(VISIBLE);
            mPlay.startAnimation(anim);
        } else {
            if (mPlay.getVisibility() == GONE)
                return;
            mPlay.setVisibility(GONE);
            mPlay.startAnimation(anim);
        }
    }

//    @Override
//    public void show(Animation showAnim) {
//        if (mPlay.getVisibility() == VISIBLE)
//            return;
//        mPlay.setVisibility(VISIBLE);
//        mPlay.startAnimation(showAnim);
//    }
//
//    @Override
//    public void hide(Animation hideAnim) {
//        if (mPlay.getVisibility() == GONE)
//            return;
//        mPlay.setVisibility(GONE);
//        mPlay.startAnimation(hideAnim);
//    }

    @Override
    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case VideoView.STATE_IDLE:
                mPlay.setSelected(false);
                mPlay.setVisibility(VISIBLE);
                mLoading.setVisibility(GONE);
                break;
            case VideoView.STATE_PLAYING:
                mPlay.setSelected(true);
                mPlay.setVisibility(GONE);
                mLoading.setVisibility(GONE);
                break;
            case VideoView.STATE_PAUSED:
                mPlay.setSelected(false);
                mPlay.setVisibility(VISIBLE);
                mLoading.setVisibility(GONE);
                break;
            case VideoView.STATE_PREPARING:
                mPlay.setVisibility(GONE);
                mLoading.setVisibility(VISIBLE);
                break;
            case VideoView.STATE_PREPARED:
                mPlay.setVisibility(GONE);
                mLoading.setVisibility(GONE);
                break;
            case VideoView.STATE_ERROR:
                mLoading.setVisibility(GONE);
                mPlay.setVisibility(GONE);
                bringToFront();
                break;
            case VideoView.STATE_BUFFERING:
                mPlay.setVisibility(GONE);
                mLoading.setVisibility(VISIBLE);
                break;
            case VideoView.STATE_BUFFERED:
                mPlay.setVisibility(GONE);
                mLoading.setVisibility(GONE);
                mPlay.setSelected(mControlWrapper.isPlaying());
                break;
            case VideoView.STATE_PLAYBACK_COMPLETED:
                bringToFront();
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {

    }

//    @Override
//    public void adjustView(int orientation, int space) {
//
//    }

    @Override
    public void setProgress(int duration, int position) {
        Log.e("画中画进度", duration + "????" + position);
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {

    }

}
