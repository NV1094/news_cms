package cn.lvdou.vod.netservice;


import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.bean.CardBean;
import cn.lvdou.vod.bean.PageResult;
import cn.lvdou.vod.bean.RecommendBean2;
import cn.lvdou.vod.bean.NewRecommendBean2;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CardService {
    @GET(ApiConfig.getCardList)
    Observable<PageResult<CardBean>> getCardList(@Query("need_vod") boolean need_vod);
//
//    @GET(ApiConfig.getRecommendList)
//    Observable<PageResult<RecommendBean2>> getRecommendList();

    @GET(ApiConfig.getRecommendList)
    Observable<NewRecommendBean2> newGetRecommendList();
}
