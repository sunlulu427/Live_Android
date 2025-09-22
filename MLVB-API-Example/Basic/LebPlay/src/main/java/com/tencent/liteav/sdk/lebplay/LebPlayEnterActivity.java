package com.tencent.liteav.sdk.lebplay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.tencent.mlvb.common.MLVBBaseActivity;

/**
 * WebRTC Playback Entrance View
 * - For the playback view, see {@link LebPlayActivity}.
 */
public class LebPlayEnterActivity extends MLVBBaseActivity {

    private EditText mEditStreamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lebplay_activity_leb_play_enter);
        initView();
    }

    private void initView() {
        mEditStreamId = findViewById(R.id.et_stream_id);

        mEditStreamId.setText(generateStreamId());
        Button btnPlayWebrtc = findViewById(R.id.btn_play_webrtc);
        btnPlayWebrtc.setOnClickListener(v -> startPlay());
    }

    private void startPlay() {
        String streamId = mEditStreamId.getText().toString();
        if (streamId.trim().isEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.lebplay_please_input_streamid),
                Toast.LENGTH_SHORT
            ).show();
        } else {
            Intent intent = new Intent(this, LebPlayActivity.class);
            intent.putExtra("STREAM_ID", streamId);
            startActivity(intent);
        }
    }

    @Override
    public void onPermissionGranted() {
        // No specific action needed when permission is granted
    }
}