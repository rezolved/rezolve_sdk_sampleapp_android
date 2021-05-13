package com.rezolve.sdk_sample.remote;

public interface ResultInterface {
    void showProgressBar(boolean value);
    void onAudioResult(byte[] bytes);
    void onImageResult(byte[] bytes);
}
