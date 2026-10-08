package cn.lvdou.vod.bean;

public class AdConfigBean {

        private String ad_select;
        private html html;
        private video video;

        public html getHtml(){return html;}
        public String getAd_select() {
            return ad_select;
        }
        public video getVideo() {
            return video;
        }

        public void setAd_select(String s){this.ad_select = s;}
        public void setHtml(html h){this.html = h;}
        public void setVideo(video v){this.video = v;}
}
class html {
    private String code;
    private int timeout;
}
class video{
    private String url;
    private String click_url;
}