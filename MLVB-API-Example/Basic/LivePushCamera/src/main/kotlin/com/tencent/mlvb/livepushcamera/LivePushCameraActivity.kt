package com.tencent.mlvb.livepushcamera

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.tencent.live2.V2TXLiveDef
import com.tencent.live2.V2TXLivePusher
import com.tencent.live2.impl.V2TXLivePusherImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.mlvb.common.URLUtils
import com.tencent.rtmp.ui.TXCloudVideoView
import java.util.*

/**
 * Publishing (Camera) View
 * Features:
 * - Start publishing [LivePushCameraActivity.startPush]
 * - Turn on mic [LivePushCameraActivity.enableMic]
 * - Set resolution [LivePushCameraActivity.showResolutionMenu]
 * - Set rotation [LivePushCameraActivity.showRotateMenu]
 * - Set mirror mode [LivePushCameraActivity.showMirrorMenu]
 * For more information, please see the integration document {https://intl.cloud.tencent
 * .com/document/product/1071/38158}.
 * RTC Push Currently only supported in China, other regions are continuing to develop.
 */
class LivePushCameraActivity : MLVBBaseActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "LivePushCameraActivity"
    }

    private lateinit var pushRenderView: TXCloudVideoView
    private var livePusher: V2TXLivePusher? = null
    private lateinit var textTitle: TextView
    private lateinit var linearResolution: LinearLayout
    private lateinit var textResolution: TextView
    private lateinit var linearRotate: LinearLayout
    private lateinit var textRotate: TextView
    private lateinit var linearMirror: LinearLayout
    private lateinit var textMirror: TextView
    private lateinit var buttonMic: Button

    private var streamId: String? = null
    private var streamType = 0
    private var micFlag = true

    private var audioQuality = V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault
    private var rotationFlag = V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0
    private var mirrorFlag = V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeAuto
    private var resolutionFlag = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.livepushcamera_activity_push_camera)
        if (checkPermission()) {
            initIntentData()
            initView()
            startPush()
        }
    }

    private fun initIntentData() {
        streamId = intent.getStringExtra("STREAM_ID")
        streamType = intent.getIntExtra("STREAM_TYPE", 0)

        val audioQualityObj = intent.getSerializableExtra("AUDIO_QUALITY")
        audioQuality = if (audioQualityObj is V2TXLiveDef.V2TXLiveAudioQuality) {
            audioQualityObj
        } else {
            V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault
        }

        Log.d(TAG, "initIntentData: $streamId : $streamType : $audioQuality")
    }

    override fun onPermissionGranted() {
        initIntentData()
        initView()
        startPush()
    }

    private fun initView() {
        pushRenderView = findViewById(R.id.pusher_tx_cloud_view)
        textTitle = findViewById(R.id.tv_stream_id)
        linearResolution = findViewById(R.id.ll_resolution)
        textResolution = findViewById(R.id.tv_resolution)
        linearRotate = findViewById(R.id.ll_rotate)
        textRotate = findViewById(R.id.tv_rotate)
        linearMirror = findViewById(R.id.ll_mirror)
        textMirror = findViewById(R.id.tv_mirror)
        buttonMic = findViewById(R.id.btn_mic)

        findViewById<View>(R.id.iv_back).setOnClickListener(this)
        findViewById<View>(R.id.btn_mic).setOnClickListener(this)
        findViewById<View>(R.id.ll_resolution).setOnClickListener(this)
        findViewById<View>(R.id.ll_rotate).setOnClickListener(this)
        findViewById<View>(R.id.ll_mirror).setOnClickListener(this)

        if (!TextUtils.isEmpty(streamId)) {
            textTitle.text = streamId
        }
    }

    private fun startPush() {
        val pushUrl = if (streamType == 0) {
            val userId = Random().nextInt(10000).toString()
            livePusher = V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC)
            URLUtils.generatePushUrl(streamId, userId, 0)
        } else {
            livePusher = V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTMP)
            URLUtils.generatePushUrl(streamId, "", 1)
        }

        Log.d(TAG, "pushUrl: $pushUrl")

        livePusher?.let { pusher ->
            pusher.setAudioQuality(audioQuality)
            pusher.setRenderView(pushRenderView)
            pusher.startCamera(true)
            val ret = pusher.startPush(pushUrl)
            pusher.startMicrophone()
            Log.i(TAG, "startPush return: $ret")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        livePusher?.let { pusher ->
            pusher.stopCamera()
            if (pusher.isPushing == 1) {
                pusher.stopPush()
            }
            livePusher = null
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> finish()
            R.id.btn_mic -> {
                micFlag = !micFlag
                enableMic(micFlag)
            }
            R.id.ll_resolution -> showResolutionMenu()
            R.id.ll_rotate -> showRotateMenu()
            R.id.ll_mirror -> showMirrorMenu()
        }
    }

    private fun showResolutionMenu() {
        val popupMenu = PopupMenu(this, linearResolution, Gravity.TOP)
        popupMenu.menuInflater.inflate(R.menu.livepushcamera_resolution, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            if (livePusher != null && livePusher?.isPushing == 1) {
                when (item.itemId) {
                    R.id.resolution_360 -> {
                        resolutionFlag = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x360
                        textResolution.text = "360P"
                    }
                    R.id.resolution_540 -> {
                        resolutionFlag = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540
                        textResolution.text = "540P"
                    }
                    R.id.resolution_720 -> {
                        resolutionFlag = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1280x720
                        textResolution.text = "720P"
                    }
                    R.id.resolution_1080 -> {
                        resolutionFlag = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1920x1080
                        textResolution.text = "1080P"
                    }
                }

                val param = V2TXLiveDef.V2TXLiveVideoEncoderParam(resolutionFlag).apply {
                    videoResolutionMode = V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModePortrait
                }
                livePusher?.setVideoQuality(param)
            } else {
                Toast.makeText(
                    this@LivePushCameraActivity,
                    getString(R.string.livepushcamera_please_ensure_pushing),
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }
        popupMenu.show()
    }

    private fun showMirrorMenu() {
        val popupMenu = PopupMenu(this, linearMirror, Gravity.TOP)
        popupMenu.menuInflater.inflate(R.menu.livepushcamera_mirror, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            if (livePusher != null && livePusher?.isPushing == 1) {
                when (item.itemId) {
                    R.id.mirror_auto -> {
                        mirrorFlag = V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeAuto
                        textMirror.setText(R.string.livepushcamera_front_camera_open)
                    }
                    R.id.mirror_enable -> {
                        mirrorFlag = V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeEnable
                        textMirror.setText(R.string.livepushcamera_camera_all_open)
                    }
                    R.id.mirror_disable -> {
                        mirrorFlag = V2TXLiveDef.V2TXLiveMirrorType.V2TXLiveMirrorTypeDisable
                        textMirror.setText(R.string.livepushcamera_camera_all_close)
                    }
                }
                livePusher?.setRenderMirror(mirrorFlag)
            } else {
                Toast.makeText(
                    this@LivePushCameraActivity,
                    getString(R.string.livepushcamera_please_ensure_pushing),
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }
        popupMenu.show()
    }

    private fun showRotateMenu() {
        val popupMenu = PopupMenu(this, linearRotate, Gravity.TOP)
        popupMenu.menuInflater.inflate(R.menu.livepushcamera_rotate, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            if (livePusher != null && livePusher?.isPushing == 1) {
                when (item.itemId) {
                    R.id.rotate_0 -> {
                        rotationFlag = V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation0
                        textRotate.text = "0"
                    }
                    R.id.rotate_90 -> {
                        rotationFlag = V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation90
                        textRotate.text = "90"
                    }
                    R.id.rotate_180 -> {
                        rotationFlag = V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation180
                        textRotate.text = "180"
                    }
                    R.id.rotate_270 -> {
                        rotationFlag = V2TXLiveDef.V2TXLiveRotation.V2TXLiveRotation270
                        textRotate.text = "270"
                    }
                }
                livePusher?.setRenderRotation(rotationFlag)
            } else {
                Toast.makeText(
                    this@LivePushCameraActivity,
                    getString(R.string.livepushcamera_please_ensure_pushing),
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }
        popupMenu.show()
    }

    private fun enableMic(micFlag: Boolean) {
        if (livePusher != null && livePusher?.isPushing == 1) {
            if (micFlag) {
                livePusher?.startMicrophone()
                buttonMic.setText(R.string.livepushcamera_close_mic)
            } else {
                livePusher?.stopMicrophone()
                buttonMic.setText(R.string.livepushcamera_open_mic)
            }
        } else {
            Toast.makeText(
                this,
                getString(R.string.livepushcamera_please_ensure_pushing),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}