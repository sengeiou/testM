package com.qingmeng.mengmeng.base

import com.qingmeng.mengmeng.entity.BaseBean
import com.qingmeng.mengmeng.entity.MyMyFollowBean
import io.reactivex.Observable
import retrofit2.http.*

/**
 * Created by zq on 2018/8/13
 */
interface Api {

    //我的关注
    @GET("api_my/get_attention")
    fun myFollow(@Query("userId") userId: Int, @Query("pageNum") pageNum: Int): Observable<BaseBean<MyMyFollowBean>>

    //修改密码
    @POST("app/user/update_password")
    @FormUrlEncoded
    fun updatePass(@Field("password") password: String, @Field("newPassword") newPassword: String, @Field("verifyPassword") verifyPassword: String, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>
}