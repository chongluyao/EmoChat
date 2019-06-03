package com.zhouqing.EmoChat.face_detection;

/**
 * Created by vinup on 7/8/2018.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.zhouqing.EmoChat.chat.ChatActivity;
import com.zhouqing.EmoChat.common.constant.Global;

import org.greenrobot.eventbus.EventBus;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Face Detector Demo. */
public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceDetectionProcessor";

    private final FirebaseVisionFaceDetector detector;

    public FaceDetectionProcessor() {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        // .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        // .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .enableTracking()
                        .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess_v2(byte[] byteData, FirebaseVisionImage image, @NonNull List<FirebaseVisionFace> faces, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {
        // save image
        graphicOverlay.clear();
        if (faces.size() == 1) {

            FirebaseVisionFace face = faces.get(0);
            Bitmap bitmap = image.getBitmapForDebugging();
            Log.d(TAG, "onSuccess_v2: bitmap " + bitmap.getHeight() + " " + bitmap.getWidth());

            // get face1
            Rect rect1 = face.getBoundingBox();
            Log.d(TAG, "onSuccess_v2: " + rect1.flattenToString());
            int left1 = rect1.left >= 1 ? rect1.left : 1;
            int top1 = rect1.top >= 1 ? rect1.top : 1;
            int right1 = rect1.right <= bitmap.getWidth() ? rect1.right : bitmap.getWidth();
            int bottom1 = rect1.bottom <= bitmap.getHeight() ? rect1.bottom : bitmap.getHeight();
            int width1 = right1 - left1;
            int height1 = bottom1 - top1;
            Bitmap bitmap_face1 = Bitmap.createBitmap(bitmap, left1, top1, width1, height1);

            // saveBitmap(bitmap_face1, "from_face");

            // feed to model
            FirebaseLocalModel localSource =
                    new FirebaseLocalModel.Builder("face_local_model")  // Assign a name to this model
                            .setAssetFilePath("face_analyze.tflite")
                            .build();
            FirebaseModelManager.getInstance().registerLocalModel(localSource);
            FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                    .setLocalModelName("face_local_model")
                    .build();
            try {
                FirebaseModelInterpreter firebaseInterpreter =
                        FirebaseModelInterpreter.getInstance(options);
                FirebaseModelInputOutputOptions inputOutputOptions =
                        new FirebaseModelInputOutputOptions.Builder()
                                .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 64, 64, 2})
                                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 2})
                                .build();
                // bitmap_face1 = Bitmap.createBitmap(bitmap_face1, 64, 64, Bitmap.Config.ARGB_8888);
            } catch (FirebaseMLException e) {
                e.printStackTrace();
            }
        }
    }

    // update face
    @Override
    protected void onSuccess(@NonNull List<FirebaseVisionFace> faces, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay,@NonNull FirebaseVisionImage image) {
        final Bitmap bitmap = image.getBitmapForDebugging();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Random random = new Random();
        String id = random.nextInt()+"";
        //saveBitmap(bitmap,"original"+id+".jpg");

        graphicOverlay.clear();
        for (int i = 0; i < faces.size(); ++i)
        {
            FirebaseVisionFace face = faces.get(i);
            FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay);
            graphicOverlay.add(faceGraphic);
            faceGraphic.updateFace(face, frameMetadata.getCameraFacing());

            int left = face.getBoundingBox().left;
            if(left<0)left = 0;
            int top = face.getBoundingBox().top;
            if(top<0)top = 0;
            int right = face.getBoundingBox().right;
            if(right>width)right = width;
            int bottom = face.getBoundingBox().bottom;
            if(bottom>height)bottom = height;
            int w = right - left ;
            int h = bottom - top ;

            //System.out.println("top:"+top+",left:"+left+",right:"+right+",bottom:"+bottom+",width:"+width+",height:"+height);
            final Bitmap newBitmap = Bitmap.createBitmap(bitmap,left,top,w,h);
            final Bitmap resized_newBitmap = Bitmap.createScaledBitmap(newBitmap, 64, 64, true);

            Log.d(TAG, "onSuccess: " + System.currentTimeMillis());
            // feed to model
            FirebaseLocalModel localSource =
                    new FirebaseLocalModel.Builder("face_local_model")  // Assign a name to this model
                            .setAssetFilePath("face_analyze.tflite")
                            .build();
            FirebaseModelManager.getInstance().registerLocalModel(localSource);
            FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                    .setLocalModelName("face_local_model")
                    .build();
            try {
                FirebaseModelInterpreter firebaseInterpreter =
                        FirebaseModelInterpreter.getInstance(options);
                FirebaseModelInputOutputOptions inputOutputOptions =
                        new FirebaseModelInputOutputOptions.Builder()
                                .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 64, 64, 1})
                                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 2})
                                .build();
                int batchNum = 0;
                float[][][][] input = new float[1][64][64][1];
                for (int x = 0; x < 64; x++) {
                    for (int y = 0; y < 64; y++) {
                        int pixel = resized_newBitmap.getPixel(x, y);
                        // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                        // model. For example, some models might require values to be normalized
                        // to the range [0.0, 1.0] instead.

                        // input[batchNum][x][y][0] = (Color.red(pixel) / 255.0f - 0.5f) * 2;
                        int r = Color.red(pixel);
                        int g = Color.green(pixel);
                        int b = Color.blue(pixel);
                        input[batchNum][y][x][0] = ((r + g + b) / 3 / 255.0f - 0.5f) * 2;
                    }
                }
                FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                        .add(input)  // add() as many input arrays as your model requires
                        .build();
                firebaseInterpreter.run(inputs, inputOutputOptions)
                        .addOnSuccessListener(
                                new OnSuccessListener<FirebaseModelOutputs>() {
                                    @Override
                                    public void onSuccess(FirebaseModelOutputs result) {
                                        // ...
                                        float[][] output = result.getOutput(0);
                                        float[] probabilities = output[0];

                                        // save
                                        // long prefix = System.currentTimeMillis();
                                        // saveBitmap(resized_newBitmap, prefix + "test.jpg");
                                        // saveText("" + probabilities[0] + " " + probabilities[1], prefix + "test.txt");
                                        EventBus.getDefault().post(new ChatActivity.EmotionEvent(0,probabilities));

                                        Log.d(TAG, "onSuccess: " + System.currentTimeMillis());
                                        Log.d(TAG, "onSuccess: " + probabilities[0] + " " + probabilities[1]);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Log.d(TAG, "onFailure: " + e);
                                    }
                                });
            } catch (FirebaseMLException e) {
                e.printStackTrace();
            }


            //saveBitmap(newBitmap,"new"+id+".jpg");

            //EventBus.getDefault().post(new ChatActivity.TestEvent(left+","+right+","+top+","+bottom));

            //此处对截取的newBitmap进行表情识别，并在识别成功的回调函数中使用EventBus发送结果到主线程
            //以下代码为随机发送的结果
//            float[] probabilities = new float[2];
//            probabilities[0] = random.nextFloat();
//            probabilities[1] = 1 - probabilities[0];
//            EventBus.getDefault().post(new ChatActivity.EmotionEvent(0,probabilities));
        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }


    /**
     * 保存bitmap
     * @param mBitmap
     */
    public static void saveBitmap(Bitmap mBitmap,String fileName) {
        String savePath = Global.PROJECT_FILE_PATH;
        File filePic;
        try {
            filePic = new File(savePath  + fileName);//保存的格式为jpg
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block

        }
    }

    /**
     * 保存预测结果
     * @param mText
     */
    public static void saveText(String mText,String fileName) {
        String savePath = Global.PROJECT_FILE_PATH;
        File filePic;
        try {
            filePic = new File(savePath  + fileName);//保存的格式为jpg
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            fos.write(mText.getBytes());
            fos.flush();
            fos.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block

        }
    }

}
