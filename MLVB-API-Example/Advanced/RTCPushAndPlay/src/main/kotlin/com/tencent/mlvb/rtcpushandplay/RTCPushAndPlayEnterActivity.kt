package com.tencent.mlvb.rtcpushandplay

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import com.tencent.mlvb.common.MLVBBaseActivity

/**
 * Entrance View for RTC Co-anchoring + Ultra-low-latency Playback
 *
 * - Enter as an anchor [RTCPushAndPlayAnchorActivity]
 * - Enter as audience [RTCPushAndPlayAudienceActivity]
 */
class RTCPushAndPlayEnterActivity : MLVBBaseActivity() {

    private lateinit var mEditStreamId: EditText
    private lateinit var mButtonCommit: Button
    private lateinit var mRadioRole: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rtcpushandplay_activity_rtc_push_and_play_enter)
        initView()
    }

    private fun initView() {
        mEditStreamId = findViewById(R.id.et_stream_id)
        mRadioRole = findViewById(R.id.rg_role)
        mButtonCommit = findViewById(R.id.btn_commit)

        mEditStreamId.setText(generateStreamId())

        mRadioRole.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_anchor -> mButtonCommit.setText(R.string.rtcpushandplay_rtc_push)
                R.id.rb_audience -> mButtonCommit.setText(R.string.rtcpushandplay_rtc_play)
            }
        }
        mRadioRole.check(R.id.rb_anchor)

        findViewById<View>(R.id.btn_commit).setOnClickListener {
            val streamId = mEditStreamId.text.toString()

            if (streamId.trim().isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.rtcpushandplay_please_input_streamid),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val intent = when (val checkedId = mRadioRole.checkedRadioButtonId) {
                R.id.rb_anchor -> Intent(this, RTCPushAndPlayAnchorActivity::class.java)
                R.id.rb_audience -> Intent(this, RTCPushAndPlayAudienceActivity::class.java)
                else -> return@setOnClickListener
            }

            intent.putExtra("STREAM_ID", streamId)
            startActivity(intent)
        }
    }

    override fun onPermissionGranted() {
        // No specific action needed when permission is granted
    }
}