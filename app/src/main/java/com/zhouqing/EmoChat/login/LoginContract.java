package com.zhouqing.EmoChat.login;

import com.zhouqing.EmoChat.common.ui.BasePresenter;
import com.zhouqing.EmoChat.common.ui.BaseView;

/**
 * Created by liqingfeng on 2017/5/22.
 */

public class LoginContract {
    public interface View extends BaseView<Presenter> {
        void showServerError();

        void showLoginError();

        void finishActivity();
    }

    public interface Presenter extends BasePresenter {
        void login(String username, String password, String ip);

        void jumpToRegisterPage();
    }
}
