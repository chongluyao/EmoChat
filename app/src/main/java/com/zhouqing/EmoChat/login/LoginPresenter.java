package com.zhouqing.EmoChat.login;

import android.app.Activity;
import android.content.Intent;

import com.zhouqing.EmoChat.common.util.SPUtil;
import com.zhouqing.EmoChat.common.util.ThreadUtil;
import com.zhouqing.EmoChat.common.util.XmppUtil;
import com.zhouqing.EmoChat.main.MainActivity;
import com.zhouqing.EmoChat.register.RegisterActivity;
import com.zhouqing.EmoChat.service.IMService;


public class LoginPresenter implements LoginContract.Presenter {
    private Activity mActivity;
    private LoginContract.View mView;

    public LoginPresenter(Activity activity, LoginContract.View view) {
        mActivity = activity;
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void login(final String username, final String password,final String ip) {
        ThreadUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                SPUtil.put(mActivity,"ip",ip);

                boolean conServer = XmppUtil.conServer();
                if (!conServer) {
                    mView.showServerError();
                    return;
                }

                //开始登录
                boolean login = XmppUtil.login(username, password);
                if (!login) {
                    mView.showLoginError();
                    return;
                }

                //把登录账号保存到本地 方便自动登录
                SPUtil.put(mActivity, "username", username);
                SPUtil.put(mActivity, "password", password);


                //设置自动登录
                SPUtil.put(mActivity, "isAutoLogin", true);

                //将连接对象保存下来
                IMService.conn = XmppUtil.getConnection();
                IMService.ACCOUNT = username + "@" + IMService.conn.getServiceName();
                IMService.NICKNAME = username;
                //开启服务去获取监听数据
                mActivity.startService(new Intent(mActivity, IMService.class));
                // 跳转到主页面
                mActivity.startActivity(new Intent(mActivity, MainActivity.class));

                mView.finishActivity();
            }
        });
    }

    @Override
    public void jumpToRegisterPage() {
        mActivity.startActivity(new Intent(mActivity, RegisterActivity.class));

    }


}
