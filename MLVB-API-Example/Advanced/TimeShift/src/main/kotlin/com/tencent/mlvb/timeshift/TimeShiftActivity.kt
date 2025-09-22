package com.tencent.mlvb.timeshift

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.tencent.live2.V2TXLiveDef
import com.tencent.live2.V2TXLivePlayer
import com.tencent.live2.V2TXLivePlayerObserver
import com.tencent.live2.impl.V2TXLivePlayerImpl
import com.tencent.mlvb.common.MLVBBaseActivity
import com.tencent.rtmp.ui.TXCloudVideoView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

/*
 time shift function
 MLVB APP time-shift code example:
 This document shows how to implement the time shift function through the mobile live broadcast SDK
 1. First, understand the basic concepts and uses of time shifting on the official website, and enable the time shifting function. https://cloud.tencent.com/document/product/267/32742
 2. Splice the time-shifted playback link according to the document rules. String timeShiftUrl = "http://[Domain]/timeshift/[AppName]/[StreamName]/timeshift.m3u8?delay=90". (delay, default minimum 90 seconds)
 3. Stop the currently playing live stream API: mLivePlayer.stopPlay();
 4. Start playing time-shift streaming API: mLivePlayer.startLivePlay(timeShiftUrl);

 Resume live stream
 1. Stop the currently playing time-shifted stream API: mLivePlayer.stopPlay();
 2. Start playing live streaming API: mLivePlayer.startLivePlay(liveUrl);

 */

class TimeShiftActivity : MLVBBaseActivity() {

    companion object {
        private val TAG = TimeShiftActivity::class.java.simpleName
        /// Time shift function demonstration, sample streaming address。
        private const val DEFAULT_PLAY_URL = "http://liteavapp.qcloud.com/live/liteavdemoplayerstreamid.flv"
        private const val DEFAULT_TIME_SHIFT_DOMAIN = "liteavapp.timeshift.qcloud.com"

        // The time shift interval is configurable https://cloud.tencent.com/document/product/267/32742
        private const val kMaxFallbackSeconds = 600
        private const val kMinFallbackSeconds = 90
    }

    private lateinit var mVideoView: TXCloudVideoView
    private lateinit var mDateView: TextView
    private lateinit var mSeekBar: SeekBar
    private var mTimer: Timer? = null
    private lateinit var mTimeShiftHelper: TimeShiftHelper
    private var mLivePlayer: V2TXLivePlayer? = null
    private var mPlayUrl = DEFAULT_PLAY_URL
    private val mPlayUrlList = mutableListOf<V2TXLiveDef.V2TXLiveStreamInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_shift)
        mTimeShiftHelper = TimeShiftHelper(DEFAULT_TIME_SHIFT_DOMAIN)
        if (checkPermission()) {
            initView()
            startPlay()
            startTimer()
        }
    }

    override fun onPermissionGranted() {
        initView()
    }

    private fun initView() {
        mVideoView = findViewById(R.id.tx_cloud_view)
        findViewById<View>(R.id.timeshift_resume_live).setOnClickListener {
            resumeLive()
        }

        mDateView = findViewById(R.id.timeshift_date)
        mSeekBar = findViewById(R.id.timeshift_seekbar)
        mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // No action needed
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // No action needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    if (it.progress > 99) {
                        resumeLive()
                    } else {
                        startTimeShift()
                    }
                    updateProgress()
                }
            }
        })
    }

    private fun startTimer() {
        mTimer = Timer(true).apply {
            val task = object : TimerTask() {
                override fun run() {
                    updateProgress()
                }
            }
            schedule(task, 0, 500)
        }
    }

    private fun stopTimer() {
        mTimer?.cancel()
    }

    private fun getCurrentDelay(): Long {
        val progress = mSeekBar.progress / 100.0
        val delay = if (progress < 0.99) {
            kMinFallbackSeconds + (kMaxFallbackSeconds - kMinFallbackSeconds) * (1.0 - progress)
        } else {
            0.0
        }
        return round(delay).toLong()
    }

    private fun updateProgress() {
        val delay = getCurrentDelay()
        val nowms = System.currentTimeMillis()
        val simpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = Date(nowms - delay * 1000)
        runOnUiThread {
            mDateView.text = simpleDateFormat.format(date)
        }
    }

    private fun stopPlay() {
        mLivePlayer?.let { player ->
            if (player.isPlaying == 1) {
                player.stopPlay()
            }
        }
        mLivePlayer = null
    }

    private fun startPlay() {
        if (mLivePlayer == null) {
            mLivePlayer = V2TXLivePlayerImpl(this).apply {
                setObserver(object : V2TXLivePlayerObserver() {
                    override fun onError(player: V2TXLivePlayer?, code: Int, msg: String?, extraInfo: Bundle?) {
                        super.onError(player, code, msg, extraInfo)
                    }

                    override fun onWarning(player: V2TXLivePlayer?, code: Int, msg: String?, extraInfo: Bundle?) {
                        super.onWarning(player, code, msg, extraInfo)
                    }

                    override fun onConnected(player: V2TXLivePlayer?, extraInfo: Bundle?) {
                        Log.d(TAG, "extraInfo:${extraInfo.toString()}")
                        mPlayUrlList.clear()
                        mLivePlayer?.streamList?.let { arrayList ->
                            if (arrayList.isNotEmpty()) {
                                mPlayUrlList.addAll(arrayList)
                            }
                        }
                    }

                    override fun onVideoResolutionChanged(player: V2TXLivePlayer?, width: Int, height: Int) {
                        runOnUiThread {
                            Toast.makeText(
                                this@TimeShiftActivity,
                                "resolution:$width*$height",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            }
        }

        mLivePlayer?.let { player ->
            player.setProperty("clearLastImage", false)
            player.setRenderView(mVideoView)
            player.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFit)
            val ret = player.startLivePlay(mPlayUrl)
            Log.i(TAG, "startPlay return: $ret")
        }
    }

    private fun resumeLive() {
        mSeekBar.progress = 100
        mLivePlayer?.let { player ->
            player.stopPlay()
            player.startLivePlay(mPlayUrl)
        }
    }

    private fun startTimeShift() {
        val timeShiftUrl = mTimeShiftHelper.getTimeShiftUrl(DEFAULT_PLAY_URL, getCurrentDelay())
        mLivePlayer?.let { player ->
            player.stopPlay()
            player.startLivePlay(timeShiftUrl)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlay()
        stopTimer()
    }

    override fun onBackPressed() {
        finish()
    }
}