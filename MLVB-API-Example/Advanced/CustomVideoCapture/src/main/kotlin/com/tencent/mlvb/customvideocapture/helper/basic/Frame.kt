package com.tencent.mlvb.customvideocapture.helper.basic

import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import java.nio.ByteBuffer

data class Frame(
    /**
     * The data contained in this frame, this buffer may be returned from [MediaCodec.getInputBuffers],
     * Needs to be returned to MediaCodec after use is completed.
     */
    var buffer: ByteBuffer? = null,

    /**
     * If [Frame.buffer] is returned from another module, this member records its index.
     */
    var bufferIndex: Int = 0,

    /**
     * Identifies which byte in the cache starts to be valid data.
     */
    var offset: Int = 0,

    /**
     * Identifies the length of valid data in the cache.
     */
    var size: Int = 0,

    /**
     * The display time corresponding to this data
     */
    var presentationTimeUs: Long = 0L,

    /**
     * Some flags, see [BufferInfo.flags] for details
     */
    var flags: Int = 0
)