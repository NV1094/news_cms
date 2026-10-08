package cn.lvdou.vod.ui.home;

import java.io.Serializable;

import cn.lvdou.vod.bean.ExtendBean;

public interface Type extends Serializable {

    String getTypeName();

    int getTypeId();

    ExtendBean getExtend();
}
