package com.zhouqing.EmoChat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


public class SmsOpenHelper extends SQLiteOpenHelper {
    public static final String T_SMS = "t_sms";

    public class SmsTable implements BaseColumns {
        /**
         * from_account;//发送者
         * to_account//接收者
         * body//消息内容
         * status//发送状态
         * type//消息类型
         * time//发送时间
         * session_account//会话id-->最近你和哪些人聊天了-->
         * face_pic//发送这条消息时人脸图片
         * emotion//用户给这条消息标注的情绪状态
         * <p>
         * //"我"  发送消息  给   "美女1" 对于我来讲  这个会话和谁的会话 ---->"美女1"
         * //"美女1"  发送消息  给   "我" 对于我来讲  这个会话和谁的会话 ---->"美女1"
         */
        public static final String FROM_ACCOUNT = "from_account";
        public static final String TO_ACCOUNT = "to_account";
        public static final String BODY = "body";
        public static final String STATUS = "status";
        public static final String TYPE = "type";
        public static final String TIME = "time";
        public static final String SESSION_ACCOUNT = "session_account";//相对于本地登录的那个账户
        public static final String MY_ACCOUNT = "my_account";//本地登录的用户
        public static final String FACE_PIC = "face_pic";
        public static final String EMOTION = "emotion";
        public static final String EMOTION_SHOW = "emotion_show";//聊天内容下方显示的表情内容

    }

    public SmsOpenHelper(Context context) {
        super(context, "sms.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE t_sms (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "from_account TEXT," +
                "to_account TEXT," +
                "body TEXT," +
                "status TEXT," +
                "type TEXT," +
                "time TEXT," +
                "session_account TEXT," +
                "my_account TEXT,"+
                "face_pic TEXT," +
                "emotion_show TEXT," +
                "emotion TEXT);";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
