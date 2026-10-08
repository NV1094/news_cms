package cn.lvdou.vod.netservice;

import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.bean.BaseResult;
import cn.lvdou.vod.bean.Page;
import cn.lvdou.vod.bean.PlayLogBean;
import cn.lvdou.vod.bean.TypeBean;
import cn.lvdou.vod.bean.PageResult;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TypeService {
    @GET(ApiConfig.getTypeList)
    Observable<PageResult<TypeBean>> getTypeList();

    @GET(ApiConfig.getPlayLogList)
    Observable<BaseResult<Page<PlayLogBean>>> getPlayLogList(@Query("page") String page, @Query("limit") String limit);
}
