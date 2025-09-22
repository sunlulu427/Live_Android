package com.tencent.mlvb.liveplay

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.tencent.mlvb.common.MLVBBaseActivity

/**
 * Playback Entrance View
 * You can play streams over RTMP, FLV, HLS or RTC.
 * - For the playback view, see [LivePlayActivity].
 * RTC Play Currently only supported in China, other regions are continuing to develop.
 */
class LivePlayEnterActivity : MLVBBaseActivity() {

    private lateinit var mEditStreamId: EditText
    private lateinit var mTextDesc: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.liveplay_activity_live_play_enter)
        initView()
    }

    private fun initView() {
        mEditStreamId = findViewById(R.id.et_stream_id)
        mEditStreamId.setText(generateStreamId())

        // Set up click listeners for different play types
        findViewById<View>(R.id.btn_play_rtmp).setOnClickListener {
            startPlay(0)
        }

        findViewById<View>(R.id.btn_play_flv).setOnClickListener {
            startPlay(1)
        }

        findViewById<View>(R.id.btn_play_hls).setOnClickListener {
            startPlay(2)
        }

        findViewById<View>(R.id.btn_play_rtc).setOnClickListener {
            startPlay(3)
        }

        mTextDesc = findViewById(R.id.tv_desc)
        setupDescriptionText()
    }

    private fun setupDescriptionText() {
        val text = mTextDesc.text.toString()
        val spannableString = SpannableString(text)

        val urlStart = text.indexOf("https://")
        val urlEnd = text.indexOf("56598") + 5

        if (urlStart != -1 && urlEnd > urlStart) {
            spannableString.setSpan(
                URLSpan("https://cloud.tencent.com/document/product/454/56598"),
                urlStart,
                urlEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        mTextDesc.movementMethod = LinkMovementMethod.getInstance()
        mTextDesc.text = spannableString
    }

    private fun startPlay(type: Int) {
        val streamId = mEditStreamId.text.toString()
        if (streamId.trim().isEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.liveplay_please_input_streamid),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val intent = Intent(this, LivePlayActivity::class.java).apply {
                putExtra("STREAM_ID", streamId)
                putExtra("STREAM_TYPE", type)
            }
            startActivity(intent)
        }
    }

    override fun onPermissionGranted() {
        // No specific action needed when permission is granted
    }
}