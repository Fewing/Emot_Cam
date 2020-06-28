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

import java.util.HashMap;
import java.util.List;

public class image_process {
    public static HashMap<Integer,Bitmap> find_faces(final Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.setScale(0.25f, 0.25f);
        Bitmap compressBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        Log.v("MLkit", bitmap.getWidth() + "," + bitmap.getHeight());
        FirebaseVisionFaceDetectorOptions idOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                        .enableTracking()
                        .build();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(compressBitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector();
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
        int i = 0;
        for (FirebaseVisionFace face : result.getResult()) {
            Rect rect = face.getBoundingBox();
            rect.left += 17;
            rect.right -= 17;
            rect.top += 30;//修正人脸框
            ans.put(face.getTrackingId(),Bitmap.createBitmap(bitmap, rect.left * 4, rect.top * 4, rect.width() * 4, rect.height() * 4));
            i++;
        }
        return ans;
    }
    public static void replace_faces(Bitmap bestMap,Bitmap[] bestFaces){
        Matrix matrix = new Matrix();
        matrix.setScale(0.25f, 0.25f);
        Bitmap compressBitmap = Bitmap.createBitmap(bestMap, 0, 0, bestMap.getWidth(), bestMap.getHeight(), matrix, true);
        Log.v("MLkit", bestMap.getWidth() + "," + bestMap.getHeight());
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(compressBitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector();
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
        int i = 0;
        for (FirebaseVisionFace face : result.getResult()) {
            Rect rect = face.getBoundingBox();
        }
    }
}

