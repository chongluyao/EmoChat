package com.zhouqing.EmoChat.register;

import com.zhouqing.EmoChat.common.ui.BasePresenter;
import com.zhouqing.EmoChat.common.ui.BaseView;


public class RegisterContract {
    public interface Presenter extends BasePresenter {
        void register(String username, String password, String ip);
    }

    public interface View extends BaseView<Presenter> {
        void serverError();

        void registerSuccess(String username, String password,String ip);

        void registerFailure();

        void userExit();
    }
}
