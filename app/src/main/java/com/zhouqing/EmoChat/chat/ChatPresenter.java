package com.zhouqing.EmoChat.chat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.text.TextUtils;

import com.zhouqing.EmoChat.R;
import com.zhouqing.EmoChat.common.util.ThreadUtil;
import com.zhouqing.EmoChat.common.util.ToastUtil;
import com.zhouqing.EmoChat.provider.SmsProvider;
import com.zhouqing.EmoChat.service.IMService;

import org.jivesoftware.smack.packet.Message;



public class ChatPresenter implements ChatContract.Presenter {
    private Context mContext;
    private ChatContract.View mView;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myBinder = (IMService.MyBinder) iBinder;
            myBinder.linkToDeath(mDeathRecipient, 0);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private IMService.MyBinder myBinder;//服务
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            myBinder.unlinkToDeath(mDeathRecipient, 0);
            myBinder = null;
            mContext.bindService(new Intent(mContext, IMService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    };

    public ChatPresenter(Context context, ChatContract.View view) {
        this.mContext = context;
        this.mView = view;
    }

    @Override
    public void getDialogueMessage(final String clickAccout) {
        ThreadUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                final Cursor c = mContext.getContentResolver().query(
                        SmsProvider.URI_SMS, null,
                        "(from_account = ? and to_account = ?) or (from_account = ? and to_account = ?)",
                        new String[]{IMService.ACCOUNT, clickAccout, clickAccout, IMService.ACCOUNT}, null);

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
    public void sendMessage(final String clickAccount, final String facePic,final String type,final String emotionShow) {
        final String body = mView.getMessage();
        if (!TextUtils.isEmpty(body)) {
            ThreadUtil.runOnThread(new Runnable() {
                @Override
                public void run() {  //发送消息的三个步骤

                    //3.创建消息对象
                    Message msg = new Message();
                    msg.setFrom(IMService.ACCOUNT);
                    msg.setTo(clickAccount);
                    msg.setBody(body);
                    msg.setType(Message.Type.chat);
                    msg.setProperty("key", "value");
                    msg.setProperty("emotion",emotionShow);
                    //发送消息 并保存消息 通过绑定服务调用服务中的方法
                    if (myBinder != null) {
                        myBinder.sendMessage(msg,facePic,type,emotionShow);
                    } else {
                        ToastUtil.showToastSafe(mContext, mContext.getString(R.string.chat_send_message_error));
                    }

                    ThreadUtil.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mView.clearMessage();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void bindIMService() {
        mContext.bindService(new Intent(mContext, IMService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void unbindIMService() {
        mContext.unbindService(mServiceConnection);
    }



}
