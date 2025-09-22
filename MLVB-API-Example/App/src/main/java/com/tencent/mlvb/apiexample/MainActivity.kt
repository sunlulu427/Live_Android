package com.tencent.mlvb.apiexample

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tencent.liteav.sdk.lebplay.LebPlayEnterActivity
import com.tencent.mlvb.customvideocapture.CustomVideoCaptureActivity
import com.tencent.mlvb.apiexample.R
import com.tencent.mlvb.hlsautobitrate.HlsAutoBitrateActivity
import com.tencent.mlvb.lebautobitrate.LebAutoBitrateActivity
import com.tencent.mlvb.linkpk.LivePKEnterActivity
import com.tencent.mlvb.livelink.LiveLinkEnterActivity
import com.tencent.mlvb.liveplay.LivePlayEnterActivity
import com.tencent.mlvb.livepushcamera.LivePushCameraEnterActivity
import com.tencent.mlvb.livepushscreen.LivePushScreenEnterActivity
import com.tencent.mlvb.newtimeshiftspriite.NewTimeShiftSpriteActivity
import com.tencent.mlvb.pictureinpicture.PictureInPictureActivity
import com.tencent.mlvb.rtcpushandplay.RTCPushAndPlayEnterActivity
import com.tencent.mlvb.switchrenderview.SwitchRenderViewActivity
import com.tencent.mlvb.thirdbeauty.ThirdBeautyEntranceActivity
import com.tencent.mlvb.timeshift.TimeShiftActivity

/**
 * MLVB API-Example Main View
 *
 * Features
 * Basic features:
 * - Publishing from camera [LivePushCameraEnterActivity]
 * - Publishing from screen [LivePushScreenEnterActivity]
 * - Playback [LivePlayEnterActivity]
 * - Co-anchoring [LiveLinkEnterActivity]
 * - Competition [LivePKEnterActivity]
 *
 * Advanced features:
 * - Dynamically switching rendering controls [SwitchRenderViewActivity]
 * - Custom video capturing [CustomVideoCaptureActivity]
 * - Third-party beauty filters [ThirdBeautyEntranceActivity]
 * - RTC co-anchoring + ultra-low-latency playback [RTCPushAndPlayEnterActivity]
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
            findViewById<View>(R.id.launch_view).visibility = View.GONE
        }, 1000)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.ll_push_camera).setOnClickListener {
            startActivity(Intent(this, LivePushCameraEnterActivity::class.java))
        }

        findViewById<View>(R.id.ll_push_screen).setOnClickListener {
            startActivity(Intent(this, LivePushScreenEnterActivity::class.java))
        }

        findViewById<View>(R.id.ll_play).setOnClickListener {
            startActivity(Intent(this, LivePlayEnterActivity::class.java))
        }

        findViewById<View>(R.id.ll_leb_play).setOnClickListener {
            startActivity(Intent(this, LebPlayEnterActivity::class.java))
        }

        findViewById<View>(R.id.ll_link).setOnClickListener {
            startActivity(Intent(this, LiveLinkEnterActivity::class.java))
        }

        findViewById<View>(R.id.ll_pk).setOnClickListener {
            startActivity(Intent(this, LivePKEnterActivity::class.java))
        }

        findViewById<View>(R.id.ll_switch_render_view).setOnClickListener {
            startActivity(Intent(this, SwitchRenderViewActivity::class.java))
        }

        findViewById<View>(R.id.ll_custom_camera).setOnClickListener {
            startActivity(Intent(this, CustomVideoCaptureActivity::class.java))
        }

        findViewById<View>(R.id.ll_third_beauty).setOnClickListener {
            startActivity(Intent(this, ThirdBeautyEntranceActivity::class.java))
        }

        findViewById<View>(R.id.ll_cloud_transcoding).setOnClickListener {
            startActivity(Intent(this, RTCPushAndPlayEnterActivity::class.java))
        }

        findViewById<View>(R.id.ll_picture_in_picture).setOnClickListener {
            startActivity(Intent(this, PictureInPictureActivity::class.java))
        }

        findViewById<View>(R.id.ll_leb_auto_play).setOnClickListener {
            startActivity(Intent(this, LebAutoBitrateActivity::class.java))
        }

        findViewById<View>(R.id.ll_hls_auto_play).setOnClickListener {
            startActivity(Intent(this, HlsAutoBitrateActivity::class.java))
        }

        findViewById<View>(R.id.ll_time_shift).setOnClickListener {
            startActivity(Intent(this, TimeShiftActivity::class.java))
        }

        findViewById<View>(R.id.ll_new_time_shift_sprite).setOnClickListener {
            startActivity(Intent(this, NewTimeShiftSpriteActivity::class.java))
        }
    }
}