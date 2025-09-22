package com.tencent.mlvb.livelink

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.tencent.live2.V2TXLiveDef
import com.tencent.live2.V2TXLivePlayer
import com.tencent.live2.V2TXLivePlayerObserver
import com.tencent.live2.V2TXLivePusher
import com.tencent.live2.impl.V2TXLivePlayerImpl
import com.tencent.live2.impl.V2TXLivePusherImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.mlvb.common.URLUtils
import com.tencent.rtmp.ui.TXCloudVideoView

/**
 * Co-anchoring View for Audience
 * Features:
 * - Play audio/video streams [LiveLinkAudienceActivity.startPlay]
 * - Start co-anchoring [LiveLinkAudienceActivity.startLink]
 * - Stop co-anchoring [LiveLinkAudienceActivity.stopLink]
 * - For more information, please see the integration document {https://intl.cloud.tencent
 * .com/document/product/1071/39888}.
 */
class LiveLinkAudienceActivity : MLVBBaseActivity() {
    companion object {
        private const val TAG = "LiveLinkActivity"
    }

    private lateinit var mVideoViewAnchor: TXCloudVideoView
    private lateinit var mVideoViewAudience: TXCloudVideoView
    private lateinit var mTextTitle: TextView
    private lateinit var mButtonStartLink: Button
    private lateinit var mButtonStopLink: Button
    private lateinit var mButtonBack: ImageView

    private var mLivePlayer: V2TXLivePlayer? = null
    private var mLivePusher: V2TXLivePusher? = null

    private lateinit var mStreamId: String
    private lateinit var mUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.livelink_activity_live_link_audience)

        if (checkPermission()) {
            initData()
            initView()
            startPlay()
        }
    }

    override fun onPermissionGranted() {
        initData()
        initView()
        startPlay()
    }

    private fun initData() {
        mStreamId = intent.getStringExtra("STREAM_ID") ?: ""
        mUserId = intent.getStringExtra("USER_ID") ?: ""
    }

    private fun initView() {
        mVideoViewAnchor = findViewById(R.id.tx_cloud_view_anchor)
        mVideoViewAudience = findViewById(R.id.tx_cloud_view_audience)

        mTextTitle = findViewById(R.id.tv_title)
        mTextTitle.text = if (TextUtils.isEmpty(mStreamId)) "" else mStreamId

        mButtonStartLink = findViewById(R.id.btn_start_link)
        mButtonStartLink.setOnClickListener {
            startLink()
        }

        mButtonStopLink = findViewById(R.id.btn_stop_link)
        mButtonStopLink.setOnClickListener {
            stopLink()
        }

        mButtonBack = findViewById(R.id.iv_back)
        mButtonBack.setOnClickListener {
            finish()
        }
    }

    private fun startPush(streamId: String, userId: String) {
        mLivePusher = V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC)
        mLivePusher?.setRenderView(mVideoViewAudience)
        mLivePusher?.startCamera(true)
        val pushUrl = URLUtils.generatePushUrl(streamId, userId, 0)
        val ret = mLivePusher?.startPush(pushUrl) ?: -1
        Log.i(TAG, "startPush return: $ret")
        mLivePusher?.startMicrophone()
    }

    private fun startPlay() {
        val playURL = URLUtils.generatePlayUrl(mStreamId, "", 4)
        if (mLivePlayer == null) {
            mLivePlayer = V2TXLivePlayerImpl(this@LiveLinkAudienceActivity)
            mLivePlayer?.setRenderView(mVideoViewAnchor)
            mLivePlayer?.setObserver(object : V2TXLivePlayerObserver() {
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

        val result = mLivePlayer?.startLivePlay(playURL) ?: -1
        Log.d(TAG, "startLivePlay : $result")
    }

    fun startLink() {
        if (mLivePlayer?.isPlaying() == 1) {
            mLivePlayer?.stopPlay()
        }

        val playURL = URLUtils.generatePlayUrl(mStreamId, mUserId, 3)
        mLivePlayer?.setRenderView(mVideoViewAnchor)
        val result = mLivePlayer?.startLivePlay(playURL) ?: -1
        Log.d(TAG, "startLivePlay : $result")

        // Note: Use userId as streamId to reduce parameters as much as possible;
        startPush(mUserId, mUserId)

        mButtonStartLink.visibility = View.GONE
        mButtonStopLink.visibility = View.VISIBLE
    }

    fun stopLink() {
        if (mLivePlayer?.isPlaying() == 1) {
            mLivePlayer?.stopPlay()
        }
        mLivePusher?.let { pusher ->
            pusher.stopCamera()
            if (pusher.isPushing() == 1) {
                pusher.stopPush()
            }
            mLivePusher = null
        }
        startPlay()

        mButtonStartLink.visibility = View.VISIBLE
        mButtonStopLink.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        mLivePusher?.let { pusher ->
            pusher.stopCamera()
            if (pusher.isPushing() == 1) {
                pusher.stopPush()
            }
            mLivePusher = null
        }

        mLivePlayer?.let { player ->
            if (player.isPlaying() == 1) {
                player.stopPlay()
            }
            mLivePlayer = null
        }
    }

    override fun onBackPressed() {
        finish()
    }
}