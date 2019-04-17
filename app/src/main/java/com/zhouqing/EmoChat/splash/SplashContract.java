package com.zhouqing.EmoChat.splash;

import com.zhouqing.EmoChat.common.ui.BasePresenter;
import com.zhouqing.EmoChat.common.ui.BaseView;



public class SplashContract {
    public interface View extends BaseView<Presenter> {

    }

    public interface Presenter extends BasePresenter {
        void endSplash();
    }
}
