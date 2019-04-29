package com.zhouqing.EmoChat.chat;

import android.database.Cursor;

import com.zhouqing.EmoChat.common.ui.BasePresenter;
import com.zhouqing.EmoChat.common.ui.BaseView;


public class ChatContract {
    public interface Presenter extends BasePresenter {
        void getDialogueMessage(String clickAccout);

        void sendMessage(String clickAccout,String facePic,String type,String emotionShow);

        void bindIMService();

        void unbindIMService();
    }

    public interface View extends BaseView<Presenter> {
        void showDialogueMessage(Cursor cursor);

        String getMessage();

        void clearMessage();
    }
}
