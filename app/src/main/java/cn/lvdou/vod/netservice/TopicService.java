package cn.lvdou.vod.netservice;

import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.bean.PageResult;
import cn.lvdou.vod.bean.SpecialtTopicBean;
import io.reactivex.Observable;
import retrofit2.http.GET;

public interface TopicService {
    @GET(ApiConfig.getTopicList)
    Observable<PageResult<SpecialtTopicBean>> getTopicList();
}
