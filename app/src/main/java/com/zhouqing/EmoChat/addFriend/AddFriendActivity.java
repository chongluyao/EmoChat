package com.zhouqing.EmoChat.addFriend;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.zhouqing.EmoChat.R;
import com.zhouqing.EmoChat.common.ui.BaseActivity;
import com.zhouqing.EmoChat.common.ui.view.MyEditText;
import com.zhouqing.EmoChat.common.util.ToastUtil;
import com.zhouqing.EmoChat.common.util.XmppUtil;
import com.zhouqing.EmoChat.service.IMService;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AddFriendActivity extends BaseActivity {

    private MyEditText searchAccount;
    private Button btnSearch;



    @Override
    protected void initUi() {
        setContentView(R.layout.activity_add_friend);
        addActionBar(getString(R.string.main_search_title),true);

        searchAccount = ((MyEditText) findViewById(R.id.search_account));
        btnSearch = (Button) findViewById(R.id.btn_search);
        Intent intent = getIntent();
        if(intent != null){
            String username = intent.getStringExtra("username");
            if(!TextUtils.isEmpty(username)){
                searchAccount.setText(username);
                searchAccount.getEditText().setSelection(username.length());
            }
        }

    }

    @Override
    protected void initListener() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = searchAccount.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    ToastUtil.showToast(AddFriendActivity.this, getString(R.string.main_search_account_empty));
                    return;
                }
                addFriend(name);
            }
        });
    }

    private void addFriend(final String name) {
        List<String> friendList = getFriendAccountList();
        System.out.println("friendList:"+friendList.toString());
        if(friendList.contains(name)){
            ToastUtil.showToast(AddFriendActivity.this,"对方已经是您的好友！");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                //添加好友前判断用户是否存在
                int state = XmppUtil.getUserState(name);
                if(state == 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(AddFriendActivity.this,"该用户不存在");
                        }
                    });
                    return;
                }
                if(state == 2){
                    System.out.println("离线");
                }
                else{
                    System.out.println("在线");
                }
                XMPPConnection conn = IMService.conn;
                String username = name + "@" + conn.getServiceName();
                Presence subscription = new Presence(Presence.Type.subscribe);
                subscription.setTo(username);
                conn.sendPacket(subscription);
                finish();
            }
        }).start();
    }

    public void back(View view) {
        finish();
    }

    public List<String> getFriendAccountList(){
        List<String> accounts = new ArrayList<>();
        Roster mRoster = IMService.conn.getRoster();
        if(mRoster != null){
            final Collection<RosterEntry> entries = mRoster.getEntries();
            for(RosterEntry entry:entries){
                accounts.add(entry.getUser().substring(0,entry.getUser().indexOf("@")));
            }
        }
        return accounts;
    }
}
