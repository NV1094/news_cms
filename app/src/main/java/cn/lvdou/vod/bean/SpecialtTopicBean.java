package cn.lvdou.vod.bean;

import cn.lvdou.vod.ApiConfig;

public class SpecialtTopicBean {
   String topic_id;

    public String getToppic_id() {
        return topic_id;
    }

    public void setTopic_id(String topic_id) {
        this.topic_id = topic_id;
    }

    public String getTopic_name() {
        return topic_name;
    }

    public void setTopic_name(String topic_name) {
        this.topic_name = topic_name;
    }

    public String getTopic_pic() {
        //修复“图片资源无法获取”：与 VodBean/CollectionBean 一致，相对路径需补服务端基址，
        //否则 Glide 拿到的相对路径无法解析（http 开头原样返回，重复调用幂等）
        topic_pic = getRealPic(topic_pic);
        return topic_pic;
    }

    //图片基址归一（与 VodBean.getRealPic 同逻辑的空值安全版）
    public String getRealPic(String u) {
        if (u == null || u.isEmpty()) {
            return "";
        }
        if (u.startsWith("/")) {
            return ApiConfig.MOGAI_BASE_URL + u;
        } else if (u.startsWith("mac")) {
            return u.replaceAll("^mac", "http");
        } else if (!u.startsWith("http")) {
            return ApiConfig.MOGAI_BASE_URL + "/" + u;
        } else {
            return u;
        }
    }

    public void setTopic_pic(String topic_pic) {
        this.topic_pic = topic_pic;
    }

    public String getTopic_blurb() {
        return topic_blurb;
    }

    public void setTopic_blurb(String topic_blurb) {
        this.topic_blurb = topic_blurb;
    }

    public String getTopic_content() {
        return topic_content;
    }

    public void setTopic_content(String topic_content) {
        this.topic_content = topic_content;
    }

    String topic_name;
   String topic_pic;
   String topic_blurb;
   String topic_content;

}
