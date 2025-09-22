package com.tencent.mlvb.rtcpushandplay

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.tencent.live2.V2TXLiveDef
import com.tencent.live2.V2TXLivePlayer
import com.tencent.live2.V2TXLivePlayerObserver
import com.tencent.live2.V2TXLivePusher
import com.tencent.live2.impl.V2TXLivePlayerImpl
import com.tencent.live2.impl.V2TXLivePusherImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.mlvb.common.URLUtils
import com.tencent.rtmp.ui.TXCloudVideoView
import java.util.Random

/**
 * RTC Co-anchoring + Ultra-low-latency Playback View for Anchors
 *
 * Features:
 * - Start publishing [RTCPushAndPlayAnchorActivity.startPush]
 * - Start co-anchoring [RTCPushAndPlayAnchorActivity.startLink]
 * - Stop co-anchoring [RTCPushAndPlayAnchorActivity.stopLink]
 * - Play the other anchor's streams [RTCPushAndPlayAnchorActivity.startPlay]
 * Currently only supported in China, other regions are continuing to develop.
 */
class RTCPushAndPlayAnchorActivity : MLVBBaseActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "LiveLinkAnchorActivity"
    }

    private lateinit var mPlayRenderView: TXCloudVideoView
    private var mLivePlayer: V2TXLivePlayer? = null
    private lateinit var mEditStreamId: EditText
    private lateinit var mButtonLink: Button
    private lateinit var mPushRenderView: TXCloudVideoView
    private var mLivePusher: V2TXLivePusher? = null
    private lateinit var mTextTitle: TextView
    private var mStreamId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rtcpushandplay_activity_rtc_push_and_play_anchor)
        if (checkPermission()) {
            initIntentData()
            initView()
            startPush()
        }
    }

    override fun onPermissionGranted() {
        initIntentData()
        initView()
        startPush()
    }

    private fun initIntentData() {
        mStreamId = intent.getStringExtra("STREAM_ID")
    }

    private fun initView() {
        mPushRenderView = findViewById(R.id.tx_cloud_view_push)
        mPlayRenderView = findViewById(R.id.tx_cloud_view_play)
        mEditStreamId = findViewById(R.id.et_stream_id)
        mButtonLink = findViewById(R.id.btn_link)
        mTextTitle = findViewById(R.id.tv_title)

        mButtonLink.setOnClickListener(this)
        findViewById<View>(R.id.iv_back).setOnClickListener(this)

        mStreamId?.takeIf { it.isNotEmpty() }?.let { streamId ->
            mTextTitle.text = streamId
        }
    }

    private fun startPush() {
        mLivePusher = V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC).apply {
            setRenderView(mPushRenderView)
            startCamera(true)

            val userId = Random().nextInt(10000).toString()
            val pushUrl = URLUtils.generatePushUrl(mStreamId, userId, 0)
            val ret = startPush(pushUrl)
            Log.i(TAG, "startPush return: $ret")
            startMicrophone()
        }
    }

    private fun link() {
        when {
            mLivePlayer?.isPlaying == 1 -> stopLink()
            else -> startLink()
        }
    }

    fun startLink() {
        val linkStreamId = mEditStreamId.text.toString()
        if (linkStreamId.isEmpty()) {
            Toast.makeText(this@RTCPushAndPlayAnchorActivity, "请输入streamId", Toast.LENGTH_SHORT).show()
            return
        }
        startPlay(linkStreamId)
        mButtonLink.setText(R.string.rtcpushandplay_stop_link)
    }

    private fun stopLink() {
        mLivePlayer?.takeIf { it.isPlaying == 1 }?.stopPlay()
        mButtonLink.setText(R.string.rtcpushandplay_start_link)
    }

    private fun startPlay(linkStreamId: String) {
        val userId = Random().nextInt(10000).toString()
        val playURL = URLUtils.generatePlayUrl(linkStreamId, userId, 3)

        if (mLivePlayer == null) {
            mLivePlayer = V2TXLivePlayerImpl(this@RTCPushAndPlayAnchorActivity).apply {
                setRenderView(mPlayRenderView)
                setObserver(object : V2TXLivePlayerObserver() {
                    override fun onError(player: V2TXLivePlayer?, code: Int, msg: String?, extraInfo: Bundle?) {
                        Log.e(TAG, "[Player] onError: player-$player code-$code msg-$msg info-$extraInfo")
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
                })
            }
        }

        val result = mLivePlayer?.startLivePlay(playURL) ?: -1
        Log.d(TAG, "startLivePlay : $result")
    }

    override fun onDestroy() {
        super.onDestroy()

        mLivePusher?.let { pusher ->
            pusher.stopCamera()
            pusher.stopMicrophone()
            if (pusher.isPushing == 1) {
                pusher.stopPush()
            }
            mLivePusher = null
        }

        mLivePlayer?.let { player ->
            if (player.isPlaying == 1) {
                player.stopPlay()
            }
            mLivePlayer = null
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> finish()
            R.id.btn_link -> link()
        }
    }
}