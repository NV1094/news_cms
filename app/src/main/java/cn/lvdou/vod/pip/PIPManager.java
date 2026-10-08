package cn.lvdou.vod.pip;

import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.dueeeke.videoplayer.player.VideoViewManager;

import org.greenrobot.eventbus.EventBus;

import cn.lvdou.av.OkApplication;
import cn.lvdou.av.play.AvVideoView;
import cn.lvdou.vod.bean.PipMsgBean;

/**
 * 悬浮播放
 * Created by Devlin_n on 2018/3/30.
 */

public class PIPManager {

    private static PIPManager instance;
    //    private VideoView mVideoView;
    private AvVideoView mVideoView;
    private FloatView mFloatView;
    private FloatController mFloatController;
    private boolean mIsShowing;
    private int mPlayingPosition = -1;
    private Class mActClass;
    private PipMsgBean pipMsgBean;


    private PIPManager() {
//        mVideoView = new VideoView(OkApplication.Companion.get());
        mVideoView = new AvVideoView(OkApplication.Companion.get());
        VideoViewManager.instance().add(mVideoView, "pip");
        mFloatController = new FloatController(OkApplication.Companion.get());
        mFloatView = new FloatView(OkApplication.Companion.get(), 0, 0);
    }


    public static PIPManager getInstance() {
        if (instance == null) {
            synchronized (PIPManager.class) {
                if (instance == null) {
                    instance = new PIPManager();
                }
            }
        }
        return instance;
    }

    public PipMsgBean getPipMsg(){
        return pipMsgBean;
    }

    public void startFloatWindow(PipMsgBean msgBean) {
        if (mIsShowing) return;
        pipMsgBean = msgBean;
        removeViewFormParent(mVideoView);
        mVideoView.setVideoController(mFloatController);
        mFloatController.setCanOpenPip(true);
        mFloatController.setPlayState(mVideoView.getCurrentPlayState());
        mFloatController.setPlayerState(mVideoView.getCurrentPlayerState());
        mFloatView.addView(mVideoView);
        mFloatView.addToWindow();
        mIsShowing = true;
    }

    public void stopFloatWindow() {
        if (!mIsShowing) return;
        mFloatView.removeFromWindow();
        removeViewFormParent(mVideoView);
        mIsShowing = false;
    }

    public void stopPlay(){
        if (!mIsShowing||mVideoView==null) return;
        mVideoView.pause();
    }

    public void startPlay(){
        if (!mIsShowing||mVideoView==null) return;
        mVideoView.resume();
        mVideoView.start();
    }

    public void closePip() {
        long currentPosition = mVideoView.getCurrentPosition();
        if (pipMsgBean == null || currentPosition == 0L) {
            stopFloatWindow();
            reset();
        } else {
            pipMsgBean.setCurPregress(currentPosition);
            EventBus.getDefault().post(pipMsgBean);
        }

    }

    //    --------------
    private Object cacheMsg;
    private boolean isShouldProgress;


    public void setVodBean(Object msg) {
        cacheMsg = msg;
    }

    public void setShouldProgress(boolean isShould){
        isShouldProgress = isShould;
    }

    public boolean getShouldProgress = isShouldProgress;

    public Object getVodBean() {
        return cacheMsg;
    }
//    -------------------

    public void setPlayingPosition(int position) {
        this.mPlayingPosition = position;
    }

    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    public void pause() {
        if (mIsShowing) return;
        mVideoView.pause();
    }

    public void resume() {
        if (mIsShowing) return;
        mVideoView.resume();
//        mVideoView.start();
    }

    public void reset() {
        if (mIsShowing) return;
        cacheMsg = null;
        pipMsgBean = null;
        removeViewFormParent(mVideoView);
        mVideoView.release();
        mVideoView.setVideoController(null);
        mPlayingPosition = -1;
        mActClass = null;
    }

    public boolean onBackPress() {
        return !mIsShowing && mVideoView.onBackPressed();
    }

    public boolean isStartFloatWindow() {
        return mIsShowing;
    }

    /**
     * 显示悬浮窗
     */
    public void setFloatViewVisible() {
        if (mIsShowing) {
            mVideoView.resume();
            mFloatView.setVisibility(View.VISIBLE);
        }
    }

    public void setActClass(Class cls) {
        this.mActClass = cls;
    }

    public Class getActClass() {
        return mActClass;
    }

    /**
     * 将View从父控件中移除
     */
    public static void removeViewFormParent(View v) {
        if (v == null) return;
        ViewParent parent = v.getParent();
        if (parent instanceof FrameLayout) {
            ((FrameLayout) parent).removeView(v);
        }
    }

}
