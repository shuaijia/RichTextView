package com.jia.xunfeidemo.net;

import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Describtion:
 * Created by jia on 2017/6/6.
 * 人之所以能，是相信能
 */
public interface BaseService {

    @POST("api/trans/vip/translate")
    Observable<TransModel> trans(@Query("q") String q, @Query("from") String from, @Query("to") String to,
                                 @Query("appid") String appid, @Query("salt") String salt, @Query("sign") String sign);

}
