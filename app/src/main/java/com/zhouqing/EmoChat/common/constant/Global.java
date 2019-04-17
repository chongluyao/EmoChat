package com.zhouqing.EmoChat.common.constant;


import android.os.Environment;

import com.zhouqing.EmoChat.R;
import com.zhouqing.EmoChat.common.AppApplication;
import com.zhouqing.EmoChat.common.util.SPUtil;
public class Global {
    public static final String HOST = (String) SPUtil.get(AppApplication.getInstance(), "ip", "47.107.244.194");
    public static final int PORT = 5222;

    public static final int AVATAR_NUM = 25;

    public static Integer[] AVATARS = {
            R.drawable.avatar01,R.drawable.avatar02,R.drawable.avatar03,R.drawable.avatar04,R.drawable.avatar05,
            R.drawable.avatar06,R.drawable.avatar07,R.drawable.avatar08,R.drawable.avatar09,R.drawable.avatar10,
            R.drawable.avatar11,R.drawable.avatar12,R.drawable.avatar13,R.drawable.avatar14,R.drawable.avatar15,
            R.drawable.avatar16,R.drawable.avatar17,R.drawable.avatar18,R.drawable.avatar19,R.drawable.avatar20,
            R.drawable.avatar21,R.drawable.avatar22,R.drawable.avatar23,R.drawable.avatar24,R.drawable.avatar25
    };

    //项目存储文件的路径
    public static String PROJECT_FILE_PATH
            = Environment.getExternalStorageDirectory() + "/CameraEmojiDemo/";

    public static final String[] EMOTION_ARRAY = {"Neutral","Angry","Disgust","Fear","Happy","Sad","Surprise"};
    public static final String[] CONVERSATION_ARRAY = {"场景1","场景2","场景3","场景4","场景5","场景6"};

    public static String accountToNickName(String account){
        return account.substring(0,account.indexOf("@"));
    }
}
