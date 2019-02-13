package com.mogujie.tt.imutils;

import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mogujie.tt.db.DBInterface;
import com.mogujie.tt.db.sp.LoginSp;
import com.mogujie.tt.db.sp.SystemConfigSp;
import com.mogujie.tt.config.UrlConstant;
import com.mogujie.tt.imservice.event.LoginEvent;
import com.mogujie.tt.imservice.event.SocketEvent;
import com.mogujie.tt.imservice.manager.IMLoginManager;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.IMServiceConnector;
import com.mogujie.tt.utils.IMUIHelper;
import com.mogujie.tt.utils.Logger;

import java.lang.ref.WeakReference;

import de.greenrobot.event.EventBus;

/**
 *
 * Date: 2017/8/29  18:52
 * describe:
 */
public class IMChatUtil {
    public static final String TAG = IMChatUtil.class.getSimpleName();
    private static IMChatUtil instance = null;

    private Logger logger = Logger.getLogger(IMChatUtil.class);

    private WeakReference<Activity> mActivityWeakReference;

    private IMService imService;

    private Handler uiHandler = new Handler();

    private boolean loginSuccess;

    private IMLoginListener imLoginListener;

    private IMChatUtil() {

    }

    public static IMChatUtil getInstance() {
        if (instance == null) {
            synchronized (IMChatUtil.class) {
                if (instance == null) {
                    instance = new IMChatUtil();
                }
            }
        }
        return instance;
    }

    public void init(Activity context) {
        mActivityWeakReference = new WeakReference<Activity>(context);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (mActivityWeakReference.get() != null) {
            SystemConfigSp.instance().init(mActivityWeakReference.get().getApplicationContext());
            LoginSp.instance().init(mActivityWeakReference.get().getApplicationContext());
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS);//设置请求聊天地址
            imServiceConnector.connect(mActivityWeakReference.get());
        }
    }

    //登录聊天
    public void login(String username, String password, IMLoginListener imLoginListener) {
        this.imLoginListener = imLoginListener;
        if (imService == null) {
            imServiceConnector.connect(mActivityWeakReference.get());
            logger.d("login#loginIM# imService connect fail");
            return;
        }
        imService.getLoginManager().login(username, password);
    }

    //退出聊天
    public void logout() {
        if (imService != null && imService.getLoginManager() != null) {
            imService.getLoginManager().logOut();
        }
    }


    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
            Log.d(TAG, "onServiceDisconnected");
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("login#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            try {
                do {
                    if (imService == null) {
                        //后台服务启动链接失败
                        logger.d("login#onIMServiceConnected#后台服务启动链接失败");
                        break;
                    }
                    IMLoginManager loginManager = imService.getLoginManager();
                    LoginSp loginSp = imService.getLoginSp();
                    if (loginManager == null || loginSp == null) {
                        // 无法获取登陆控制器
                        logger.d("login#onIMServiceConnected#无法获取登陆控制器");
                        break;
                    }

                    LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
                    if (loginIdentity == null) {
                        logger.d("login#onIMServiceConnected#之前没有保存任何登陆相关的");
                        // 之前没有保存任何登陆相关的，跳转到登陆页面
                        break;
                    }

                    //                    mNameView.setText(loginIdentity.getLoginName());
                    if (TextUtils.isEmpty(loginIdentity.getPwd())) {
                        logger.d("login#onIMServiceConnected#密码为空，可能是loginOut");
                        // 密码为空，可能是loginOut
                        break;
                    }
                    handleGotLoginIdentity(loginIdentity);
                    return;
                } while (false);

                // 异常分支都会执行这个
                handleNoLoginIdentity();
            } catch (Exception e) {
                // 任何未知的异常
                logger.w("loadIdentity failed");
                handleNoLoginIdentity();
            }
        }
    };

    /**
     * 自动登陆
     */
    private void handleGotLoginIdentity(final LoginSp.SpLoginIdentity loginIdentity) {
        logger.i("login#handleGotLoginIdentity");

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logger.d("login#start auto login");
                if (imService == null || imService.getLoginManager() == null) {
                    showLoginPage();
                }
                imService.getLoginManager().login(loginIdentity);
            }
        }, 500);
    }

    /**
     * 跳转到登陆的页面
     */
    private void handleNoLoginIdentity() {
        logger.i("login#handleNoLoginIdentity");
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoginPage();
            }
        }, 1000);
    }

    private void showLoginPage() {

    }


    /**
     * ----------------------------event 事件驱动----------------------------
     */
    public void onEventMainThread(LoginEvent event) {
        switch (event) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
                onLoginSuccess();
                break;
            case LOGIN_AUTH_FAILED:
            case LOGIN_INNER_FAILED:
                if (!loginSuccess) {
                    onLoginFailure(event);
                }
                break;
        }
    }

    public void onEventMainThread(SocketEvent event) {
        switch (event) {
            case CONNECT_MSG_SERVER_FAILED:
            case REQ_MSG_SERVER_ADDRS_FAILED:
                if (!loginSuccess) {
                    onSocketFailure(event);
                }
                break;
        }
    }

    private void onLoginSuccess() {
        logger.i("login#onLoginSuccess");
        loginSuccess = true;
        //        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        //        startActivity(intent);
        //        LoginActivity.this.finish();
        if (imLoginListener != null) {
            imLoginListener.onLoginSuccess();
        }
        Toast.makeText(mActivityWeakReference.get().getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
        logger.i("login#onLoginSuccess#loginInfo" + imService.getLoginManager().getLoginInfo().toString());
        DBInterface.instance().initDbHelp(mActivityWeakReference.get().getApplicationContext(),imService.getLoginManager().getLoginId());
    }

    private void onLoginFailure(LoginEvent event) {
        logger.e("login#onLoginError -> errorCode:%s", event.name());
        showLoginPage();
        String errorTip = "";
        if (mActivityWeakReference.get() != null) {
            errorTip = mActivityWeakReference.get().getString(IMUIHelper.getLoginErrorTip(event));
            logger.d("login#errorTip:%s", errorTip);
            Toast.makeText(mActivityWeakReference.get().getApplicationContext(), errorTip, Toast.LENGTH_SHORT).show();
        }
        if (imLoginListener != null) {
            imLoginListener.onLoginFail(errorTip);
        }
    }

    private void onSocketFailure(SocketEvent event) {
        logger.e("login#onLoginError -> errorCode:%s,", event.name());
        showLoginPage();
        String errorTip = "";
        if (mActivityWeakReference.get() != null) {
            errorTip = mActivityWeakReference.get().getString(IMUIHelper.getSocketErrorTip(event));
            logger.d("login#errorTip:%s", errorTip);
            Toast.makeText(mActivityWeakReference.get().getApplicationContext(), errorTip, Toast.LENGTH_SHORT).show();
        }
        if (imLoginListener != null) {
            imLoginListener.onLoginFail(errorTip);
        }
    }


    public void destroyIM() {
        if (mActivityWeakReference.get() != null) {
            imServiceConnector.disconnect(mActivityWeakReference.get());
        }
        EventBus.getDefault().unregister(this);
    }
}
