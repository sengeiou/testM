package com.mogujie.tt.db.entity;

import android.support.annotation.NonNull;
import com.mogujie.tt.protobuf.helper.EntityChangeEngine;

import java.io.Serializable;

/**
 * @author : yingmu on 15-3-25.
 * @email : yingmu@mogujie.com.
 *
 * 聊天对象抽象类  may be user/group
 */
public abstract class PeerEntity implements Serializable{
    private static final long serialVersionUID = -7506882071807803834L;
    protected Long id;
    protected int peerId;
    /** Not-null value.
     * userEntity --> nickName
     * groupEntity --> groupName
     * */
    protected String mainName;
    /** Not-null value.*/
    protected String avatar;
    protected int created;
    protected int updated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public String getMainName() {
        return mainName;
    }

    public void setMainName(String mainName) {
        this.mainName = mainName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    // peer就能生成sessionKey
    public String getSessionKey(){
       return EntityChangeEngine.getSessionKey(peerId,getType());
    }

    public abstract int getType();

    @Override
    public String toString() {
        return "PeerEntity{" +
        "id=" + id +
        ", peerId=" + peerId +
        ", mainName='" + mainName + '\'' +
        ", avatar='" + avatar + '\'' +
        ", created=" + created +
        ", updated=" + updated +
        '}';
    }

    @NonNull
    public static PeerEntity getPeerEntity(String sessionKey) {
        PeerEntity peerEntity;
        String[] sessionInfo = EntityChangeEngine.spiltSessionKey(sessionKey);
        int peerType = Integer.parseInt(sessionInfo[0]);
        int peerId = Integer.parseInt(sessionInfo[1]);
        if (peerType == 1) {
            peerEntity = new UserEntity();
            peerEntity.setPeerId(peerId);
        } else {
            peerEntity = new GroupEntity();
            peerEntity.setPeerId(peerId);
        }
        return peerEntity;
    }
}
