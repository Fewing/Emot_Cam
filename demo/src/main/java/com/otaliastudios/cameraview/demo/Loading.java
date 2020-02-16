package com.otaliastudios.cameraview.demo;

import android.app.Activity;
import android.app.ProgressDialog;

public class Loading {
    private ProgressDialog progressDialog;
    public void buildProgressDialog(String text, Activity activity) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage(text);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }
    public void cancelProgressDialog() {
        if (progressDialog != null)
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
    }
}
