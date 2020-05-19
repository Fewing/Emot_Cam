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

import java.util.ArrayList;
import java.util.List;

public class image_process {
    public Bitmap[] process(Bitmap bitmap) {
        Paint paint = new Paint();//画人脸区域用到的Paint
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(8);
        paint.setStyle(Paint.Style.STROKE);
        Bitmap RGB_Bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        Matrix matrix = new Matrix();
        matrix.setScale(0.25f, 0.25f);
        RGB_Bitmap = Bitmap.createBitmap(RGB_Bitmap, 0, 0, RGB_Bitmap.getWidth(), RGB_Bitmap.getHeight(), matrix, true);
        FaceDetector faceDetector = new FaceDetector(RGB_Bitmap.getWidth(), RGB_Bitmap.getHeight(), 1);
        FaceDetector.Face[] faces = new FaceDetector.Face[1];
        int realFaceNum = faceDetector.findFaces(RGB_Bitmap, faces);
        Log.v("realFaceNum", String.valueOf(realFaceNum));
        if (realFaceNum >= 1) {
            Bitmap[] ans = new Bitmap[realFaceNum];
            for (int i = 0; i < realFaceNum; i++) {
                PointF MidPoint = new PointF();
                //Canvas canvas = new Canvas(bitmap);
                float eyesDistance;
                FaceDetector.Face face = faces[i];
                face.getMidPoint(MidPoint);//获取人脸中心点
                eyesDistance = face.eyesDistance();//获取人脸两眼的间距
                ans[i] = Bitmap.createBitmap(bitmap,
                        (int) (MidPoint.x - eyesDistance * 1.1) * 4,
                        (int) (MidPoint.y - eyesDistance * 0.6) * 4,
                        (int) (eyesDistance * 2.2) * 4,
                        (int) (eyesDistance * 2.2) * 4);
            /*画出人脸的区域
            canvas.drawRect(//矩形框的位置参数
                    ((int) (MidPoint.x - eyesDistance * 1.1)) * 4,
                    ((int) (MidPoint.y - eyesDistance * 0.6)) * 4,
                    ((int) (MidPoint.x + eyesDistance * 1.1)) * 4,
                    ((int) (MidPoint.y + eyesDistance * 1.6)) * 4,
                    paint);
             */
            }
            return ans;
        } else {
            Log.e("错误:", "没有检测到人脸");
            return null;
        }
    }

    public Bitmap[] new_process(final Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.setScale(0.25f, 0.25f);
        Bitmap compressBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        Log.v("MLkit", bitmap.getWidth()+","+bitmap.getHeight());
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
        while (!result.isComplete()){}
        Bitmap[] ans = new Bitmap[result.getResult().size()];
        Log.v("realFaceNum", String.valueOf(result.getResult().size()));
        int i=0;
        for (FirebaseVisionFace face : result.getResult()) {
            Rect rect = face.getBoundingBox();
            rect.left+=17;
            rect.right-=17;
            rect.top+=30;//修正人脸框
            ans[i] = Bitmap.createBitmap(bitmap,rect.left*4,rect.top*4,rect.width()*4,rect.height()*4);
            i++;
        }
        return ans;
    }
}

