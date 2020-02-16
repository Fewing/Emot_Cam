package com.fecostudio.EmotCam.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.Log;

public class image_process {
    public Bitmap process(Bitmap bitmap) {
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
        if (realFaceNum == 1) {
            PointF MidPoint = new PointF();
            Canvas canvas = new Canvas(bitmap);
            float eyesDistance;
            FaceDetector.Face face = faces[0];
            face.getMidPoint(MidPoint);//获取人脸中心点
            eyesDistance = face.eyesDistance();//获取人脸两眼的间距
            Bitmap ans =Bitmap.createBitmap(bitmap,
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
            return ans;
        } else {
            Log.e("错误:", "检测到0个或多个人脸");
            return null;
        }
    }
}

