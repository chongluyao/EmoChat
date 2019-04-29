package com.zhouqing.EmoChat.face_detection;

/**
 * Created by vinup on 7/8/2018.
 */

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

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
    protected void onSuccess(@NonNull List<FirebaseVisionFace> faces, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {
        // update face
        graphicOverlay.clear();
        for (int i = 0; i < faces.size(); ++i)
        {
            FirebaseVisionFace face = faces.get(i);
            FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay);
            graphicOverlay.add(faceGraphic);
            faceGraphic.updateFace(face, frameMetadata.getCameraFacing());
        }
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
            int left1 = rect1.left >= 1 ? rect1.left:1;
            int top1 = rect1.top >= 1 ? rect1.top:1;
            int right1 = rect1.right <= bitmap.getWidth() ? rect1.right:bitmap.getWidth();
            int bottom1 = rect1.bottom <= bitmap.getHeight() ? rect1.bottom:bitmap.getHeight();
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


        // update face
        graphicOverlay.clear();
        for (int i = 0; i < faces.size(); ++i)
        {
            FirebaseVisionFace face = faces.get(i);
            FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay);
            graphicOverlay.add(faceGraphic);
            faceGraphic.updateFace(face, frameMetadata.getCameraFacing());
        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }

    private void saveBitmap(Bitmap bitmap, String filename) {

        String dir = Environment.getExternalStorageDirectory() + "/EmoChat/";
        long millis = System.currentTimeMillis();
        String timename = "" + millis;
        try {
            File file = new File(dir + timename + filename + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
