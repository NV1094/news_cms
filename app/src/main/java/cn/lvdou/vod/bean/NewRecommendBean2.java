package cn.lvdou.vod.bean;

import java.util.List;

/**
 * @author : yjz
 * @date : 2020/10/29 17:21
 * @des :
 */
public class NewRecommendBean2 {

    private int code;
    private String msg;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {

        private List<RecommendBean2> zhui;
        private List<RecommendBean2> list;

        public List<RecommendBean2> getZhui() {
            return zhui;
        }

        public void setZhui(List<RecommendBean2> zhui) {
            this.zhui = zhui;
        }

        public List<RecommendBean2> getList() {
            return list;
        }

        public void setList(List<RecommendBean2> list) {
            this.list = list;
        }
    }
}
