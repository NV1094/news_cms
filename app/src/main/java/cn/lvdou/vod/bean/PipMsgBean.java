package cn.lvdou.vod.bean;

import java.io.Serializable;

/**
 * 画中画当前播放数据
 */
public class PipMsgBean implements Serializable {
    private String voidid = "";
    private String vodSelectedWorks = "";
    private String playSource = "";
    private String percentage = "";
    private int urlIndex = 0;
    private int playSourceIndex = 0;
    private Long curPregress = 0L;

    public Long getCurPregress() {
        return curPregress;
    }

    public void setCurPregress(Long curPregress) {
        this.curPregress = curPregress;
    }

    public int getPlaySourceIndex() {
        return playSourceIndex;
    }

    public void setPlaySourceIndex(int playSourceIndex) {
        this.playSourceIndex = playSourceIndex;
    }

    public String getVoidid() {
        return voidid;
    }

    public void setVoidid(String voidid) {
        this.voidid = voidid;
    }

    public String getVodSelectedWorks() {
        return vodSelectedWorks;
    }

    public void setVodSelectedWorks(String vodSelectedWorks) {
        this.vodSelectedWorks = vodSelectedWorks;
    }

    public String getPlaySource() {
        return playSource;
    }

    public void setPlaySource(String playSource) {
        this.playSource = playSource;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public int getUrlIndex() {
        return urlIndex;
    }

    public void setUrlIndex(int urlIndex) {
        this.urlIndex = urlIndex;
    }
}
