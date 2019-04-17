package com.zhouqing.EmoChat.main;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;

import com.zhouqing.EmoChat.chat.ChatActivity;
import com.zhouqing.EmoChat.common.util.ThreadUtil;
import com.zhouqing.EmoChat.db.ContactOpenHelper;
import com.zhouqing.EmoChat.provider.ContactProvider;
import com.zhouqing.EmoChat.provider.SmsProvider;
import com.zhouqing.EmoChat.service.IMService;


public class MessagePresenter implements MessageContract.Presenter {
    private Activity mAcitivity;
    private MessageContract.View mView;

    public MessagePresenter(Activity acitivity, MessageContract.View view) {
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
                       mView.showContact(cursor);
                    }

                });
            }
        });
    }

    @Override
    public void chat(String account) {
        // 拿到nickName-->显示效果
        String nickname = getNickNameByAccount(account);

        Intent intent = new Intent(mAcitivity, ChatActivity.class);

        intent.putExtra(ContactOpenHelper.ContactTable.ACCOUNT, account);
        intent.putExtra(ContactOpenHelper.ContactTable.NICKNAME, nickname);

        mAcitivity.startActivity(intent);
    }

    @Override
    public void deleteSession(String clickAccount) {
        mAcitivity.getContentResolver().delete(
                SmsProvider.URI_SMS,
                "(from_account = ? and to_account = ?) or (from_account = ? and to_account = ?)",
                new String[]{IMService.ACCOUNT, clickAccount, clickAccount, IMService.ACCOUNT});
    }

    /**
     * 通过账户获取昵称
     */
    private String getNickNameByAccount(String account) {
        String nickName = "";
        Cursor c = mAcitivity.getContentResolver().query(ContactProvider.URI_CONTACT, null,
                ContactOpenHelper.ContactTable.ACCOUNT + "=?", new String[]{account}, null);
        if (c.getCount() > 0) {// 有数据
            c.moveToFirst();
            nickName = c.getString(c.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));
        }
        return nickName;
    }
}
