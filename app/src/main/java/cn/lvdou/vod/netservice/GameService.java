package cn.lvdou.vod.netservice;

import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.bean.GameBean;
import cn.lvdou.vod.bean.PageResult;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GameService {
    @GET(ApiConfig.getGameList)
    Observable<PageResult<GameBean>> getGameList(@Query("limit") String limit, @Query("page") String page,@Query("size") String size);
}
