package cn.lvdou.vod.utils;

import android.content.Context;
import com.blankj.utilcode.util.Utils;
import java.util.List;

public class LelinkHelper {
//    private static LelinkHelper sLelinkHelper;
//    private Context mContext;
//    private LelinkPlayer leLinkPlayer;
//    private LelinkServiceInfo lelinkServiceInfo;
//
//    public static LelinkHelper getInstance() {
//        if (sLelinkHelper == null) {
//            sLelinkHelper = new LelinkHelper(Utils.getApp());
//        }
//        return sLelinkHelper;
//    }
//
//    private LelinkHelper(Context context) {
//        mContext = context;
//    }
//
//    public void initLelink() {
//        LelinkSetting lelinkSetting = new LelinkSetting.LelinkSettingBuilder("17721", "bbc421df60bcc62dd9b2f9e0bd13e057").build();//打包前修改：乐播投屏id
//        ILelinkServiceManager lelinkServiceManager = LelinkServiceManager.getInstance(mContext);
//        lelinkServiceManager.setLelinkSetting(lelinkSetting);
//    }
//
//    public void connect(LelinkServiceInfo info, IConnectListener connectListener) {
//        lelinkServiceInfo = info;
//        leLinkPlayer = new LelinkPlayer(mContext);
//        leLinkPlayer.setConnectListener(connectListener);
//        leLinkPlayer.connect(info);
//    }
//
//    public void disConnect() {
//        if (lelinkServiceInfo != null) {
//            leLinkPlayer.stop();
//            leLinkPlayer.disConnect(lelinkServiceInfo);
//            leLinkPlayer.release();
//            leLinkPlayer = null;
//            lelinkServiceInfo = null;
//        }
//    }
//
//    public void back(){
//        if (lelinkServiceInfo != null) {
//            leLinkPlayer.disConnect(lelinkServiceInfo);
//            leLinkPlayer.release();
//            leLinkPlayer = null;
//            lelinkServiceInfo = null;
//        }
//    }
//
//    public void setPlayerInfo(LelinkPlayerInfo lelinkPlayerInfo) {
//        if (leLinkPlayer!=null){
//            leLinkPlayer.setDataSource(lelinkPlayerInfo);
//        }
//    }
//
//
//    public void setLelinkPlayerListener(ILelinkPlayerListener playerListener) {
//        if (leLinkPlayer!=null) {
//            leLinkPlayer.setPlayerListener(playerListener);
//        }
//    }
//
//    public void start() {
//        leLinkPlayer.start();
//    }
//
//    public void stop() {
//        leLinkPlayer.stop();
//    }
//
//    public void pause() {
//        leLinkPlayer.pause();
//    }
//
//    public void resume() {
//        leLinkPlayer.resume();
//    }
//
//    public List<LelinkServiceInfo> getConnectLelinkServiceInfos() {
//        if (leLinkPlayer != null)
//            return leLinkPlayer.getConnectLelinkServiceInfos();
//        else
//            return null;
//    }
}
