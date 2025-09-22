package com.tencent.mlvb.thirdbeauty

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class ThirdBeautyEntranceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third_beauty_entrance)
        supportActionBar?.hide()

        findViewById<View>(R.id.ll_third_beauty_tencent_effect).setOnClickListener {
            val intent = Intent(this@ThirdBeautyEntranceActivity, ThirdBeautyTencentEffectActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.ll_third_beauty_faceunity).setOnClickListener {
            val intent = Intent(this@ThirdBeautyEntranceActivity, ThirdBeautyFaceUnityActivity::class.java)
            startActivity(intent)
        }
    }
}