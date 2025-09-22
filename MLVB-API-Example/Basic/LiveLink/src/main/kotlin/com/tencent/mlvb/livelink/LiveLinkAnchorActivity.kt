package com.tencent.mlvb.livelink

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.tencent.live2.V2TXLiveCode.V2TXLIVE_OK
import com.tencent.live2.V2TXLiveDef
import com.tencent.live2.V2TXLiveDef.V2TXLiveMixInputType.V2TXLiveMixInputTypePureVideo
import com.tencent.live2.V2TXLivePlayer
import com.tencent.live2.V2TXLivePlayerObserver
import com.tencent.live2.V2TXLivePusher
import com.tencent.live2.impl.V2TXLivePlayerImpl
import com.tencent.live2.impl.V2TXLivePusherImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.mlvb.common.URLUtils
import com.tencent.rtmp.ui.TXCloudVideoView

/**
 * Co-anchoring View for Anchors
 * Features:
 * - Start publishing [LiveLinkAnchorActivity.startPush]
 * - Start co-anchoring [LiveLinkAnchorActivity.startLink]
 * - Stop co-anchoring [LiveLinkAnchorActivity.stopLink]
 * - Play the co-anchoring user's streams [LiveLinkAnchorActivity.startPlay]
 * For more information, please see the integration document {https://intl.cloud.tencent
 * .com/document/product/1071/39888}.
 * Currently only supported in China, other regions are continuing to develop.
 */
class LiveLinkAnchorActivity : MLVBBaseActivity() {
    companion object {
        private const val TAG = "LiveLinkAnchorActivity"
    }

    private lateinit var mTextTitle: TextView
    private lateinit var mButtonBack: ImageView
    private lateinit var mVideoViewAnchor: TXCloudVideoView
    private lateinit var mVideoViewAudience: TXCloudVideoView
    private lateinit var mButtonAcceptLink: Button
    private lateinit var mButtonStopLink: Button

    private var mLivePlayer: V2TXLivePlayer? = null
    private var mLivePusher: V2TXLivePusher? = null

    private lateinit var mStreamId: String
    private lateinit var mUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.livelink_activity_live_link_anchor)
        if (checkPermission()) {
            initData()
            initView()
            startPush()
        }
    }

    override fun onPermissionGranted() {
        initData()
        initView()
        startPush()
    }

    private fun initData() {
        mStreamId = intent.getStringExtra("STREAM_ID") ?: ""
        mUserId = intent.getStringExtra("USER_ID") ?: ""
    }

    private fun initView() {
        mVideoViewAudience = findViewById(R.id.tx_cloud_view_anchor)
        mVideoViewAnchor = findViewById(R.id.tx_cloud_view_audience)

        mTextTitle = findViewById(R.id.tv_title)
        mTextTitle.text = if (TextUtils.isEmpty(mStreamId)) "" else mStreamId

        mButtonAcceptLink = findViewById(R.id.btn_accept_link)
        mButtonAcceptLink.setOnClickListener {
            showInputUserIdDialog()
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

    private fun startPush() {
        mLivePusher = V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC)
        mLivePusher?.setRenderView(mVideoViewAudience)
        mLivePusher?.startCamera(true)
        val pushUrl = URLUtils.generatePushUrl(mStreamId, mUserId, 0)
        val ret = mLivePusher?.startPush(pushUrl) ?: -1
        Log.i(TAG, "startPush return: $ret")
        mLivePusher?.startMicrophone()
    }

    /**
     * Start connecting with the specified user.
     *
     * @param linkUserId The user ID that needs to be connected.
     */
    fun startLink(linkUserId: String) {
        if (TextUtils.isEmpty(linkUserId)) {
            Toast.makeText(this@LiveLinkAnchorActivity, getString(R.string.livelink_please_input_userid),
                Toast.LENGTH_SHORT).show()
            return
        }

        // Note: Because the audience register uses userId as streamId, this is the UserId of the Lianmai audience;
        startPlay(linkUserId)

        val result = mLivePusher?.setMixTranscodingConfig(createConfig(linkUserId, linkUserId)) ?: -1
        if (result == V2TXLIVE_OK) {
            mButtonAcceptLink.visibility = View.GONE
            mButtonStopLink.visibility = View.VISIBLE
        } else {
            Toast.makeText(this@LiveLinkAnchorActivity, getString(R.string.livelink_mix_stream_fail),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLink() {
        if (mLivePusher?.isPushing() == 1) {
            mLivePusher?.setMixTranscodingConfig(null)
        }
        if (mLivePlayer?.isPlaying() == 1) {
            mLivePlayer?.stopPlay()
        }
        mButtonAcceptLink.visibility = View.VISIBLE
        mButtonStopLink.visibility = View.GONE
    }

    private fun startPlay(linkStreamId: String) {
        val playURL = URLUtils.generatePlayUrl(linkStreamId, mUserId, 3)
        if (mLivePlayer == null) {
            mLivePlayer = V2TXLivePlayerImpl(this@LiveLinkAnchorActivity)
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

    private fun showInputUserIdDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(R.string.livelink_tips_input_userid)
        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        dialog.setView(editText)
        dialog.setPositiveButton(R.string.livelink_ok) { _, _ ->
            startLink(editText.text.toString())
        }

        dialog.create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLivePusher?.let { pusher ->
            pusher.stopCamera()
            pusher.stopMicrophone()
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

    private fun createConfig(linkStreamId: String, linkUserId: String): V2TXLiveDef.V2TXLiveTranscodingConfig {
        val config = V2TXLiveDef.V2TXLiveTranscodingConfig()
        config.videoWidth = 360
        config.videoHeight = 640
        config.videoBitrate = 900
        config.videoFramerate = 15
        config.videoGOP = 2
        config.backgroundColor = 0x000000
        config.backgroundImage = null
        config.audioSampleRate = 48000
        config.audioBitrate = 64
        config.audioChannels = 1
        config.outputStreamId = null
        config.mixStreams = ArrayList()

        val mixStream = V2TXLiveDef.V2TXLiveMixStream()
        mixStream.userId = mUserId
        mixStream.streamId = mStreamId
        mixStream.x = 0
        mixStream.y = 0
        mixStream.width = 360
        mixStream.height = 640
        mixStream.zOrder = 0
        mixStream.inputType = V2TXLiveMixInputTypePureVideo
        config.mixStreams.add(mixStream)

        val remote = V2TXLiveDef.V2TXLiveMixStream()
        remote.userId = linkUserId
        remote.streamId = linkStreamId
        remote.x = 150
        remote.y = 300
        remote.width = 135
        remote.height = 240
        remote.zOrder = 1
        remote.inputType = V2TXLiveMixInputTypePureVideo
        config.mixStreams.add(remote)
        return config
    }
}