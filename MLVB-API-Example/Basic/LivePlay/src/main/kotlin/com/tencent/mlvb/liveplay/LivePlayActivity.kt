package com.tencent.mlvb.liveplay

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.tencent.live2.V2TXLiveDef
import com.tencent.live2.V2TXLivePlayer
import com.tencent.live2.V2TXLivePlayerObserver
import com.tencent.live2.impl.V2TXLivePlayerImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.mlvb.common.URLUtils
import com.tencent.rtmp.ui.TXCloudVideoView
import java.util.Random

/**
 * Playback View
 * Features:
 * - Start playback [LivePlayActivity.startPlay]
 * - Mute [LivePlayActivity.mute]
 * For more information, please see the integration document {https://cloud.tencent.com/document/product/454/56598}.
 */
class LivePlayActivity : MLVBBaseActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "LivePlayActivity"
    }

    private lateinit var mPlayRenderView: TXCloudVideoView
    private var mLivePlayer: V2TXLivePlayer? = null
    private var mPlayFlag = false
    private lateinit var mButtonMute: Button
    private lateinit var mTextTitle: TextView
    private var mStreamId: String? = null
    private var mStreamType = 3 // 0:RTMP  1：FLV 2:HLS 3:RTC
    private var mPlayAudioFlag = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.liveplay_activity_live_play)
        initIntentData()
        if (checkPermission()) {
            initView()
            startPlay()
        }
    }

    override fun onPermissionGranted() {
        initView()
        startPlay()
    }

    private fun initIntentData() {
        mStreamId = intent.getStringExtra("STREAM_ID")
        mStreamType = intent.getIntExtra("STREAM_TYPE", 0)
    }

    private fun initView() {
        mPlayRenderView = findViewById(R.id.play_tx_cloud_view)
        mButtonMute = findViewById(R.id.btn_mute)
        mTextTitle = findViewById(R.id.tv_title)

        mButtonMute.setOnClickListener(this)
        findViewById<View>(R.id.iv_back).setOnClickListener(this)

        mStreamId?.takeIf { it.isNotEmpty() }?.let { streamId ->
            mTextTitle.text = streamId
        }
    }

    private fun startPlay() {
        mLivePlayer = V2TXLivePlayerImpl(this).apply {
            setRenderView(mPlayRenderView)
            setObserver(object : V2TXLivePlayerObserver() {
                override fun onError(player: V2TXLivePlayer?, code: Int, msg: String?, extraInfo: Bundle?) {
                    Log.d(TAG, "[Player] onError: player-$player code-$code msg-$msg info-$extraInfo")
                }

                override fun onVideoLoading(player: V2TXLivePlayer?, extraInfo: Bundle?) {
                    Log.i(TAG, "[Player] onVideoLoading: player-$player, extraInfo-$extraInfo")
                }

                override fun onVideoPlaying(player: V2TXLivePlayer?, firstPlay: Boolean, extraInfo: Bundle?) {
                    Log.i(TAG, "[Player] onVideoPlaying: player-$player firstPlay-$firstPlay info-$extraInfo")
                }

                override fun onVideoResolutionChanged(player: V2TXLivePlayer?, width: Int, height: Int) {
                    Log.i(TAG, "[Player] onVideoResolutionChanged: player-$player width-$width height-$height")
                }

                override fun onWarning(v2TXLivePlayer: V2TXLivePlayer?, i: Int, s: String?, bundle: Bundle?) {
                    Log.d(TAG, "[Player] Override: player-$v2TXLivePlayer, i-$i, s-$s")
                }

                override fun onRenderVideoFrame(player: V2TXLivePlayer?, v2TXLiveVideoFrame: V2TXLiveDef.V2TXLiveVideoFrame?) {
                    super.onRenderVideoFrame(player, v2TXLiveVideoFrame)
                    Log.d(TAG, "[Player] onRenderVideoFrame: player-$player, v2TXLiveVideoFrame-$v2TXLiveVideoFrame")
                }
            })

            val userId = Random().nextInt(10000).toString()
            val playURL = URLUtils.generatePlayUrl(mStreamId, userId, mStreamType)
            val result = startLivePlay(playURL)

            if (result == 0) {
                mPlayFlag = true
            }
            Log.d(TAG, "startLivePlay : $result")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mLivePlayer?.let { player ->
            if (mPlayFlag) {
                player.stopPlay()
            }
            mLivePlayer = null
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> finish()
            R.id.btn_mute -> mute()
        }
    }

    private fun mute() {
        mLivePlayer?.takeIf { it.isPlaying == 1 }?.let { player ->
            if (mPlayAudioFlag) {
                player.pauseAudio()
                mPlayAudioFlag = false
                mButtonMute.setText(R.string.liveplay_cancel_mute)
            } else {
                player.resumeAudio()
                mPlayAudioFlag = true
                mButtonMute.setText(R.string.liveplay_mute)
            }
        }
    }
}