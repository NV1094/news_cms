package cn.lvdou.vod.bean;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import cn.lvdou.vod.ApiConfig;

public class PlayScoreBean extends LitePalSupport {

    private int vodId;
    private int typeId;
    private int id;
    private String vodName;
    private String vodscore;
    private String vodImgUrl;
    private String vodarea;
    private String vodclass;
    private String vodcontent;
    private String vodSelectedWorks;
    private float percentage;
    private long curProgress;
    private int urlIndex;
    private int playSourceIndex;
    @Column(ignore = true)
    private boolean isSelect;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public long getCurProgress() {
        return curProgress;
    }

    public void setCurProgress(long curProgress) {
        this.curProgress = curProgress;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public String getVodSelectedWorks() {
        return vodSelectedWorks;
    }

    public void setVodSelectedWorks(String vodSelectedWorks) {
        this.vodSelectedWorks = vodSelectedWorks;
    }

    public String getVodName() {
        return vodName;
    }

    public void setVodName(String vodName) {
        this.vodName = vodName;
    }

    public String getVodscore() {
        return vodscore;
    }

    public void setVodscore(String vodscore) {
        this.vodscore = vodscore;
    }

    public String getVodarea(){
        return vodarea;
    }

    public String getVodclass() {
        return vodclass;
    }

    public void setVodclass(String vodclass) {
        this.vodclass = vodclass;
    }

    public void setVodarea(String vodarea){
        this.vodarea = vodarea;
    }

    public String getVodcontent(){
        return vodcontent;
    }

    public void setVodcontent(String vodcontent){
        this.vodcontent = vodcontent;
    }

    public void setVod_pic(String vodImgUrl) {
        this.vodImgUrl = vodImgUrl;
    }

    public String getRealPic(String u){
        // u="mac://ae01.alicdn.com/kf/Uf81c9d5172164aa7b91e9c5b63da0f44a.jpg";
        if (u.startsWith("/")){
            return ApiConfig.MOGAI_BASE_URL+u;
        }else if (u.startsWith("mac")){
            return u.replaceAll("^mac","http");
        }else if (!u.startsWith("http") && !u.equals("") &&!u.isEmpty()){
            return ApiConfig.MOGAI_BASE_URL+"/"+u;
        } else {
            return u;
        }
        //  return "";
    }

    public String getVodImgUrl() {
        setVod_pic(getRealPic(vodImgUrl));
        return vodImgUrl;
    }

    public void setVodImgUrl(String vodImgUrl) {
        this.vodImgUrl = vodImgUrl;
    }

    public int getVodId() {
        return vodId;
    }

    public void setVodId(int vodId) {
        this.vodId = vodId;
    }

    public int getUrlIndex() {
        return urlIndex;
    }

    public void setUrlIndex(int urlIndex) {
        this.urlIndex = urlIndex;
    }

    public int getPlaySourceIndex() {
        return playSourceIndex;
    }

    public void setPlaySourceIndex(int playSourceIndex) {
        this.playSourceIndex = playSourceIndex;
    }
}
