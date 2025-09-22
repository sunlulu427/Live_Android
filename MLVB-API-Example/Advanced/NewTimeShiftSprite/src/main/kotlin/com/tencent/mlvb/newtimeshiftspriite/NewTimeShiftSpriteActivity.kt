package com.tencent.mlvb.newtimeshiftspriite

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.tencent.mlvb.common.MLVBBaseActivity
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class NewTimeShiftSpriteActivity : MLVBBaseActivity(), View.OnClickListener, TXSpriteImageFetcher.TXSpriteImageFetcherCallback {

    companion object {
        private val TAG = NewTimeShiftSpriteActivity::class.java.simpleName
        const val NEW_TIME_SHIFT_DOMAIN = "5000.liveplay.myqcloud.com"
        const val NEW_TIME_SHIFT_PATH = "live"
        const val NEW_TIME_SHIFT_STREAMID = "5000_testsprite"
    }

    private lateinit var mEditDomain: EditText
    private lateinit var mEditPath: EditText
    private lateinit var mEditStream: EditText
    private lateinit var mEditStartH: EditText
    private lateinit var mEditStartM: EditText
    private lateinit var mEditStartS: EditText
    private lateinit var mEditEndH: EditText
    private lateinit var mEditEndM: EditText
    private lateinit var mEditEndS: EditText
    private lateinit var mEditOffsetH: EditText
    private lateinit var mEditOffsetM: EditText
    private lateinit var mEditOffsetS: EditText
    private lateinit var mImageViewThumb: ImageView

    private var mSpriteImageFetcher: TXSpriteImageFetcher? = null

    override fun onPermissionGranted() {
        initView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_time_shift_sprite)
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSpriteImageFetcher?.apply {
            clear()
        }
        mSpriteImageFetcher = null
    }

    private fun initView() {
        findViewById<View>(R.id.iv_back).setOnClickListener(this)
        findViewById<View>(R.id.new_timeshift_show).setOnClickListener(this)

        mEditDomain = findViewById(R.id.et_domain)
        mEditPath = findViewById(R.id.et_path)
        mEditStream = findViewById(R.id.et_stream)
        mEditStartH = findViewById(R.id.et_start_h)
        mEditStartM = findViewById(R.id.et_start_m)
        mEditStartS = findViewById(R.id.et_start_s)
        mEditEndH = findViewById(R.id.et_end_h)
        mEditEndM = findViewById(R.id.et_end_m)
        mEditEndS = findViewById(R.id.et_end_s)
        mEditOffsetH = findViewById(R.id.et_offset_h)
        mEditOffsetM = findViewById(R.id.et_offset_m)
        mEditOffsetS = findViewById(R.id.et_offset_s)
        mImageViewThumb = findViewById(R.id.iv_thumb)

        mEditDomain.setText(NEW_TIME_SHIFT_DOMAIN)
        mEditPath.setText(NEW_TIME_SHIFT_PATH)
        mEditStream.setText(NEW_TIME_SHIFT_STREAMID)

        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentTime = sdf.format(Date())
        val parts = currentTime.split(":")
        mEditStartH.setText(parts[0])
        mEditStartM.setText(parts[1])
        mEditStartS.setText(parts[2])

        val calendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
        }
        val anHourLaterTime = sdf.format(calendar.time)
        val endParts = anHourLaterTime.split(":")
        mEditEndH.setText(endParts[0])
        mEditEndM.setText(endParts[1])
        mEditEndS.setText(endParts[2])

        mEditOffsetH.setText("00")
        mEditOffsetM.setText("00")
        mEditOffsetS.setText("10")
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> finish()
            R.id.new_timeshift_show -> showThumb()
        }
    }

    private fun showThumb() {
        mImageViewThumb.setImageBitmap(null)

        if (mSpriteImageFetcher == null) {
            mSpriteImageFetcher = TXSpriteImageFetcher(applicationContext)
        }

        var startTime = 0L
        var endTime = 0L
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val strStartTime = "$dateString ${mEditStartH.text}:${mEditStartM.text}:${mEditStartS.text}"
        val strEndTime = "$dateString ${mEditEndH.text}:${mEditEndM.text}:${mEditEndS.text}"

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            val startDate = sdf.parse(strStartTime)
            startTime = (startDate?.time ?: 0) / 1000

            val endDate = sdf.parse(strEndTime)
            endTime = (endDate?.time ?: 0) / 1000
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        mSpriteImageFetcher?.let { fetcher ->
            fetcher.init(
                mEditDomain.text.toString(),
                mEditPath.text.toString(),
                mEditStream.text.toString(),
                startTime,
                endTime
            )
            fetcher.setCallback(this)

            val time = mEditOffsetH.text.toString().toLong() * 3600 +
                    mEditOffsetM.text.toString().toLong() * 60 +
                    mEditOffsetS.text.toString().toLong()

            fetcher.getThumbnail(time)
        }
    }

    override fun onFetchDone(errCode: Int, image: Bitmap?) {
        val msg = "onFetchDone errCode is $errCode"
        Log.i(TAG, msg)

        if (errCode != TXSpriteImageFetcher.SPRITE_THUMBNAIL_FETCH_SUCC) {
            mImageViewThumb.post {
                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
            }
        }

        mImageViewThumb.post {
            mImageViewThumb.setImageBitmap(image)
        }
    }
}