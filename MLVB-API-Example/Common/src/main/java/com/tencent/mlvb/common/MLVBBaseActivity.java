package com.tencent.mlvb.common;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class MLVBBaseActivity extends AppCompatActivity {

    protected static final int REQ_PERMISSION_CODE = 0x1000;

    protected int mGrantedCount = 0;

    protected abstract void onPermissionGranted();

    protected boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();

            String[] permissionsToCheck = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE
            };

            for (String permission : permissionsToCheck) {
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, permission)) {
                    permissions.add(permission);
                }
            }

            if (!permissions.isEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    permissions.toArray(new String[0]),
                    REQ_PERMISSION_CODE
                );
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_CODE:
                mGrantedCount = 0;
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        mGrantedCount++;
                    }
                }

                if (mGrantedCount == permissions.length) {
                    onPermissionGranted();
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.common_please_input_roomid_and_userid),
                        Toast.LENGTH_SHORT
                    ).show();
                }
                mGrantedCount = 0;
                break;
        }
    }

    public String generateStreamId() {
        Random random = new Random();
        int flag = random.nextInt(999999);
        if (flag < 100000) {
            flag += 100000;
        }
        return String.valueOf(flag);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.common_bg_color);
    }

    public void setStatusBarColor(int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(colorId, getTheme()));
        }
    }
}