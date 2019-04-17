package com.zhouqing.EmoChat.setting;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.zhouqing.EmoChat.R;
import com.zhouqing.EmoChat.common.ui.BaseActivity;
import com.zhouqing.EmoChat.common.util.SPUtil;
import com.zhouqing.EmoChat.main.util.FragmentFactory;
import com.zhouqing.EmoChat.service.IMService;


public class SettingActivity extends BaseActivity {

    @Override
    protected void initUi() {
        setContentView(R.layout.activity_setting);
        addActionBar(getString(R.string.setting_title),true);

    }

    public void setting(View view) {
        View v = View.inflate(this, R.layout.layout_logout_dialog, null);
        Button btnZhuxiao = (Button) v.findViewById(R.id.zhuxiao);
        Button btnTuichu = (Button) v.findViewById(R.id.tuichu);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alertDialog = builder.create();
        alertDialog.setView(v);
        alertDialog.setCanceledOnTouchOutside(true);

        btnZhuxiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                XmppUtil.deleteAccount(IMService.conn);
                stopService(new Intent(SettingActivity.this, IMService.class));
                FragmentFactory.clearAll();
                restartApplication();
            }
        });

        btnTuichu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                stopService(new Intent(SettingActivity.this,IMService.class));
                killAll();
            }
        });

        alertDialog.show();
    }
    public void description(View view){
        View v = View.inflate(this, R.layout.layout_description_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alertDialog = builder.create();
        alertDialog.setView(v);
        alertDialog.setCanceledOnTouchOutside(true);


        alertDialog.show();
    }
    /**
     * 重新启动应用
     * */
    private void restartApplication() {
        SPUtil.put(SettingActivity.this,"isAutoLogin",false);

        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
