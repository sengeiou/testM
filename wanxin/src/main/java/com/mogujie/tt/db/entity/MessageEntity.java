package com.mogujie.tt.db.entity;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mogujie.tt.config.DBConstant;
import com.mogujie.tt.config.MessageConstant;
import com.mogujie.tt.imservice.entity.BrandMessage;
import com.mogujie.tt.imservice.entity.ContentEntity;
import com.mogujie.tt.imservice.entity.ImageMessage;
import com.mogujie.tt.imservice.entity.TextMessage;
import com.mogujie.tt.imservice.entity.VideoMessage;
import com.mogujie.tt.protobuf.helper.EntityChangeEngine;
import com.mogujie.tt.utils.ObjectTrans;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 这个类不同与其他自动生成代码
 * 需要依赖conten与display 依赖不同的状态
 */
// KEEP INCLUDES END

/**
 * Entity mapped to table Message.
 */
@Entity(nameInDb = "MessageDao",
indexes = {
@Index(value = "msgId"),
@Index(value = "sessionKey")
}
)
public class MessageEntity implements Serializable {
    private static final long serialVersionUID = -2719594838594429816L;
    @Id(autoincrement = true)
    protected Long id;
    //消息ID
    @NotNull
    protected int msgId;
    //发送用户的id
    @NotNull
    protected int fromId;
    //接收消息用户id
    @NotNull
    protected int toId;
    @NotNull
    protected String sessionKey;
    //消息内容，如果是语音消息则存储语音id。
    @NotNull
    protected String content;
    //类型，文本or语音
    @NotNull
    protected int msgType;
    @NotNull
    protected int displayType;
    //状态 '0正常 1被删除'
    @NotNull
    @Index
    protected int status;
    //创建时间
    @NotNull
    @Index
    protected int created;
    //更新时间
    @NotNull
    protected int updated;

    // KEEP FIELDS - put your custom fields here
    @Transient
    protected boolean isGIfEmo;
    // KEEP FIELDS END

    //不生成
    @Transient
    protected HashMap<String, Object> extMap = new HashMap<String, Object>();
    @Transient
    protected String info;
    @Transient
    protected int infoType;
    @Transient
    protected String nickname;
    @Transient
    protected boolean special;

    {
        setAttribute("time", System.currentTimeMillis());
    }

    public MessageEntity() {
    }

    public MessageEntity(Long id) {
        this.id = id;
    }

    @Keep
    public MessageEntity(Long id, int msgId, int fromId, int toId, String sessionKey, String content, int msgType, int displayType, int status, int created, int updated) {
        this.id = id;
        this.msgId = msgId;
        this.fromId = fromId;
        this.toId = toId;
        this.sessionKey = sessionKey;
        this.content = content;
        this.msgType = msgType;
        this.displayType = displayType;
        this.status = status;
        this.created = created;
        this.updated = updated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }

    /**
     * Not-null value.
     */
    public String getSessionKey() {
        return sessionKey;
    }

    /**
     * Not-null value; ensure this value is available before it is saved to the database.
     */
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    /**
     * Not-null value; ensure this value is available before it is saved to the database.
     */
    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getDisplayType() {
        return displayType;
    }

    public void setDisplayType(int displayType) {
        this.displayType = displayType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    /**
     * -----根据自身状态判断的---------
     */
    @Keep
    public int getSessionType() {
        switch (msgType) {
            case DBConstant.MSG_TYPE_SINGLE_TEXT:
            case DBConstant.MSG_TYPE_SINGLE_AUDIO:
                return DBConstant.SESSION_TYPE_SINGLE;
            case DBConstant.MSG_TYPE_GROUP_TEXT:
            case DBConstant.MSG_TYPE_GROUP_AUDIO:
                return DBConstant.SESSION_TYPE_GROUP;
            default:
                //todo 有问题
                return DBConstant.SESSION_TYPE_SINGLE;
        }
    }

    @Keep
    public String getMessageDisplay() {
        switch (displayType) {
            case DBConstant.SHOW_AUDIO_TYPE:
                return DBConstant.DISPLAY_FOR_AUDIO;
            case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
                return content;
            case DBConstant.SHOW_IMAGE_TYPE:
                return DBConstant.DISPLAY_FOR_IMAGE;
            case DBConstant.SHOW_MIX_TEXT:
                return DBConstant.DISPLAY_FOR_MIX;
            case DBConstant.SHOW_VIDEO_TYPE:
                return DBConstant.DISPLAY_FOR_VIDEO;
            case DBConstant.SHOW_BRAND_TYPE:
                return DBConstant.DISPLAY_FOR_BRAND;
            default:
                return DBConstant.DISPLAY_FOR_ERROR;
        }
    }

    @Override
    public String toString() {
        return "MessageEntity{" +
        "id=" + id +
        ", msgId=" + msgId +
        ", fromId=" + fromId +
        ", toId=" + toId +
        ", sessionKey='" + sessionKey + '\'' +
        ", content='" + content + '\'' +
        ", msgType=" + msgType +
        ", displayType=" + displayType +
        ", status=" + status +
        ", created=" + created +
        ", updated=" + updated +
        ", isGIfEmo=" + isGIfEmo +
        ", info='" + info + '\'' +
        ", infoType=" + infoType +
        ", nickname=" + nickname +
        '}';
    }

    @Keep
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageEntity)) {
            return false;
        }

        MessageEntity that = (MessageEntity) o;

        if (created != that.created) {
            return false;
        }
        if (displayType != that.displayType) {
            return false;
        }
        if (fromId != that.fromId) {
            return false;
        }
        if (msgId != that.msgId) {
            return false;
        }
        if (msgType != that.msgType) {
            return false;
        }
        if (status != that.status) {
            return false;
        }
        if (toId != that.toId) {
            return false;
        }
        if (updated != that.updated) {
            return false;
        }
        if (!content.equals(that.content)) {
            return false;
        }
        if (!id.equals(that.id)) {
            return false;
        }
        if (!sessionKey.equals(that.sessionKey)) {
            return false;
        }

        return true;
    }

    @Keep
    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + msgId;
        result = 31 * result + fromId;
        result = 31 * result + toId;
        result = 31 * result + sessionKey.hashCode();
        result = 31 * result + content.hashCode();
        result = 31 * result + msgType;
        result = 31 * result + displayType;
        result = 31 * result + status;
        result = 31 * result + created;
        result = 31 * result + updated;
        return result;
    }


    /**
     * 获取会话的sessionId
     *
     * @param isSend
     * @return
     */
    @Keep
    public int getPeerId(boolean isSend) {
        if (isSend) {
            /**自己发出去的*/
            return toId;
        } else {
            /**接受到的*/
            switch (getSessionType()) {
                case DBConstant.SESSION_TYPE_SINGLE:
                    return fromId;
                case DBConstant.SESSION_TYPE_GROUP:
                    return toId;
                default:
                    return toId;
            }
        }
    }

    @Keep
    public byte[] getSendContent() {
        return null;
    }

    @Keep
    public boolean isGIfEmo() {
        return isGIfEmo;
    }

    @Keep
    public void setGIfEmo(boolean isGIfEmo) {
        this.isGIfEmo = isGIfEmo;
    }

    @Keep
    public boolean isSend(int loginId) {
        boolean isSend = (loginId == fromId) ? true : false;
        return isSend;
    }

    @Keep
    public String buildSessionKey(boolean isSend) {
        int sessionType = getSessionType();
        int peerId = getPeerId(isSend);
        sessionKey = EntityChangeEngine.getSessionKey(peerId, sessionType);
        return sessionKey;
    }

    /**
     * 获取扩展消息
     *
     * @return
     */
    @Keep
    public String getExtContent() {
        JSONObject extContent = new JSONObject();
        Set<String> keySet = extMap.keySet();
        for (Iterator<String> i = keySet.iterator(); i.hasNext(); ) {
            String key = i.next();
            Object value = extMap.get(key);
            try {
                if (value instanceof String) {
                    extContent.put(key, ObjectTrans.getString(value));
                } else if (value instanceof Integer) {
                    extContent.put(key, ObjectTrans.getInt(value));
                } else if (value instanceof Boolean) {
                    extContent.put(key, ObjectTrans.getBoolean(value));
                } else if (value instanceof Long) {
                    extContent.put(key, ObjectTrans.getLong(value));
                } else if (value instanceof Double) {
                    extContent.put(key, ObjectTrans.getDouble(value));
                } else {
                    extContent.put(key, value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return extContent.toString();
    }

    /**
     * 将content 拆分为 内容 和扩展消息
     *
     * @param content
     */
    @Keep
    public void contentSetInfo(String content) {
        ContentEntity contentEntity = null;
        if (!TextUtils.isEmpty(content)) {
            if (content.startsWith(MessageConstant.IMAGE_MSG_START) && content.endsWith(MessageConstant.IMAGE_MSG_END)) {
                int start = MessageConstant.IMAGE_MSG_START.length();
                int end = content.indexOf(MessageConstant.IMAGE_MSG_END);
                if (start < end) {
                    content = content.substring(start, end);
                }
            }
            try {
                contentEntity = new Gson().fromJson(content, ContentEntity.class);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        if (contentEntity != null) {
            info = contentEntity.getInfo();
            infoType = contentEntity.getInfoType();
            nickname = contentEntity.getNickname();
            special = contentEntity.isSpecial();
            String extInfo = contentEntity.getExtInfo();
            if (!TextUtils.isEmpty(extInfo)) {
                try {
                    JSONObject extraContent = new JSONObject(extInfo);
                    if (extraContent != null) {
                        Iterator<String> iterator = extraContent.keys();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            Object value = extraContent.opt(key);
                            extMap.put(key, value);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Keep
    public String getInfo() {
        return info;
    }

    @Keep
    public int getInfoType() {
        if (infoType == 0) {
            contentSetInfo(getContent());
        }
        return infoType;
    }

    @Keep
    public void setInfoType(int infoType) {
        this.infoType = infoType;
    }

    @Keep
    public void setInfo(String info) {
        this.info = info;
    }

    @Keep
    public String getNickname() {
        return nickname;
    }

    @Keep
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Keep
    public boolean isSpecial() {
        return special;
    }

    @Keep
    public boolean getSpecial() {
        return special;
    }

    @Keep
    public void setSpecial(boolean special) {
        this.special = special;
    }

    @Keep
    public void setAttribute(String key, Object value) {
        extMap.put(key, value);
    }

    @Keep
    public void setAttribute(String key, String value) {
        extMap.put(key, value);
    }

    @Keep
    public void setAttribute(String key, int value) {
        extMap.put(key, value);
    }

    @Keep
    public void setAttribute(String key, boolean value) {
        extMap.put(key, value);
    }

    @Keep
    public String getAttributeString(String key) {
        Object object = extMap.get(key);
        return ObjectTrans.getString(object);
    }

    @Keep
    public int getAttributeInt(String key) {
        Object object = extMap.get(key);
        return ObjectTrans.getInt(object);
    }

    @Keep
    public boolean getAttributeBoolean(String key) {
        Object object = extMap.get(key);
        return ObjectTrans.getBoolean(object);
    }

    @Keep
    public void parseMessage(MessageEntity entity) {
        /**父类的id*/
        id = entity.getId();
        msgId = entity.getMsgId();
        fromId = entity.getFromId();
        toId = entity.getToId();
        sessionKey = entity.getSessionKey();
        content = entity.getContent();
        msgType = entity.getMsgType();
        displayType = entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();
        contentSetInfo(entity.getContent());
    }

    @Keep
    public MessageEntity parseFromNets() {
        switch (getDisplayType()) {
            case DBConstant.SHOW_IMAGE_TYPE:
                return ImageMessage.Companion.parseFromNet(this);
            case DBConstant.SHOW_VIDEO_TYPE:
                return VideoMessage.Companion.parseFromNet(this);
            case DBConstant.SHOW_BRAND_TYPE:
                return BrandMessage.Companion.parseFromNet(this);
            default:
                return TextMessage.Companion.parseFromNet(this);
        }
    }
    // KEEP METHODS END

}
