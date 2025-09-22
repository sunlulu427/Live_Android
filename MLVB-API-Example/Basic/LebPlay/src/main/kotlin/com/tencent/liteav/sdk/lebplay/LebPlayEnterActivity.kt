package com.tencent.liteav.sdk.lebplay

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.tencent.mlvb.common.MLVBBaseActivity

/**
 * WebRTC Playback Entrance View
 * - For the playback view, see [LebPlayActivity].
 */
class LebPlayEnterActivity : MLVBBaseActivity() {

    private lateinit var mEditStreamId: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lebplay_activity_leb_play_enter)
        initView()
    }

    private fun initView() {
        mEditStreamId = findViewById(R.id.et_stream_id)

        mEditStreamId.setText(generateStreamId())
        val btnPlayWebrtc = findViewById<Button>(R.id.btn_play_webrtc)
        btnPlayWebrtc.setOnClickListener { startPlay() }
    }

    private fun startPlay() {
        val streamId = mEditStreamId.text.toString()
        if (streamId.trim().isEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.lebplay_please_input_streamid),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val intent = Intent(this, LebPlayActivity::class.java).apply {
                putExtra("STREAM_ID", streamId)
            }
            startActivity(intent)
        }
    }

    override fun onPermissionGranted() {
        // No specific action needed when permission is granted
    }
}