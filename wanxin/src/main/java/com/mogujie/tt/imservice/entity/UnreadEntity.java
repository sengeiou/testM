package com.mogujie.tt.imservice.entity;

import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mogujie.tt.protobuf.helper.EntityChangeEngine;

/**
 * @author : yingmu on 15-1-6.
 * @email : yingmu@mogujie.com.
 * <p>
 * 未读session实体，并未保存在DB中
 */
public class UnreadEntity {
    private String sessionKey;
    private int peerId;
    private int sessionType;
    private int unReadCnt;
    private int laststMsgId;
    private String latestMsgData;
    private boolean isForbidden = false;

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }

    public int getUnReadCnt() {
        return unReadCnt;
    }

    public void setUnReadCnt(int unReadCnt) {
        this.unReadCnt = unReadCnt;
    }

    public int getLaststMsgId() {
        return laststMsgId;
    }

    public void setLaststMsgId(int laststMsgId) {
        this.laststMsgId = laststMsgId;
    }

    public String getLatestMsgData() {
        return latestMsgData;
    }

    public void setLatestMsgData(String latestMsgData) {
        this.latestMsgData = latestMsgData;
    }

    public boolean isForbidden() {
        return isForbidden;
    }

    public void setForbidden(boolean isForbidden) {
        this.isForbidden = isForbidden;
    }

    @Override
    public String toString() {
        return "UnreadEntity{" +
        "sessionKey='" + sessionKey + '\'' +
        ", peerId=" + peerId +
        ", sessionType=" + sessionType +
        ", unReadCnt=" + unReadCnt +
        ", laststMsgId=" + laststMsgId +
        ", latestMsgData='" + latestMsgData + '\'' +
        ", isForbidden=" + isForbidden +
        '}';
    }

    public String buildSessionKey() {
        if (sessionType <= 0 || peerId <= 0) {
            throw new IllegalArgumentException(
            "SessionEntity buildSessionKey error,cause by some params <=0");
        }
        sessionKey = EntityChangeEngine.getSessionKey(peerId, sessionType);
        return sessionKey;
    }

    //获取消息内容
    public String getInfo() {
        if (!TextUtils.isEmpty(latestMsgData)) {
            try {
                ContentEntity contentEntity = new Gson().fromJson(latestMsgData, ContentEntity.class);
                if (contentEntity != null) {
                    return contentEntity.getInfo();
                }
            } catch (JsonSyntaxException e) {
                Log.d(getClass().getSimpleName(),"isn't json："+latestMsgData);
            }
        }
        return latestMsgData;
    }

    //获取昵称
    public String getNickName() {
        if (!TextUtils.isEmpty(latestMsgData)) {
            try {
                ContentEntity contentEntity = new Gson().fromJson(latestMsgData, ContentEntity.class);
                if (contentEntity != null) {
                    return contentEntity.getNickname();
                }
            } catch (JsonSyntaxException e) {
                Log.d(getClass().getSimpleName(),"isn't json："+latestMsgData);
            }
        }
        return latestMsgData;
    }
}
