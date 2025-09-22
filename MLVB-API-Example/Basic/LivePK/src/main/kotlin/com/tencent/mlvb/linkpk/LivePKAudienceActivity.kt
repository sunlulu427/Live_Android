package com.tencent.mlvb.linkpk

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import com.tencent.live2.V2TXLivePlayer
import com.tencent.live2.V2TXLivePlayerObserver
import com.tencent.live2.impl.V2TXLivePlayerImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.mlvb.common.URLUtils
import com.tencent.mlvb.livepk.R
import com.tencent.rtmp.ui.TXCloudVideoView

/**
 * Competition View for Audience
 * Features:
 * - Play audio/video streams [LivePKAudienceActivity.startPlay]
 * - For more information, please see the integration document {https://intl.cloud.tencent
 * .com/document/product/1071/39888}.
 */
class LivePKAudienceActivity : MLVBBaseActivity() {
    companion object {
        private const val TAG = "LivePKAudienceActivity"
    }

    private lateinit var mPlayRenderView: TXCloudVideoView
    private var mLivePlayer: V2TXLivePlayer? = null
    private lateinit var mTextTitle: TextView

    private lateinit var mStreamId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.livepk_activity_live_pk_audience)
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
        mStreamId = intent.getStringExtra("STREAM_ID") ?: ""
    }

    private fun initView() {
        mPlayRenderView = findViewById(R.id.tx_cloud_view_play)
        mTextTitle = findViewById(R.id.tv_title)

        findViewById<View>(R.id.iv_back).setOnClickListener {
            finish()
        }

        if (!TextUtils.isEmpty(mStreamId)) {
            mTextTitle.text = mStreamId
        }
    }

    private fun startPlay() {
        val playURL = URLUtils.generatePlayUrl(mStreamId, "", 4)
        if (mLivePlayer == null) {
            mLivePlayer = V2TXLivePlayerImpl(this@LivePKAudienceActivity)
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

    override fun onDestroy() {
        super.onDestroy()
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