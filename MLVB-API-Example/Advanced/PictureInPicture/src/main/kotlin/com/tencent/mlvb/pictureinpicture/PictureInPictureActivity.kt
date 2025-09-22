package com.tencent.mlvb.pictureinpicture

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.tencent.live2.V2TXLivePlayer
import com.tencent.live2.impl.V2TXLivePlayerImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.rtmp.ui.TXCloudVideoView

class PictureInPictureActivity : MLVBBaseActivity() {

    companion object {
        private val TAG = PictureInPictureActivity::class.java.simpleName
        private const val PLAY_URL = "http://liteavapp.qcloud.com/live/liteavdemoplayerstreamid.flv"
    }

    private lateinit var mVideoView: TXCloudVideoView
    private var mLivePlayer: V2TXLivePlayer? = null
    private lateinit var mButtonBack: ImageView
    private lateinit var mButtonEnablePictureInPicture: Button
    private lateinit var mButtonPause: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_in_picture)
        if (checkPermission()) {
            initView()
            initPlayer()
            startPlay()
        }
    }

    override fun onPermissionGranted() {
        initView()
        initPlayer()
        startPlay()
    }

    private fun initView() {
        mVideoView = findViewById(R.id.video_view)
        mButtonBack = findViewById(R.id.iv_back)
        mButtonEnablePictureInPicture = findViewById(R.id.btn_enable_picture_in_picture)
        mButtonPause = findViewById(R.id.btn_first_pause)

        mButtonBack.setOnClickListener {
            finish()
        }

        mButtonEnablePictureInPicture.setOnClickListener {
            startPictureInPicture()
        }

        mButtonPause.setOnClickListener {
            mLivePlayer?.let { player ->
                if (player.isPlaying == 1) {
                    stopPlay()
                    mButtonPause.setText(R.string.resume)
                } else {
                    startPlay()
                    mButtonPause.setText(R.string.pause)
                }
            }
        }
    }

    private fun initPlayer() {
        mLivePlayer = V2TXLivePlayerImpl(this).apply {
            setRenderView(mVideoView)
        }
    }

    private fun startPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pictureInPictureBuilder = PictureInPictureParams.Builder()
            val aspectRatio = Rational(mVideoView.width, mVideoView.height)
            pictureInPictureBuilder.setAspectRatio(aspectRatio)
            enterPictureInPictureMode(pictureInPictureBuilder.build())
        } else {
            Toast.makeText(this, R.string.picture_in_picture_not_supported, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            mButtonEnablePictureInPicture.visibility = View.GONE
            mButtonPause.visibility = View.GONE
        } else {
            mButtonEnablePictureInPicture.visibility = View.VISIBLE
            mButtonPause.visibility = View.VISIBLE
        }
    }

    private fun stopPlay() {
        mLivePlayer?.stopPlay()
    }

    private fun startPlay() {
        mLivePlayer?.let { player ->
            val ret = player.startLivePlay(PLAY_URL)
            Log.i(TAG, "startPlay return: $ret")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlay()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}