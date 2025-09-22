package com.tencent.mlvb.customvideocapture;

import android.annotation.SuppressLint;
import android.opengl.EGLContext;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLiveDef.V2TXLiveBufferType;
import com.tencent.live2.V2TXLiveDef.V2TXLivePixelFormat;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.live2.impl.V2TXLivePusherImpl;
import com.tencent.mlvb.common.MLVBBaseActivity;
import com.tencent.mlvb.common.URLUtils;
import com.tencent.mlvb.customvideocapture.helper.CustomCameraCapture;
import com.tencent.mlvb.customvideocapture.helper.CustomFrameRender;
import com.tencent.rtmp.ui.TXCloudVideoView;
import java.util.Random;

/**
 * Example for Custom Video Capturing & Rendering
 * This document shows how to enable custom video capturing and rendering.
 * Custom capturing:
 * - Before stream publishing, call {@link V2TXLivePusher#enableCustomVideoCapture(boolean)} to enable custom capturing.
 * - Call {@link V2TXLivePusher#sendCustomVideoFrame(V2TXLiveDef.V2TXLiveVideoFrame)} to send data to the SDK.
 * Custom rendering
 * - Before stream publishing, call
 * {@link V2TXLivePusher#enableCustomVideoProcess(boolean, V2TXLiveDef.V2TXLivePixelFormat,
 * V2TXLiveDef.V2TXLiveBufferType)} to enable custom rendering.
 * - Call {@link V2TXLivePusher#setObserver(V2TXLivePusherObserver)} to listen for video data from the SDK.
 * - After data is received, execute the rendering logic in
 * {@link V2TXLivePusherObserver#onProcessVideoFrame(V2TXLiveDef.V2TXLiveVideoFrame, V2TXLiveDef.V2TXLiveVideoFrame)}.
 * - For more information, please see the API document {https://cloud.tencent.com/document/product/454/56601}.
 */
public class CustomVideoCaptureActivity extends MLVBBaseActivity implements View.OnClickListener {

    private static final String TAG = CustomVideoCaptureActivity.class.getSimpleName();

    private V2TXLivePusher mLivePusher;
    private EditText mEditStreamId;
    private Button mButtonPush;
    private CustomCameraCapture mCustomCameraCapture;
    private CustomFrameRender mCustomFrameRender;
    private TXCloudVideoView mPushRenderView;
    private TextView mTextTitle;

    private final CustomCameraCapture.VideoFrameReadListener mVideoFrameReadListener = new CustomCameraCapture.VideoFrameReadListener() {
        @SuppressLint("NewApi")
        @Override
        public void onFrameAvailable(EGLContext eglContext, int textureId, int width, int height) {
            V2TXLiveDef.V2TXLiveVideoFrame videoFrame = new V2TXLiveDef.V2TXLiveVideoFrame();
            videoFrame.pixelFormat = V2TXLivePixelFormat.V2TXLivePixelFormatTexture2D;
            videoFrame.bufferType = V2TXLiveBufferType.V2TXLiveBufferTypeTexture;

            V2TXLiveDef.V2TXLiveTexture texture = new V2TXLiveDef.V2TXLiveTexture();
            texture.textureId = textureId;
            texture.eglContext14 = eglContext;
            videoFrame.texture = texture;

            videoFrame.width = width;
            videoFrame.height = height;

            if (mLivePusher != null && mLivePusher.isPushing() == 1) {
                int ret = mLivePusher.sendCustomVideoFrame(videoFrame);
                Log.d(TAG, "sendCustomVideoFrame : " + ret);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customvideocaptureactivity_activity_custom_video_capture);
        if (checkPermission()) {
            initView();
        }
    }

    @Override
    public void onPermissionGranted() {
        initView();
    }

    private void initView() {
        mPushRenderView = findViewById(R.id.tx_cloud_view);
        mButtonPush = findViewById(R.id.btn_push);
        mEditStreamId = findViewById(R.id.et_stream_id);
        mTextTitle = findViewById(R.id.tv_title);

        mEditStreamId.setText(generateStreamId());
        findViewById(R.id.iv_back).setOnClickListener(this);
        mButtonPush.setOnClickListener(this);

        if (!TextUtils.isEmpty(mEditStreamId.getText().toString())) {
            mTextTitle.setText(mEditStreamId.getText().toString());
        }
    }

    private void startPush() {
        String streamId = mEditStreamId.getText().toString();
        if (TextUtils.isEmpty(streamId)) {
            Toast.makeText(
                CustomVideoCaptureActivity.this,
                getString(R.string.customvideocapture_please_input_streamid),
                Toast.LENGTH_SHORT
            ).show();
            return;
        }
        mTextTitle.setText(streamId);

        mCustomCameraCapture = new CustomCameraCapture();
        mCustomFrameRender = new CustomFrameRender();

        mLivePusher = new V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTMP);
        mLivePusher.setObserver(mCustomFrameRender);
        mLivePusher.enableCustomVideoCapture(true);

        String userId = String.valueOf(new Random().nextInt(10000));
        String pushUrl = URLUtils.generatePushUrl(streamId, userId, 1);
        int ret = mLivePusher.startPush(pushUrl);
        Log.i(TAG, "startPush return: " + ret);
        mLivePusher.startMicrophone();

        if (ret == 0) {
            mCustomCameraCapture.start(mVideoFrameReadListener);

            mLivePusher.enableCustomVideoProcess(true, V2TXLivePixelFormat.V2TXLivePixelFormatTexture2D, V2TXLiveBufferType.V2TXLiveBufferTypeTexture);
            TextureView textureView = new TextureView(this);
            mPushRenderView.addVideoView(textureView);
            mCustomFrameRender.start(textureView);
            mButtonPush.setText(R.string.customvideocapture_stop_push);
        }
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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_push) {
            push();
        } else if (id == R.id.iv_back) {
            finish();
        }
    }

    private void push() {
        if (mLivePusher != null && mLivePusher.isPushing() == 1) {
            stopPush();
        } else {
            startPush();
        }
    }

    private void stopPush() {
        if (mCustomCameraCapture != null) {
            mCustomCameraCapture.stop();
        }
        if (mCustomFrameRender != null) {
            mCustomFrameRender.stop();
        }

        if (mLivePusher != null) {
            mLivePusher.stopMicrophone();
            if (mLivePusher.isPushing() == 1) {
                mLivePusher.stopPush();
            }
        }
        mLivePusher = null;
        mButtonPush.setText(R.string.customvideocapture_start_push);
    }
}