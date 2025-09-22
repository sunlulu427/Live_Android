package com.tencent.mlvb.newtimeshiftspriite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class TXSpriteImageFetcher {

    private static final String TAG = TXSpriteImageFetcher.class.getSimpleName();
    private static final String CONFIG_URL_FORMAT = "http://%s%s%d.jpg?txTimeshift=on";
    private static final String BIG_IMAGE_URL_FORMAT = "http://%s/%s/%s.json?txTimeshift=on&tsFormat=unix&tsSpritemode=1&tsStart=%d&tsEnd=%d";

    public static final int SPRITE_THUMBNAIL_FETCH_SUCC = 0;
    public static final int SPRITE_THUMBNAIL_FETCH_PARAM_INVALID = -1;
    public static final int SPRITE_THUMBNAIL_FETCH_NETWORK_ERR = -2;
    public static final int SPRITE_THUMBNAIL_FETCH_SERVER_ERROR = -3;

    public interface TXSpriteImageFetcherCallback {
        void onFetchDone(int errCode, Bitmap image);
    }

    private static class TXSpriteConfigData {
        public long mStartTime = 0;
        public long mEndTime = 0;
        public double mDuration = 0.0;
        public String mPath = "";
        public int mCols = 0;
        public int mRows = 0;
        public int mIntervalS = 0;
        public int mHeight = 0;
        public int mWidth = 0;

        public void initWithData(JSONObject data) {
            mStartTime = data.optLong("start_time", 0);
            mEndTime = data.optLong("end_time", 0);
            mDuration = data.optDouble("duration", 0.0);
            mPath = data.optString("path", "");
            mCols = data.optInt("cols", 0);
            mRows = data.optInt("rows", 0);
            mIntervalS = data.optInt("interval", 0);
            mHeight = data.optInt("height", 0);
            mWidth = data.optInt("width", 0);
        }

        public boolean isValid() {
            return !mPath.trim().isEmpty() && mStartTime > 0 && mEndTime > 0 &&
                   mCols > 0 && mRows > 0 && mIntervalS > 0 && mWidth > 0 && mHeight > 0;
        }
    }

    private final Context mContext;
    private final BitmapFactory.Options mBitmapOption = new BitmapFactory.Options();
    private String mDomain = "";
    private String mPath = "";
    private String mStreamId = "";
    private long mStartTs = 0;
    private long mEndTs = 0;

    private volatile boolean mIsFetchingSpriteConfig = false;
    private final List<String> mDownloadingImageUrls = new ArrayList<>();
    private final List<TXSpriteConfigData> mSpriteConfigDatas = new ArrayList<>();
    private final LruCache<String, BitmapRegionDecoder> mBigImgCache = new LruCache<>(30);
    private final LruCache<Long, Bitmap> mSmallImgCache = new LruCache<>(10);

    private long mFetchingTime = 0;
    private TXSpriteImageFetcherCallback mCallback = null;

    public TXSpriteImageFetcher(Context context) {
        this.mContext = context;
    }

    public void init(String domain, String path, String streamId, long startTs, long endTs) {
        this.mDomain = domain;
        this.mPath = path;
        this.mStreamId = streamId;
        this.mStartTs = startTs;
        this.mEndTs = endTs;
    }

    public synchronized void setCallback(TXSpriteImageFetcherCallback callback) {
        mCallback = callback;
    }

    public void getThumbnail(long time) {
        synchronized (this) {
            mFetchingTime = time;
        }

        Bitmap smallImage = getThumbnailFromSmallImageCache(time);
        if (smallImage != null) {
            notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_SUCC, smallImage);
            return;
        }

        Bitmap bigImage = getThumbnailFromBigImageCache(time);
        if (bigImage != null) {
            mSmallImgCache.put(time, bigImage);
            notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_SUCC, bigImage);
            return;
        }

        if (!isSpriteConfigDataExist(time)) {
            fetchSpriteConfig();
            return;
        }

        String bigImageUrl = getBigImageUrl(time);
        if (bigImageUrl.trim().isEmpty()) {
            notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_PARAM_INVALID, null);
        } else {
            fetchBigImage(bigImageUrl);
        }
    }

    public void setCacheSize(int size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBigImgCache.resize(size);
            mSmallImgCache.resize(size);
        }
    }

    public void clear() {
        mDomain = "";
        mPath = "";
        mStreamId = "";
        mStartTs = 0;
        mEndTs = 0;
        setIsFetchingSpriteConfig(false);
        mSpriteConfigDatas.clear();

        for (BitmapRegionDecoder decoder : mBigImgCache.snapshot().values()) {
            if (decoder != null) {
                decoder.recycle();
            }
        }
        mBigImgCache.evictAll();

        mSmallImgCache.evictAll();
        mDownloadingImageUrls.clear();
        mFetchingTime = 0;
        mCallback = null;
    }

    private boolean isSpriteConfigDataExist(long time) {
        TXSpriteConfigData configData = getSpriteConfig(time);
        return configData != null && configData.isValid();
    }

    private Bitmap getThumbnailFromSmallImageCache(long time) {
        return mSmallImgCache.get(time);
    }

    private BitmapRegionDecoder getBigImageDecoderFromCache(long time) {
        String bigImageUrl = getBigImageUrl(time);
        return mBigImgCache.get(bigImageUrl);
    }

    private Bitmap getThumbnailFromBigImageCache(long time) {
        BitmapRegionDecoder bigImageDecoder = getBigImageDecoderFromCache(time);
        if (bigImageDecoder == null) return null;

        TXSpriteConfigData configData = getSpriteConfig(time);
        if (configData == null || !configData.isValid()) return null;

        long relativeOffset = getRelativeOffset(time, configData);
        if (relativeOffset < 0) {
            Log.d(TAG, "getThumbnail time[" + time + "] is invalid, relativeOffset is " + relativeOffset + ".");
            return null;
        }

        Rect smallImageRect = getSmallImageRect(relativeOffset, configData);
        return bigImageDecoder.decodeRegion(smallImageRect, mBitmapOption);
    }

    private String getBigImageUrl(long time) {
        TXSpriteConfigData configData = getSpriteConfig(time);
        if (configData == null || !configData.isValid()) return "";

        long relativeOffset = getRelativeOffset(time, configData);
        if (relativeOffset < 0) {
            Log.d(TAG, "getThumbnail time[" + time + "] is invalid, relativeOffset is " + relativeOffset + ".");
            return "";
        }

        long picNo = relativeOffset / (configData.mIntervalS * configData.mCols * configData.mRows);
        return String.format(CONFIG_URL_FORMAT, mDomain, configData.mPath, picNo);
    }

    private void fetchSpriteConfig() {
        if (getIsFetchingSpriteConfig()) return;
        setIsFetchingSpriteConfig(true);

        String strUrl = String.format(BIG_IMAGE_URL_FORMAT, mDomain, mPath, mStreamId, mStartTs, mEndTs);
        Log.d(TAG, "fetchSpriteConfig url is : " + strUrl);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build();

        Request request = new Request.Builder()
            .url(strUrl)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "fetchSpriteConfig failed : " + e);
                handleFetchSpriteConfigFailed();
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.d(TAG, "fetchSpriteConfig response code : " + response.code());
                handleFetchSpriteConfigResponse(response);
            }
        });
    }

    private void handleFetchSpriteConfigResponse(Response response) {
        setIsFetchingSpriteConfig(false);
        if (response.isSuccessful()) {
            mSpriteConfigDatas.clear();
            try {
                String strData = "";
                if (response.body() != null) {
                    strData = response.body().string();
                }
                Log.d(TAG, "fetchSpriteConfig response data: " + strData);
                JSONArray dataArrays = new JSONArray(strData);
                for (int i = 0; i < dataArrays.length(); i++) {
                    TXSpriteConfigData configData = new TXSpriteConfigData();
                    configData.initWithData(dataArrays.getJSONObject(i));
                    mSpriteConfigDatas.add(configData);
                }

                TXSpriteConfigData configData = getSpriteConfig(mFetchingTime);
                if (configData == null || !configData.isValid()) {
                    notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_SERVER_ERROR, null);
                    return;
                }

                String bigImageUrl = getBigImageUrl(mFetchingTime);
                if (bigImageUrl.trim().isEmpty()) {
                    notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_PARAM_INVALID, null);
                } else {
                    fetchBigImage(bigImageUrl);
                }
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_SERVER_ERROR, null);
        }
    }

    private void handleFetchSpriteConfigFailed() {
        setIsFetchingSpriteConfig(false);
        notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_NETWORK_ERR, null);
    }

    private TXSpriteConfigData getSpriteConfig(long time) {
        for (TXSpriteConfigData data : mSpriteConfigDatas) {
            if (mStartTs + time >= data.mStartTime && mStartTs + time < data.mEndTime) {
                return data;
            }
        }
        return null;
    }

    private long getRelativeOffset(long time, TXSpriteConfigData configData) {
        long relativeOffset = time;
        if (mStartTs < configData.mStartTime) {
            relativeOffset -= (configData.mStartTime - mStartTs);
        } else {
            relativeOffset += (mStartTs - configData.mStartTime);
        }
        return relativeOffset;
    }

    private void fetchBigImage(String bigImageUrl) {
        if (isDownloadingBigImage(bigImageUrl)) return;
        addDownloadingBigImage(bigImageUrl);

        Log.d(TAG, "fetchBigImage url is : " + bigImageUrl);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

        Request request = new Request.Builder().url(bigImageUrl).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.d(TAG, "fetchBigImage response code : " + response.code());
                handleFetchBigImageResponse(bigImageUrl, response);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "fetchBigImage failed : " + e);
                handleFetchBigImageFailed(bigImageUrl);
            }
        });
    }

    private void handleFetchBigImageResponse(String bigImageUrl, Response response) {
        removeDownloadingBigImage(bigImageUrl);
        if (response.isSuccessful()) {
            InputStream inputStream = null;
            if (response.body() != null) {
                inputStream = response.body().byteStream();
            }
            if (inputStream != null) {
                try {
                    mBigImgCache.put(bigImageUrl, BitmapRegionDecoder.newInstance(inputStream, true));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            Bitmap smallImage = getThumbnailFromBigImageCache(mFetchingTime);
            notifyFetchThumbnailResult(
                smallImage == null ? SPRITE_THUMBNAIL_FETCH_SERVER_ERROR : SPRITE_THUMBNAIL_FETCH_SUCC,
                smallImage
            );
        } else {
            notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_SERVER_ERROR, null);
        }
    }

    private void handleFetchBigImageFailed(String bigImageUrl) {
        removeDownloadingBigImage(bigImageUrl);
        notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_NETWORK_ERR, null);
    }

    private Rect getSmallImageRect(long time, TXSpriteConfigData configData) {
        int picOffset = (int) (time % (configData.mIntervalS * configData.mRows * configData.mCols) / configData.mIntervalS);

        Rect rect = new Rect();
        rect.left = (picOffset % configData.mCols) * configData.mWidth;
        rect.top = (picOffset / configData.mCols) * configData.mHeight;
        rect.right = rect.left + configData.mWidth;
        rect.bottom = rect.top + configData.mHeight;
        return rect;
    }

    private synchronized void setIsFetchingSpriteConfig(boolean value) {
        mIsFetchingSpriteConfig = value;
    }

    private synchronized boolean getIsFetchingSpriteConfig() {
        return mIsFetchingSpriteConfig;
    }

    private synchronized boolean isDownloadingBigImage(String url) {
        return mDownloadingImageUrls.contains(url);
    }

    private synchronized void addDownloadingBigImage(String url) {
        mDownloadingImageUrls.add(url);
    }

    private synchronized void removeDownloadingBigImage(String url) {
        mDownloadingImageUrls.remove(url);
    }

    private void notifyFetchThumbnailResult(int errCode, Bitmap image) {
        Log.d(TAG, "notifyFetchThumbnailResult errCode is " + errCode);
        synchronized (this) {
            if (mCallback != null) {
                mCallback.onFetchDone(errCode, image);
            }
        }
    }
}