package com.tencent.mlvb.common;

import com.tencent.mlvb.debug.GenerateTestUserSig;
import java.io.File;

/**
 * Generating Streaming URLs
 * See https://cloud.tencent.com/document/product/454/7915.
 */
public class URLUtils {

    public static final String WEBRTC = "webrtc://";
    public static final String RTMP = "rtmp://";
    public static final String HTTP = "http://";
    public static final String TRTC = "trtc://";
    public static final String TRTC_DOMAIN = "cloud.tencent.com";
    public static final String APP_NAME = "live";

    /**
     * Push URL types
     */
    public enum PushType {
        RTC(0),
        RTMP(1);

        private final int value;

        PushType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Play URL types
     */
    public enum PlayType {
        RTMP(0),
        FLV(1),
        HLS(2),
        RTC(3),
        WEBRTC(4);

        private final int value;

        PlayType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Generating Publishing URLs
     *
     * @param streamId
     * @param userId
     * @param type 0:RTC  1：RTMP
     * @return
     */
    public static String generatePushUrl(String streamId, String userId, int type) {
        switch (type) {
            case 0: // PushType.RTC
                return TRTC + TRTC_DOMAIN + "/push/" + streamId + "?sdkappid=" + GenerateTestUserSig.SDKAPPID +
                       "&userid=" + userId + "&usersig=" + GenerateTestUserSig.genTestUserSig(userId);
            case 1: // PushType.RTMP
                return RTMP + GenerateTestUserSig.PUSH_DOMAIN + File.separator + APP_NAME +
                       File.separator + streamId + GenerateTestUserSig.getSafeUrl(streamId);
            default:
                return "";
        }
    }

    /**
     * Generating Publishing URLs with enum type
     */
    public static String generatePushUrl(String streamId, String userId, PushType type) {
        return generatePushUrl(streamId, userId, type.getValue());
    }

    /**
     * Generating Playback URLs
     *
     * @param streamId
     * @param userId
     * @param type type 0:RTMP  1：FLV 2:HLS 3:RTC 4:WEBRTC
     * @return
     */
    public static String generatePlayUrl(String streamId, String userId, int type) {
        switch (type) {
            case 0: // PlayType.RTMP
                return RTMP + GenerateTestUserSig.PLAY_DOMAIN + File.separator + APP_NAME + File.separator + streamId;
            case 1: // PlayType.FLV
                return HTTP + GenerateTestUserSig.PLAY_DOMAIN + File.separator + APP_NAME + File.separator + streamId + ".flv";
            case 2: // PlayType.HLS
                return HTTP + GenerateTestUserSig.PLAY_DOMAIN + File.separator + APP_NAME + File.separator + streamId + ".m3u8";
            case 3: // PlayType.RTC
                return TRTC + TRTC_DOMAIN + "/play/" + streamId + "?sdkappid=" + GenerateTestUserSig.SDKAPPID +
                       "&userid=" + userId + "&usersig=" + GenerateTestUserSig.genTestUserSig(userId);
            case 4: // PlayType.WEBRTC
                return WEBRTC + GenerateTestUserSig.PLAY_DOMAIN + File.separator + APP_NAME + File.separator + streamId;
            default:
                return "";
        }
    }

    /**
     * Generating Playback URLs with enum type
     */
    public static String generatePlayUrl(String streamId, String userId, PlayType type) {
        return generatePlayUrl(streamId, userId, type.getValue());
    }
}