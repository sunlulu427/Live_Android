package com.tencent.mlvb.rtcpushandplay

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.tencent.live2.V2TXLivePlayer
import com.tencent.live2.V2TXLivePlayerObserver
import com.tencent.live2.impl.V2TXLivePlayerImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.mlvb.common.URLUtils
import com.tencent.rtmp.ui.TXCloudVideoView
import java.util.Random

/**
 * RTC Co-anchoring + Ultra-low-latency Playbook View for Audience
 *
 * Features:
 * - Start playback [RTCPushAndPlayAudienceActivity.startPlay]
 * - Start co-anchoring [RTCPushAndPlayAudienceActivity.startLink]
 * - Stop co-anchoring [RTCPushAndPlayAudienceActivity.stopLink]
 * Currently only supported in China, other regions are continuing to develop.
 */
class RTCPushAndPlayAudienceActivity : MLVBBaseActivity(), View.OnClickListener {

    companion object {
        private val TAG = RTCPushAndPlayAudienceActivity::class.java.simpleName
    }

    private lateinit var mLinkPlayRenderView: TXCloudVideoView
    private var mLivePlayer: V2TXLivePlayer? = null
    private lateinit var mEditStreamId: EditText
    private lateinit var mButtonLink: Button
    private lateinit var mPlayRenderView: TXCloudVideoView
    private var mLinkPlayer: V2TXLivePlayer? = null
    private lateinit var mTextTitle: TextView
    private var mStreamId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rtcpushandplay_activity_rtc_push_and_play_audience)
        if (checkPermission()) {
            initIntentData()
            initView()
            startPlay()
        }
    }

    override fun onPermissionGranted() {
        initIntentData()
        initView()
        startPlay()
    }

    private fun initIntentData() {
        mStreamId = intent.getStringExtra("STREAM_ID")
    }

    private fun initView() {
        mPlayRenderView = findViewById(R.id.tx_cloud_view_push)
        mLinkPlayRenderView = findViewById(R.id.tx_cloud_view_play)
        mEditStreamId = findViewById(R.id.et_stream_id)
        mButtonLink = findViewById(R.id.btn_link)
        mTextTitle = findViewById(R.id.tv_title)

        findViewById<View>(R.id.iv_back).setOnClickListener(this)
        mButtonLink.setOnClickListener(this)

        mStreamId?.takeIf { it.isNotEmpty() }?.let { streamId ->
            mTextTitle.text = streamId
        }
    }

    private fun startPlay() {
        val userId = Random().nextInt(10000).toString()
        val playURL = URLUtils.generatePlayUrl(mStreamId, userId, 3)

        if (mLivePlayer == null) {
            mLivePlayer = V2TXLivePlayerImpl(this@RTCPushAndPlayAudienceActivity).apply {
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

    private fun link() {
        when {
            mLinkPlayer?.isPlaying == 1 -> stopLink()
            else -> startLink()
        }
    }

    private fun startLink() {
        val linkStreamId = mEditStreamId.text.toString()
        if (linkStreamId.isEmpty()) {
            Toast.makeText(this@RTCPushAndPlayAudienceActivity, "请输入streamId", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = Random().nextInt(10000).toString()
        val playURL = URLUtils.generatePlayUrl(linkStreamId, userId, 3)

        if (mLinkPlayer == null) {
            mLinkPlayer = V2TXLivePlayerImpl(this@RTCPushAndPlayAudienceActivity).apply {
                setRenderView(mLinkPlayRenderView)
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

        val result = mLinkPlayer?.startLivePlay(playURL) ?: -1
        Log.d(TAG, "startLivePlay : $result")
        mButtonLink.setText(R.string.rtcpushandplay_stop_play)
    }

    private fun stopLink() {
        mLinkPlayer?.takeIf { it.isPlaying == 1 }?.stopPlay()
        mButtonLink.setText(R.string.rtcpushandplay_rtc_play)
    }

    override fun onDestroy() {
        super.onDestroy()

        mLivePlayer?.let { player ->
            if (player.isPlaying == 1) {
                player.stopPlay()
            }
            mLivePlayer = null
        }

        mLinkPlayer?.let { player ->
            if (player.isPlaying == 1) {
                player.stopPlay()
            }
            mLinkPlayer = null
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