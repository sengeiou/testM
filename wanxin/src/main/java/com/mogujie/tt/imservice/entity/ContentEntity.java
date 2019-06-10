package com.mogujie.tt.imservice.entity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.mogujie.tt.utils.BooleanTypeAdapter;

/**
 * 扩展消息
 * Created by wangru
 * Date: 2018/3/14  10:41
 * mail: 1902065822@qq.com
 * describe:
 */

public class ContentEntity {
    /**
     * infoType:0
     * extInfo : {}
     * info : "你好"
     */
    @SerializedName("infoType")
    private int infoType;
    //原来的消息
    @SerializedName("info")
    private String info;
    @SerializedName("title")
    private String title;
    @SerializedName("nickname")
    private String nickname;
    //扩展消息
    @SerializedName("extInfo")
    private JsonObject extInfo;
    //透传消息
    @JsonAdapter(BooleanTypeAdapter.class)
    @SerializedName("special")
    private boolean special;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public JsonObject getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(JsonObject extInfo) {
        this.extInfo = extInfo;
    }

    public int getInfoType() {
        return infoType;
    }

    public void setInfoType(int infoType) {
        this.infoType = infoType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this).toString();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }
}
