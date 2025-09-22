package com.tencent.mlvb.switchrenderview;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.live2.impl.V2TXLivePusherImpl;
import com.tencent.mlvb.common.MLVBBaseActivity;
import com.tencent.mlvb.common.URLUtils;
import com.tencent.rtmp.ui.TXCloudVideoView;
import java.util.Random;

/**
 * Example for Dynamically Switching Rendering Controls
 *
 * This document shows how to use different types of views to render a video.
 * Views supported include:
 * TextureView, SurfaceView, TXCloudVideoView
 *
 * - For more information, please see the API document {
 * https://liteav.sdk.qcloud.com/doc/api/zh-cn/group__V2TXLivePusher__android.html#afc848d88fe99790b8c0988b8525dd4d9}.
 */
public class SwitchRenderViewActivity extends MLVBBaseActivity {

    private static final String TAG = SwitchRenderViewActivity.class.getSimpleName();

    private TXCloudVideoView mPushTencentView;
    private TextureView mPushTextureView;
    private SurfaceView mPushSurfaceView;
    private EditText mEditStreamId;
    private V2TXLivePusher mLivePusher;
    private RadioGroup mRadioView;
    private Button mButtonPush;
    private TextView mTextTitle;

    private int mLastRadioButton = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.switchrenderview_activity_switch_render_view);
        if (checkPermission()) {
            initView();
        }
    }

    @Override
    public void onPermissionGranted() {
        initView();
    }

    private void initView() {
        mPushTencentView = findViewById(R.id.tx_cloud_view);
        mPushTextureView = findViewById(R.id.tuv_texture);
        mPushSurfaceView = findViewById(R.id.sv_surface);
        mRadioView = findViewById(R.id.rg_view);
        mEditStreamId = findViewById(R.id.et_stream_id);
        mButtonPush = findViewById(R.id.btn_push);
        mTextTitle = findViewById(R.id.tv_title);

        mEditStreamId.setText(generateStreamId());

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRadioView.check(R.id.rb_txcloudvideoview);
        mLastRadioButton = R.id.rb_txcloudvideoview;

        mButtonPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLivePusher != null && mLivePusher.isPushing() == 1) {
                    stopPush();
                    mButtonPush.setText(R.string.switchrenderview_start_push);
                } else {
                    startPush();
                    mButtonPush.setText(R.string.switchrenderview_stop_push);
                }
            }
        });

        mRadioView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_txcloudvideoview) {
                    if (mLivePusher != null && mLivePusher.isPushing() == 1) {
                        Toast.makeText(
                            SwitchRenderViewActivity.this,
                            getString(R.string.switchrenderview_please_restart_push),
                            Toast.LENGTH_SHORT
                        ).show();
                        mRadioView.check(mLastRadioButton);
                        return;
                    }
                    mPushSurfaceView.setVisibility(View.GONE);
                    mPushTextureView.setVisibility(View.GONE);
                    mPushTencentView.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.rb_textureview) {
                    if (mLivePusher != null && mLivePusher.isPushing() == 1) {
                        Toast.makeText(
                            SwitchRenderViewActivity.this,
                            getString(R.string.switchrenderview_please_restart_push),
                            Toast.LENGTH_SHORT
                        ).show();
                        mRadioView.check(mLastRadioButton);
                        return;
                    }
                    mPushSurfaceView.setVisibility(View.GONE);
                    mPushTencentView.setVisibility(View.GONE);
                    mPushTextureView.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.rb_surfaceview) {
                    if (mLivePusher != null && mLivePusher.isPushing() == 1) {
                        Toast.makeText(
                            SwitchRenderViewActivity.this,
                            getString(R.string.switchrenderview_please_restart_push),
                            Toast.LENGTH_SHORT
                        ).show();
                        mRadioView.check(mLastRadioButton);
                        return;
                    }
                    mPushTextureView.setVisibility(View.GONE);
                    mPushTencentView.setVisibility(View.GONE);
                    mPushSurfaceView.setVisibility(View.VISIBLE);
                }
            }
        });

        if (!TextUtils.isEmpty(mEditStreamId.getText().toString())) {
            mTextTitle.setText(mEditStreamId.getText().toString());
        }
    }

    private void stopPush() {
        if (mLivePusher != null) {
            mLivePusher.stopCamera();
            mLivePusher.stopMicrophone();
            if (mLivePusher.isPushing() == 1) {
                mLivePusher.stopPush();
            }
        }
        mLivePusher = null;
    }

    private void startPush() {
        String streamId = mEditStreamId.getText().toString();
        if (TextUtils.isEmpty(streamId)) {
            Toast.makeText(
                SwitchRenderViewActivity.this,
                getString(R.string.switchrenderview_please_input_streamid),
                Toast.LENGTH_SHORT
            ).show();
            return;
        }

        mTextTitle.setText(streamId);

        if (mLivePusher == null) {
            mLivePusher = new V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTMP);
        }

        int checkedId = mRadioView.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_txcloudvideoview) {
            mLivePusher.setRenderView(mPushTencentView);
            mPushSurfaceView.setVisibility(View.GONE);
            mPushTextureView.setVisibility(View.GONE);
            mPushTencentView.setVisibility(View.VISIBLE);
            mLastRadioButton = R.id.rb_txcloudvideoview;
        } else if (checkedId == R.id.rb_textureview) {
            mLivePusher.setRenderView(mPushTextureView);
            mPushSurfaceView.setVisibility(View.GONE);
            mPushTextureView.setVisibility(View.VISIBLE);
            mPushTencentView.setVisibility(View.GONE);
            mLastRadioButton = R.id.rb_textureview;
        } else if (checkedId == R.id.rb_surfaceview) {
            mLivePusher.setRenderView(mPushSurfaceView);
            mPushSurfaceView.setVisibility(View.VISIBLE);
            mPushTextureView.setVisibility(View.GONE);
            mPushTencentView.setVisibility(View.GONE);
            mLastRadioButton = R.id.rb_surfaceview;
        }

        mLivePusher.startCamera(true);
        String userId = String.valueOf(new Random().nextInt(10000));
        String pushUrl = URLUtils.generatePushUrl(streamId, userId, 1);
        int ret = mLivePusher.startPush(pushUrl);
        Log.i(TAG, "startPush return: " + ret);
        mLivePusher.startMicrophone();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPush();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}