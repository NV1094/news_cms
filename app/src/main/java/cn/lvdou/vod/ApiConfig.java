package cn.lvdou.vod;


import android.util.Base64;

import cn.lvdou.vod.bean.BaseResult;
import cn.lvdou.vod.ui.start.StartActivity;
import cn.lvdou.vod.utils.MMkvUtils;

public class ApiConfig {

    public static int UNFEIYUYIN;
    public static int LVDOU_LOGIN_TYPE;
    public static String MOGAI_BASE_URLB;
    public static BaseResult<String> LVDOU_TAB;

    public static String  MOGAI_BASE_URL;
    public static boolean SdkSwitchm = false; //是否PRO,这个值由APP.JAVA控制

    public static final String getStart = "/icciu_api.php/v1.main/startup";
    public static final String getTypeList = "/icciu_api.php/v1.vod/types";
    public static final String getBannerList = "/icciu_api.php/v1.vod";
    //专题
    public static final String getTopicList = "/icciu_api.php/v1.topic/topicList";
    //专题详情
    public static final String getTopicDetail = "/icciu_api.php/v1.topic/topicDetail";
    //游戏
    public static final String getGameList = "/icciu_api.php/v1.youxi/index";
    //添加视频播放记录+1
    public static final String addPlayLog = "/icciu_api.php/v1.user/addViewLog";
    //上报观影时长+1
    public static final String watchTimeLong = "/icciu_api.php/v1.user/viewSeconds";
    //获取视频购买记录/购买记录
    public static final String getPlayLogList = "/icciu_api.php/v1.user/viewLog";
    //PUST推送
    public static final String getPushList = "/icciu_api.php/v1.vod/push";
    //获取视频播放进度
    public static final String getVideoProgress = "/icciu_api.php/v1.vod/videoProgress";
    //删除播放记录+1
    public static final String dleltePlayLogList = "/icciu_api.php/v1.user/delVlog";
    //直播列表
    public static final String getLiveList = "/icciu_api.php/v1.zhibo";
    //播放器开始播放广告
    public static final String getAdconfig = "/icciu_api.php/v1.Vodad";
    //找回密码短信发送
    public static final String findpasssms = "/icciu_api.php/v1.auth/findpasssms";
    //找回密码
    public static final String findpass = "/icciu_api.php/v1.auth/findpass";
    //弹幕列表
    public static final String getDanMuList = "/icciu_api.php/v1.danmu/index";
    public static final String getTopList = "/icciu_api.php/v1.vod";
    public static final String getCardList = "/icciu_api.php/v1.main/category";
    public static final String getRecommendList = "/icciu_api.php/v1.vod/vodPhbAll";
    public static final String getCardListByType = "/icciu_api.php/v1.vod/type";
    public static final String getVodList = "/icciu_api.php/v1.vod";
    public static final String getVod = "/icciu_api.php/v1.vod/detail";
    public static final String COMMENT = "/icciu_api.php/v1.comment";
    public static final String USER_INFO = "/icciu_api.php/v1.user/detail";
    public static final String LOGIN = "/icciu_api.php/v1.auth/login";
    public static final String LOGOUT = "/icciu_api.php/v1.auth/logout";
    public static final String REGISTER = "/icciu_api.php/v1.auth/register";
    public static final String VERIFY_CODE = "/icciu_api.php/v1.auth/registerSms";//发送注册验证码
    public static final String OPEN_REGISTER = "/icciu_api.php/v1.user/phoneReg";
    public static final String SIGN = "/icciu_api.php/v1.sign";
    public static final String GROUP_CHAT = "/icciu_api.php/v1.groupchat";
    public static final String CARD_BUY = "/icciu_api.php/v1.user/buy";
    public static final String UPGRADE_GROUP = "/icciu_api.php/v1.user/group";
    public static final String SCORE_LIST = "/icciu_api.php/v1.user/groups";
    public static final String CHANGE_AGENTS = "/icciu_api.php/v1.user/changeAgents";
    public static final String AGENTS_SCORE = "/icciu_api.php/v1.user/agentsScore";
    public static final String POINT_PURCHASE = "/icciu_api.php/v1.user/order";
    public static final String CHANGE_NICKNAME = "/icciu_api.php/v1.user";
    public static final String CHANGE_AVATOR = "/icciu_api.php/v1.upload/user";
    public static final String GOLD_WITHDRAW = "/icciu_api.php/v1.user/goldWithdrawApply";
    public static final String PAY_TIP = "/icciu_api.php/v1.user/payTip";
    public static final String GOLD_TIP = "/icciu_api.php/v1.user/goldTip";
    public static final String FEEDBACK = "/icciu_api.php/v1.gbook";
    public static final String COLLECTION_LIST = "/icciu_api.php/v1.user/favs";
    public static final String COLLECTION = "/icciu_api.php/v1.user/ulog";
    public static final String SHARE_SCORE = "/icciu_api.php/v1.user/shareScore";
    public static final String TASK_LIST = "/icciu_api.php/v1.user/task";
    public static final String MSG_LIST = "/icciu_api.php/v1.message/index";
    public static final String MSG_DETAIL = "/icciu_api.php/v1.message/detail";
    public static final String EXPAND_CENTER = "/icciu_api.php/v1.user/userLevelConfig";
    public static final String MY_EXPAND = "/icciu_api.php/v1.user/subUsers";
    public static final String SEND_DANMU = "/icciu_api.php/v1.danmu";
    public static final String SCORE = "/icciu_api.php/v1.vod/score";
    public static final String CHECK_VOD_TRYSEE = "/icciu_api.php/v1.user/checkVodTrySee";
    public static final String BUY_VIDEO = "/icciu_api.php/v1.user/buypopedom";
    public static final String CHECK_VERSION = "/icciu_api.php/v1.main/version";
    public static final String PAY = "/icciu_api.php/v1.user/pay";
    public static final String ORDER = "/icciu_api.php/v1.user/order";
    public static final String APP_CONFIG = "/icciu_api.php/v1.user/appConfig";
    public static final String SHARE_INFO = "/icciu_api.php/v1.user/shareInfo";
    public static final String video_count = "/icciu_api.php/v1.vod/videoViewRecode";
    public static final String tabFourInfo = "/icciu_api.php/v1.youxi/index";
    public static final String tabThreeName = "/icciu_api.php/v1.zhibo/thirdUiName";
    public static final String getRankList = "/icciu_api.php/v1.vod/vodphb";
    public static void main() {}
}
