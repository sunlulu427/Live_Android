package com.tencent.mlvb.newtimeshiftspriite;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.tencent.mlvb.common.MLVBBaseActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewTimeShiftSpriteActivity extends MLVBBaseActivity implements View.OnClickListener, TXSpriteImageFetcher.TXSpriteImageFetcherCallback {

    private static final String TAG = NewTimeShiftSpriteActivity.class.getSimpleName();
    public static final String NEW_TIME_SHIFT_DOMAIN = "5000.liveplay.myqcloud.com";
    public static final String NEW_TIME_SHIFT_PATH = "live";
    public static final String NEW_TIME_SHIFT_STREAMID = "5000_testsprite";

    private EditText mEditDomain;
    private EditText mEditPath;
    private EditText mEditStream;
    private EditText mEditStartH;
    private EditText mEditStartM;
    private EditText mEditStartS;
    private EditText mEditEndH;
    private EditText mEditEndM;
    private EditText mEditEndS;
    private EditText mEditOffsetH;
    private EditText mEditOffsetM;
    private EditText mEditOffsetS;
    private ImageView mImageViewThumb;

    private TXSpriteImageFetcher mSpriteImageFetcher;

    @Override
    public void onPermissionGranted() {
        initView();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_time_shift_sprite);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpriteImageFetcher != null) {
            mSpriteImageFetcher.clear();
            mSpriteImageFetcher = null;
        }
    }

    private void initView() {
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.new_timeshift_show).setOnClickListener(this);

        mEditDomain = findViewById(R.id.et_domain);
        mEditPath = findViewById(R.id.et_path);
        mEditStream = findViewById(R.id.et_stream);
        mEditStartH = findViewById(R.id.et_start_h);
        mEditStartM = findViewById(R.id.et_start_m);
        mEditStartS = findViewById(R.id.et_start_s);
        mEditEndH = findViewById(R.id.et_end_h);
        mEditEndM = findViewById(R.id.et_end_m);
        mEditEndS = findViewById(R.id.et_end_s);
        mEditOffsetH = findViewById(R.id.et_offset_h);
        mEditOffsetM = findViewById(R.id.et_offset_m);
        mEditOffsetS = findViewById(R.id.et_offset_s);
        mImageViewThumb = findViewById(R.id.iv_thumb);

        mEditDomain.setText(NEW_TIME_SHIFT_DOMAIN);
        mEditPath.setText(NEW_TIME_SHIFT_PATH);
        mEditStream.setText(NEW_TIME_SHIFT_STREAMID);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        String[] parts = currentTime.split(":");
        mEditStartH.setText(parts[0]);
        mEditStartM.setText(parts[1]);
        mEditStartS.setText(parts[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        String anHourLaterTime = sdf.format(calendar.getTime());
        String[] endParts = anHourLaterTime.split(":");
        mEditEndH.setText(endParts[0]);
        mEditEndM.setText(endParts[1]);
        mEditEndS.setText(endParts[2]);

        mEditOffsetH.setText("00");
        mEditOffsetM.setText("00");
        mEditOffsetS.setText("10");
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.new_timeshift_show) {
            showThumb();
        }
    }

    private void showThumb() {
        mImageViewThumb.setImageBitmap(null);

        if (mSpriteImageFetcher == null) {
            mSpriteImageFetcher = new TXSpriteImageFetcher(getApplicationContext());
        }

        long startTime = 0L;
        long endTime = 0L;
        String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String strStartTime = dateString + " " + mEditStartH.getText() + ":" + mEditStartM.getText() + ":" + mEditStartS.getText();
        String strEndTime = dateString + " " + mEditEndH.getText() + ":" + mEditEndM.getText() + ":" + mEditEndS.getText();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date startDate = sdf.parse(strStartTime);
            startTime = (startDate != null ? startDate.getTime() : 0) / 1000;

            Date endDate = sdf.parse(strEndTime);
            endTime = (endDate != null ? endDate.getTime() : 0) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (mSpriteImageFetcher != null) {
            mSpriteImageFetcher.init(
                mEditDomain.getText().toString(),
                mEditPath.getText().toString(),
                mEditStream.getText().toString(),
                startTime,
                endTime
            );
            mSpriteImageFetcher.setCallback(this);

            long time = Long.parseLong(mEditOffsetH.getText().toString()) * 3600 +
                    Long.parseLong(mEditOffsetM.getText().toString()) * 60 +
                    Long.parseLong(mEditOffsetS.getText().toString());

            mSpriteImageFetcher.getThumbnail(time);
        }
    }

    @Override
    public void onFetchDone(int errCode, Bitmap image) {
        String msg = "onFetchDone errCode is " + errCode;
        Log.i(TAG, msg);

        if (errCode != TXSpriteImageFetcher.SPRITE_THUMBNAIL_FETCH_SUCC) {
            mImageViewThumb.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        }

        mImageViewThumb.post(new Runnable() {
            @Override
            public void run() {
                mImageViewThumb.setImageBitmap(image);
            }
        });
    }
}