package com.lemo.emojcenter.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zhy.http.okhttp.callback.ReturnCallback;
import com.zhy.http.okhttp.constant.Constant;
import com.zhy.http.okhttp.utils.Convert;
import com.zhy.http.okhttp.utils.ForbidException;
import com.zhy.http.okhttp.utils.ResponseExcepiton;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Response;


/**
 * @author 王兴岭
 * @desc gson 转换器
 * @email wxlhdm@qq.com
 * @create 2017/6/6 14:05
 **/
public class GsonReturnCallBack<T> extends ReturnCallback<T> {
    @Override
    public T parseNetworkResponse(Response response, int id) throws IOException {

        Type type = checkType();
        String string = response.body().string();
        JsonParser parser = new JsonParser();
        JsonElement parse = parser.parse(string);
        JsonObject jsonObject = parse.getAsJsonObject();
        int code = jsonObject.get(Constant.CODE).getAsInt();
        if (code == 75000) {
            throw new ForbidException("该账号已被封停");
        }
        String msg = jsonObject.get(Constant.MSG).getAsString();
        if (code == Constant.SUCCESS_CODE) {
            String data = jsonObject.get("result").toString();
            T result = Convert.fromJson(data, type);
            return result;
        } else {
            throw new ResponseExcepiton(msg, code);
        }


    }

    @Override
    public void onError(Call call, Exception e, int id) {

    }

    public void onResponse(T response, int id) {

    }
}
