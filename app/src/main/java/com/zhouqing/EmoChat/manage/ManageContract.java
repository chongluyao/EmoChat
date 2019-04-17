package com.zhouqing.EmoChat.manage;

import android.database.Cursor;

import com.zhouqing.EmoChat.common.ui.BasePresenter;
import com.zhouqing.EmoChat.common.ui.BaseView;

public class ManageContract {
    public interface Presenter extends BasePresenter {
        void getContact();
        void getDialogueMessage(final String clickAccout,String emotion);
        void inputEmotion(Cursor cursor);
    }

    public interface View extends BaseView<ManageContract.Presenter> {
        void showSession(Cursor cursor);
        void showDialogueMessage(Cursor cursor);
    }
}
