package com.tencent.mlvb.customvideocapture.helper.basic;

import android.opengl.EGLContext;

public class TextureFrame {
    public EGLContext eglContext = null;
    public int textureId = 0;
    public int width = 0;
    public int height = 0;
    public long timestampMs = 0L;

    public TextureFrame() {
    }

    public TextureFrame(EGLContext eglContext, int textureId, int width, int height, long timestampMs) {
        this.eglContext = eglContext;
        this.textureId = textureId;
        this.width = width;
        this.height = height;
        this.timestampMs = timestampMs;
    }
}