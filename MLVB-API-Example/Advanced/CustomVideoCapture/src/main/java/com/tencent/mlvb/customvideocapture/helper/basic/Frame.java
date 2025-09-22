package com.tencent.mlvb.customvideocapture.helper.basic;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import java.nio.ByteBuffer;

public class Frame {
    /**
     * The data contained in this frame, this buffer may be returned from {@link MediaCodec#getInputBuffers()},
     * Needs to be returned to MediaCodec after use is completed.
     */
    private ByteBuffer buffer;

    /**
     * If {@link Frame#buffer} is returned from another module, this member records its index.
     */
    private int bufferIndex;

    /**
     * Identifies which byte in the cache starts to be valid data.
     */
    private int offset;

    /**
     * Identifies the length of valid data in the cache.
     */
    private int size;

    /**
     * The display time corresponding to this data
     */
    private long presentationTimeUs;

    /**
     * Some flags, see {@link BufferInfo#flags} for details
     */
    private int flags;

    public Frame() {
        this.buffer = null;
        this.bufferIndex = 0;
        this.offset = 0;
        this.size = 0;
        this.presentationTimeUs = 0L;
        this.flags = 0;
    }

    public Frame(ByteBuffer buffer, int bufferIndex, int offset, int size, long presentationTimeUs, int flags) {
        this.buffer = buffer;
        this.bufferIndex = bufferIndex;
        this.offset = offset;
        this.size = size;
        this.presentationTimeUs = presentationTimeUs;
        this.flags = flags;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public int getBufferIndex() {
        return bufferIndex;
    }

    public void setBufferIndex(int bufferIndex) {
        this.bufferIndex = bufferIndex;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getPresentationTimeUs() {
        return presentationTimeUs;
    }

    public void setPresentationTimeUs(long presentationTimeUs) {
        this.presentationTimeUs = presentationTimeUs;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }
}