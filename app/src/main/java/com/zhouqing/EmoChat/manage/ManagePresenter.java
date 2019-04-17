package com.zhouqing.EmoChat.manage;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;

import com.zhouqing.EmoChat.common.util.ThreadUtil;
import com.zhouqing.EmoChat.db.SmsOpenHelper;
import com.zhouqing.EmoChat.provider.SmsProvider;
import com.zhouqing.EmoChat.service.IMService;

public class ManagePresenter implements ManageContract.Presenter{
    private Activity mAcitivity;
    private ManageContract.View mView;
    public ManagePresenter(Activity acitivity, ManageContract.View view) {
        this.mAcitivity = acitivity;
        this.mView = view;
    }
    @Override
    public void getContact() {
        ThreadUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                // 对应查询记录
                final Cursor cursor = mAcitivity.getContentResolver().query(SmsProvider.URI_SESSION,
                        null, null, new String[]{IMService.ACCOUNT}, null);

                // 设置adapter,然后显示数据
                ThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 假如没有数据的时候
                        mView.showSession(cursor);
                    }

                });
            }
        });
    }

    @Override
    public void getDialogueMessage(final String clickAccout, final String emotion) {
        ThreadUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                final Cursor c = mAcitivity.getContentResolver().query(
                        SmsProvider.URI_SMS, null,
                        "((from_account = ? and to_account = ?) or (from_account = ? and to_account = ?)) and type = ?",
                        new String[]{IMService.ACCOUNT, clickAccout, clickAccout, IMService.ACCOUNT,emotion}, null);

                //显示聊天消息
                ThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mView.showDialogueMessage(c);
                    }
                });
            }
        });
    }

    @Override
    public void inputEmotion(Cursor cursor) {
        // 拿到nickName-->显示效果
        int id = cursor.getInt(cursor.getColumnIndex("_id"));
        String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
        String fromAccount = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.FROM_ACCOUNT));
        String toAccount = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.TO_ACCOUNT));
        String facePic = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.FACE_PIC));

        if(!fromAccount.equals(IMService.ACCOUNT)){
            return;
        }

        Intent intent = new Intent(mAcitivity, InputEmotionActivity.class);

        intent.putExtra("id", id);
        intent.putExtra("toUser", toAccount);
        intent.putExtra("facePic",facePic);
        intent.putExtra("body",body);
        intent.putExtra("fromUser",fromAccount);

        mAcitivity.startActivity(intent);
    }
}
