package cn.lvdou.vod.netservice;

import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.bean.VodBean;
import cn.lvdou.vod.bean.PageResult;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ScreenService {

    @GET(ApiConfig.getVodList)
    Observable<PageResult<VodBean>> getVodList(@Query("type") int type, @Query("class") String zlass, @Query("lang") String lang, @Query("area") String area, @Query("year") String year,@Query("by") String by, @Query("page") int page, @Query("limit") int limit, @Query("apikey") String apikey, @Query("keytime") String keytime);

}
