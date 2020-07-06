package com.fecostudio.EmotCam.app;

import android.graphics.Bitmap;

public class FaceImage {
    public Bitmap image;
    public double score;
    public int picid;

    public FaceImage(Bitmap image, double score, int picid) {
        this.image = image;
        this.score = score;
        this.picid = picid;
    }
}
