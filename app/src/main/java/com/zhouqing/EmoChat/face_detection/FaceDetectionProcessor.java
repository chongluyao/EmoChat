package com.zhouqing.EmoChat.face_detection;

/**
 * Created by vinup on 7/8/2018.
 */

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
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
import java.util.List;
import java.util.Random;

/** Face Detector Demo. */
public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceDetectionProcessor";

    private final FirebaseVisionFaceDetector detector;

    public FaceDetectionProcessor() {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setTrackingEnabled(true)
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
    protected void onSuccess(@NonNull List<FirebaseVisionFace> faces, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay,@NonNull FirebaseVisionImage image) {
        Bitmap bitmap = image.getBitmapForDebugging();
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
            Bitmap newBitmap = Bitmap.createBitmap(bitmap,left,top,w,h);

            //saveBitmap(newBitmap,"new"+id+".jpg");

            EventBus.getDefault().post(new ChatActivity.TestEvent(left+","+right+","+top+","+bottom));


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

}
