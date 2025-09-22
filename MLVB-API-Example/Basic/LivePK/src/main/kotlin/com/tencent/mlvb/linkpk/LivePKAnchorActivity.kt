package com.tencent.mlvb.linkpk

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
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
import com.tencent.mlvb.livepk.R
import com.tencent.rtmp.ui.TXCloudVideoView
import java.util.*
import kotlin.random.Random

/**
 * Competition View for Anchors
 * Features:
 * - Start publishing [LivePKAnchorActivity.startPush]
 * - Start competition [LivePKAnchorActivity.startPK]
 * - Stop competition [LivePKAnchorActivity.stopPK]
 * - Play the other anchor's streams [LivePKAnchorActivity.startPlay]
 * For more information, please see the integration document {https://intl.cloud.tencent
 * .com/document/product/1071/39888}.
 * Currently only supported in China, other regions are continuing to develop.
 */
class LivePKAnchorActivity : MLVBBaseActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "LivePKAnchorActivity"
    }

    private lateinit var mPlayRenderView: TXCloudVideoView
    private var mLivePlayer: V2TXLivePlayer? = null
    private lateinit var mPushRenderView: TXCloudVideoView
    private var mLivePusher: V2TXLivePusher? = null
    private lateinit var mTextTitle: TextView

    private lateinit var mStreamId: String
    private lateinit var mUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.livepk_activity_live_pk_anchor)
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
        mStreamId = intent.getStringExtra("STREAM_ID") ?: ""
        mUserId = intent.getStringExtra("USER_ID") ?: ""
    }

    private fun initView() {
        mPushRenderView = findViewById(R.id.tx_cloud_view_push)
        mPlayRenderView = findViewById(R.id.tx_cloud_view_play)
        mTextTitle = findViewById(R.id.tv_title)

        findViewById<View>(R.id.iv_back).setOnClickListener(this)
        findViewById<View>(R.id.btn_accept_pk).setOnClickListener(this)
        findViewById<View>(R.id.btn_stop_pk).setOnClickListener(this)

        if (!TextUtils.isEmpty(mStreamId)) {
            mTextTitle.text = mStreamId
        }
    }

    private fun startPush() {
        mLivePusher = V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC)
        mLivePusher?.setRenderView(mPushRenderView)
        mLivePusher?.startCamera(true)
        val pushUrl = URLUtils.generatePushUrl(mStreamId, mUserId, 0)
        val ret = mLivePusher?.startPush(pushUrl) ?: -1
        Log.i(TAG, "startPush return: $ret")
        mLivePusher?.startMicrophone()
    }

    /**
     * Start pk with the user corresponding to the stream id.
     *
     * @param pkStreamId The stream id that needs to be PKed.
     */
    @SuppressLint("SetTextI18n")
    fun startPK(pkStreamId: String) {
        if (TextUtils.isEmpty(pkStreamId)) {
            Toast.makeText(this@LivePKAnchorActivity, getString(R.string.livepk_please_input_streamid),
                Toast.LENGTH_SHORT).show()
            return
        }

        startPlay(pkStreamId)

        val result = mLivePusher?.setMixTranscodingConfig(createConfig(pkStreamId)) ?: -1
        if (result == V2TXLIVE_OK) {
            findViewById<View>(R.id.btn_stop_pk).visibility = View.VISIBLE
            findViewById<View>(R.id.btn_accept_pk).visibility = View.GONE
        } else {
            Toast.makeText(this@LivePKAnchorActivity, getString(R.string.livepk_mix_stream_fail), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun stopPK() {
        if (mLivePusher?.isPushing() == 1) {
            mLivePusher?.setMixTranscodingConfig(null)
        }
        if (mLivePlayer?.isPlaying() == 1) {
            mLivePlayer?.stopPlay()
        }
        findViewById<View>(R.id.btn_stop_pk).visibility = View.GONE
        findViewById<View>(R.id.btn_accept_pk).visibility = View.VISIBLE
    }

    private fun startPlay(linkStreamId: String) {
        val userId = Random.nextInt(10000).toString()
        val playURL = URLUtils.generatePlayUrl(linkStreamId, userId, 3)
        if (mLivePlayer == null) {
            mLivePlayer = V2TXLivePlayerImpl(this@LivePKAnchorActivity)
            mLivePlayer?.setRenderView(mPlayRenderView)
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

    private fun createConfig(linkStreamId: String): V2TXLiveDef.V2TXLiveTranscodingConfig {
        val config = V2TXLiveDef.V2TXLiveTranscodingConfig()
        config.videoWidth = 750
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
        mixStream.x = 10
        mixStream.y = 0
        mixStream.width = 360
        mixStream.height = 640
        mixStream.zOrder = 0
        mixStream.inputType = V2TXLiveMixInputTypePureVideo
        config.mixStreams.add(mixStream)

        val remote = V2TXLiveDef.V2TXLiveMixStream()
        remote.streamId = linkStreamId
        remote.x = 380
        remote.y = 0
        remote.width = 360
        remote.height = 640
        remote.zOrder = 1
        remote.inputType = V2TXLiveMixInputTypePureVideo
        config.mixStreams.add(remote)
        return config
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

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> finish()
            R.id.btn_accept_pk -> showInputUserIdDialog()
            R.id.btn_stop_pk -> stopPK()
        }
    }

    private fun showInputUserIdDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(R.string.livepk_input_other_info)
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.VERTICAL
        val editStreamId = EditText(this)
        editStreamId.inputType = InputType.TYPE_CLASS_NUMBER
        editStreamId.hint = getString(R.string.livepk_please_input_streamid)
        ll.addView(editStreamId)
        dialog.setView(ll)
        dialog.setPositiveButton(R.string.livepk_ok) { _, _ ->
            val streamId = editStreamId.text.toString()
            startPK(streamId)
        }
        dialog.create()
        dialog.show()
    }
}