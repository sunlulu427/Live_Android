package com.tencent.mlvb.newtimeshiftspriite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.util.LruCache
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

class TXSpriteImageFetcher(private val context: Context) {

    companion object {
        private val TAG = TXSpriteImageFetcher::class.java.simpleName
        private const val CONFIG_URL_FORMAT = "http://%s%s%d.jpg?txTimeshift=on"
        private const val BIG_IMAGE_URL_FORMAT = "http://%s/%s/%s.json?txTimeshift=on&tsFormat=unix&tsSpritemode=1&tsStart=%d&tsEnd=%d"

        const val SPRITE_THUMBNAIL_FETCH_SUCC = 0
        const val SPRITE_THUMBNAIL_FETCH_PARAM_INVALID = -1
        const val SPRITE_THUMBNAIL_FETCH_NETWORK_ERR = -2
        const val SPRITE_THUMBNAIL_FETCH_SERVER_ERROR = -3
    }

    interface TXSpriteImageFetcherCallback {
        fun onFetchDone(errCode: Int, image: Bitmap?)
    }

    private data class TXSpriteConfigData(
        var startTime: Long = 0,
        var endTime: Long = 0,
        var duration: Double = 0.0,
        var path: String = "",
        var cols: Int = 0,
        var rows: Int = 0,
        var intervalS: Int = 0,
        var height: Int = 0,
        var width: Int = 0
    ) {
        fun initWithData(data: JSONObject) {
            startTime = data.optLong("start_time", 0)
            endTime = data.optLong("end_time", 0)
            duration = data.optDouble("duration", 0.0)
            path = data.optString("path", "")
            cols = data.optInt("cols", 0)
            rows = data.optInt("rows", 0)
            intervalS = data.optInt("interval", 0)
            height = data.optInt("height", 0)
            width = data.optInt("width", 0)
        }

        fun isValid(): Boolean {
            return path.trim().isNotEmpty() && startTime > 0 && endTime > 0 &&
                    cols > 0 && rows > 0 && intervalS > 0 && width > 0 && height > 0
        }
    }

    private val bitmapOption = BitmapFactory.Options()
    private var domain = ""
    private var path = ""
    private var streamId = ""
    private var startTs = 0L
    private var endTs = 0L

    @Volatile
    private var isFetchingSpriteConfig = false
    private val downloadingImageUrls = mutableListOf<String>()
    private val spriteConfigDatas = mutableListOf<TXSpriteConfigData>()
    private val bigImgCache = LruCache<String, BitmapRegionDecoder>(30)
    private val smallImgCache = LruCache<Long, Bitmap>(10)

    private var fetchingTime = 0L
    private var callback: TXSpriteImageFetcherCallback? = null

    fun init(domain: String, path: String, streamId: String, startTs: Long, endTs: Long) {
        this.domain = domain
        this.path = path
        this.streamId = streamId
        this.startTs = startTs
        this.endTs = endTs
    }

    @Synchronized
    fun setCallback(callback: TXSpriteImageFetcherCallback?) {
        this.callback = callback
    }

    fun getThumbnail(time: Long) {
        synchronized(this) {
            fetchingTime = time
        }

        getThumbnailFromSmallImageCache(time)?.let { smallImage ->
            notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_SUCC, smallImage)
            return
        }

        getThumbnailFromBigImageCache(time)?.let { bigImage ->
            smallImgCache.put(time, bigImage)
            notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_SUCC, bigImage)
            return
        }

        if (!isSpriteConfigDataExist(time)) {
            fetchSpriteConfig()
            return
        }

        val bigImageUrl = getBigImageUrl(time)
        if (bigImageUrl.trim().isEmpty()) {
            notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_PARAM_INVALID, null)
        } else {
            fetchBigImage(bigImageUrl)
        }
    }

    fun setCacheSize(size: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bigImgCache.resize(size)
            smallImgCache.resize(size)
        }
    }

    fun clear() {
        domain = ""
        path = ""
        streamId = ""
        startTs = 0
        endTs = 0
        setIsFetchingSpriteConfig(false)
        spriteConfigDatas.clear()

        bigImgCache.snapshot().values.forEach { decoder ->
            decoder?.recycle()
        }
        bigImgCache.evictAll()

        smallImgCache.evictAll()
        downloadingImageUrls.clear()
        fetchingTime = 0
        callback = null
    }

    private fun isSpriteConfigDataExist(time: Long): Boolean {
        val configData = getSpriteConfig(time)
        return configData?.isValid() == true
    }

    private fun getThumbnailFromSmallImageCache(time: Long): Bitmap? {
        return smallImgCache.get(time)
    }

    private fun getBigImageDecoderFromCache(time: Long): BitmapRegionDecoder? {
        val bigImageUrl = getBigImageUrl(time)
        return bigImgCache.get(bigImageUrl)
    }

    private fun getThumbnailFromBigImageCache(time: Long): Bitmap? {
        val bigImageDecoder = getBigImageDecoderFromCache(time) ?: return null
        val configData = getSpriteConfig(time) ?: return null

        if (!configData.isValid()) return null

        val relativeOffset = getRelativeOffset(time, configData)
        if (relativeOffset < 0) {
            Log.d(TAG, "getThumbnail time[$time] is invalid, relativeOffset is $relativeOffset.")
            return null
        }

        val smallImageRect = getSmallImageRect(relativeOffset, configData)
        return bigImageDecoder.decodeRegion(smallImageRect, bitmapOption)
    }

    private fun getBigImageUrl(time: Long): String {
        val configData = getSpriteConfig(time) ?: return ""
        if (!configData.isValid()) return ""

        val relativeOffset = getRelativeOffset(time, configData)
        if (relativeOffset < 0) {
            Log.d(TAG, "getThumbnail time[$time] is invalid, relativeOffset is $relativeOffset.")
            return ""
        }

        val picNo = relativeOffset / (configData.intervalS * configData.cols * configData.rows)
        return CONFIG_URL_FORMAT.format(domain, configData.path, picNo)
    }

    private fun fetchSpriteConfig() {
        if (getIsFetchingSpriteConfig()) return
        setIsFetchingSpriteConfig(true)

        val strUrl = BIG_IMAGE_URL_FORMAT.format(domain, path, streamId, startTs, endTs)
        Log.d(TAG, "fetchSpriteConfig url is : $strUrl")

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(strUrl)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "fetchSpriteConfig failed : $e")
                handleFetchSpriteConfigFailed()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "fetchSpriteConfig response code : ${response.code}")
                handleFetchSpriteConfigResponse(response)
            }
        })
    }

    private fun handleFetchSpriteConfigResponse(response: Response) {
        setIsFetchingSpriteConfig(false)
        if (response.isSuccessful) {
            spriteConfigDatas.clear()
            try {
                val strData = response.body?.string() ?: ""
                Log.d(TAG, "fetchSpriteConfig response data: $strData")
                val dataArrays = JSONArray(strData)

                for (i in 0 until dataArrays.length()) {
                    val configData = TXSpriteConfigData()
                    configData.initWithData(dataArrays.getJSONObject(i))
                    spriteConfigDatas.add(configData)
                }

                val configData = getSpriteConfig(fetchingTime)
                if (configData?.isValid() != true) {
                    notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_SERVER_ERROR, null)
                    return
                }

                val bigImageUrl = getBigImageUrl(fetchingTime)
                if (bigImageUrl.trim().isEmpty()) {
                    notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_PARAM_INVALID, null)
                } else {
                    fetchBigImage(bigImageUrl)
                }
            } catch (e: JSONException) {
                Log.e(TAG, e.toString())
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
            }
        } else {
            notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_SERVER_ERROR, null)
        }
    }

    private fun handleFetchSpriteConfigFailed() {
        setIsFetchingSpriteConfig(false)
        notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_NETWORK_ERR, null)
    }

    private fun getSpriteConfig(time: Long): TXSpriteConfigData? {
        return spriteConfigDatas.find { data ->
            startTs + time >= data.startTime && startTs + time < data.endTime
        }
    }

    private fun getRelativeOffset(time: Long, configData: TXSpriteConfigData): Long {
        var relativeOffset = time
        when {
            startTs < configData.startTime -> relativeOffset -= (configData.startTime - startTs)
            else -> relativeOffset += (startTs - configData.startTime)
        }
        return relativeOffset
    }

    private fun fetchBigImage(bigImageUrl: String) {
        if (isDownloadingBigImage(bigImageUrl)) return
        addDownloadingBigImage(bigImageUrl)

        Log.d(TAG, "fetchBigImage url is : $bigImageUrl")
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder().url(bigImageUrl).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "fetchBigImage response code : ${response.code}")
                handleFetchBigImageResponse(bigImageUrl, response)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "fetchBigImage failed : $e")
                handleFetchBigImageFailed(bigImageUrl)
            }
        })
    }

    private fun handleFetchBigImageResponse(bigImageUrl: String, response: Response) {
        removeDownloadingBigImage(bigImageUrl)
        if (response.isSuccessful) {
            val inputStream: InputStream? = response.body?.byteStream()
            inputStream?.let {
                try {
                    bigImgCache.put(bigImageUrl, BitmapRegionDecoder.newInstance(it, true))
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }

            val smallImage = getThumbnailFromBigImageCache(fetchingTime)
            notifyFetchThumbnailResult(
                if (smallImage == null) SPRITE_THUMBNAIL_FETCH_SERVER_ERROR else SPRITE_THUMBNAIL_FETCH_SUCC,
                smallImage
            )
        } else {
            notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_SERVER_ERROR, null)
        }
    }

    private fun handleFetchBigImageFailed(bigImageUrl: String) {
        removeDownloadingBigImage(bigImageUrl)
        notifyFetchThumbnailResult(SPRITE_THUMBNAIL_FETCH_NETWORK_ERR, null)
    }

    private fun getSmallImageRect(time: Long, configData: TXSpriteConfigData): Rect {
        val picOffset = (time % (configData.intervalS * configData.rows * configData.cols) / configData.intervalS).toInt()

        return Rect().apply {
            left = (picOffset % configData.cols) * configData.width
            top = (picOffset / configData.cols) * configData.height
            right = left + configData.width
            bottom = top + configData.height
        }
    }

    @Synchronized
    private fun setIsFetchingSpriteConfig(value: Boolean) {
        isFetchingSpriteConfig = value
    }

    @Synchronized
    private fun getIsFetchingSpriteConfig(): Boolean {
        return isFetchingSpriteConfig
    }

    @Synchronized
    private fun isDownloadingBigImage(url: String): Boolean {
        return downloadingImageUrls.contains(url)
    }

    @Synchronized
    private fun addDownloadingBigImage(url: String) {
        downloadingImageUrls.add(url)
    }

    @Synchronized
    private fun removeDownloadingBigImage(url: String) {
        downloadingImageUrls.remove(url)
    }

    private fun notifyFetchThumbnailResult(errCode: Int, image: Bitmap?) {
        Log.d(TAG, "notifyFetchThumbnailResult errCode is $errCode")
        synchronized(this) {
            callback?.onFetchDone(errCode, image)
        }
    }
}