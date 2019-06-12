package com.mogujie.tt;

import com.google.protobuf.GeneratedMessageLite;
import com.mogujie.tt.db.entity.MessageEntity;
import com.mogujie.tt.db.sp.LoginSp;
import com.mogujie.tt.imservice.entity.RecentInfo;
import com.mogujie.tt.imservice.event.LoginEvent;
import com.mogujie.tt.imservice.manager.IMLoginManager;
import com.mogujie.tt.imservice.manager.IMSocketManager;
import com.mogujie.tt.imservice.network.MsgServerHandler;
import com.mogujie.tt.protobuf.IMBaseDefine;
import com.mogujie.tt.protobuf.IMMessage;
import com.mogujie.tt.protobuf.base.Header;
import com.mogujie.tt.imservice.entity.MsgAnalyzeEngine;
import com.mogujie.tt.ui.adapter.MessageAdapter;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import com.mogujie.tt.imservice.manager.IMMessageManager;

import java.util.ArrayList;

/**
 * Created by wangru
 * Date: 2017/8/25  10:14
 * mail: 1902065822@qq.com
 * describe:
 */
public class 流程 {
    /**
     * 新版本流程如下
     1.客户端通过域名获得login_server的地址
     2.客户端通过login_server获得msg_serv的地址
     3.客户端带着用户名密码对msg_serv进行登录
     4.msg_serv转给db_proxy进行认证（do not care on client）
     5.将认证结果返回给客户端
     */

    /**
     * 登录
     * {@link com.mogujie.tt.ui.activity.LoginActivity }
     *
     * {@link com.mogujie.tt.config.UrlConstant#ACCESS_MSG_ADDRESS}默认服务器地址
     *
     * {@link IMLoginManager#login(String, String)} 登录
     * {@link IMLoginManager#login(LoginSp.SpLoginIdentity)}   自动登录
     *
     * {@link IMSocketManager#reqMsgServerAddrs()}  获得login_server的地址对象 {@link IMSocketManager.MsgServerAddrsEntity}
     * {@link IMSocketManager#connectMsgServer(IMSocketManager.MsgServerAddrsEntity)}   获取地址成功开启线程连接
     *
     * {@link com.mogujie.tt.imservice.network.SocketThread#doConnect()} 使用 netty 连接
     * {@link MsgServerHandler#channelConnected(ChannelHandlerContext, ChannelStateEvent)} 连接成功
     * {@link MsgServerHandler#messageReceived(ChannelHandlerContext, MessageEvent)} 接收消息（ 登录信息，所有联系人，所有消息，所有群组信息）
     *
     *
     * {@link MsgAnalyzeEngine#analyzeMessage(IMBaseDefine.MsgInfo msgInfo)} 接收消息处理
     *
     *
     * {@link IMLoginManager#reqLoginMsgServer()}连接成功后，请求登录
     * {@link com.mogujie.tt.ui.activity.LoginActivity#onEventMainThread(LoginEvent)} 登录返回结果   登录结果
     * {@link com.mogujie.tt.ui.activity.LoginActivity#onEventMainThread(SocketEvent)} 网络连接结果
     */


    /**
     * 聊天
     * {@link MsgServerHandler#messageReceived(ChannelHandlerContext, MessageEvent)} 接收消息
     */


    /**
     * 发送消息
     * {@link com.mogujie.tt.imservice.manager.IMMessageManager#sendMessage(MessageEntity)}发送消息
     * {@link com.mogujie.tt.imservice.network.SocketThread#sendRequest(GeneratedMessageLite, Header)} 最终
     * {@link com.mogujie.tt.imservice.entity.AudioMessage#getSendContent} 语音文件处理
     * {@link com.mogujie.tt.imservice.service.LoadImageService} 图片处理
     */


    /**
     * 数据库取信息
     * {@link com.mogujie.tt.db.dao.DaoSession} 所有数据库
     *
     * {@link com.mogujie.tt.imutils.IMChatUtil}
     * {@link com.mogujie.tt.imservice.manager.IMContactManager}
     * {@link com.mogujie.tt.db.DBInterface}
     */


    /**
     * log 级别输出
     * {@link com.mogujie.tt.utils.Logger#logLevel} 设置输出日志级别
     */


    /**
     * {@link com.mogujie.tt.ui.widget.message.TextRenderView} 聊天左右界面
     * {@link com.mogujie.tt.ui.widget.message.BaseMsgRenderView#render}   控件赋值
     */

    /**
     * 会话
     * {@link com.mogujie.tt.imservice.manager.IMSessionManager#reqGetRecentContacts(int)}  请求最近回话
     * {@link com.mogujie.tt.imservice.manager.IMSessionManager#reqRemoveSession(RecentInfo)}  请求删除会话
     */


    /*********                聊天消息处理       ***********/
    /**
     显示消息类型
     {@link com.mogujie.tt.imservice.entity.MsgAnalyzeEngine#analyzeMessageDisplay(String)}
     {@link com.mogujie.tt.imservice.entity.MsgAnalyzeEngine#analyzeMessage(IMBaseDefine.MsgInfo)}
     */

    /**
     发送消息注意：
     1、牙牙表情发送的是Text 消息类型  isMsgGif {@link MessageAdapter#isMsgGif(MessageEntity)}  判断是否是表情，显示为图片类型
     */


    /**
     * 添加新类型时
     * {@link MessageEntity#parseFromNets()}
     */


    /**
     * {@link IMMessageManager#onRecvMessage(IMMessage.IMMsgData)} 接收聊天消息
     */


    /***
     * 请求用户详情信息
     * {@link com.mogujie.tt.imservice.manager.IMContactManager#reqGetDetaillUsers(ArrayList)}
     * UserInfoEvent.USER_INFO_UPDATE  EventBus获取
     */
}
