package cn.lvdou.vod.bean;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class StartBean implements Serializable {
    private static final long serialVersionUID = -2259983684774852421L;
    private Sign sign;
    private Vodmark vod_mark;
    private Comment comment;
    private Danmu danmu;
    private Document document;
    private Ads ads;
    private Player player;
    private List<Payment> payments;

    private List<String> search_hot;
    private String ui_home_topbg;
    private String ui_home_data;
    private String app_play_recommend;
    private String vip_close_ad;
    private String share_url;
    private String share_logo;
    private String sg;
    private String app_ui_home_small_icons;
    private String app_ui_tab_fun;
    private String app_cc_fd_jc;
    private String app_user_adurl;
    private String push;
    private String app_theme_ui;
    private String app_banner_style;
    private String app_home_ads;
    private String app_login_type;
    private String vip_purchase;
    private String online_recharge;

    private Conf conf;

    public Conf getConf() {
        return conf;
    }

    public void setConf(Conf conf) {
        this.conf = conf;
    }

    public Vodmark getVod_mark() {
        return vod_mark;
    }

    public void setVod_mark(Vodmark vod_mark) {
        this.vod_mark = vod_mark;
    }

    public String getApp_ui_home_small_icons() {
        return app_ui_home_small_icons;
    }

    public void setApp_ui_home_small_icons(String app_ui_home_small_icons) {
        this.app_ui_home_small_icons = app_ui_home_small_icons;
    }

    public String getApp_theme_ui() {
        return app_theme_ui;
    }

    public void setApp_theme_ui(String app_theme_ui) {
        this.app_theme_ui = app_theme_ui;
    }

    public String getApp_banner_style() {
        return app_banner_style;
    }

    public void setApp_banner_style(String app_banner_style) {
        this.app_banner_style = app_banner_style;
    }

    public String getApp_home_ads() {
        return app_home_ads;
    }

    public void setApp_home_ads(String app_home_ads) {
        this.app_home_ads = app_home_ads;
    }

    public String getApp_login_type() {
        return app_login_type;
    }

    public void setApp_login_type(String app_login_type) {
        this.app_login_type = app_login_type;
    }

    public String getVip_purchase() {
        return vip_purchase;
    }

    public void setVip_purchase(String vip_purchase) {
        this.vip_purchase = vip_purchase;
    }

    public String getOnline_recharge() {
        return online_recharge;
    }

    public void setOnline_recharge(String online_recharge) {
        this.online_recharge = online_recharge;
    }

    public String getApp_ui_tab_fun() {
        return app_ui_tab_fun;
    }

    public String getApp_cc_fd_jc() {
        return app_cc_fd_jc;
    }

    public void setApp_ui_tab_fun(String app_ui_tab_fun) {
        this.app_ui_tab_fun = app_ui_tab_fun;
    }

    public void setApp_cc_fd_jc(String app_cc_fd_jc) {
        this.app_cc_fd_jc = app_cc_fd_jc;
    }

    public String getApp_user_adurl() {
        return app_user_adurl;
    }

    public void setApp_user_adurl(String app_user_adurl) {
        this.app_user_adurl = app_user_adurl;
    }

    public String getPushS() {
        return push;
    }

    public void setPushS(String push) {
        this.push = push;
    }

    public String getApp_play_recommend() {
        return app_play_recommend;
    }

    public void setApp_play_recommend(String app_play_recommend) {
        this.app_play_recommend = app_play_recommend;
    }

    public String getVip_close_ad() {
        return vip_close_ad;
    }

    public void setVip_close_ad(String vip_close_ad) {
        this.vip_close_ad = vip_close_ad;
    }

    public String getUi_home_data() {
        return ui_home_data;
    }

    public void setUi_home_data(String ui_home_data) {
        this.ui_home_data = ui_home_data;
    }

    public String getSg() {
        return sg;
    }

    public void setSg(String sg) {
        this.sg = sg;
    }

    public String getShare_url() {
        return share_url;
    }

    public void setShare_url(String share_url) {
        this.share_url = share_url;
    }

    public String getShare_logo() {
        return share_logo;
    }

    public void setShare_logo(String share_logo) {
        this.share_logo = share_logo;
    }

    public List<String> getSearch_hot() {
        return search_hot;
    }

    public void setSearch_hot(List<String> search_hot) {
        this.search_hot = search_hot;
    }

    public String getUi_home_topbg() {
        return ui_home_topbg;
    }

    public void setUi_home_topbg(String ui_home_topbg) {
        this.ui_home_topbg = ui_home_topbg;
    }

    public Sign getSign() {
        return sign;
    }

    public void setSign(Sign sign) {
        this.sign = sign;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public Danmu getDanmu() {
        return danmu;
    }

    public void setDanmu(Danmu danmu) {
        this.danmu = danmu;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Ads getAds() {
        return ads;
    }

    public void setAds(Ads ads) {
        this.ads = ads;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public static class Payment implements Serializable {

        private static final long serialVersionUID = 7734695863148246513L;

        private String payment;
        private String name;
        private int status;

        public String getPayment() {
            return payment;
        }

        public void setPayment(String payment) {
            this.payment = payment;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    public static class Player implements Serializable {

        private static final long serialVersionUID = -5788614876100276279L;

        private String app_logo;

        public String getApp_logo() {
            return app_logo;
        }

        public void setApp_logo(String app_logo) {
            this.app_logo = app_logo;
        }
    }

    public static class Ads implements Serializable {

        private static final long serialVersionUID = -4076207452483830162L;
        private Ad user_center;
        private Ad searcher;
        private Ad player_pause;
        private Ad player_down;
        private Ad variety;
        private Ad cartoon;
        private Ad sitcom;
        private Ad vod;
        private Ad index;
        private Ad startup_adv;
        private Ad xunfei_appid;
        private Ad service_qq;
        private Ad service_qqqun;
        private Ad test;
        private Ad download;
        private Ad pictureinpicture;
        private Ad projection;
        private Ad define_account;

        //SDK广告开始
        //应用ID
        private Ad application_id;
        //开屏
        private Ad sdk_startup;
        //插屏
        private Ad sdk_Insertscreen;
        //首页插屏
        private Ad sdk_Insertscreens;
        //播放页插屏
        private Ad sdk_Insertscreenss;
        //底部按钮四直播和短视频切换
        private Ad sdk_ui_tab_fun;
        //关闭套餐购买
        private Ad ccvip_purchase;
        //关闭在线充值
        private Ad cconline_recharge;
        //会员中心样式
        private Ad sdk_theme_ui;
        //充值入口
        private Ad rechargeentry;
        //信息流
        private Ad sdk_informationflow;
        //sdk_banner
        private Ad sdk_banner;
        //视频信息流
        private Ad sdk_Videoinformationstream;
        //激励视频
        private Ad sdk_excitation;
        //全屏视频
        private Ad sdk_Fullscreenvideo;
        //SDK广告奖励
        private Ad sdk_nreward;
        private Ad share_description;
        private Ad play_logo;
        private Ad liuyan_message;
        private Ad home_logo;
        private Ad home_backg;
        private Ad home_backg_b;
        private Ad user_backg;
        private Ad search_feedback;
        private Ad live_vip;
        private Ad player_down_isvip;

        public Ad getLive_vip() {
            return live_vip;
        }

        public void setLive_vip(Ad live_vip) {
            this.live_vip = live_vip;
        }

        public Ad getSearch_feedback() {
            return search_feedback;
        }

        public void setSearch_feedback(Ad search_feedback) {
            this.search_feedback = search_feedback;
        }

        public Ad getUser_backg() {
            return user_backg;
        }

        public void setUser_backg(Ad user_backg) {
            this.user_backg = user_backg;
        }

        public Ad getHome_backg() {
            return home_backg;
        }

        public void setHome_backg(Ad home_backg) {
            this.home_backg = home_backg;
        }

        public Ad getHome_backg_b() {
            return home_backg_b;
        }

        public void setHome_backg_b(Ad home_backg_b) {
            this.home_backg_b = home_backg_b;
        }

        public Ad getHome_logo() {
            return home_logo;
        }

        public void setHome_logo(Ad home_logo) {
            this.home_logo = home_logo;
        }

        public Ad getLiuyan_message() {
            return liuyan_message;
        }

        public void setLiuyan_message(Ad liuyan_message) {
            this.liuyan_message = liuyan_message;
        }

        public Ad getSdk_Fullscreenvideo() {
            return sdk_Fullscreenvideo;
        }

        public void setSdk_Fullscreenvideo(Ad sdk_Fullscreenvideo) {
            this.sdk_Fullscreenvideo = sdk_Fullscreenvideo;
        }

        public Ad getSdk_nreward() {
            return sdk_nreward;
        }

        public void setSdk_nreward(Ad sdk_nreward) {
            this.sdk_nreward = sdk_nreward;
        }

        public Ad getSdk_informationflow() {
            return sdk_informationflow;
        }

        public void setSdk_informationflow(Ad sdk_informationflow) {
            this.sdk_informationflow = sdk_informationflow;
        }

        public Ad getSdk_banner() {
            return sdk_banner;
        }

        public void setSdk_banner(Ad sdk_banner) {
            this.sdk_banner = sdk_banner;
        }

        public Ad getPlayer_down_isvip() {
            return player_down_isvip;
        }

        public void setPlayer_down_isvip(Ad player_down_isvip) {
            this.player_down_isvip = player_down_isvip;
        }

        public Ad getdk_Videoinformationstream() {
            return sdk_Videoinformationstream;
        }

        public void setSdk_Videoinformationstream(Ad sdk_Videoinformationstream) {
            this.sdk_Videoinformationstream = sdk_Videoinformationstream;
        }
        public Ad getSdk_excitation() {
            return sdk_excitation;
        }

        public void setSdk_excitation(Ad sdk_excitation) {
            this.sdk_excitation = sdk_excitation;
        }

        public Ad getShare_description() {
            return share_description;
        }

        public void setShare_description(Ad share_description) {
            this.share_description = share_description;
        }

        public Ad getPlay_logo() {
            return play_logo;
        }

        public void setPlay_logo(Ad play_logo) {
            this.play_logo = play_logo;
        }

        public static long getSerialVersionUID() {
            return serialVersionUID;
        }

        public Ad getApplication_id() {
            return application_id;
        }

        public void setApplication_id(Ad application_id) {
            this.application_id = application_id;
        }

        public Ad getSdk_startup() {
            return sdk_startup;
        }

        public void setSdk_startup(Ad sdk_startup) {
            this.sdk_startup = sdk_startup;
        }

        public Ad getRechargeentry() {
            return rechargeentry;
        }

        public void setRechargeentry(Ad rechargeentry) {
            this.rechargeentry = rechargeentry;
        }

        public Ad getSdk_Insertscreen() {
            return sdk_Insertscreen;
        }
        //首页插屏
        public Ad getSdk_Insertscreens() {
            return sdk_Insertscreens;
        }
        //播放页插屏
        public Ad getSdk_Insertscreenss() {
            return sdk_Insertscreenss;
        }
        //底部按钮四直播和短视频切换
        public Ad getSdk_ui_tab_fun() {
            return sdk_ui_tab_fun;
        }
        //关闭套餐购买
        public Ad getCcvip_purchase() {
            return ccvip_purchase;
        }
        //关闭在线充值
        public Ad getCconline_recharge() {
            return cconline_recharge;
        }
        //会员中心样式
        public Ad getSdk_theme_ui() { return sdk_theme_ui; }

        public void setSdk_Insertscreen(Ad sdk_Insertscreen) {
            this.sdk_Insertscreen = sdk_Insertscreen;
        }
        public void setProjection(Ad projection) {
            this.projection = projection;
        }

        public Ad getProjection() {
            return projection;
        }

        public void setPictureinpicture(Ad pictureinpicture) {
            this.pictureinpicture = pictureinpicture;
        }

        public Ad getPictureinpicture() {
            return pictureinpicture;
        }

        public void setDownload(Ad download) {
            this.download = download;
        }

        public Ad getDownload() {
            return download;
        }

        public Ad getUser_center() {
            return user_center;
        }

        public void setUser_center(Ad user_center) {
            this.user_center = user_center;
        }

        public Ad getDefine_account() {
            return define_account;
        }

        public void setDefine_account(Ad define_account) {
            this.define_account = define_account;
        }

        public Ad getSearcher() {
            return searcher;
        }

        public void setSearcher(Ad searcher) {
            this.searcher = searcher;
        }

        public Ad getPlayer_pause() {
            return player_pause;
        }

        public void setPlayer_pause(Ad player_pause) {
            this.player_pause = player_pause;
        }

        public Ad getPlayer_down() {
            return player_down;
        }

        public void setPlayer_down(Ad player_down) {
            this.player_down = player_down;
        }

        public Ad getVariety() {
            return variety;
        }

        public void setVariety(Ad variety) {
            this.variety = variety;
        }

        public Ad getCartoon() {
            return cartoon;
        }

        public void setCartoon(Ad cartoon) {
            this.cartoon = cartoon;
        }

        public Ad getSitcom() {
            return sitcom;
        }

        public void setSitcom(Ad sitcom) {
            this.sitcom = sitcom;
        }

        public Ad getVod() {
            return vod;
        }

        public void setVod(Ad vod) {
            this.vod = vod;
        }

        public Ad getIndex() {
            return index;
        }

        public void setIndex(Ad index) {
            this.index = index;
        }

        public Ad getStartup_adv() {
            return startup_adv;
        }

        public void setStartup_adv(Ad startup_adv) {
            this.startup_adv = startup_adv;
        }

        public Ad getXunfei_appid() {
            return xunfei_appid;
        }

        public void setXunfei_appid(Ad xunfei_appid) {
            this.xunfei_appid = xunfei_appid;
        }

        public Ad getService_qq() {
            return service_qq;
        }

        public void setService_qq(Ad service_qq) {
            this.service_qq = service_qq;
        }

        public Ad getService_qqqun() {
            return service_qqqun;
        }

        public void setService_qqqun(Ad service_qqqun) {
            this.service_qqqun = service_qqqun;
        }

        public Ad getTest() {
            return test;
        }

        public void setTest(Ad test) {
            this.test = test;
        }

        public Ad getXtw_video2_adv() {
            return xtw_video2_adv;
        }
        private Ad xtw_video2_adv;
        public void setXtw_video2_adv(Ad xtw_video2_adv) {
            this.xtw_video2_adv = xtw_video2_adv;
        }
    }

    public static class Ad implements Serializable {

        private static final long serialVersionUID = 588907356034112780L;

        private int id;
        private String typename;
        private int status;
        private int sort;
        private String tag;
        private String description;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTypename() {
            return typename;
        }

        public void setTypename(String typename) {
            this.typename = typename;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

    public static class Document implements Serializable {

        private static final long serialVersionUID = 8078081983086676725L;

        private Register un_register;
        private Register registerd;
        private Register notice;
        private Register roll_notice;

        public Register getUn_register() {
            return un_register;
        }

        public void setUn_register(Register un_register) {
            this.un_register = un_register;
        }

        public Register getRegisterd() {
            return registerd;
        }

        public void setRegisterd(Register registerd) {
            this.registerd = registerd;
        }

        public Register getNotice() {
            return notice;
        }

        public void setNotice(Register notice) {
            this.notice = notice;
        }

        public Register getRoll_notice() {
            return roll_notice;
        }

        public void setRoll_notice(Register roll_notice) {
            this.roll_notice = roll_notice;
        }
    }

    public static class Register implements Serializable {

        private static final long serialVersionUID = 8588651912763025366L;

        private String status;
        private String title;
        private String content;
        private String type;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class Danmu implements Serializable {

        private static final long serialVersionUID = 4470900707347861579L;
        private Boolean os;
        private String status;
        private Reward reward;

        public Boolean getOs() {
            return os;
        }

        public void setOs(Boolean os) {
            this.os = os;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Reward getReward() {
            return reward;
        }

        public void setReward(Reward reward) {
            this.reward = reward;
        }
    }

    public static class Comment implements Serializable {

        private static final long serialVersionUID = 4592326999981178806L;

        private String status;
        private Reward reward;
        private String reward_num;
        private String sg;

        public String getSg() {
            return sg;
        }

        public void setSg(String sg) {
            this.sg = sg;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Reward getReward() {
            return reward;
        }

        public void setReward(Reward reward) {
            this.reward = reward;
        }

        public String getReward_num() {
            return reward_num;
        }

        public void setReward_num(String reward_num) {
            this.reward_num = reward_num;
        }
    }

    public static class Vodmark implements Serializable {

        private static final long serialVersionUID = 8033475746385964499L;
        private String status;
        private Reward reward;
        private String reward_num;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Reward getReward() {
            return reward;
        }

        public void setReward(Reward reward) {
            this.reward = reward;
        }

        public String getReward_num() {
            return reward_num;
        }

        public void setReward_num(String reward_num) {
            this.reward_num = reward_num;
        }
    }

    public static class Conf implements Serializable {

        private static final long serialVersionUID = 8033475746385964499L;
        private String umeng;
        private String sdkid;

        public String getUmeng() {
            return umeng;
        }

        public void setUmeng(String umeng) {
            this.umeng = umeng;
        }

        public String getSdkid() {
            return sdkid;
        }

        public void setSdkid(String sdkid) {
            this.sdkid = sdkid;
        }
    }

    public static class Sign implements Serializable {

        private static final long serialVersionUID = 6618705643876600439L;

        private String status;
        private Reward reward;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Reward getReward() {
            return reward;
        }

        public void setReward(Reward reward) {
            this.reward = reward;
        }
    }

    public static class Reward implements Serializable {

        private static final long serialVersionUID = -6154405135159596170L;

        private String points;

        public String getPoints() {
            return points;
        }

        public void setPoints(String points) {
            this.points = points;
        }
    }

}
