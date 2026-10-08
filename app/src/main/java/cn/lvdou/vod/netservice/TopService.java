package cn.lvdou.vod.netservice;

import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.bean.VodBean;
import cn.lvdou.vod.bean.PageResult;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TopService {
    @GET(ApiConfig.getTopList)
    Observable<PageResult<VodBean>> getTopList(@Query("request_type") String request_type, @Query("limit") int limit, @Query("page") int page, @Query("apikey") String apikey, @Query("keytime") String keytime);

    @GET(ApiConfig.getTopList)
    Observable<PageResult<VodBean>> getTopList(@Query("type") int type_id, @Query("request_type") String request_type, @Query("limit") int limit, @Query("page") int page, @Query("apikey") String apikey, @Query("keytime") String keytime);
}
