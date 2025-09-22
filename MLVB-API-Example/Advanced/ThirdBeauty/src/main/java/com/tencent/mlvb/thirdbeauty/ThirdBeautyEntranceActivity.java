package com.tencent.mlvb.thirdbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class ThirdBeautyEntranceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_beauty_entrance);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        findViewById(R.id.ll_third_beauty_tencent_effect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThirdBeautyEntranceActivity.this, ThirdBeautyTencentEffectActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.ll_third_beauty_faceunity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThirdBeautyEntranceActivity.this, ThirdBeautyFaceUnityActivity.class);
                startActivity(intent);
            }
        });
    }
}