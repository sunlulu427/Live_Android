package com.tencent.mlvb.hlsautobitrate

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.tencent.live2.V2TXLiveDef
import com.tencent.live2.V2TXLivePlayer
import com.tencent.live2.V2TXLivePlayerObserver
import com.tencent.live2.impl.V2TXLivePlayerImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.rtmp.ui.TXCloudVideoView

/**
 * HLS Auto Bitrate
 *  MLVB APP HLS Auto Bitrate
 *  1、Set Render View API:[self.livePlayer setRenderView:self.view]
 *  2、Start Play API: [self.livePlayer startLivePlay:url]
 * Documentation: https://cloud.tencent.com/document/product/454/81211
 */
class HlsAutoBitrateActivity : MLVBBaseActivity() {

    companion object {
        private val TAG = HlsAutoBitrateActivity::class.java.simpleName
        private const val DEFAULT_PLAY_URL_HLS = "http://liteavapp.qcloud.com/live/liteavdemoplayerstreamid_autoAdjust.m3u8"
    }

    private lateinit var mVideoView: TXCloudVideoView
    private var mLivePlayer: V2TXLivePlayer? = null
    private var mPlayUrl = DEFAULT_PLAY_URL_HLS
    private val mPlayUrlList = mutableListOf<V2TXLiveDef.V2TXLiveStreamInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hlsautobitrate_activity_hls_auto_bitrate)
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

        findViewById<View>(R.id.btn_switch).setOnClickListener {
            switchAutoBitrate()
        }

        findViewById<View>(R.id.btn_switch_1080).setOnClickListener {
            switchBitrate(1080)
        }

        findViewById<View>(R.id.btn_switch_720).setOnClickListener {
            switchBitrate(720)
        }

        findViewById<View>(R.id.btn_switch_540).setOnClickListener {
            switchBitrate(540)
        }
    }

    private fun switchBitrate(bitrate: Int) {
        if (mPlayUrlList.isEmpty()) {
            Log.d(TAG, "liveplayer getStreamList return empty")
            return
        }

        mPlayUrlList.find { streamInfo ->
            streamInfo.height != 0 && streamInfo.width != 0 &&
                    !TextUtils.isEmpty(streamInfo.url) &&
                    minOf(streamInfo.height, streamInfo.width) == bitrate
        }?.let { streamInfo ->
            mPlayUrl = streamInfo.url
            mLivePlayer?.switchStream(mPlayUrl)
        }
    }

    private fun switchAutoBitrate() {
        mPlayUrl = DEFAULT_PLAY_URL_HLS
        mLivePlayer?.switchStream(mPlayUrl)
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

                    override fun onConnected(player: V2TXLivePlayer?, extraInfo: Bundle?) {
                        Log.d(TAG, "extraInfo: $extraInfo")
                        mPlayUrlList.clear()
                        mLivePlayer?.streamList?.let { streamList ->
                            if (streamList.isNotEmpty()) {
                                mPlayUrlList.addAll(streamList)
                            }
                        }
                    }

                    override fun onVideoResolutionChanged(player: V2TXLivePlayer?, width: Int, height: Int) {
                        runOnUiThread {
                            Toast.makeText(
                                this@HlsAutoBitrateActivity,
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