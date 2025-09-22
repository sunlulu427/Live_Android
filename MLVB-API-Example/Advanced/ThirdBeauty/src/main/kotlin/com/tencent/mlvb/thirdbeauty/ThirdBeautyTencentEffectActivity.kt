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
import com.tencent.live2.V2TXLivePusher
import com.tencent.live2.V2TXLivePusherObserver
import com.tencent.live2.impl.V2TXLivePusherImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.mlvb.common.URLUtils
import com.tencent.rtmp.ui.TXCloudVideoView
import java.util.*

/**
 * MLVB Third-Party Beauty Filter View
 * Access steps：
 * First step：
 * Integrate Tencent Effect SDK and copy resources（You can refer to the access document provided by Tencent Effects：https://cloud.tencent.com/document/product/616/65888）
 * Second step：Authentication and initialization of Tencent Effect SDK,
 * see details[ThirdBeautyTencentEffectActivity.authXmagic],to obtain the license, please refer to {https://cloud.tencent.com/document/product/616/65878}
 * Third step：Using Tencent Effect in MLVB，see details[ThirdBeautyTencentEffectActivity.startPush]
 * You must call [V2TXLivePusher.enableCustomVideoProcess]
 * to enable custom video processing before you can receive this callback.
 * - Before stream publishing, call [V2TXLivePusher.enableCustomVideoProcess] to enable custom rendering.
 * - Call [V2TXLivePusher.setObserver] to listen for video data from the SDK.
 * - After data is received, use third-party beauty filters to process the data
 * in [V2TXLivePusherObserver.onProcessVideoFrame].
 * Note：The applicationId and License provided by Tencent Effects are in one-to-one correspondence.
 * During the test process, the applicationId needs to be modified to the applicationId corresponding to the License.
 **/
class ThirdBeautyTencentEffectActivity : MLVBBaseActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "ThirdBeautyFaceUnityActivity"
    }

    private lateinit var mPushRenderView: TXCloudVideoView
    private var mLivePusher: V2TXLivePusher? = null
    private lateinit var mSeekBlurLevel: SeekBar
    private lateinit var mTextBlurLevel: TextView
    private lateinit var mEditStreamId: EditText
    private lateinit var mButtonPush: Button
    private lateinit var mTextTitle: TextView

//    private var mXmagicApi: XmagicApi? = null
//    private var mProperty: XmagicProperty<XmagicProperty.XmagicPropertyValues>? = null
//    private val XMAGIC_LICENSE_URL = ""
//    private val XMAGIC_LICENSE_KEY = ""
//    private val XMAGIC_RES_PATH = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third_beauty_tencent_effect)
        supportActionBar?.hide()

        if (checkPermission()) {
            initView()
        }
//        authXmagic()
    }

//    private fun authXmagic() {
//        TELicenseCheck.getInstance().setTELicense(this,
//                XMAGIC_LICENSE_URL,
//                XMAGIC_LICENSE_KEY,
//                object : TELicenseCheck.TELicenseCheckListener {
//                    override fun onLicenseCheckFinish(errorCode: Int, msg: String?) {
//                        //Note: This callback is not necessarily in the calling thread
//                        if (errorCode == TELicenseCheck.ERROR_OK) {
//                            initXmagicApi()
//                        }
//                    }
//                })
//    }

//    private fun initXmagicApi() {
//        mXmagicApi = XmagicApi(this, XMAGIC_RES_PATH, object : XmagicApi.OnXmagicPropertyErrorListener {
//            override fun onXmagicPropertyError(s: String?, i: Int) {
//                // Handle error
//            }
//        })
//        mProperty = XmagicProperty(XmagicProperty.Category.BEAUTY, null, null,
//                XmagicConstant.BeautyConstant.BEAUTY_SMOOTH,
//                XmagicProperty.XmagicPropertyValues(0, 100, 50, 0, 1))
//    }

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
//                if (mLivePusher?.isPushing == 1 && fromUser && mProperty != null && mXmagicApi != null) {
//                    mProperty?.effValue?.setCurrentDisplayValue(progress * 10)
//                    mXmagicApi?.updateProperty(mProperty)
//                }
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

//        mLivePusher?.enableCustomVideoProcess(true, V2TXLivePixelFormatTexture2D, V2TXLiveBufferTypeTexture)
//        mLivePusher?.setObserver(object : V2TXLivePusherObserver() {
//            override fun onGLContextCreated() {
//                // Handle GL context creation
//            }
//
//            override fun onProcessVideoFrame(
//                srcFrame: V2TXLiveDef.V2TXLiveVideoFrame?,
//                dstFrame: V2TXLiveDef.V2TXLiveVideoFrame?
//            ): Int {
//                dstFrame?.texture?.textureId = mXmagicApi?.process(srcFrame?.texture?.textureId ?: 0,
//                        srcFrame?.width ?: 0, srcFrame?.height ?: 0) ?: 0
//                return 0
//            }
//
//            override fun onGLContextDestroyed() {
//                mXmagicApi?.onDestroy()
//            }
//        })

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