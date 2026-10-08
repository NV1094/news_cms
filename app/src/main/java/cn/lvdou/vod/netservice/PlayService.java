package cn.lvdou.vod.netservice;

import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.bean.CommentBean;
import cn.lvdou.vod.bean.VodBean;
import cn.lvdou.vod.bean.PageResult;
import cn.lvdou.vod.bean.BaseResult;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlayService {

    @GET(ApiConfig.getVod)
    Observable<BaseResult<VodBean>> getVod(@Query("vod_id") int vod_id, @Query("rel_limit") int rel_limit,@Query("apikey") String apikey, @Query("keytime") String keytime);

    @GET(ApiConfig.getVodList)
    Observable<PageResult<VodBean>> getSameTypeList(@Query("type") int type, @Query("class") String zlass, @Query("page") int page, @Query("limit") int limit,@Query("apikey") String apikey, @Query("keytime") String keytime);

    @GET(ApiConfig.getVodList)
    Observable<PageResult<VodBean>> getSameActorList(@Query("type") int type, @Query("actor") String actor,  @Query("page") int page, @Query("limit") int limit,@Query("apikey") String apikey, @Query("keytime") String keytime);

    @GET(ApiConfig.COMMENT)
    Observable<PageResult<CommentBean>> getCommentList(@Query("rid") int type, @Query("mid") String mid, @Query("page") int page, @Query("limit") int limit);

}