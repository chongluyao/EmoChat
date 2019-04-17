package com.zhouqing.EmoChat.manage;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.zhouqing.EmoChat.R;
import com.zhouqing.EmoChat.common.constant.Global;
import com.zhouqing.EmoChat.common.ui.BaseActivity;
import com.zhouqing.EmoChat.common.util.XmppUtil;
import com.zhouqing.EmoChat.db.SmsOpenHelper;
import com.zhouqing.EmoChat.provider.SmsProvider;

import java.io.File;

public class InputEmotionActivity extends BaseActivity {

    private Spinner spEmotion;
    private ImageView ivHead;
    private TextView tvContent;
    private ImageView ivFacePic;

    private int id;
    private String fromUser;
    private String toUser;
    private String body;
    private String facePic;
    private String emotion;

    int currentUserAvatarId;

    //为Activity添加ToolBar
    protected void addActionBar(String title, boolean isBackable) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        if (!TextUtils.isEmpty(title)) {
            mActionBar.setTitle(title);
        }
        if (isBackable) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //退出前将表情信息保存
                    saveData();
                }
            });
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();//注释掉这行,back键不退出activity
        saveData();
    }

    @Override
    protected void initUi() {
        setContentView(R.layout.activity_input_emotion);
        ivHead = findViewById(R.id.head);
        tvContent = findViewById(R.id.content);
        ivFacePic = findViewById(R.id.iv_face_pic);
        spEmotion = findViewById(R.id.sp_emotion);
        ArrayAdapter<String> shopAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,Global.EMOTION_ARRAY);
        spEmotion.setAdapter(shopAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            fromUser = intent.getStringExtra("fromUser");
            toUser = intent.getStringExtra("toUser");
            body = intent.getStringExtra("body");
            facePic = intent.getStringExtra("facePic");
            id = intent.getIntExtra("id",-1);
        }
        currentUserAvatarId = XmppUtil.getCurrentUserAvatar();


        if(currentUserAvatarId != -1){
            ivHead.setImageResource(Global.AVATARS[currentUserAvatarId]);
        }
        tvContent.setText(body);
        String path = getFilePath(fromUser,toUser,facePic);
        ivFacePic.setImageURI(Uri.fromFile(new File(path)));
        addActionBar(Global.accountToNickName(toUser), true);
    }


    public String getFilePath(String fromUser,String toUser,String facePic){
        String fromNick = fromUser.substring(0,fromUser.indexOf("@"));
        String toNick = toUser.substring(0,toUser.indexOf("@"));
        return Global.PROJECT_FILE_PATH + fromNick+"/"+toNick+"/"+facePic;
    }

    @Override
    protected void initListener() {
        super.initListener();
        spEmotion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                emotion = Global.EMOTION_ARRAY[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
    }

    public void saveData(){
        ContentValues values = new ContentValues();
        values.put(SmsOpenHelper.SmsTable.EMOTION,emotion);
        //values.put(SmsOpenHelper.SmsTable.EMOTION,"");
        InputEmotionActivity.this.getContentResolver().update(SmsProvider.URI_SMS, values,"_id = ? and from_account = ? and to_account = ? and body = ? and face_pic = ?",new String[]{
                id+"",fromUser,toUser,body,facePic});
        finish();
    }
}
