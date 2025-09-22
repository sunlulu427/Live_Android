package com.tencent.mlvb.lebautobitrate

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.tencent.live2.V2TXLivePlayer
import com.tencent.live2.V2TXLivePlayerObserver
import com.tencent.live2.impl.V2TXLivePlayerImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.rtmp.ui.TXCloudVideoView

/**
 * Webrtc Auto Bitrate
 * MLVB APP Webrtc Auto Bitrate
 * - 1、Set Render View API:[V2TXLivePlayer.setRenderView]
 * - 2、Start Play API: [V2TXLivePlayer.startLivePlay]
 * Documentation: https://cloud.tencent.com/document/product/454/81212
 * After the adaptive bitrate playback is started, seamless streaming cannot be performed.
 * If you enter the adaptive bit rate in the playback state,
 * Need to stop current playback before starting adaptive playback
 */
class LebAutoBitrateActivity : MLVBBaseActivity() {

    companion object {
        private val TAG = LebAutoBitrateActivity::class.java.simpleName
        private const val NORMAL_PLAY_URL_1080 = "webrtc://liteavapp.qcloud.com/live/liteavdemoplayerstreamid?" +
                "tabr_bitrates=demo1080p,demo720p,demo540p&tabr_start_bitrate=demo1080p"
        private const val NORMAL_PLAY_URL_720 = "webrtc://liteavapp.qcloud.com/live/liteavdemoplayerstreamid?" +
                "tabr_bitrates=demo1080p,demo720p,demo540p&tabr_start_bitrate=demo720p"
        private const val NORMAL_PLAY_URL_540 = "webrtc://liteavapp.qcloud.com/live/liteavdemoplayerstreamid?" +
                "tabr_bitrates=demo1080p,demo720p,demo540p&tabr_start_bitrate=demo540p"
        private const val AUTO_BITRATE_SUFFIX = "&tabr_control=auto"
    }

    private lateinit var mVideoView: TXCloudVideoView
    private var mLivePlayer: V2TXLivePlayer? = null
    private var mAutoBitrate = false
    private var mPlayUrl = NORMAL_PLAY_URL_720
    private lateinit var mButton1080P: Button
    private lateinit var mButton720P: Button
    private lateinit var mButton540P: Button
    private lateinit var mButtonSwitch: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lebautobitrate_activity_leb_auto_bitrate)
        if (checkPermission()) {
            initView()
            startPlay()
        }
    }

    override fun onPermissionGranted() {
        initView()
    }

    private fun initView() {
        mVideoView = findViewById(R.id.tx_cloud_view)
        mButtonSwitch = findViewById(R.id.btn_switch)
        mButton1080P = findViewById(R.id.btn_switch_1080)
        mButton720P = findViewById(R.id.btn_switch_720)
        mButton540P = findViewById(R.id.btn_switch_540)

        mButtonSwitch.setOnClickListener {
            mAutoBitrate = !mAutoBitrate
            switchAutoBitrate(mAutoBitrate)
        }

        mButton1080P.setOnClickListener {
            switchBitrate(1080)
        }

        mButton720P.setOnClickListener {
            switchBitrate(720)
        }

        mButton540P.setOnClickListener {
            switchBitrate(540)
        }
    }

    private fun switchBitrate(bitrate: Int) {
        if (mAutoBitrate) {
            return
        }

        mPlayUrl = when (bitrate) {
            1080 -> NORMAL_PLAY_URL_1080
            720 -> NORMAL_PLAY_URL_720
            540 -> NORMAL_PLAY_URL_540
            else -> NORMAL_PLAY_URL_720
        }

        val urlToUse = if (mAutoBitrate) "$mPlayUrl$AUTO_BITRATE_SUFFIX" else mPlayUrl
        mLivePlayer?.switchStream(urlToUse)
    }

    private fun switchAutoBitrate(autoBitrate: Boolean) {
        mLivePlayer?.stopPlay()

        if (autoBitrate) {
            mPlayUrl = NORMAL_PLAY_URL_540
            mLivePlayer?.startLivePlay("$mPlayUrl$AUTO_BITRATE_SUFFIX")
            mButton1080P.setBackgroundResource(R.drawable.common_button_grey_bg)
            mButton720P.setBackgroundResource(R.drawable.common_button_grey_bg)
            mButton540P.setBackgroundResource(R.drawable.common_button_grey_bg)
            mButtonSwitch.setText(R.string.lebautobitrate_switch_stop)
        } else {
            mLivePlayer?.startLivePlay(mPlayUrl)
            mButton1080P.setBackgroundResource(R.drawable.common_button_bg)
            mButton720P.setBackgroundResource(R.drawable.common_button_bg)
            mButton540P.setBackgroundResource(R.drawable.common_button_bg)
            mButtonSwitch.setText(R.string.lebautobitrate_switch_start)
        }
    }

    private fun stopPlay() {
        mLivePlayer?.let { player ->
            if (player.isPlaying == 1) {
                player.stopPlay()
            }
        }
        mLivePlayer = null
    }

    private fun startPlay() {
        if (mLivePlayer == null) {
            mLivePlayer = V2TXLivePlayerImpl(this).apply {
                setObserver(object : V2TXLivePlayerObserver() {
                    override fun onError(player: V2TXLivePlayer?, code: Int, msg: String?, extraInfo: Bundle?) {
                        super.onError(player, code, msg, extraInfo)
                    }

                    override fun onWarning(player: V2TXLivePlayer?, code: Int, msg: String?, extraInfo: Bundle?) {
                        super.onWarning(player, code, msg, extraInfo)
                    }

                    override fun onVideoResolutionChanged(player: V2TXLivePlayer?, width: Int, height: Int) {
                        runOnUiThread {
                            Toast.makeText(
                                this@LebAutoBitrateActivity,
                                "resolution: $width*$height",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            }
        }

        mLivePlayer?.let { player ->
            player.setRenderView(mVideoView)
            val ret = player.startLivePlay(mPlayUrl)
            Log.i(TAG, "startPlay return: $ret")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlay()
    }

    override fun onBackPressed() {
        finish()
    }
}