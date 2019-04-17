package com.zhouqing.EmoChat.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xys.libzxing.zxing.encoding.EncodingUtils;
import com.zhouqing.EmoChat.R;
import com.zhouqing.EmoChat.avatar.AvatarActivity;
import com.zhouqing.EmoChat.common.constant.Global;
import com.zhouqing.EmoChat.common.ui.BaseFragment;
import com.zhouqing.EmoChat.common.ui.view.SettingView;
import com.zhouqing.EmoChat.common.util.DensityUtil;
import com.zhouqing.EmoChat.common.util.ToastUtil;
import com.zhouqing.EmoChat.common.util.XmppUtil;
import com.zhouqing.EmoChat.db.SmsOpenHelper;
import com.zhouqing.EmoChat.main.util.FileUtil;
import com.zhouqing.EmoChat.manage.ManageActivity;
import com.zhouqing.EmoChat.provider.SmsProvider;
import com.zhouqing.EmoChat.service.IMService;
import com.zhouqing.EmoChat.setting.SettingActivity;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import static android.app.Activity.RESULT_OK;


public class MeFragment extends BaseFragment implements View.OnClickListener {
    private LinearLayout ll_account;
    private TextView mNickname;
    private TextView mAccount;
    private SettingView avatar_pic;
    private SettingView mBtnSetting;
    private ImageView ivQRCode;
    private ImageView avatar;
    private SettingView manage;
    private SettingView save;
    private String account;
    private String nickname;


    private int selectedAvatar;

    @Override
    protected View initUi() {
        View view = View.inflate(mActivity, R.layout.fragment_me, null);
        ll_account = (LinearLayout) view.findViewById(R.id.ll_account);
        mNickname = (TextView) view.findViewById(R.id.nickname);
        mAccount = (TextView) view.findViewById(R.id.account);
        mBtnSetting = (SettingView) view.findViewById(R.id.setting);
        ivQRCode = (ImageView) view.findViewById(R.id.iv_qr_code);
        manage = view.findViewById(R.id.manage);
        save = view.findViewById(R.id.save);
        avatar_pic = view.findViewById(R.id.avatar_pic);
        avatar = view.findViewById(R.id.avatar);
        return view;
    }

    @Override
    protected void initData() {
        super.initData();
        account = IMService.ACCOUNT;
        nickname = account.substring(0, account.indexOf("@"));
        mNickname.setText(getString(R.string.main_me_message_nickname) + nickname);
        mAccount.setText(getString(R.string.main_me_message_account) + account);
        if(XmppUtil.getCurrentUserAvatar() != -1){
            avatar.setImageResource(Global.AVATARS[XmppUtil.getCurrentUserAvatar()]);
        }
    }

    @Override
    protected void initListener() {
        mBtnSetting.setOnClickListener(this);
        avatar_pic.setOnClickListener(this);
        ivQRCode.setOnClickListener(this);
        ll_account.setOnClickListener(this);
        manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),ManageActivity.class));
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity()).setTitle("确认导出数据库内容到文件？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“确认”后的操作
                                saveChatContentToFile();
                            }
                        })
                        .setNegativeButton("返回", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“返回”后的操作,这里不设置没有任何操作

                            }
                        }).show();
            }
        });

    }

    public void saveChatContentToFile(){
        //获取所有聊天对象
        final Cursor sessionCursor = getActivity().getContentResolver().query(SmsProvider.URI_SESSION,
                null, null, new String[]{IMService.ACCOUNT}, null);
        if(sessionCursor!=null&&sessionCursor.moveToFirst()){
            String[] sessions = new String[sessionCursor.getCount()];
            int index = 0;
            do{
                String account = sessionCursor.getString(sessionCursor.getColumnIndex(SmsOpenHelper.SmsTable.SESSION_ACCOUNT));
                sessions[index++] = account;
            }while(sessionCursor.moveToNext());
            //ToastUtil.showToast(ManageActivity.this, Arrays.toString(sessions));
            //获取每个聊天对象的各种聊天场景下的聊天内容
            for(String session:sessions){
                for(String emotionItem:Global.CONVERSATION_ARRAY){
                    StringBuilder sb = new StringBuilder();
                    final Cursor chatCursor = getActivity().getContentResolver().query(
                            SmsProvider.URI_SMS, null,
                            "((from_account = ? and to_account = ?) or (from_account = ? and to_account = ?)) and type = ?",
                            new String[]{IMService.ACCOUNT, session, session, IMService.ACCOUNT,emotionItem}, null);
                    if(chatCursor != null && chatCursor.moveToFirst()){
                        do{
                            /**
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
                             **/
                            int id = chatCursor.getInt(chatCursor.getColumnIndex("_id"));
                            String fromAccount = Global.accountToNickName(chatCursor.getString(chatCursor.getColumnIndex(SmsOpenHelper.SmsTable.FROM_ACCOUNT)));
                            String toAccount = Global.accountToNickName(chatCursor.getString(chatCursor.getColumnIndex(SmsOpenHelper.SmsTable.TO_ACCOUNT)));
                            String body = chatCursor.getString(chatCursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));
                            String facePic = chatCursor.getString(chatCursor.getColumnIndex(SmsOpenHelper.SmsTable.FACE_PIC));
                            String type = chatCursor.getString(chatCursor.getColumnIndex(SmsOpenHelper.SmsTable.TYPE));
                            String time = chatCursor.getString(chatCursor.getColumnIndex(SmsOpenHelper.SmsTable.TIME));
                            String emotion = chatCursor.getString(chatCursor.getColumnIndex(SmsOpenHelper.SmsTable.EMOTION));
                            String myAccount = chatCursor.getString(chatCursor.getColumnIndex(SmsOpenHelper.SmsTable.MY_ACCOUNT));
                            sb.append(id+","+fromAccount+","+toAccount+","+body+","+facePic+","+type+","+time+","+emotion+","+myAccount+"\n");
                        }while(chatCursor.moveToNext());
                        String path = Global.PROJECT_FILE_PATH +Global.accountToNickName(IMService.ACCOUNT) +"/"+Global.accountToNickName(session)+"/"+emotionItem+"/";
                        FileUtil.writeStrToPath(sb.toString(),path);
                    }
                }
            }
        }
        ToastUtil.showToast(getActivity(),"导出成功！");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting:
                startActivity(new Intent(mActivity, SettingActivity.class));
                break;
            case R.id.iv_qr_code:
            case R.id.ll_account:
                showQrCodeDialog();
                break;
            case R.id.avatar_pic:
                selectAvatar();
                break;

        }
    }

    private void showQrCodeDialog() {
        View view = View.inflate(mActivity, R.layout.layout_qrcode, null);
        ImageView iv_qr_code = (ImageView) view.findViewById(R.id.iv_qr_code);
        TextView iv_nickname = (TextView) view.findViewById(R.id.nickname);
        TextView iv_account = (TextView) view.findViewById(R.id.account);
        ImageView avatar = view.findViewById(R.id.avatar);

        iv_nickname.setText(getString(R.string.main_me_message_nickname) + nickname);
        iv_account.setText(getString(R.string.main_me_message_account) + account);
        int avatarId = -1;
        if(XmppUtil.getCurrentUserAvatar() != -1){
            avatarId = Global.AVATARS[XmppUtil.getCurrentUserAvatar()];
        }
        Bitmap qrCode = EncodingUtils.createQRCode(account,
                DensityUtil.dp2px(mActivity, 250),
                DensityUtil.dp2px(mActivity, 250),
                BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        if(avatarId != -1){
            qrCode = EncodingUtils.createQRCode(account,
                    DensityUtil.dp2px(mActivity, 250),
                    DensityUtil.dp2px(mActivity, 250),
                    BitmapFactory.decodeResource(getResources(), avatarId));
            avatar.setImageResource(avatarId);
        }
        iv_qr_code.setImageBitmap(qrCode);

        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setView(view);
        builder.setCancelable(true);
        final AlertDialog dialog = builder.show();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    //选择头像
    public void selectAvatar(){
        Intent intent = new Intent(getActivity(),AvatarActivity.class);
        startActivityForResult(intent,10001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 10001:
                if(resultCode == RESULT_OK){
                    selectedAvatar = data.getIntExtra("selectedAvatar",-1);
                    if(selectedAvatar != -1){
                        avatar.setImageResource(Global.AVATARS[selectedAvatar]);
                        XMPPConnection connection = XmppUtil.getConnection();
                        if(connection != null){
                            //Toast.makeText(getActivity(),selectedAvatar+"",Toast.LENGTH_SHORT).show();
                            VCard vCard = new VCard();
                            vCard.setField("avatarId",String.valueOf(selectedAvatar));
                            try {
                                vCard.save(connection);
                            } catch (XMPPException e) {
                                e.printStackTrace();
                            }
                        }
                }
                    //Toast.makeText(getActivity(),selectedAvatar+"",Toast.LENGTH_SHORT).show();
                }
        }
    }


}
