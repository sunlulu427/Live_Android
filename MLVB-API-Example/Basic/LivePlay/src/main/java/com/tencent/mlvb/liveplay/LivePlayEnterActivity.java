package com.tencent.mlvb.liveplay;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.tencent.mlvb.common.MLVBBaseActivity;

/**
 * Playback Entrance View
 * You can play streams over RTMP, FLV, HLS or RTC.
 * - For the playback view, see {@link LivePlayActivity}.
 * RTC Play Currently only supported in China, other regions are continuing to develop.
 */
public class LivePlayEnterActivity extends MLVBBaseActivity {

    private EditText mEditStreamId;
    private TextView mTextDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.liveplay_activity_live_play_enter);
        initView();
    }

    private void initView() {
        mEditStreamId = findViewById(R.id.et_stream_id);
        mEditStreamId.setText(generateStreamId());

        // Set up click listeners for different play types
        findViewById(R.id.btn_play_rtmp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlay(0);
            }
        });

        findViewById(R.id.btn_play_flv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlay(1);
            }
        });

        findViewById(R.id.btn_play_hls).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlay(2);
            }
        });

        findViewById(R.id.btn_play_rtc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlay(3);
            }
        });

        mTextDesc = findViewById(R.id.tv_desc);
        setupDescriptionText();
    }

    private void setupDescriptionText() {
        String text = mTextDesc.getText().toString();
        SpannableString spannableString = new SpannableString(text);

        int urlStart = text.indexOf("https://");
        int urlEnd = text.indexOf("56598") + 5;

        if (urlStart != -1 && urlEnd > urlStart) {
            spannableString.setSpan(
                new URLSpan("https://cloud.tencent.com/document/product/454/56598"),
                urlStart,
                urlEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        mTextDesc.setMovementMethod(LinkMovementMethod.getInstance());
        mTextDesc.setText(spannableString);
    }

    private void startPlay(int type) {
        String streamId = mEditStreamId.getText().toString();
        if (streamId.trim().isEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.liveplay_please_input_streamid),
                Toast.LENGTH_SHORT
            ).show();
        } else {
            Intent intent = new Intent(this, LivePlayActivity.class);
            intent.putExtra("STREAM_ID", streamId);
            intent.putExtra("STREAM_TYPE", type);
            startActivity(intent);
        }
    }

    @Override
    public void onPermissionGranted() {
        // No specific action needed when permission is granted
    }
}