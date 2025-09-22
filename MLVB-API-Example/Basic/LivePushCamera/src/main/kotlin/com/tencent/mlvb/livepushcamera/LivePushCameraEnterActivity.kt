package com.tencent.mlvb.livepushcamera

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.tencent.live2.V2TXLiveDef
import com.tencent.mlvb.common.MLVBBaseActivity

/**
 * Publishing (Camera) Entrance View
 * You can publish via RTC (recommended) or RTMP.
 * - For the publishing view, see [LivePushCameraActivity].
 */
class LivePushCameraEnterActivity : MLVBBaseActivity() {

    private lateinit var mEditStreamId: EditText
    private lateinit var mRadioAudiQuality: RadioGroup
    private lateinit var mTextDesc: TextView
    private var mAudioQuality = V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.livepushcamera_activity_push_camera_enter)
        initView()
    }

    private fun initView() {
        mEditStreamId = findViewById(R.id.et_stream_id)
        mRadioAudiQuality = findViewById(R.id.rg_audio_quality)

        mEditStreamId.setText(generateStreamId())

        mRadioAudiQuality.setOnCheckedChangeListener { _, checkedId ->
            mAudioQuality = when (checkedId) {
                0 -> V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault
                1 -> V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualitySpeech
                else -> V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityMusic
            }
        }
        mRadioAudiQuality.check(R.id.rb_default)

        findViewById<View>(R.id.btn_push_rtc).setOnClickListener {
            startPushCamera(0)
        }

        findViewById<View>(R.id.btn_push_rtmp).setOnClickListener {
            startPushCamera(1)
        }

        mTextDesc = findViewById(R.id.tv_rtc_desc)
        setupDescriptionText()
    }

    private fun setupDescriptionText() {
        val text = mTextDesc.text.toString()
        val spannableString = SpannableString(text)

        val urlStart = text.indexOf("https://")
        val urlEnd = text.indexOf("56592") + 5

        if (urlStart != -1 && urlEnd > urlStart) {
            spannableString.setSpan(
                URLSpan("https://cloud.tencent.com/document/product/454/56592"),
                urlStart,
                urlEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        mTextDesc.movementMethod = LinkMovementMethod.getInstance()
        mTextDesc.text = spannableString
    }

    private fun startPushCamera(type: Int) {
        val streamId = mEditStreamId.text.toString()
        if (streamId.trim().isEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.livepushcamera_please_input_streamid),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val intent = Intent(this, LivePushCameraActivity::class.java).apply {
                putExtra("STREAM_ID", streamId)
                putExtra("STREAM_TYPE", type)
                putExtra("AUDIO_QUALITY", mAudioQuality)
            }
            startActivity(intent)
        }
    }

    override fun onPermissionGranted() {
        // No specific action needed when permission is granted
    }
}