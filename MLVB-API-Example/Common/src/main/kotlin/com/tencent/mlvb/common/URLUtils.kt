package com.tencent.mlvb.common

import com.tencent.mlvb.debug.GenerateTestUserSig
import java.io.File

/**
 * Generating Streaming URLs
 * See https://cloud.tencent.com/document/product/454/7915.
 */
object URLUtils {

    const val WEBRTC = "webrtc://"
    const val RTMP = "rtmp://"
    const val HTTP = "http://"
    const val TRTC = "trtc://"
    const val TRTC_DOMAIN = "cloud.tencent.com"
    const val APP_NAME = "live"

    /**
     * Push URL types
     */
    enum class PushType(val value: Int) {
        RTC(0),
        RTMP(1)
    }

    /**
     * Play URL types
     */
    enum class PlayType(val value: Int) {
        RTMP(0),
        FLV(1),
        HLS(2),
        RTC(3),
        WEBRTC(4)
    }

    /**
     * Generating Publishing URLs
     *
     * @param streamId
     * @param userId
     * @param type 0:RTC  1：RTMP
     * @return
     */
    @JvmStatic
    fun generatePushUrl(streamId: String?, userId: String, type: Int): String {
        return when (type) {
            0 -> // PushType.RTC
                "$TRTC$TRTC_DOMAIN/push/$streamId?sdkappid=${GenerateTestUserSig.SDKAPPID}" +
                        "&userid=$userId&usersig=${GenerateTestUserSig.genTestUserSig(userId)}"
            1 -> // PushType.RTMP
                "$RTMP${GenerateTestUserSig.PUSH_DOMAIN}${File.separator}$APP_NAME" +
                        "${File.separator}$streamId${GenerateTestUserSig.getSafeUrl(streamId)}"
            else -> ""
        }
    }

    /**
     * Generating Publishing URLs with enum type
     */
    @JvmStatic
    fun generatePushUrl(streamId: String?, userId: String, type: PushType): String {
        return generatePushUrl(streamId, userId, type.value)
    }

    /**
     * Generating Playback URLs
     *
     * @param streamId
     * @param userId
     * @param type type 0:RTMP  1：FLV 2:HLS 3:RTC 4:WEBRTC
     * @return
     */
    @JvmStatic
    fun generatePlayUrl(streamId: String?, userId: String, type: Int): String {
        return when (type) {
            0 -> // PlayType.RTMP
                "$RTMP${GenerateTestUserSig.PLAY_DOMAIN}${File.separator}$APP_NAME${File.separator}$streamId"
            1 -> // PlayType.FLV
                "$HTTP${GenerateTestUserSig.PLAY_DOMAIN}${File.separator}$APP_NAME${File.separator}$streamId.flv"
            2 -> // PlayType.HLS
                "$HTTP${GenerateTestUserSig.PLAY_DOMAIN}${File.separator}$APP_NAME${File.separator}$streamId.m3u8"
            3 -> // PlayType.RTC
                "$TRTC$TRTC_DOMAIN/play/$streamId?sdkappid=${GenerateTestUserSig.SDKAPPID}" +
                        "&userid=$userId&usersig=${GenerateTestUserSig.genTestUserSig(userId)}"
            4 -> // PlayType.WEBRTC
                "$WEBRTC${GenerateTestUserSig.PLAY_DOMAIN}${File.separator}$APP_NAME${File.separator}$streamId"
            else -> ""
        }
    }

    /**
     * Generating Playback URLs with enum type
     */
    @JvmStatic
    fun generatePlayUrl(streamId: String?, userId: String, type: PlayType): String {
        return generatePlayUrl(streamId, userId, type.value)
    }
}