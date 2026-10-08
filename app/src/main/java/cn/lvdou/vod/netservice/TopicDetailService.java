package cn.lvdou.vod.netservice;

import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.bean.BaseResult;
import cn.lvdou.vod.bean.TopicDetailBean;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TopicDetailService {
    @GET(ApiConfig.getTopicDetail)
    Observable<BaseResult<TopicDetailBean>> getTopicDetail(@Query("topic_id") String topic_id);
}
