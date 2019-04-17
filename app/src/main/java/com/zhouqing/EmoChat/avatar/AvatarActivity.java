package com.zhouqing.EmoChat.avatar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhouqing.EmoChat.R;
import com.zhouqing.EmoChat.common.constant.Global;

public class AvatarActivity extends AppCompatActivity {

    private GridView gv_avatars;
    private TextView tv_cancel;
    private TextView tv_confirm;
    private ImageView iv_avatar;

    Integer selectedPos = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);
        gv_avatars = (GridView) findViewById(R.id.gv_avatars);
        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        tv_cancel= (TextView) findViewById(R.id.tv_cancel);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);
        gv_avatars.setAdapter(new ImageAdapter(this));
        gv_avatars.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(AvatarActivity.this,"you select "+position,Toast.LENGTH_SHORT).show();
                if(selectedPos == null){
                    selectedPos = position;
                    tv_cancel.setTextColor(Color.BLACK);
                    tv_confirm.setTextColor(Color.BLACK);
                    tv_cancel.setEnabled(true);
                    tv_confirm.setEnabled(true);
                    iv_avatar.setImageResource(Global.AVATARS[position]);
                }
                else if(selectedPos == position){
                    selectedPos = null;
                    tv_cancel.setTextColor(Color.GRAY);
                    tv_confirm.setTextColor(Color.GRAY);
                    tv_cancel.setEnabled(false);
                    tv_confirm.setEnabled(false);
                    iv_avatar.setImageBitmap(null);
                }
                else{
                    selectedPos = position;
                    iv_avatar.setImageResource(Global.AVATARS[position]);
                }
            }
        });
        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(AvatarActivity.this,"you select "+selectedPos,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                if(selectedPos == null){
                    intent.putExtra("selectedAvatar",-1);
                }
                else{
                    intent.putExtra("selectedAvatar",selectedPos);
                    setResult(RESULT_OK,intent);
                }
                AvatarActivity.this.finish();
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPos = null;
                tv_cancel.setTextColor(Color.GRAY);
                tv_confirm.setTextColor(Color.GRAY);
                tv_cancel.setEnabled(false);
                tv_confirm.setEnabled(false);
                iv_avatar.setImageBitmap(null);
            }
        });
    }

    class ImageAdapter extends BaseAdapter{
        private Context mContext;

        public ImageAdapter(Context context){
            mContext = context;
        }

        @Override
        public int getCount() {
            return Global.AVATARS.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if(convertView == null){
                imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            else{
                imageView = (ImageView)convertView;
            }
            imageView.setImageResource(Global.AVATARS[position]);
            return imageView;
        }
    }

}
