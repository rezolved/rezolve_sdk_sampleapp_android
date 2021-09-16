package com.rezolve.sdk_sample.remote;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.rezolve.sdk_sample.R;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageCaptureHelper {

    private final static String TAG = ImageCaptureHelper.class.getSimpleName();

    private final AppCompatActivity activity;
    private final ResultInterface callback;

    private final ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private final Handler cameraCaptureHandler;
    private final PreviewView previewView;
    private int frameCounter = 0;

    public ImageCaptureHelper(AppCompatActivity activity, ResultInterface callback) {
        this.activity = activity;
        this.callback = callback;
        cameraCaptureHandler = new Handler(Looper.getMainLooper());
        cameraExecutor = Executors.newSingleThreadExecutor();
        previewView = activity.findViewById(R.id.camera_preview);
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(activity);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(activity.getWindowManager().getDefaultDisplay().getRotation())
                        .setTargetResolution(Size.parseSize("1280x720"))
                        .build();

                cameraProvider.bindToLifecycle(activity, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(activity));
    }

    public void scanVideo() {
        if (imageCapture != null) {
            frameCounter = 0;
            cameraCaptureHandler.post(captureCameraFrame);
        }
    }

    public void captureNextFrame() {
        if (frameCounter < 5) {
            cameraCaptureHandler.post(captureCameraFrame);
        } else {
            callback.showProgressBar(false);
        }
    }

    private final Runnable captureCameraFrame = new Runnable() {
        @Override
        public void run() {
            frameCounter++;
            imageCapture.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
                @SuppressLint("UnsafeOptInUsageError")
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy image) {
                    Log.d(TAG, "onCaptureSuccess");
                    if (image.getImage() != null) {
                        callback.showProgressBar(true);
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        buffer.rewind();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        callback.onImageResult(bytes);
                        image.close();
                    }
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    exception.printStackTrace();
                }
            });
        }
    };

    public void onDestroy() {
        cameraExecutor.shutdown();
    }
}
