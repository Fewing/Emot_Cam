package com.fecostudio.EmotCam.app;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Preview;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class CameraActivity extends AppCompatActivity implements View.OnClickListener, OptionView.Callback {

    private final static CameraLogger LOG = CameraLogger.create("DemoApp");
    private final static boolean USE_FRAME_PROCESSOR = true;
    private final static boolean DECODE_BITMAP = true;
    public static CameraActivity Instance = null;

    private CameraView camera;
    private ViewGroup controlPanel;
    private long mCaptureTime;
    private String path;
    private int picid = 0;
    /*权限获取*/
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    List<String> mPermissionList = new ArrayList<>();
    /*权限获取*/
    long cam_time = 0;
    private EmotionRate tflite = new EmotionRate();
    private Activity activity = this;
    private Bitmap bestmap = null;
    private PictureResult bestres = null;//newly add
    ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    HashMap<Integer, FaceImage> bestFaces = new HashMap<Integer, FaceImage>();
    private float best_score = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Instance = this;
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        tflite.load_model(this);//加载tflite模型
        setContentView(R.layout.activity_camera);
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE);
        //切换无任务栏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        getSupportActionBar().hide();

        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);
        camera.addCameraListener(new Listener());
        /* 权限获取 */
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(CameraActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/";
            Toast.makeText(CameraActivity.this, path, Toast.LENGTH_LONG).show();

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
            ActivityCompat.requestPermissions(CameraActivity.this, permissions, 1);
        }
        /* 权限获取 */
        if (USE_FRAME_PROCESSOR) {
            camera.addFrameProcessor(new FrameProcessor() {
                private long lastTime = System.currentTimeMillis();

                @Override
                public void process(@NonNull Frame frame) {
                    long newTime = frame.getTime();
                    long delay = newTime - lastTime;
                    lastTime = newTime;
                    LOG.e("Frame delayMillis:", delay, "FPS:", 1000 / delay);
                    if (DECODE_BITMAP) {
                        if (frame.getFormat() == ImageFormat.NV21
                                && frame.getDataClass() == byte[].class) {
                            byte[] data = frame.getData();
                            YuvImage yuvImage = new YuvImage(data,
                                    frame.getFormat(),
                                    frame.getSize().getWidth(),
                                    frame.getSize().getHeight(),
                                    null);
                            ByteArrayOutputStream jpegStream = new ByteArrayOutputStream();
                            yuvImage.compressToJpeg(new Rect(0, 0,
                                    frame.getSize().getWidth(),
                                    frame.getSize().getHeight()), 100, jpegStream);
                            byte[] jpegByteArray = jpegStream.toByteArray();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegByteArray,
                                    0, jpegByteArray.length);
                            //noinspection ResultOfMethodCallIgnored
                            bitmap.toString();
                        }
                    }
                }
            });
        }

        //findViewById(R.id.edit).setOnClickListener(this);
        findViewById(R.id.capturePicture).setOnClickListener(this);
        //findViewById(R.id.capturePictureSnapshot).setOnClickListener(this);
        //findViewById(R.id.captureVideo).setOnClickListener(this);
        //findViewById(R.id.captureVideoSnapshot).setOnClickListener(this);
        findViewById(R.id.toggleCamera).setOnClickListener(this);
        //findViewById(R.id.changeFilter).setOnClickListener(this);

        controlPanel = findViewById(R.id.controls);
        ViewGroup group = (ViewGroup) controlPanel.getChildAt(0);
        //final View watermark = findViewById(R.id.watermark);

        List<Option<?>> options = Arrays.asList(
                // Layout
                new Option.Width(), new Option.Height(),
                // Engine and preview
                new Option.Mode(), new Option.Engine(), new Option.Preview(),
                // Some controls
                new Option.Flash(), new Option.WhiteBalance(), new Option.Hdr(),
                new Option.PictureMetering(), new Option.PictureSnapshotMetering(),
                new Option.PictureFormat(),
                // Video recording
                new Option.PreviewFrameRate(), new Option.VideoCodec(), new Option.Audio(),
                // Gestures
                new Option.Pinch(), new Option.HorizontalScroll(), new Option.VerticalScroll(),
                new Option.Tap(), new Option.LongTap(),
                // Watermarks
                //new Option.OverlayInPreview(watermark),
                // new Option.OverlayInPictureSnapshot(watermark),
                // Option.OverlayInVideoSnapshot(watermark),
                // Frame Processing
                new Option.FrameProcessingFormat(),
                // Other
                new Option.Grid(), new Option.GridColor(), new Option.UseDeviceOrientation()
        );
        List<Boolean> dividers = Arrays.asList(
                // Layout
                false, true,
                // Engine and preview
                false, false, true,
                // Some controls
                false, false, false, false, false, true,
                // Video recording
                false, false, true,
                // Gestures
                false, false, false, false, true,
                // Watermarks
                false, false, true,
                // Frame Processing
                true,
                // Other
                false, false, true
        );
        for (int i = 0; i < options.size(); i++) {
            OptionView view = new OptionView(this);
            //noinspection unchecked
            view.setOption(options.get(i), this);
            view.setHasDivider(dividers.get(i));
            group.addView(view,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        controlPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
                b.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        // Animate the watermark just to show we record the animation in video snapshots
        ValueAnimator animator = ValueAnimator.ofFloat(1F, 0.8F);
        animator.setDuration(300);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                //watermark.setScaleX(scale);
                //watermark.setScaleY(scale);
                //watermark.setRotation(watermark.getRotation() + 2);
            }
        });
        animator.start();
    }

    private void message(@NonNull String content, boolean important) {
        if (important) {
            LOG.w(content);
            Toast.makeText(this, content, Toast.LENGTH_LONG).show();
        } else {
            LOG.i(content);
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
        }
    }

    private class Listener extends CameraListener {

        @Override
        public void onCameraOpened(@NonNull CameraOptions options) {
            ViewGroup group = (ViewGroup) controlPanel.getChildAt(0);
            for (int i = 0; i < group.getChildCount(); i++) {
                OptionView view = (OptionView) group.getChildAt(i);
                view.onCameraOpened(camera, options);
            }
        }

        @Override
        public void onCameraError(@NonNull CameraException exception) {
            super.onCameraError(exception);
            message("Got CameraException #" + exception.getReason(), true);
        }

        public void onPictureTaken(@NonNull final PictureResult result) {
            super.onPictureTaken(result);
            long callbackTime = System.currentTimeMillis();
            if (mCaptureTime == 0) mCaptureTime = callbackTime - 300;
            //LOG.v("onPictureTaken called! Launching activity. Delay:", callbackTime - mCaptureTime);
            Log.v("拍照时间", (callbackTime - mCaptureTime) + "ms");
            result.toBitmap(4000, 4000, new BitmapCallback() {
                @Override
                public void onBitmapReady(Bitmap bitmap) {
                    float score = 0;
                    HashMap<Integer, Bitmap> face_maps = ImageProcess.find_faces(bitmap);
                    if (face_maps != null) {
                        for (HashMap.Entry<Integer, Bitmap> entry : face_maps.entrySet()) {
                            Log.v("face_id", entry.getKey().toString());
                            score += tflite.predict(entry.getValue(), activity);
                        }
                        score /= face_maps.size();
                        Log.v("分数", String.valueOf(score));
                    } else {
                        score = 0.0f;
                    }
                    if (score > best_score) {
                        bestmap = bitmap;
                        bestres = result;
                        best_score = score;
                    }
                    picid++;
                }
            });
            long now_time = System.currentTimeMillis();
            if (now_time - cam_time <= 2000) {
                capturePicture();
            } else {
                if (bestmap != null) {
                    //Toast.makeText(activity, "保存照片", Toast.LENGTH_SHORT).show();

                    PicturePreviewActivity.setPictureResult(bestres);
                    PicturePreviewActivity.setBitmapResult(bestmap);
                    Intent intent = new Intent(CameraActivity.this, PicturePreviewActivity.class);
                    //intent.putExtra("delay", callbackTime - mCaptureTime);
                    startActivity(intent);

                    //savePicture();//存为.jpg文件
                } else {
                    Toast.makeText(activity, "未检测到人脸", Toast.LENGTH_SHORT).show();
                    Log.e("错误", "未检测到人脸");
                }
                best_score = 0.0f;
                bestmap = null;
                picid = 0;
            }
        }

        private void previewSave(Bitmap bitmap) {
            Date nowDate = new Date();
            SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

            String fileName = fileFormat.format(nowDate);
            //fileName += String.valueOf(best_score);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savePictureQ(fileName, bitmap);
            } else {
                savePicture(bitmap);
            }
        }

        private void savePictureQ(String fileName, Bitmap bitmap) {
            try {
                //设置保存参数到ContentValues中
                final String relativeLocation = Environment.DIRECTORY_PICTURES;
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                //设置文件类型
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/JPEG");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);

                final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Uri uri = getContentResolver().insert(contentUri, contentValues);
                if (uri != null) {
                    //Log.e("成功:", "uri看起来没问题");
                    //若生成了uri，则表示该文件添加成功
                    //使用流将内容写入该uri中即可
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();
                        //Log.e("成功:", "输出流看起来没问题");
                    }
                    Toast.makeText(activity, "保存成功", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(activity, "保存失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        private void savePicture(Bitmap bitmap) {
            Date nowDate = new Date();
            SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

            String fileName = fileFormat.format(nowDate).toString();
            fileName += String.valueOf(best_score);
            File file = new File(path, fileName + ".jpg");
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                Toast.makeText(activity, "保存失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        @Override
        public void onExposureCorrectionChanged(float newValue, @NonNull float[] bounds, @Nullable PointF[] fingers) {
            super.onExposureCorrectionChanged(newValue, bounds, fingers);
            message("Exposure correction:" + newValue, false);
        }

        @Override
        public void onZoomChanged(float newValue, @NonNull float[] bounds, @Nullable PointF[] fingers) {
            super.onZoomChanged(newValue, bounds, fingers);
            message("Zoom:" + newValue, false);
        }
    }

    public void previewSave(Bitmap bitmap) {
        Date nowDate = new Date();
        SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

        String fileName = fileFormat.format(nowDate);
        //fileName += String.valueOf(best_score);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            savePictureQ(fileName, bitmap);
        } else {
            savePicture(bitmap);
        }
    }

    private void savePictureQ(String fileName, Bitmap bitmap) {
        try {
            //设置保存参数到ContentValues中
            final String relativeLocation = Environment.DIRECTORY_PICTURES;
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            //设置文件类型
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/JPEG");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);

            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uri = getContentResolver().insert(contentUri, contentValues);
            if (uri != null) {
                //Log.e("成功:", "uri看起来没问题");
                //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    //Log.e("成功:", "输出流看起来没问题");
                }
                //Toast.makeText(activity, "保存成功", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Toast.makeText(activity, "保存失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void savePicture(Bitmap bitmap) {
        Date nowDate = new Date();
        SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

        String fileName = fileFormat.format(nowDate).toString();
        fileName += String.valueOf(best_score);
        File file = new File(path, fileName + ".jpg");
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            Toast.makeText(activity, "保存失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.capturePicture:
                Toast.makeText(activity, "正在拍摄", Toast.LENGTH_SHORT).show();
                best_score = 0.0f;
                bestmap = null;
                bitmaps.clear();
                bestFaces.clear();
                picid = 0;//初始化值
                capturePicture();
                cam_time = System.currentTimeMillis();
                break;
            case R.id.toggleCamera:
                toggleCamera();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
        if (b.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            b.setState(BottomSheetBehavior.STATE_HIDDEN);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d("msg", "The onResume() event");
    }

    private void edit() {
        BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
        b.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void capturePicture() {
        /*if (camera.getMode() == Mode.VIDEO) {
            message("请开启照片模式。", false);
            return;
        }*/
        if (camera.isTakingPicture()) return;
        mCaptureTime = System.currentTimeMillis();

        Log.v("成功:", "正在拍摄" + picid);
        //Toast.makeText(this, picid, Toast.LENGTH_SHORT).show();
        camera.takePictureSnapshot();
    }

    private void toggleCamera() {
        if (camera.isTakingPicture() || camera.isTakingVideo()) return;
        switch (camera.toggleFacing()) {
            case BACK:
                //message("切换到后置", false);
                break;

            case FRONT:
                //message("切换到前置", false);
                break;
        }
    }

    @Override
    public <T> boolean onValueChanged(@NonNull Option<T> option, @NonNull T value, @NonNull String name) {
        if ((option instanceof Option.Width || option instanceof Option.Height)) {
            Preview preview = camera.getPreview();
            boolean wrapContent = (Integer) value == ViewGroup.LayoutParams.WRAP_CONTENT;
            if (preview == Preview.SURFACE && !wrapContent) {
                message("The SurfaceView preview does not support width or height changes. " +
                        "The view will act as WRAP_CONTENT by default.", true);
                return false;
            }
        }
        option.set(camera, value);
        BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
        b.setState(BottomSheetBehavior.STATE_HIDDEN);
        message("Changed " + option.getName() + " to " + name, false);
        return true;
    }

    //region Permissions

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("权限", permissions[0]);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean valid = true;
        for (int grantResult : grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED;
        }
        if (valid && !camera.isOpened() && permissions[0].equals("android.permission.CAMERA")) {
            camera.open();
        }
    }

    //endregion
}
