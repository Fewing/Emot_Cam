package com.fecostudio.EmotCam.app;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.FileCallback;
import com.otaliastudios.cameraview.size.AspectRatio;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.PictureResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PicturePreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private static PictureResult picture;
    private Activity activity = this;
    private String path;
    private Bitmap finalResult;
    /*权限获取*/
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    List<String> mPermissionList = new ArrayList<>();
    /*权限获取*/

    public static void setPictureResult(@Nullable PictureResult pictureResult) {
        picture = pictureResult;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_preview);
        final PictureResult result = picture;
        if (result == null) {
            finish();
            return;
        }
        //切换无任务栏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        getSupportActionBar().hide();

        final ImageView imageView = findViewById(R.id.image);

        findViewById(R.id.previewSave).setOnClickListener(this);
        findViewById(R.id.reShot).setOnClickListener(this);
        /* 权限获取 */
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(PicturePreviewActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/";
            //Toast.makeText(PicturePreviewActivity.this, path, Toast.LENGTH_LONG).show();

            String tmpFilePath = path + "Emotcam/";
            File tmpFile = new File(tmpFilePath);
            if (!tmpFile.exists()) {
                Log.e("成功:", "成功进入建立文件夹部分");
                tmpFile.mkdir();
            }
            path = tmpFilePath;
            //boolean fileExist = fileIsExists(path);
            //readImg(showImg);
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(PicturePreviewActivity.this, permissions, 1);
        }
        /* 权限获取 */

        AspectRatio ratio = AspectRatio.of(result.getSize());
        try {
            result.toBitmap(1000, 1000, new BitmapCallback() {
                @Override
                public void onBitmapReady(Bitmap bitmap) {
                    imageView.setImageBitmap(bitmap);
                }
            });
        } catch (UnsupportedOperationException e) {
            imageView.setImageDrawable(new ColorDrawable(Color.GREEN));
            Toast.makeText(this, "出现了无法预览的未知错误？？" + picture.getFormat(),
                    Toast.LENGTH_LONG).show();
        }

        if (result.isSnapshot()) {
            // Log the real size for debugging reason.
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(result.getData(), 0, result.getData().length, options);
            if (result.getRotation() % 180 != 0) {
                Log.e("PicturePreview", "The picture full size is " + result.getSize().getHeight() + "x" + result.getSize().getWidth());
            } else {
                Log.e("PicturePreview", "The picture full size is " + result.getSize().getWidth() + "x" + result.getSize().getHeight());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            setPictureResult(null);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.previewSave:
                newPreviewSave();
                break;
            case R.id.reShot:
                reShot();
                break;
        }
    }

    private void newPreviewSave(){
        //Intent intent = getIntent();
        //this.setResult(0, intent);
        picture.toBitmap(2000, 2000, new BitmapCallback() {
                    @Override
                    public void onBitmapReady(Bitmap bitmap) {
                        CameraActivity.Instance.previewSave(bitmap);
                    }
                });
        finish();
    }

    private void reShot(){
        //Intent intent = getIntent();
        //this.setResult(1, intent);
        finish();
    }

}
