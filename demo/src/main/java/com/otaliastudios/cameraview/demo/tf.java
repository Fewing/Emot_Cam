package com.otaliastudios.cameraview.demo;

import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.common.TensorProcessor;


import android.app.Activity;
import android.content.pm.FeatureGroupInfo;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.nio.MappedByteBuffer;
import java.io.IOException;


public class tf {
    private Interpreter tflite=null;
    public void load_model(Activity activity)
    {
        GpuDelegate delegate = new GpuDelegate();//使用GPU
        Interpreter.Options options = (new Interpreter.Options()).addDelegate(delegate);
        /*Interpreter.Options options = (new Interpreter.Options());//使用NNAPI
        NnApiDelegate nnApiDelegate = null;
        nnApiDelegate = new NnApiDelegate();
        options.addDelegate(nnApiDelegate);*/
        try {
            MappedByteBuffer tfliteModel
                    = FileUtil.loadMappedFile(activity,
                    "model.tflite");
            tflite = new Interpreter(tfliteModel,options);
        } catch (IOException e) {
            Log.e("tfliteSupport", "Error reading model", e);
        }
    }
    private TensorImage load_image(final Bitmap bitmap) {
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(128, 128, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(new NormalizeOp(0, 255))
                        .build();
        TensorImage tImage = new TensorImage(DataType.FLOAT32);
        tImage.load(bitmap);
        tImage = imageProcessor.process(tImage);
        /*float[] temp=tImage.getTensorBuffer().getFloatArray();
        for (int i=0;i<128*128*3;i++)归一化
        {
            temp[i]/=(float)255.0;
        }
        tImage.load(temp,new int[]{128,128,3});*/
        return tImage;
    }

    public float predict(final Bitmap bitmap, Activity activity) {
        TensorImage tImage = load_image(bitmap);
        TensorBuffer probabilityBuffer =
                TensorBuffer.createFixedSize(new int[]{1,1}, DataType.FLOAT32);//创建输出buffer
        // Initialise the model
        if(tflite==null)
        {
            Log.v("load:","加载");
            load_model(activity);
        }
        tflite.run(tImage.getBuffer(), probabilityBuffer.getBuffer());
        TensorProcessor probabilityProcessor =
                new TensorProcessor.Builder().build();
        float result [];
        result = probabilityProcessor.process(probabilityBuffer).getFloatArray();
        return result[0]+(float)3.0;
    }
}

