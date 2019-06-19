package com.mogujie.tt.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.google.gson.Gson;
import com.mogujie.tt.config.DBConstant;
import com.mogujie.tt.config.MessageConstant;
import com.mogujie.tt.db.dao.*;
import com.mogujie.tt.db.entity.*;
import com.mogujie.tt.db.util.MySQLiteOpenHelper;
import com.mogujie.tt.imservice.entity.*;
import com.mogujie.tt.imservice.manager.IMMessageExt;
import com.mogujie.tt.utils.Logger;

import java.util.*;

/**
 * @author : yingmu on 15-1-5.
 * @email : yingmu@mogujie.com.
 * <p>
 * 有两个静态标识可开启QueryBuilder的SQL和参数的日志输出：
 * QueryBuilder.LOG_SQL = true;
 * QueryBuilder.LOG_VALUES = true;
 */
public class DBInterface {
    private static final String TAG = "DBInterface";
    private Logger logger = Logger.getLogger(DBInterface.class);
    private static DBInterface dbInterface = null;
    private MySQLiteOpenHelper openHelper;
    private Context context = null;
    private int loginUserId = 0;

    public static DBInterface instance() {
        if (dbInterface == null) {
            synchronized (DBInterface.class) {
                if (dbInterface == null) {
                    dbInterface = new DBInterface();
                }
            }
        }
        return dbInterface;
    }

    private DBInterface() {
    }

    /**
     * 上下文环境的更新
     * 1. 环境变量的clear
     * check
     */
    public void close() {
        if (openHelper != null) {
            openHelper.close();
            openHelper = null;
            context = null;
            loginUserId = 0;
        }
    }


    public void initDbHelp(Context ctx, int loginId) {
        if (ctx == null || loginId <= 0) {
//            throw new RuntimeException("#DBInterface# init DB exception!");
            Log.e("DBInterface","#DBInterface# init DB exception!");
            return;
        }
        // 临时处理，为了解决离线登陆db实例初始化的过程
        if (context != ctx || loginUserId != loginId) {
            context = ctx;
            loginUserId = loginId;
            close();
            logger.i("DB init,loginId:%d", loginId);
            String DBName = "tt_" + loginId + ".db";
            //            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(ctx, DBName, null);
            //解决升级数据库删除数据问题
            MySQLiteOpenHelper helper = new MySQLiteOpenHelper(ctx, DBName, null);
            this.openHelper = helper;
        }
    }

    /**
     * Query for readable DB
     */
    private DaoSession openReadableDb() {
        isInitOk();
        SQLiteDatabase db = openHelper.getReadableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        return daoSession;
    }

    /**
     * Query for writable DB
     */
    private DaoSession openWritableDb() {
        isInitOk();
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        return daoSession;
    }


    private void isInitOk() {
        if (openHelper == null) {
            logger.e("DBInterface#isInit not success or start,cause by openHelper is null");
            // 抛出异常 todo
            // throw new RuntimeException("DBInterface#isInit not success or start,cause by openHelper is null");
        }
    }


    /**
     * -------------------------下面开始department 操作相关---------------------------------------
     */
    public void batchInsertOrUpdateDepart(List<DepartmentEntity> entityList) {
        if (entityList == null || entityList.size() <= 0) {
            return;
        }
        //        for (DepartmentEntity entity : entityList) {
        //            DepartmentEntity entityTemp = getDepartmentByDepartId(entity.getDepartId());
        //            if (entityTemp != null && entityTemp.getId() != null) {
        //                entity.setId(entityTemp.getId());
        //            }
        //        }
        DepartmentEntityDao dao = openWritableDb().getDepartmentEntityDao();
        dao.insertOrReplaceInTx(entityList);
    }

    /**
     * update
     */
    public int getDeptLastTime() {
        DepartmentEntityDao dao = openReadableDb().getDepartmentEntityDao();
        DepartmentEntity entity = dao.queryBuilder()
        .orderDesc(DepartmentEntityDao.Properties.Updated)
        .limit(1)
        .unique();
        if (entity == null) {
            return 0;
        } else {
            return entity.getUpdated();
        }
    }

    // 部门被删除的情况
    public List<DepartmentEntity> loadAllDept() {
        DepartmentEntityDao dao = openReadableDb().getDepartmentEntityDao();
        List<DepartmentEntity> result = dao.loadAll();
        return result;
    }

    public DepartmentEntity getDepartmentByDepartId(int departId) {
        DepartmentEntityDao dao = openReadableDb().getDepartmentEntityDao();
        DepartmentEntity entity = dao.queryBuilder().where(DepartmentEntityDao.Properties.DepartId.eq(departId)).limit(1).build().unique();
        return entity;
    }

    /**-------------------------下面开始User 操作相关---------------------------------------*/
    /**
     * @return todo  USER_STATUS_LEAVE
     */
    public List<UserEntity> loadAllUsers() {
        UserEntityDao dao = openReadableDb().getUserEntityDao();
        List<UserEntity> result = dao.loadAll();
        return result;
    }

    public UserEntity getByUserName(String uName) {
        UserEntityDao dao = openReadableDb().getUserEntityDao();
        UserEntity entity = dao.queryBuilder().where(UserEntityDao.Properties.PinyinName.eq(uName)).limit(1).build().unique();
        return entity;
    }

    public UserEntity getByLoginId(int loginId) {
        UserEntityDao dao = openReadableDb().getUserEntityDao();
        UserEntity entity = dao.queryBuilder().where(UserEntityDao.Properties.PeerId.eq(loginId)).limit(1).build().unique();
        return entity;
    }


    public void insertOrUpdateUser(UserEntity entity) {
        if (entity == null) {
            return;
        }
        UserEntityDao UserEntityDao = openWritableDb().getUserEntityDao();
        UserEntity entityTemp = getUserByPeerId(entity.getPeerId());
        if (entityTemp != null && entityTemp.getId() != null && entityTemp.getId() > 0) {
            entity.setId(entityTemp.getId());
        }
        long rowId = UserEntityDao.insertOrReplace(entity);
    }

    public void batchInsertOrUpdateUser(List<UserEntity> entityList) {
        if (entityList == null || entityList.size() <= 0) {
            return;
        }
        for (UserEntity entity : entityList) {
            UserEntity entityTemp = getUserByPeerId(entity.getPeerId());
            if (entityTemp != null && entityTemp.getId() != null && entityTemp.getId() > 0) {
                entity.setId(entityTemp.getId());
            }
        }
        UserEntityDao UserEntityDao = openWritableDb().getUserEntityDao();
        UserEntityDao.insertOrReplaceInTx(entityList);
    }

    /**
     * update
     */
    public int getUserInfoLastTime() {
        UserEntityDao SessionEntityDao = openReadableDb().getUserEntityDao();
        UserEntity userEntity = SessionEntityDao.queryBuilder()
        .orderDesc(UserEntityDao.Properties.Updated)
        .limit(1)
        .unique();
        if (userEntity == null) {
            return 0;
        } else {
            return userEntity.getUpdated();
        }
    }

    public UserEntity getUserByPeerId(int peerId) {
        UserEntityDao dao = openReadableDb().getUserEntityDao();
        UserEntity entity = dao.queryBuilder().where(UserEntityDao.Properties.PeerId.eq(peerId)).limit(1).build().unique();
        return entity;
    }

    /**-------------------------下面开始Group 操作相关---------------------------------------*/
    /**
     * 载入Group的所有数据
     *
     * @return
     */
    public List<GroupEntity> loadAllGroup() {
        GroupEntityDao dao = openReadableDb().getGroupEntityDao();
        List<GroupEntity> result = dao.loadAll();
        return result;
    }

    public long insertOrUpdateGroup(GroupEntity groupEntity) {
        GroupEntityDao dao = openWritableDb().getGroupEntityDao();
        long pkId = dao.insertOrReplace(groupEntity);
        return pkId;
    }

    public void batchInsertOrUpdateGroup(List<GroupEntity> entityList) {
        if (entityList == null || entityList.size() <= 0) {
            return;
        }
        for (GroupEntity entity : entityList) {
            GroupEntity entityTemp = getGroupByPeerId(entity.getPeerId());
            if (entityTemp != null && entityTemp.getId() != null) {
                entity.setId(entityTemp.getId());
            }
        }
        GroupEntityDao dao = openWritableDb().getGroupEntityDao();
        dao.insertOrReplaceInTx(entityList);
    }

    public GroupEntity getGroupByPeerId(int peerId) {
        GroupEntityDao dao = openReadableDb().getGroupEntityDao();
        GroupEntity entity = dao.queryBuilder().where(GroupEntityDao.Properties.PeerId.eq(peerId)).limit(1).build().unique();
        return entity;
    }

    /**-------------------------下面开始session 操作相关---------------------------------------*/
    /**
     * 载入session 表中的所有数据
     *
     * @return
     */
    public List<SessionEntity> loadAllSession() {
        logger.v("db#loadAllSession");
        SessionEntityDao dao = openReadableDb().getSessionEntityDao();
        List<SessionEntity> result = dao.queryBuilder()
        .orderDesc(SessionEntityDao.Properties.Updated)
        .list();
        return result;
    }

    public long insertOrUpdateSession(SessionEntity sessionEntity) {
        logger.v("db#insertOrUpdateSession");
        SessionEntityDao dao = openWritableDb().getSessionEntityDao();
        long pkId = dao.insertOrReplace(sessionEntity);
        return pkId;
    }

    public void batchInsertOrUpdateSession(List<SessionEntity> entityList) {
        logger.v("db#batchInsertOrUpdateSession");
        if (entityList == null || entityList.size() <= 0) {
            return;
        }
        for (SessionEntity entity : entityList) {
            SessionEntity entityTemp = getSessionBySessionKey(entity.getSessionKey());
            if (entityTemp != null && entityTemp.getId() != null) {
                entity.setId(entityTemp.getId());
            }
        }
        SessionEntityDao dao = openWritableDb().getSessionEntityDao();
        dao.insertOrReplaceInTx(entityList);
    }

    public void deleteSession(String sessionKey) {
        logger.v("db#deleteSession:%s", sessionKey);
        //        SessionEntityDao SessionEntityDao =  openWritableDb().getSessionEntityDao();
        //        DeleteQuery<SessionEntity> bd = SessionEntityDao.queryBuilder()
        //                .where(SessionEntityDao.Properties.SessionKey.eq(sessionKey))
        //                .buildDelete();
        //        bd.executeDeleteWithoutDetachingEntities();
        SessionEntityDao dao = openWritableDb().getSessionEntityDao();
        SessionEntity sessionEntity = dao.queryBuilder().where(SessionEntityDao.Properties.SessionKey.eq(sessionKey)).limit(1).build().unique();
        if (sessionEntity != null) {
            //通过Key来删除，这里的Key就是user字段中的ID号
            dao.deleteByKey(sessionEntity.getId());
        }
    }

    public SessionEntity getSessionBySessionKey(String sessionKey) {
        SessionEntityDao dao = openReadableDb().getSessionEntityDao();
        List<SessionEntity> sessionEntityList = dao.queryBuilder().where(SessionEntityDao.Properties.SessionKey.eq(sessionKey)).build().list();
        if (sessionEntityList != null && sessionEntityList.size() > 0) {
            sessionEntityList.get(0);
        }
        return null;
    }

    /**
     * 获取最后回话的时间，便于获取联系人列表变化
     * 问题: 本地消息发送失败，依旧会更新session的时间 [存在会话、不存在的会话]
     * 本质上还是最后一条成功消息的时间
     *
     * @return
     */
    public int getSessionLastTime() {
        int timeLine = 0;
        MessageEntityDao dao = openReadableDb().getMessageEntityDao();
        //        String successType = String.valueOf(MessageConstant.MSG_SUCCESS);
        //        String sql = "select created from MessageDao where status=? order by created desc limit 1";
        //        Cursor cursor = dao.getDatabase().rawQuery(sql, new String[] {successType});
        //        try {
        //            if (cursor != null && cursor.getCount() == 1) {
        //                cursor.moveToFirst();
        //                timeLine = cursor.getInt(0);
        //            }
        //        } catch (Exception e) {
        //            logger.e("DBInterface#getSessionLastTime cursor 查询异常");
        //        } finally {
        //            cursor.close();
        //        }

        MessageEntity messageEntity = dao.queryBuilder().where(MessageEntityDao.Properties.Status.eq(MessageConstant.MSG_SUCCESS)).orderDesc(MessageEntityDao.Properties.Created).limit(1).build().unique();
        if (messageEntity == null) {
            return 0;
        }
        timeLine = messageEntity.getCreated();
        return timeLine;
    }

    /**
     * -------------------------下面开始message 操作相关---------------------------------------
     */

    // where (msgId >= startMsgId and msgId<=lastMsgId) or
    // (msgId=0 and status = 0)
    // order by created desc
    // limit count;
    // 按照时间排序
    public List<MessageEntity> getHistoryMsg(String chatKey, int lastMsgId, int lastCreateTime, int count) {
        logger.v("db#getHistoryMsg");
        /**解决消息重复的问题*/
        int preMsgId = lastMsgId + 1;
        MessageEntityDao dao = openReadableDb().getMessageEntityDao();
        List<MessageEntity> listMsg = dao.queryBuilder()
        .where(MessageEntityDao.Properties.Created.le(lastCreateTime), MessageEntityDao.Properties.SessionKey.eq(chatKey), MessageEntityDao.Properties.MsgId.notEq(preMsgId))
        .whereOr(MessageEntityDao.Properties.MsgId.le(lastMsgId), MessageEntityDao.Properties.MsgId.gt(90000000))
        .orderDesc(MessageEntityDao.Properties.Created)
        .orderDesc(MessageEntityDao.Properties.MsgId)
        .limit(count)
        .list();
        for (MessageEntity msg : listMsg) {
            Log.i(TAG, "getHistoryMsg: " + msg.toString());
        }
        return formatMessage(listMsg);
    }

    /**
     * IMGetLatestMsgIdReq 后去最后一条合法的msgid
     */
    public List<Integer> refreshHistoryMsgId(String chatKey, int beginMsgId, int lastMsgId) {
        logger.v("db#refreshHistoryMsgId");
        MessageEntityDao dao = openReadableDb().getMessageEntityDao();
        //        String sql = "select MSG_ID from Message where SESSION_KEY = ? and MSG_ID >= ? and MSG_ID <= ? order by MSG_ID asc";
        //        Cursor cursor = dao.getDatabase().rawQuery(sql, new String[] {chatKey, String.valueOf(beginMsgId), String.valueOf(lastMsgId)});
        //        List<Integer> msgIdList = new ArrayList<>();
        //        try {
        //            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        //                int msgId = cursor.getInt(0);
        //                msgIdList.add(msgId);
        //            }
        //        } finally {
        //            cursor.close();
        //        }

        List<Integer> msgIdList = new ArrayList<>();
        List<MessageEntity> list = dao.queryBuilder()
        .where(MessageEntityDao.Properties.SessionKey.eq(chatKey), MessageEntityDao.Properties.MsgId.ge(beginMsgId), MessageEntityDao.Properties.MsgId.le(lastMsgId))
        .orderAsc(MessageEntityDao.Properties.MsgId).list();
        if (list != null && list.size() > 0) {
            for (MessageEntity entity : list) {
                msgIdList.add(entity.getMsgId());
            }
        }
        return msgIdList;
    }


    public long insertOrUpdateMix(MessageEntity message) {
        logger.v("db#insertOrUpdateMix");
        MessageEntityDao dao = openWritableDb().getMessageEntityDao();
        MessageEntity parent = dao.queryBuilder().where(MessageEntityDao.Properties.MsgId.eq(message.getMsgId())
        , MessageEntityDao.Properties.SessionKey.eq(message.getSessionKey())).limit(1).build().unique();

        long resId = parent.getId();
        if (parent.getDisplayType() != DBConstant.SHOW_MIX_TEXT) {
            return resId;
        }

        boolean needUpdate = false;
        MixMessage mixParent = (MixMessage) formatMessage(parent);
        List<MessageEntity> msgList = mixParent.getMsgList();
        for (int index = 0; index < msgList.size(); index++) {
            if (msgList.get(index).getId() == message.getId()) {
                msgList.set(index, message);
                needUpdate = true;
                break;
            }
        }

        if (needUpdate) {
            mixParent.setMsgList(msgList);
            long pkId = dao.insertOrReplace(mixParent);
            return pkId;
        }
        return resId;
    }

    /**
     * 有可能是混合消息
     * 批量接口{batchInsertOrUpdateMessage} 没有存在场景
     */
    public long insertOrUpdateMessage(MessageEntity message) {
        logger.v("db#insertOrUpdateMessage: #message:%s", message.toString());
        if (message.getId() != null && message.getId() < 0) {
            // mix消息
            return insertOrUpdateMix(message);
        }
        MessageEntityDao dao = openWritableDb().getMessageEntityDao();
        long pkId = dao.insertOrReplace(message);
        return pkId;
    }

    /**
     * todo 这个地方调用存在特殊场景，如果list中包含Id为负Mix子类型，更新就有问题
     * 现在的调用列表没有这个情景，使用的时候注意
     */
    public void batchInsertOrUpdateMessage(List<MessageEntity> entityList) {
        logger.v("db#batchInsertOrUpdateMessage: %s", new Gson().toJson(entityList).toString());
        if (entityList == null || entityList.size() <= 0) {
            return;
        }
        for (MessageEntity entity : entityList) {
            MessageEntity messageTemp = getMessageByMsgId(entity.getMsgId());
            if (messageTemp != null && messageTemp.getId() != null) {
                entity.setId(messageTemp.getId());
            }
        }
        MessageEntityDao dao = openWritableDb().getMessageEntityDao();
        dao.insertOrReplaceInTx(entityList);

    }


    public void deleteMessageById(long localId) {
        logger.v("db#deleteMessageById");
        if (localId <= 0) {
            return;
        }
        Set<Long> setIds = new TreeSet<>();
        setIds.add(localId);
        batchDeleteMessageById(setIds);
    }

    public void batchDeleteMessageById(Set<Long> pkIds) {
        logger.v("db#batchDeleteMessageById");
        if (pkIds.size() <= 0) {
            return;
        }
        MessageEntityDao dao = openWritableDb().getMessageEntityDao();
        dao.deleteByKeyInTx(pkIds);
    }

    public void deleteMessageByMsgId(int msgId) {
        logger.v("db#deleteMessageByMsgId");
        if (msgId <= 0) {
            return;
        }
        //        MessageEntityDao MessageEntityDao =  openWritableDb().getMessageEntityDao();
        //        QueryBuilder<MessageEntity> qb = openWritableDb().getMessageEntityDao().queryBuilder();
        //        DeleteQuery<MessageEntity> bd = qb.where(MessageEntityDao.Properties.MsgId.eq(msgId)).buildDelete();
        //        bd.executeDeleteWithoutDetachingEntities();

        MessageEntityDao dao = openWritableDb().getMessageEntityDao();
        MessageEntity messageEntity = dao.queryBuilder().where(MessageEntityDao.Properties.MsgId.eq(msgId)).limit(1).build().unique();
        if (messageEntity != null) {
            dao.deleteByKey(messageEntity.getId());
        }
    }

    //    public MessageEntity getMessageById(int id) {
    //        Log.e(TAG, "getMessageByMsgId: ");
    //        MessageEntityDao dao = openReadableDb().getMessageEntityDao();
    //        //        Query query = dao.queryBuilder().where(MessageDao.Properties.Id.eq(messageId)).build();
    //        return dao.queryBuilder().where(MessageEntityDao.Properties.Id.eq(id)).build().unique();
    //    }

    public MessageEntity getMessageByMsgId(int msgId) {
        MessageEntityDao dao = openReadableDb().getMessageEntityDao();
        //        Query query = dao.queryBuilder().where(MessageDao.Properties.Id.eq(messageId)).build();
        return dao.queryBuilder().where(MessageEntityDao.Properties.MsgId.eq(msgId)).limit(1).build().unique();
    }

    /**
     * 根据主键查询
     * not use
     */
    public MessageEntity getMessageById(long localId) {
        logger.v("db#getMessageById:" + localId);
        MessageEntityDao dao = openReadableDb().getMessageEntityDao();
        MessageEntity messageEntity =
        dao.queryBuilder().where(MessageEntityDao.Properties.Id.eq(localId)).limit(1).build().unique();
        return formatMessage(messageEntity);
    }


    private MessageEntity formatMessage(MessageEntity msg) {
        return IMMessageExt.INSTANCE.parseFromDB(msg);
    }

    private List<MessageEntity> formatMessage(List<MessageEntity> msgList) {
        logger.v("db#formatMessage");
        if (msgList.size() <= 0) {
            return Collections.emptyList();
        }
        ArrayList<MessageEntity> newList = new ArrayList<>();
        for (MessageEntity info : msgList) {
            newList.add(IMMessageExt.INSTANCE.parseFromDB(info));
        }
        return newList;
    }



}
