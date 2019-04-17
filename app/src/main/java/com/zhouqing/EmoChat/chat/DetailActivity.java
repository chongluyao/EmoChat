package com.zhouqing.EmoChat.chat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhouqing.EmoChat.R;
import com.zhouqing.EmoChat.common.constant.Global;
import com.zhouqing.EmoChat.common.ui.BaseActivity;
import com.zhouqing.EmoChat.common.util.XmppUtil;
import com.zhouqing.EmoChat.db.ContactOpenHelper;


public class DetailActivity extends BaseActivity {
    private TextView tvNickname;
    private TextView tvAccount;
    private ImageView ivAvatar;

    private String mClickNickname;
    private String mClickAccount;
    private String mClickAvatar;
    @Override
    protected void initUi() {
        setContentView(R.layout.activity_detail);
        addActionBar("详细资料",true);

        tvNickname = (TextView) findViewById(R.id.nickname);
        tvAccount = (TextView) findViewById(R.id.account);
        ivAvatar = (ImageView) findViewById(R.id.avatar);
        Intent intent = getIntent();
        if (intent != null) {
            mClickAccount = intent.getStringExtra(ContactOpenHelper.ContactTable.ACCOUNT);
            mClickNickname = intent.getStringExtra(ContactOpenHelper.ContactTable.NICKNAME);
            mClickAvatar = intent.getStringExtra(ContactOpenHelper.ContactTable.AVATAR);
        }
        tvNickname.setText("昵称: "+mClickNickname);
        tvAccount.setText("账号: "+mClickAccount);
        if(mClickAvatar != null){
            ivAvatar.setImageResource(Global.AVATARS[Integer.parseInt(mClickAvatar)]);
        }

    }
    public void back(View view){
        finish();
    }
    public void sendMessage(View view){
        finish();
        Intent intent = new Intent(DetailActivity.this, ChatActivity.class);
        intent.putExtra(ContactOpenHelper.ContactTable.ACCOUNT, mClickAccount);
        intent.putExtra(ContactOpenHelper.ContactTable.NICKNAME, mClickNickname);
        startActivity(intent);
    }

    //删除好友
    public void deleteFriend(View view){
        AlertDialog.Builder dialog = new AlertDialog.Builder(DetailActivity.this);
        dialog.setTitle("删除好友");
        dialog.setMessage("确定要删除当前好友吗？");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                XmppUtil.deleteFriend(mClickAccount);
                dialog.dismiss();
                DetailActivity.this.finish();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
