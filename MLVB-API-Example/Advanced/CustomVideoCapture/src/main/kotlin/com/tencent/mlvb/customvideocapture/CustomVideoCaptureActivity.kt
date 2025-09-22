package com.tencent.mlvb.customvideocapture

import android.annotation.SuppressLint
import android.opengl.EGLContext
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.tencent.live2.V2TXLiveDef
import com.tencent.live2.V2TXLiveDef.V2TXLiveBufferType
import com.tencent.live2.V2TXLiveDef.V2TXLivePixelFormat
import com.tencent.live2.V2TXLivePusher
import com.tencent.live2.impl.V2TXLivePusherImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.mlvb.common.URLUtils
import com.tencent.mlvb.customvideocapture.helper.CustomCameraCapture
import com.tencent.mlvb.customvideocapture.helper.CustomFrameRender
import com.tencent.rtmp.ui.TXCloudVideoView
import java.util.*

/**
 * Example for Custom Video Capturing & Rendering
 * This document shows how to enable custom video capturing and rendering.
 * Custom capturing:
 * - Before stream publishing, call [V2TXLivePusher.enableCustomVideoCapture] to enable custom capturing.
 * - Call [V2TXLivePusher.sendCustomVideoFrame] to send data to the SDK.
 * Custom rendering
 * - Before stream publishing, call
 * [V2TXLivePusher.enableCustomVideoProcess] to enable custom rendering.
 * - Call [V2TXLivePusher.setObserver] to listen for video data from the SDK.
 * - After data is received, execute the rendering logic in
 * [V2TXLivePusherObserver.onProcessVideoFrame].
 * - For more information, please see the API document {https://cloud.tencent.com/document/product/454/56601}.
 */
class CustomVideoCaptureActivity : MLVBBaseActivity(), View.OnClickListener {

    companion object {
        private val TAG = CustomVideoCaptureActivity::class.java.simpleName
    }

    private var mLivePusher: V2TXLivePusher? = null
    private lateinit var mEditStreamId: EditText
    private lateinit var mButtonPush: Button
    private var mCustomCameraCapture: CustomCameraCapture? = null
    private var mCustomFrameRender: CustomFrameRender? = null
    private lateinit var mPushRenderView: TXCloudVideoView
    private lateinit var mTextTitle: TextView

    private val mVideoFrameReadListener = object : CustomCameraCapture.VideoFrameReadListener {
        @SuppressLint("NewApi")
        override fun onFrameAvailable(eglContext: EGLContext?, textureId: Int, width: Int, height: Int) {
            val videoFrame = V2TXLiveDef.V2TXLiveVideoFrame().apply {
                pixelFormat = V2TXLivePixelFormat.V2TXLivePixelFormatTexture2D
                bufferType = V2TXLiveBufferType.V2TXLiveBufferTypeTexture

                texture = V2TXLiveDef.V2TXLiveTexture().apply {
                    this.textureId = textureId
                    eglContext14 = eglContext
                }

                this.width = width
                this.height = height
            }

            mLivePusher?.let { pusher ->
                if (pusher.isPushing == 1) {
                    val ret = pusher.sendCustomVideoFrame(videoFrame)
                    Log.d(TAG, "sendCustomVideoFrame : $ret")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.customvideocaptureactivity_activity_custom_video_capture)
        if (checkPermission()) {
            initView()
        }
    }

    override fun onPermissionGranted() {
        initView()
    }

    private fun initView() {
        mPushRenderView = findViewById(R.id.tx_cloud_view)
        mButtonPush = findViewById(R.id.btn_push)
        mEditStreamId = findViewById(R.id.et_stream_id)
        mTextTitle = findViewById(R.id.tv_title)

        mEditStreamId.setText(generateStreamId())
        findViewById<View>(R.id.iv_back).setOnClickListener(this)
        mButtonPush.setOnClickListener(this)

        mEditStreamId.text?.toString()?.takeIf { it.isNotEmpty() }?.let { streamId ->
            mTextTitle.text = streamId
        }
    }

    private fun startPush() {
        val streamId = mEditStreamId.text.toString()
        if (TextUtils.isEmpty(streamId)) {
            Toast.makeText(
                this,
                getString(R.string.customvideocapture_please_input_streamid),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        mTextTitle.text = streamId

        mCustomCameraCapture = CustomCameraCapture()
        mCustomFrameRender = CustomFrameRender()

        mLivePusher = V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTMP).apply {
            setObserver(mCustomFrameRender)
            enableCustomVideoCapture(true)
        }

        val userId = Random().nextInt(10000).toString()
        val pushUrl = URLUtils.generatePushUrl(streamId, userId, 1)
        val ret = mLivePusher?.startPush(pushUrl) ?: -1
        Log.i(TAG, "startPush return: $ret")
        mLivePusher?.startMicrophone()

        if (ret == 0) {
            mCustomCameraCapture?.start(mVideoFrameReadListener)

            mLivePusher?.enableCustomVideoProcess(
                true,
                V2TXLivePixelFormat.V2TXLivePixelFormatTexture2D,
                V2TXLiveBufferType.V2TXLiveBufferTypeTexture
            )
            val textureView = TextureView(this)
            mPushRenderView.addVideoView(textureView)
            mCustomFrameRender?.start(textureView)
            mButtonPush.setText(R.string.customvideocapture_stop_push)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPush()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_push -> push()
            R.id.iv_back -> finish()
        }
    }

    private fun push() {
        mLivePusher?.let { pusher ->
            if (pusher.isPushing == 1) {
                stopPush()
            } else {
                startPush()
            }
        } ?: startPush()
    }

    private fun stopPush() {
        mCustomCameraCapture?.stop()
        mCustomFrameRender?.stop()

        mLivePusher?.let { pusher ->
            pusher.stopMicrophone()
            if (pusher.isPushing == 1) {
                pusher.stopPush()
            }
        }
        mLivePusher = null
        mButtonPush.setText(R.string.customvideocapture_start_push)
    }
}