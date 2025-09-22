package com.tencent.mlvb.thirdbeauty

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.tencent.live2.V2TXLiveDef
import com.tencent.live2.V2TXLiveDef.V2TXLiveBufferType
import com.tencent.live2.V2TXLiveDef.V2TXLivePixelFormat
import com.tencent.live2.V2TXLivePusher
import com.tencent.live2.V2TXLivePusherObserver
import com.tencent.live2.impl.V2TXLivePusherImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.mlvb.common.URLUtils
import com.tencent.rtmp.ui.TXCloudVideoView
import java.util.*

/**
 * TRTC Third-Party Beauty Filter View
 * You must call [V2TXLivePusher.enableCustomVideoProcess]
 * to enable custom video processing before you can receive this callback.
 * - Before stream publishing, call [V2TXLivePusher.enableCustomVideoProcess] to enable custom rendering.
 * - Call [V2TXLivePusher.setObserver] to listen for video data from the SDK.
 * - After data is received, use third-party beauty filters to process the data
 * in [V2TXLivePusherObserver.onProcessVideoFrame].
 * This demo integrates the third-party beauty function of faceunity
 * To enable this function, you need to refer to the faceunity SDK integration document:{ https://www.faceunity.com/developer-center.html }This demo has integrated the faceunity SDK,
 * However, it should be noted that the certificate issued by faceunity
 * technology for Android terminal is AuthPack.java file.
 * You need to obtain the certificate and use your certificate to replace the AuthPack file in our demo
 */
class ThirdBeautyFaceUnityActivity : MLVBBaseActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "FaceUnityActivity"
    }

    private lateinit var mPushRenderView: TXCloudVideoView
    private var mLivePusher: V2TXLivePusher? = null
    private lateinit var mSeekBlurLevel: SeekBar
    private lateinit var mTextBlurLevel: TextView
    private lateinit var mEditStreamId: EditText
    private lateinit var mButtonPush: Button
    private lateinit var mTextTitle: TextView
//    private var mFURenderer: FURenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.thirdbeauty_activity_third_beauty)
//        FURenderer.setup(applicationContext)
//        mFURenderer = FURenderer.Builder(applicationContext)
//                .setCreateEglContext(false)
//                .setInputTextureType(0)
//                .setCreateFaceBeauty(true)
//                .build()
        if (checkPermission()) {
            initView()
        }
    }

    override fun onPermissionGranted() {
        initView()
    }

    private fun initView() {
        mPushRenderView = findViewById(R.id.pusher_tx_cloud_view)
        mSeekBlurLevel = findViewById(R.id.sb_blur_level)
        mTextBlurLevel = findViewById(R.id.tv_blur_level)
        mButtonPush = findViewById(R.id.btn_push)
        mEditStreamId = findViewById(R.id.et_stream_id)
        mTextTitle = findViewById(R.id.tv_title)

        mEditStreamId.setText(generateStreamId())
        findViewById<View>(R.id.iv_back).setOnClickListener(this)

        mSeekBlurLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (mLivePusher?.isPushing == 1 && fromUser) {
//                    mFURenderer?.faceBeautyModule?.setBlurLevel(seekBar.progress / 9f)
                }
                mTextBlurLevel.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }
        })

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
                getString(R.string.thirdbeauty_please_input_streamd),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        mTextTitle.text = streamId
        mLivePusher = V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTMP)

        mLivePusher?.enableCustomVideoProcess(true, V2TXLivePixelFormat.V2TXLivePixelFormatTexture2D, V2TXLiveBufferType.V2TXLiveBufferTypeTexture)
        mLivePusher?.setObserver(object : V2TXLivePusherObserver() {
            override fun onGLContextCreated() {
//                mFURenderer?.onSurfaceCreated()
            }

            override fun onProcessVideoFrame(
                srcFrame: V2TXLiveDef.V2TXLiveVideoFrame?,
                dstFrame: V2TXLiveDef.V2TXLiveVideoFrame?
            ): Int {
//                dstFrame?.texture?.textureId = mFURenderer
//                        ?.onDrawFrameSingleInput(srcFrame?.texture?.textureId ?: 0, srcFrame?.width ?: 0, srcFrame?.height ?: 0) ?: 0
                return 0
            }

            override fun onGLContextDestroyed() {
//                mFURenderer?.onSurfaceDestroyed()
            }
        })
        mLivePusher?.setRenderView(mPushRenderView)
        mLivePusher?.startCamera(true)
        val userId = Random().nextInt(10000).toString()
        val pushUrl = URLUtils.generatePushUrl(streamId, userId, 1)
        val ret = mLivePusher?.startPush(pushUrl) ?: -1
        Log.i(TAG, "startPush return: $ret")
        mLivePusher?.startMicrophone()
        mButtonPush.setText(R.string.thirdbeauty_stop_push)
    }

    override fun onDestroy() {
        super.onDestroy()
        mLivePusher?.let { pusher ->
            pusher.stopCamera()
            pusher.stopMicrophone()
            if (pusher.isPushing == 1) {
                pusher.stopPush()
            }
        }
        mLivePusher = null
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
        mLivePusher?.let { pusher ->
            pusher.stopCamera()
            pusher.stopMicrophone()
            if (pusher.isPushing == 1) {
                pusher.stopPush()
            }
        }
        mLivePusher = null
        mButtonPush.setText(R.string.thirdbeauty_start_push)
    }
}