package cn.lvdou.vod.netservice;

import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.bean.VodBean;
import cn.lvdou.vod.bean.PageResult;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BannerService {

    @GET(ApiConfig.getBannerList)
    Observable<PageResult<VodBean>> getBannerList(@Query("level") int level, @Query("apikey") String apikey, @Query("keytime") String keytime);

    @GET(ApiConfig.getBannerList)
    Observable<PageResult<VodBean>> getBannerList(@Query("type") int type_id, @Query("level") int level, @Query("apikey") String apikey, @Query("keytime") String keytime);
}
