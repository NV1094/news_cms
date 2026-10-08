package cn.lvdou.vod.bean;

import java.util.List;

public class DanMuBean {

    /**
     * limit : 30
     * start : null
     * list : [{"danmu_id":36,"content":"你好好啊","vod_id":1553637,"at_time":2147483647,"user_id":72,"danmu_time":1623476356,"status":1,"dianzan_num":0},{"danmu_id":35,"content":"你好啊","vod_id":1553637,"at_time":2147483647,"user_id":72,"danmu_time":1623476342,"status":1,"dianzan_num":0},{"danmu_id":34,"content":"好的的的","vod_id":1553637,"at_time":2147483647,"user_id":72,"danmu_time":1623475833,"status":1,"dianzan_num":0},{"danmu_id":32,"content":"你好啊哈哈","vod_id":1553637,"at_time":2147483647,"user_id":72,"danmu_time":1623475472,"status":1,"dianzan_num":0},{"danmu_id":30,"content":"弄好啊","vod_id":1553637,"at_time":2147483647,"user_id":72,"danmu_time":1623474746,"status":1,"dianzan_num":0},{"danmu_id":27,"content":"萝卜啊","vod_id":1553637,"at_time":2147483647,"user_id":72,"danmu_time":1623471788,"status":1,"dianzan_num":0},{"danmu_id":26,"content":"你好啊","vod_id":1553637,"at_time":2147483647,"user_id":72,"danmu_time":1623471681,"status":1,"dianzan_num":0},{"danmu_id":25,"content":"哈哈","vod_id":1553637,"at_time":2147483647,"user_id":5,"danmu_time":1623468432,"status":1,"dianzan_num":0},{"danmu_id":24,"content":"哈哈","vod_id":1553637,"at_time":2147483647,"user_id":5,"danmu_time":1623468293,"status":1,"dianzan_num":0},{"danmu_id":23,"content":"你好啊朋友","vod_id":1553637,"at_time":2147483647,"user_id":72,"danmu_time":1623468237,"status":1,"dianzan_num":0},{"danmu_id":22,"content":"你好","vod_id":1553637,"at_time":2147483647,"user_id":72,"danmu_time":1623468203,"status":1,"dianzan_num":0},{"danmu_id":21,"content":"什么情况","vod_id":1553637,"at_time":2147483647,"user_id":1,"danmu_time":1623416737,"status":1,"dianzan_num":0},{"danmu_id":20,"content":"vvvv吧广告v","vod_id":1553637,"at_time":2147483647,"user_id":1,"danmu_time":1623416656,"status":1,"dianzan_num":0},{"danmu_id":19,"content":"非常潮菜馆","vod_id":1553637,"at_time":2147483647,"user_id":1,"danmu_time":1623416627,"status":1,"dianzan_num":0},{"danmu_id":18,"content":"给vv个","vod_id":1553637,"at_time":2147483647,"user_id":1,"danmu_time":1623416590,"status":1,"dianzan_num":0},{"danmu_id":48,"content":"大家好哈哈","vod_id":1553637,"at_time":2147483647,"user_id":5,"danmu_time":1623774040,"status":1,"dianzan_num":0}]
     */

    private String limit;
    private Object start;
    private List<ListBean> list;

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public Object getStart() {
        return start;
    }

    public void setStart(Object start) {
        this.start = start;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        /**
         * danmu_id : 36
         * content : 你好好啊
         * vod_id : 1553637
         * at_time : 2147483647
         * user_id : 72
         * danmu_time : 1623476356
         * status : 1
         * dianzan_num : 0
         */

        private int danmu_id;
        private String content;
        private int vod_id;
        private long at_time;
        private int user_id;
        private long danmu_time;
        private int status;
        private int dianzan_num;
        private String color;

        public int getDanmu_id() {
            return danmu_id;
        }

        public void setDanmu_id(int danmu_id) {
            this.danmu_id = danmu_id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getVod_id() {
            return vod_id;
        }

        public void setVod_id(int vod_id) {
            this.vod_id = vod_id;
        }

        public long getAt_time() {
            return at_time;
        }

        public void setAt_time(long at_time) {
            this.at_time = at_time;
        }

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public long getDanmu_time() {
            return danmu_time;
        }

        public void setDanmu_time(long danmu_time) {
            this.danmu_time = danmu_time;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getDianzan_num() {
            return dianzan_num;
        }

        public void setDianzan_num(int dianzan_num) {
            this.dianzan_num = dianzan_num;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }
}
