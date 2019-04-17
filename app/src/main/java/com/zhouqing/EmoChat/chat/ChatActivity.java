package com.zhouqing.EmoChat.chat;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zhouqing.EmoChat.R;
import com.zhouqing.EmoChat.common.AppApplication;
import com.zhouqing.EmoChat.common.constant.Global;
import com.zhouqing.EmoChat.common.ui.view.AutoFitTextureView;
import com.zhouqing.EmoChat.common.util.EmotionUtil;
import com.zhouqing.EmoChat.common.util.SpanStringUtil;
import com.zhouqing.EmoChat.common.util.XmppUtil;
import com.zhouqing.EmoChat.db.ContactOpenHelper;
import com.zhouqing.EmoChat.db.SmsOpenHelper;
import com.zhouqing.EmoChat.provider.SmsProvider;
import com.zhouqing.EmoChat.service.IMService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ChatActivity extends AppCompatActivity implements ChatContract.View, View.OnClickListener {

    private ListView mListView;
    private EditText etChatMessage;
    private Button btnSend;
    private Spinner spEmotion;
    private boolean startEntering;
    private String emotion="";//标记当前对话属于哪种情绪氛围
    //private ImageView iv_more;
    //private ImageView iv_emotion;
    //private FrameLayout fl_emotion;

    private InputMethodManager imm;//软键盘服务

    private boolean isEmotionShowing;//表情界面是否显示
    private boolean isMoreShowing;//更多界面是否显示
    private String mClickAccount;
    private String mClickNickname;
    private ContentObserver mContentObserver = new MyContentObserver(new Handler());//数据库的观察者
    private MyCursorAdapter mCursorAdapter;
    private ActionBar mActionBar;
    private ChatContract.Presenter mPresenter;

    int currentUserAvatarId;
    String otherUserAvatarId;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppApplication.getInstance().addActivity(this);
        //注册内容观察者
        getContentResolver().registerContentObserver(SmsProvider.URI_SMS, true, mContentObserver);
        //绑定服务
        mPresenter = new ChatPresenter(this, this);
        mPresenter.bindIMService();
        initUi();
        initData();
        initListener();

        mFacesSurfaceView = (AutoFitTextureView) findViewById(R.id.faces);
        mPreviewSurfaceView = (AutoFitTextureView) findViewById(R.id.texture);

        mFacesSurfaceView.setOpaque(false);

        mFacePaint.setStyle(Paint.Style.STROKE);
        mFacePaint.setColor(Color.RED);

        // mFile = new File(getExternalFilesDir(null), "pic.jpg");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppApplication.getInstance().removeActivity(this);
        getContentResolver().unregisterContentObserver(mContentObserver);
        //解除绑定
        mPresenter.unbindIMService();
    }

    protected void initUi() {
        setContentView(R.layout.activity_chat);
        addActionBar("", true);

        mListView = (ListView) findViewById(R.id.listview);
        etChatMessage = (EditText) findViewById(R.id.et_chat_message);
        //iv_more = (ImageView) findViewById(R.id.iv_more);
        //iv_emotion = (ImageView) findViewById(R.id.iv_emotion);
        btnSend = (Button) findViewById(R.id.btn_send);
        //fl_emotion = (FrameLayout) findViewById(R.id.fl_emotion);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        spEmotion = findViewById(R.id.sp_emotion);
        ArrayAdapter<String> shopAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,Global.CONVERSATION_ARRAY);
        spEmotion.setAdapter(shopAdapter);


        //获取用户名以及账号
        Intent intent = getIntent();
        if (intent != null) {
            mClickAccount = intent.getStringExtra(ContactOpenHelper.ContactTable.ACCOUNT);
            mClickNickname = intent.getStringExtra(ContactOpenHelper.ContactTable.NICKNAME);
            mActionBar.setTitle(mClickNickname);//更改actionBar的名称
            otherUserAvatarId = XmppUtil.getOtherUserAvatar(mClickAccount);
            currentUserAvatarId = XmppUtil.getCurrentUserAvatar();
        }
    }

    protected void initData() {
        mPresenter.getDialogueMessage(mClickAccount);
        startEntering = false;
    }

    protected void initListener() {
        btnSend.setOnClickListener(this);
        //iv_emotion.setOnClickListener(this);
        //iv_more.setOnClickListener(this);
        etChatMessage.setOnClickListener(this);

        spEmotion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                emotion = Global.CONVERSATION_ARRAY[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //编辑框焦点事件
//        etChatMessage.setOnFocusChangeListener(new android.view.View.
//                OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    //ToastUtil.showToast(ChatActivity.this,"获得焦点！");
//                    openCameraPreview();
//                    mPreviewSurfaceView.setVisibility(View.VISIBLE);
//                    mFacesSurfaceView.setVisibility(View.VISIBLE);
//                } else {
//                    // 此处为失去焦点时的处理内容
//                    //ToastUtil.showToast(ChatActivity.this,"失去焦点！");
//                    closeCameraPreview();
//                    mPreviewSurfaceView.setVisibility(View.GONE);
//                    mFacesSurfaceView.setVisibility(View.GONE);
//                }
//            }
//        });
        etChatMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String s = editable.toString();
                if (TextUtils.isEmpty(s)) {
                    btnSend.setVisibility(View.GONE);
                    //iv_more.setVisibility(View.VISIBLE);
                } else {
                    btnSend.setVisibility(View.VISIBLE);
                    //iv_more.setVisibility(View.GONE);
                }
                if (!startEntering && getMessage().length() > 0) {
                    showToast(getMessage());
                    openCameraPreview();
                    // EmoCameraManager.startCamera();
                    startEntering = true;
                }
            }
        });
        //当ListView触摸的时候 强制关闭软键盘
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(etChatMessage.getWindowToken(), 0);
                }
                if (isEmotionShowing || isMoreShowing) {
                    //fl_emotion.setVisibility(View.GONE);
                    isMoreShowing = false;
                    isEmotionShowing = false;
                }
                return false;
            }
        });

    }

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
                    finish();
                }
            });
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 让Fragment能获取edit的内容
     */
    public String getText() {
        return etChatMessage.getText().toString();
    }

    /**
     * 让Fragment能获取edit
     */
    public EditText getEdit() {
        return etChatMessage;
    }

    /**
     * 让Fragment更改fragment的内容
     */
    public void setText(CharSequence text) {
        etChatMessage.setText(text);
        if (!TextUtils.isEmpty(text)) {
            etChatMessage.setSelection(text.length());
        }
    }

    //改变表情界面状态
    private void changeEmotionStatus() {
        if (!isMoreShowing) {
            if (!isEmotionShowing) {
                //fl_emotion.setVisibility(View.VISIBLE);
                imm.hideSoftInputFromWindow(etChatMessage.getWindowToken(), 0);
                isEmotionShowing = true;
                isMoreShowing = false;
                showEmotionFragment();

            } else {
                //fl_emotion.setVisibility(View.GONE);
                imm.showSoftInput(etChatMessage, InputMethodManager.SHOW_IMPLICIT);
                isEmotionShowing = false;
                isMoreShowing = false;
            }
        } else {
            showEmotionFragment();
            isEmotionShowing = true;
            isMoreShowing = false;
        }
    }

    //点击选择更多
//    private void selectMore() {
//        if (!isEmotionShowing) {
//            if (!isMoreShowing) {
//                fl_emotion.setVisibility(View.VISIBLE);
//                imm.hideSoftInputFromWindow(etChatMessage.getWindowToken(), 0);
//                isEmotionShowing = false;
//                isMoreShowing = true;
//                showMoreFrament();
//
//            } else {
//                fl_emotion.setVisibility(View.GONE);
//                imm.showSoftInput(etChatMessage, InputMethodManager.SHOW_IMPLICIT);
//                isEmotionShowing = false;
//                isMoreShowing = false;
//            }
//        } else {
//            showMoreFrament();
//            isEmotionShowing = false;
//            isMoreShowing = true;
//        }
//    }

    /**
     * 显示表情界面
     */
    private void showEmotionFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_emotion, new ChatEmotionFragment())
                .commit();
    }

    /**
     * 显示更多界面
     */
//    private void showMoreFrament() {
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.fl_emotion, new ChatMoreFragment())
//                .commit();
//    }

    @Override
    public void onBackPressed() {
        if (isEmotionShowing) {
            isEmotionShowing = false;
            //fl_emotion.setVisibility(View.GONE);
            return;
        }
        if (isMoreShowing) {
            isMoreShowing = false;
            //fl_emotion.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                //etChatMessage.clearFocus();
                String prefix = "" + System.currentTimeMillis();
                //存储照片与文字
                String path = Global.PROJECT_FILE_PATH +Global.accountToNickName(IMService.ACCOUNT) +"/"+mClickNickname+"/";
                if (!new File(path).exists()) {
                    new File(path).mkdirs();
                }
                mFilePic = new File(path,  prefix + ".png");
                mFileText = new File(path, prefix + ".txt");
                // save picture
                takePicture();
                // save text
                saveText(getMessage());
                mPresenter.sendMessage(mClickAccount,prefix + ".png",emotion);
                startEntering = false;
                closeCameraPreview();
                break;
            case R.id.iv_emotion:
                changeEmotionStatus();
                break;
//            case R.id.iv_more:
//                selectMore();
//                break;
//            case R.id.et_chat_message:
//                //隐藏表情界面
//                if (isEmotionShowing | isMoreShowing) {
//                    //fl_emotion.setVisibility(View.GONE);
//                    isEmotionShowing = false;
//                    isMoreShowing = false;
//                }
//                break;
        }
    }

    public void saveText(String message) {
        try {
            FileOutputStream fos = new FileOutputStream(mFileText);
            OutputStreamWriter writer=new OutputStreamWriter(fos,"utf-8");
            writer.write(message);
            writer.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * ListView的适配器
     */
    private class MyCursorAdapter extends CursorAdapter {
        private static final int SEND = 0;
        private static final int RECEIVE = 1;
        private Cursor cursor;

        public MyCursorAdapter(Context context, Cursor c) {
            super(context, c);
            cursor = c;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return null;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
        }

        //获取view的类型
        @Override
        public int getItemViewType(int position) {
            cursor.moveToPosition(position);
            // 取出消息的创建者
            String fromAccount = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.FROM_ACCOUNT));
            if (IMService.ACCOUNT.equals(fromAccount)) {// 接收
                return SEND;
            } else {// 发送
                return RECEIVE;
            }
            // return super.getItemViewType(position);// 0 1
            // 接收--->如果当前的账号 不等于 消息的创建者
            // 发送
        }

        //总共有几种类型的View
        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (getItemViewType(position) == RECEIVE) {
                if (convertView == null) {
                    convertView = View.inflate(ChatActivity.this, R.layout.item_chat_receiver, null);
                    holder = new ViewHolder();
                    convertView.setTag(holder);

                    // holder赋值
                    holder.time = (TextView) convertView.findViewById(R.id.time);
                    holder.body = (TextView) convertView.findViewById(R.id.content);
                    holder.head = (ImageView) convertView.findViewById(R.id.head);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                // 得到数据,展示数据
                if(otherUserAvatarId != null){
                    holder.head.setImageResource(Global.AVATARS[Integer.parseInt(otherUserAvatarId)]);
                }
            } else {// 发送
                if (convertView == null) {
                    convertView = View.inflate(ChatActivity.this, R.layout.item_chat_send, null);
                    holder = new ViewHolder();
                    convertView.setTag(holder);

                    // holder赋值
                    holder.time = (TextView) convertView.findViewById(R.id.time);
                    holder.body = (TextView) convertView.findViewById(R.id.content);
                    holder.head = (ImageView) convertView.findViewById(R.id.head);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                 //得到数据,展示数据
                if(currentUserAvatarId != -1){
                    holder.head.setImageResource(Global.AVATARS[currentUserAvatarId]);
                }
            }
            // 得到数据,展示数据
            cursor.moveToPosition(position);

            String time = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.TIME));
            String body = cursor.getString(cursor.getColumnIndex(SmsOpenHelper.SmsTable.BODY));

            String formatTime = new SimpleDateFormat("HH:mm").format(new Date(Long
                    .parseLong(time)));

            holder.time.setText(formatTime);

            //使用SpanImage代替特殊字符
            System.out.println("time:"+time+",body:"+body);
            SpannableString emotionContent = SpanStringUtil.getEmotionContent(EmotionUtil.EMOTION_CLASSIC_TYPE,
                    ChatActivity.this, holder.body, body);
            holder.body.setText(emotionContent);


            return convertView;
        }

        private class ViewHolder {
            TextView body;
            TextView time;
            ImageView head;

        }
    }


    /**
     * SmsOpenHelper的观察者
     */
    private class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            System.out.println("观察到改变");
            mPresenter.getDialogueMessage(mClickAccount);
        }
    }


    @Override
    public void setPresenter(ChatContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void showDialogueMessage(Cursor cursor) {
        if (mCursorAdapter != null) {
            mCursorAdapter.getCursor().requery();
            mListView.setSelection(cursor.getCount() - 1);//让ListView到达最后一列
            return;
        }

        mCursorAdapter = new MyCursorAdapter(ChatActivity.this, cursor);
        mListView.setAdapter(mCursorAdapter);
        mListView.setSelection(mCursorAdapter.getCount() - 1);

    }

    @Override
    public String getMessage() {
        return getText();
    }

    @Override
    public void clearMessage() {
        etChatMessage.setText("");
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final String TAG = "CameraAndFaceDetect";

    private int mState = STATE_PREVIEW;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_PICTURE_TAKEN = 4;

    private AutoFitTextureView mPreviewSurfaceView;
    private AutoFitTextureView mFacesSurfaceView;

    private String mCameraId;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private CameraCharacteristics mCharacteristics;
    private Integer mFacing = CameraCharacteristics.LENS_FACING_FRONT;
    private Rect mCameraRect;

    private int mSensorOrientation;
    private Size mPreviewSize;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private ImageReader mImageReader;
    private File mFilePic;
    private File mFileText;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private Paint mFacePaint = new Paint();
    private Face[] mFaces;

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            finish();
        }

    };

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            // showToast(getMessage());
        }

    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            int cameraSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            final Face[] faces = mFaces;
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFilePic, faces,
                    cameraSensorOrientation, mCameraRect));
        }

    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    Log.d(TAG, "process: " + System.currentTimeMillis());
                    mFaces = result.get(CaptureResult.STATISTICS_FACES);
                    Canvas canvas = mFacesSurfaceView.lockCanvas();
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    if (mFaces != null && mFaces.length > 0) {
                        Log.d(TAG, "process: " + "faces: " + mFaces.length);
                        CustomFace[] customFaces = computeFacesFromCameraCoordinates(mFaces);
                        for (CustomFace customFace : customFaces) {
                            Rect face = customFace.getBounds();
                            canvas.save();
                            canvas.drawRect(face.left, face.top, face.right, face.bottom, mFacePaint);
                            canvas.restore();
                        }
                    }
                    mFacesSurfaceView.unlockCanvasAndPost(canvas);
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChatActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param largest           The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size largest) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = largest.getWidth();
        int h = largest.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            //Log.e(TAG,"Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // openCameraPreview();
    }

    @Override
    public void onPause() {
        super.onPause();
        // closeCameraPreview();
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                mCharacteristics = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = mCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && !facing.equals(mFacing)) {
                    continue;
                }

                StreamConfigurationMap map = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                System.out.println("size:("+mPreviewSize.getWidth()+","+mPreviewSize.getHeight()+")");

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mPreviewSurfaceView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    mFacesSurfaceView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mPreviewSurfaceView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                    mFacesSurfaceView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.

        }
    }

    /**
     * Opens the camera specified by {@link }.
     */

    private void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
//            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
//                throw new RuntimeException("Time out waiting to lock camera opening.");
//            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mPreviewSurfaceView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {

                                //set FaceDetection
                                mPreviewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mPreviewSurfaceView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mPreviewSurfaceView` is fixed.
     *
     * @param viewWidth  The width of `mPreviewSurfaceView`
     * @param viewHeight The height of `mPreviewSurfaceView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mPreviewSurfaceView || null == mPreviewSize ) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mPreviewSurfaceView.setTransform(matrix);
        mFacesSurfaceView.setTransform(matrix);
        mPreviewSurfaceView.setBackgroundColor(getResources().getColor(R.color.gray));
        mFacesSurfaceView.setBackgroundColor(getResources().getColor(R.color.gray));
        // showToast("configureTransform");
    }

    /**
     * Initiate a still image capture.
     */
    private void takePicture() {
        mState = STATE_PICTURE_TAKEN;
        captureStillPicture();
    }


    /**
     * Capture a still picture. This method should be called when we get a response in
     */
    private void captureStillPicture() {
        try {
            if (null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    //showToast("Saved: " + mFilePic);
                    Log.d(TAG, mFilePic.toString());
                    unlockFocus();
                    //etChatMessage.clearFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        private final Face[] mFaces;

        private final int mCameraSensorOrientation;


        private final Rect mCameraRect;

        public ImageSaver(Image image, File file, Face[] faces, int cameraSensorOrientation, Rect cameraRect) {
            mImage = image;
            mFile = file;
            mFaces = faces;
            mCameraSensorOrientation = cameraSensorOrientation;
            mCameraRect = cameraRect;
        }

        @Override
        public void run() {

            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

//            if (mFaces != null && mFaces.length > 0) {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
//
//
//                int wRatio = (int) Math.ceil(options.outWidth / (float) 1080);
//                int hRatio = (int) Math.ceil(options.outHeight / (float) 1920);
//                int ratio = 1;
//                //获取采样率
//                if (wRatio > 1 && hRatio > 1) {
//                    if (wRatio > hRatio) {
//                        ratio = wRatio;
//                    } else {
//                        ratio = hRatio;
//                    }
//                }
//                options.inSampleSize = ratio;
//                options.inJustDecodeBounds = false;
//                options.inMutable = true;
//                options.inPreferredConfig = Bitmap.Config.RGB_565;
//                Bitmap face = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
//
//
//                float x = ((mCameraRect.bottom - mCameraRect.top) / (float) face.getWidth());
//                float y = ((mCameraRect.right - mCameraRect.left) / (float) face.getHeight());
//
//                Rect bounds = mFaces[0].getBounds();
//                switch (mCameraSensorOrientation) {
//                    case 90:
//                        face = Bitmap.createBitmap(face, (int) (face.getWidth() - bounds.bottom / y),
//                                (int) (bounds.left / x),
//                                (int) ((bounds.bottom - bounds.top) / y),
//                                (int) ((bounds.right - bounds.left) / x));
//                        break;
//                    case 270:
//                        face = Bitmap.createBitmap(face, (int) (bounds.top / x),
//                                (int) (face.getHeight() - bounds.right / y),
//                                (int) ((bounds.bottom - bounds.top) / x),
//                                (int) ((bounds.right - bounds.left) / y));
//                        break;
//                }
//
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                face.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                bytes = stream.toByteArray();
//            }


            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    public static class CustomFace {
        private Rect rect = null;

        public CustomFace(Rect rect) {
            this.rect = rect;
        }

        public Rect getBounds() {
            return rect;
        }
    }

    private static RectF rectToRectF(Rect r) {
        return new RectF(r.left, r.top, r.right, r.bottom);
    }

    private static Rect rectFToRect(RectF r) {
        return new Rect((int) r.left, (int) r.top, (int) r.right, (int) r.bottom);
    }

    private CustomFace[] computeFacesFromCameraCoordinates(Face[] faces) {
        CustomFace[] mappedFacesList = new CustomFace[faces.length];

        mCameraRect = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

        for (int i = 0; i < faces.length; i++) {

            RectF mappedRect = new RectF();

            Matrix mCameraToPreviewMatrix = new Matrix();

            mCameraToPreviewMatrix.mapRect(mappedRect, rectToRectF(faces[i].getBounds()));


            Rect auxRect = new Rect(rectFToRect(mappedRect));


            int cameraSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            float x = (mCameraRect.bottom - mCameraRect.top) / (float) mFacesSurfaceView.getmRealWidth();
            float y = (mCameraRect.right - mCameraRect.left) / (float) mFacesSurfaceView.getmRealHeight();

            if (mFacing == CameraCharacteristics.LENS_FACING_BACK) {
                switch (cameraSensorOrientation) {
                    case 90:
                        mappedRect.left = (mCameraRect.bottom - auxRect.bottom) / x;
                        mappedRect.top = auxRect.left / y;
                        mappedRect.right = (mCameraRect.bottom - auxRect.top) / x;
                        mappedRect.bottom = auxRect.right / y;
                        break;
                    case 270:
                        mappedRect.left = auxRect.top / x;
                        mappedRect.top = (mCameraRect.right - auxRect.right) / y;
                        mappedRect.right = auxRect.bottom / x;
                        mappedRect.bottom = (mCameraRect.right - auxRect.left) / y;
                        break;
                }
            } else if (mFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                switch (cameraSensorOrientation) {
                    case 270:
                        mappedRect.left = (mCameraRect.bottom - auxRect.top) / x;
                        mappedRect.top = (mCameraRect.right - auxRect.right) / y;
                        mappedRect.right = (mCameraRect.bottom - auxRect.bottom) / x;
                        mappedRect.bottom = (mCameraRect.right - auxRect.left) / y;
                        break;
                }
            } else {
                throw new IllegalArgumentException("not support this camera!");
            }


            mappedFacesList[i] = new CustomFace(rectFToRect(mappedRect));
        }

        return mappedFacesList;

    }

    /**
     * 打开预览窗口
     */
    public void openCameraPreview(){
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mPreviewSurfaceView.isAvailable()) {
            openCamera(mPreviewSurfaceView.getWidth(), mPreviewSurfaceView.getHeight());
        } else {
            mPreviewSurfaceView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    /**
     * 关闭预览窗口
     */
    public void closeCameraPreview(){
        closeCamera();
        stopBackgroundThread();
    }

}
