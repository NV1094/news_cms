package cn.lvdou.vod.netservice;

import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.bean.LiveBean;
import cn.lvdou.vod.bean.PageResult;
import io.reactivex.Observable;
import retrofit2.http.GET;

public interface LiveService {
    @GET(ApiConfig.getLiveList)
    Observable<PageResult<LiveBean>> getLiveList();
}
