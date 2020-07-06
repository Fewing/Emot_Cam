package com.fecostudio.EmotCam.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.HashMap;
import java.util.List;

public class ImageProcess {
    public static HashMap<Integer,Bitmap> find_faces(final Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.setScale(0.25f, 0.25f);
        Bitmap compressBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        Log.v("MLkit", bitmap.getWidth() + "," + bitmap.getHeight());
        FirebaseVisionFaceDetectorOptions idOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                        .enableTracking()
                        .build();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(compressBitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(idOpts);
        final Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        Log.v("MLkit", "人脸检测成功");
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("MLkit", "人脸检测失败");
                                    }
                                });
        while (!result.isComplete()) {
        }
        HashMap<Integer,Bitmap> ans = new HashMap<Integer, Bitmap>();
        Log.v("realFaceNum", String.valueOf(result.getResult().size()));
        for (FirebaseVisionFace face : result.getResult()) {
            Rect rect = face.getBoundingBox();
            FirebaseVisionFaceLandmark leftEye =face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
            FirebaseVisionFaceLandmark rightEye =face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
            FirebaseVisionFaceLandmark mouthButton =face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
            int left = leftEye.getPosition().getX().intValue()-55;
            int right = rightEye.getPosition().getX().intValue()+55;
            int bottom = mouthButton.getPosition().getY().intValue()+35;
            int top = leftEye.getPosition().getY().intValue()- 55;
            ans.put(face.getTrackingId(),Bitmap.createBitmap(bitmap, left * 4, top * 4, (right-left) * 4,(bottom-top)  * 4));
        }
        return ans;
    }
    public static void replace_faces(Bitmap bestMap,Bitmap[] bestFaces){
        Matrix matrix = new Matrix();
        matrix.setScale(0.25f, 0.25f);
        Bitmap compressBitmap = Bitmap.createBitmap(bestMap, 0, 0, bestMap.getWidth(), bestMap.getHeight(), matrix, true);
        Log.v("MLkit", bestMap.getWidth() + "," + bestMap.getHeight());
        FirebaseVisionFaceDetectorOptions idOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                        .enableTracking()
                        .build();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(compressBitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(idOpts);
        final Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        Log.v("MLkit", "人脸检测成功");
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("MLkit", "人脸检测失败");
                                    }
                                });
        while (!result.isComplete()) {
        }
        Log.v("realFaceNum", String.valueOf(result.getResult().size()));
        for (FirebaseVisionFace face : result.getResult()) {
            Rect rect = face.getBoundingBox();

        }
    }
}

