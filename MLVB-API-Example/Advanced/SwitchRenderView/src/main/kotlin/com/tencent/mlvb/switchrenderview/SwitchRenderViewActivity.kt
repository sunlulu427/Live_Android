package com.tencent.mlvb.switchrenderview

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.tencent.live2.V2TXLiveDef
import com.tencent.live2.V2TXLivePusher
import com.tencent.live2.impl.V2TXLivePusherImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.mlvb.common.URLUtils
import com.tencent.rtmp.ui.TXCloudVideoView
import java.util.Random

/**
 * Example for Dynamically Switching Rendering Controls
 *
 * This document shows how to use different types of views to render a video.
 * Views supported include:
 * TextureView, SurfaceView, TXCloudVideoView
 *
 * - For more information, please see the API document {
 * https://liteav.sdk.qcloud.com/doc/api/zh-cn/group__V2TXLivePusher__android.html#afc848d88fe99790b8c0988b8525dd4d9}.
 */
class SwitchRenderViewActivity : MLVBBaseActivity() {

    companion object {
        private val TAG = SwitchRenderViewActivity::class.java.simpleName
    }

    private lateinit var mPushTencentView: TXCloudVideoView
    private lateinit var mPushTextureView: TextureView
    private lateinit var mPushSurfaceView: SurfaceView
    private lateinit var mEditStreamId: EditText
    private var mLivePusher: V2TXLivePusher? = null
    private lateinit var mRadioView: RadioGroup
    private lateinit var mButtonPush: Button
    private lateinit var mTextTitle: TextView
    private var mLastRadioButton = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.switchrenderview_activity_switch_render_view)
        if (checkPermission()) {
            initView()
        }
    }

    override fun onPermissionGranted() {
        initView()
    }

    private fun initView() {
        mPushTencentView = findViewById(R.id.tx_cloud_view)
        mPushTextureView = findViewById(R.id.tuv_texture)
        mPushSurfaceView = findViewById(R.id.sv_surface)
        mRadioView = findViewById(R.id.rg_view)
        mEditStreamId = findViewById(R.id.et_stream_id)
        mButtonPush = findViewById(R.id.btn_push)
        mTextTitle = findViewById(R.id.tv_title)

        mEditStreamId.setText(generateStreamId())

        findViewById<View>(R.id.iv_back).setOnClickListener {
            finish()
        }

        mRadioView.check(R.id.rb_txcloudvideoview)
        mLastRadioButton = R.id.rb_txcloudvideoview

        mButtonPush.setOnClickListener {
            if (mLivePusher?.isPushing == 1) {
                stopPush()
                mButtonPush.setText(R.string.switchrenderview_start_push)
            } else {
                startPush()
                mButtonPush.setText(R.string.switchrenderview_stop_push)
            }
        }

        mRadioView.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_txcloudvideoview -> {
                    if (mLivePusher?.isPushing == 1) {
                        Toast.makeText(
                            this@SwitchRenderViewActivity,
                            getString(R.string.switchrenderview_please_restart_push),
                            Toast.LENGTH_SHORT
                        ).show()
                        mRadioView.check(mLastRadioButton)
                        return@setOnCheckedChangeListener
                    }
                    mPushSurfaceView.visibility = View.GONE
                    mPushTextureView.visibility = View.GONE
                    mPushTencentView.visibility = View.VISIBLE
                }
                R.id.rb_textureview -> {
                    if (mLivePusher?.isPushing == 1) {
                        Toast.makeText(
                            this@SwitchRenderViewActivity,
                            getString(R.string.switchrenderview_please_restart_push),
                            Toast.LENGTH_SHORT
                        ).show()
                        mRadioView.check(mLastRadioButton)
                        return@setOnCheckedChangeListener
                    }
                    mPushSurfaceView.visibility = View.GONE
                    mPushTencentView.visibility = View.GONE
                    mPushTextureView.visibility = View.VISIBLE
                }
                R.id.rb_surfaceview -> {
                    if (mLivePusher?.isPushing == 1) {
                        Toast.makeText(
                            this@SwitchRenderViewActivity,
                            getString(R.string.switchrenderview_please_restart_push),
                            Toast.LENGTH_SHORT
                        ).show()
                        mRadioView.check(mLastRadioButton)
                        return@setOnCheckedChangeListener
                    }
                    mPushTextureView.visibility = View.GONE
                    mPushTencentView.visibility = View.GONE
                    mPushSurfaceView.visibility = View.VISIBLE
                }
            }
        }

        mEditStreamId.text.toString().takeIf { it.isNotEmpty() }?.let { streamId ->
            mTextTitle.text = streamId
        }
    }

    private fun stopPush() {
        mLivePusher?.let { pusher ->
            pusher.stopCamera()
            pusher.stopMicrophone()
            if (pusher.isPushing == 1) {
                pusher.stopPush()
            }
        }
        mLivePusher = null
    }

    private fun startPush() {
        val streamId = mEditStreamId.text.toString()
        if (streamId.isEmpty()) {
            Toast.makeText(
                this@SwitchRenderViewActivity,
                getString(R.string.switchrenderview_please_input_streamid),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        mTextTitle.text = streamId

        if (mLivePusher == null) {
            mLivePusher = V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTMP)
        }

        val checkedId = mRadioView.checkedRadioButtonId
        when (checkedId) {
            R.id.rb_txcloudvideoview -> {
                mLivePusher?.setRenderView(mPushTencentView)
                mPushSurfaceView.visibility = View.GONE
                mPushTextureView.visibility = View.GONE
                mPushTencentView.visibility = View.VISIBLE
                mLastRadioButton = R.id.rb_txcloudvideoview
            }
            R.id.rb_textureview -> {
                mLivePusher?.setRenderView(mPushTextureView)
                mPushSurfaceView.visibility = View.GONE
                mPushTextureView.visibility = View.VISIBLE
                mPushTencentView.visibility = View.GONE
                mLastRadioButton = R.id.rb_textureview
            }
            R.id.rb_surfaceview -> {
                mLivePusher?.setRenderView(mPushSurfaceView)
                mPushSurfaceView.visibility = View.VISIBLE
                mPushTextureView.visibility = View.GONE
                mPushTencentView.visibility = View.GONE
                mLastRadioButton = R.id.rb_surfaceview
            }
        }

        mLivePusher?.let { pusher ->
            pusher.startCamera(true)
            val userId = Random().nextInt(10000).toString()
            val pushUrl = URLUtils.generatePushUrl(streamId, userId, 1)
            val ret = pusher.startPush(pushUrl)
            Log.i(TAG, "startPush return: $ret")
            pusher.startMicrophone()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPush()
    }

    override fun onBackPressed() {
        finish()
    }
}