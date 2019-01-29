package com.mogujie.tt.db.entity;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import com.mogujie.tt.protobuf.helper.EntityChangeEngine;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
// KEEP INCLUDES END

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Entity mapped to table Session.
 */
@Entity(nameInDb = "SessionDao")
public class SessionEntity implements Serializable{

    private static final long serialVersionUID = -4410006864508400999L;
    @Id(autoincrement = true)
    private Long id;
    /** Not-null value. */
    @Unique
    @NotNull
    private String sessionKey;
    @NotNull
    private int peerId;
    @NotNull
    private int peerType;
    @NotNull
    private int latestMsgType;
    @NotNull
    private int latestMsgId;
    /** Not-null value. */
    @NotNull
    private String latestMsgData;
    @NotNull
    private int talkId;
    @NotNull
    private int created;
    @NotNull
    private int updated;
    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public SessionEntity() {
    }

    public SessionEntity(Long id) {
        this.id = id;
    }

    @Keep
    public SessionEntity(Long id, @NotNull String sessionKey, int peerId, int peerType, int latestMsgType, int latestMsgId, @NotNull String latestMsgData, int talkId, int created,
            int updated) {
        this.id = id;
        this.sessionKey = sessionKey;
        this.peerId = peerId;
        this.peerType = peerType;
        this.latestMsgType = latestMsgType;
        this.latestMsgId = latestMsgId;
        this.latestMsgData = latestMsgData;
        this.talkId = talkId;
        this.created = created;
        this.updated = updated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getSessionKey() {
        return sessionKey;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getPeerType() {
        return peerType;
    }

    public void setPeerType(int peerType) {
        this.peerType = peerType;
    }

    public int getLatestMsgType() {
        return latestMsgType;
    }

    public void setLatestMsgType(int latestMsgType) {
        this.latestMsgType = latestMsgType;
    }

    public int getLatestMsgId() {
        return latestMsgId;
    }

    public void setLatestMsgId(int latestMsgId) {
        this.latestMsgId = latestMsgId;
    }

    /** Not-null value. */
    public String getLatestMsgData() {
        return latestMsgData;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setLatestMsgData(String latestMsgData) {
        this.latestMsgData = latestMsgData;
    }

    public int getTalkId() {
        return talkId;
    }

    public void setTalkId(int talkId) {
        this.talkId = talkId;
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

    // KEEP METHODS - put your custom methods here
    @Keep
    public String buildSessionKey(){
        if(peerType <=0 || peerId <=0){
            throw new IllegalArgumentException(
                    "SessionEntity buildSessionKey error,cause by some params <=0");
        }
        sessionKey = EntityChangeEngine.getSessionKey(peerId, peerType);
        return sessionKey;
    }
    // KEEP METHODS END


    @Override
    public String toString() {
        return "SessionEntity{" +
        "id=" + id +
        ", sessionKey='" + sessionKey + '\'' +
        ", peerId=" + peerId +
        ", peerType=" + peerType +
        ", latestMsgType=" + latestMsgType +
        ", latestMsgId=" + latestMsgId +
        ", latestMsgData='" + latestMsgData + '\'' +
        ", talkId=" + talkId +
        ", created=" + created +
        ", updated=" + updated +
        '}';
    }
}