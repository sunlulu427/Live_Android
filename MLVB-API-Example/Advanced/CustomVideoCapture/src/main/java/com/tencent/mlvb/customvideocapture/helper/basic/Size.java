package com.tencent.mlvb.customvideocapture.helper.basic;

public class Size {
    public int width = 0;
    public int height = 0;

    public Size() {
    }

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void swap() {
        int temp = width;
        width = height;
        height = temp;
    }
}