package com.tencent.mlvb.livepushscreen

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.tencent.live2.V2TXLiveCode.V2TXLIVE_OK
import com.tencent.live2.V2TXLiveDef
import com.tencent.live2.V2TXLivePusher
import com.tencent.live2.impl.V2TXLivePusherImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.mlvb.common.URLUtils
import kotlin.random.Random

/**
 * Publishing (Screen) View
 *
 * Features:
 * - Start screen sharing [LivePushScreenActivity.startScreenPush]
 * - Stop screen sharing [LivePushScreenActivity.stopScreenPush]
 *
 * For more information, please see the integration document {https://cloud.tencent.com/document/product/454/56595}.
 * Currently only supported in China, other regions are continuing to develop.
 */
class LivePushScreenActivity : MLVBBaseActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "LivePushScreenActivity"
    }

    private var mLivePusher: V2TXLivePusher? = null
    private lateinit var mTextTitle: TextView
    private lateinit var mButtonPush: Button

    private lateinit var mStreamId: String
    private var mStreamType = 0
    private var mPushFlag = false

    private var mAudioQuality: V2TXLiveDef.V2TXLiveAudioQuality =
        V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.livepushscreen_activity_push_screen)
        if (checkPermission()) {
            initIntentData()
            initView()
        }
    }

    private fun initIntentData() {
        mStreamId = intent.getStringExtra("STREAM_ID") ?: ""
        mStreamType = intent.getIntExtra("STREAM_TYPE", 0)
        mAudioQuality = intent.getSerializableExtra("AUDIO_QUALITY") as? V2TXLiveDef.V2TXLiveAudioQuality
            ?: V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault

        Log.d(TAG, "initIntentData: $mStreamId : $mStreamType : $mAudioQuality")
    }

    override fun onPermissionGranted() {
        initIntentData()
        initView()
    }

    private fun initView() {
        mTextTitle = findViewById(R.id.tv_stream_id)
        mButtonPush = findViewById(R.id.btn_push)

        mButtonPush.setOnClickListener(this)
        findViewById<View>(R.id.iv_back).setOnClickListener(this)

        if (!TextUtils.isEmpty(mStreamId)) {
            mTextTitle.text = mStreamId
        }
    }

    private fun startScreenPush() {
        val pushUrl: String = if (mStreamType == 0) {
            val userId = Random.nextInt(10000).toString()
            val url = URLUtils.generatePushUrl(mStreamId, userId, 0)
            mLivePusher = V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC)
            url
        } else {
            val url = URLUtils.generatePushUrl(mStreamId, "", 1)
            mLivePusher = V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTMP)
            url
        }

        Log.d(TAG, "pushUrl: $pushUrl")
        mLivePusher?.setAudioQuality(mAudioQuality)
        mLivePusher?.startScreenCapture()
        val ret = mLivePusher?.startPush(pushUrl) ?: -1
        if (ret == V2TXLIVE_OK) {
            mLivePusher?.startMicrophone()
            mPushFlag = true
            mButtonPush.setText(R.string.livepushscreen_close_screen_push)
        }
        Log.i(TAG, "startPush return: $ret")
    }

    private fun stopScreenPush() {
        if (mPushFlag && mLivePusher != null) {
            mLivePusher?.stopMicrophone()
            mLivePusher?.stopScreenCapture()
            mLivePusher = null
        }
        mButtonPush.setText(R.string.livepushscreen_start_screen_push)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScreenPush()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> finish()
            R.id.btn_push -> {
                if (mPushFlag) {
                    stopScreenPush()
                    mPushFlag = false
                } else {
                    startScreenPush()
                }
            }
        }
    }
}