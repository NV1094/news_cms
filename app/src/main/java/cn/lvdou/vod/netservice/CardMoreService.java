package cn.lvdou.vod.netservice;


import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.bean.TypeBean;
import cn.lvdou.vod.bean.BaseResult;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CardMoreService {
    @GET(ApiConfig.getCardListByType)
    Observable<BaseResult<TypeBean>> getCardListByType(@Query("type_id") int type_id);
}
