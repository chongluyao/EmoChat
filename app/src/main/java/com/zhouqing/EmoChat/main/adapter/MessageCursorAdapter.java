package com.zhouqing.EmoChat.main.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhouqing.EmoChat.R;
import com.zhouqing.EmoChat.common.constant.Global;
import com.zhouqing.EmoChat.common.util.EmotionUtil;
import com.zhouqing.EmoChat.common.util.SpanStringUtil;
import com.zhouqing.EmoChat.common.util.XmppUtil;
import com.zhouqing.EmoChat.db.ContactOpenHelper;
import com.zhouqing.EmoChat.db.SmsOpenHelper;
import com.zhouqing.EmoChat.provider.ContactProvider;

import java.text.SimpleDateFormat;
import java.util.Date;



public class MessageCursorAdapter extends CursorAdapter {
    private Cursor c;
    private Context mActivity;

    public MessageCursorAdapter(Context context, Cursor c) {
        super(context, c);
        this.c = c;
        this.mActivity = context;
    }

    // 如果convertView==null,返回一个具体的根视图
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = View.inflate(context, R.layout.item_message_fragment, null);
        return view;
    }

    // 设置数据显示数据
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvTime = (TextView) view.findViewById(R.id.time);
        TextView tvBody = (TextView) view.findViewById(R.id.body);
        TextView tvNickName = (TextView) view.findViewById(R.id.nickname);
        ImageView ivHead = view.findViewById(R.id.head);

        String time = c.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.TIME));
        String body = c.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
        String acccount = c.getString(c.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));
        String avatar = null;
        if(XmppUtil.getOtherUserAvatar(acccount) != null){
            avatar = XmppUtil.getOtherUserAvatar(acccount);
        }

        String nickName = getNickNameByAccount(acccount);
        // acccount 但是在聊天记录表(sms)里面没有保存别名信息,只有(Contact表里面有)
        tvBody.setText(SpanStringUtil.getEmotionContent(EmotionUtil.EMOTION_CLASSIC_TYPE, mActivity, tvBody, body));
        tvNickName.setText(nickName);

        String formatTime = new SimpleDateFormat("HH:mm").format(new Date(Long
                .parseLong(time)));
        tvTime.setText(formatTime);
        if(avatar != null){
            ivHead.setImageResource(Global.AVATARS[Integer.parseInt(avatar)]);
        }
        else{
            ivHead.setImageResource(R.mipmap.ic_launcher);
        }
    }

    /**
     * 通过账户获取昵称
     */
    private String getNickNameByAccount(String account) {
        String nickName = "";
        Cursor c = mActivity.getContentResolver().query(ContactProvider.URI_CONTACT, null,
                ContactOpenHelper.ContactTable.ACCOUNT + "=?", new String[]{account}, null);
        if (c.getCount() > 0) {// 有数据
            c.moveToFirst();
            nickName = c.getString(c.getColumnIndex(ContactOpenHelper.ContactTable.NICKNAME));
        }
        return nickName;
    }
}
