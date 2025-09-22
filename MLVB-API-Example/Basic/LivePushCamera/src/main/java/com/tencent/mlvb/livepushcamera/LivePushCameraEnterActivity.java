package com.tencent.mlvb.livepushcamera;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.mlvb.common.MLVBBaseActivity;

/**
 * Publishing (Camera) Entrance View
 * You can publish via RTC (recommended) or RTMP.
 * - For the publishing view, see {@link LivePushCameraActivity}.
 */
public class LivePushCameraEnterActivity extends MLVBBaseActivity {

    private EditText mEditStreamId;
    private RadioGroup mRadioAudiQuality;
    private TextView mTextDesc;

    private V2TXLiveDef.V2TXLiveAudioQuality mAudioQuality = V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.livepushcamera_activity_push_camera_enter);
        initView();
    }

    private void initView() {
        mEditStreamId = findViewById(R.id.et_stream_id);
        mRadioAudiQuality = findViewById(R.id.rg_audio_quality);

        mEditStreamId.setText(generateStreamId());

        mRadioAudiQuality.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == 0) {
                    mAudioQuality = V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault;
                } else if (checkedId == 1) {
                    mAudioQuality = V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualitySpeech;
                } else {
                    mAudioQuality = V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityMusic;
                }
            }
        });
        mRadioAudiQuality.check(R.id.rb_default);

        findViewById(R.id.btn_push_rtc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPushCamera(0);
            }
        });

        findViewById(R.id.btn_push_rtmp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPushCamera(1);
            }
        });

        mTextDesc = findViewById(R.id.tv_rtc_desc);
        setupDescriptionText();
    }

    private void setupDescriptionText() {
        String text = mTextDesc.getText().toString();
        SpannableString spannableString = new SpannableString(text);

        int urlStart = text.indexOf("https://");
        int urlEnd = text.indexOf("56592") + 5;

        if (urlStart != -1 && urlEnd > urlStart) {
            spannableString.setSpan(
                new URLSpan("https://cloud.tencent.com/document/product/454/56592"),
                urlStart,
                urlEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        mTextDesc.setMovementMethod(LinkMovementMethod.getInstance());
        mTextDesc.setText(spannableString);
    }

    private void startPushCamera(int type) {
        String streamId = mEditStreamId.getText().toString();
        if (streamId.trim().isEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.livepushcamera_please_input_streamid),
                Toast.LENGTH_SHORT
            ).show();
        } else {
            Intent intent = new Intent(this, LivePushCameraActivity.class);
            intent.putExtra("STREAM_ID", streamId);
            intent.putExtra("STREAM_TYPE", type);
            intent.putExtra("AUDIO_QUALITY", mAudioQuality);
            startActivity(intent);
        }
    }

    @Override
    public void onPermissionGranted() {
        // No specific action needed when permission is granted
    }
}