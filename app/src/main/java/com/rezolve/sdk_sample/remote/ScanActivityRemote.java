package com.rezolve.sdk_sample.remote;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.ssp.interfaces.SspFromCpmInterface;
import com.rezolve.sdk.ssp.model.SspObject;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.providers.RemoteScanResolverProvider;

public class ScanActivityRemote extends AppCompatActivity {

    private static final String TAG = ScanActivityRemote.class.getSimpleName();

    private static final String[] REQUIRED_PERMISSIONS = new String[]{ Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static final int REQUEST_CODE_PERMISSIONS = 10203;
    private final static int DESIRED_IMAGE_WIDTH = 400;

    private ProgressBar progressBar;

    private AudioRecorderHelper audioRecorderHelper;
    private ImageCaptureHelper imageCaptureHelper;

    private final ResultInterface resultInterface = new ResultInterface() {

        @Override
        public void showProgressBar(boolean value) {
            runOnUiThread(() -> progressBar.setVisibility(value ? View.VISIBLE : View.GONE));
        }

        @Override
        public void onAudioResult(byte[] bytes) {
            RemoteScanResolverProvider.getInstance().getRemoteScanResolver().resolveAudio(bytes, DESIRED_IMAGE_WIDTH, new SspFromCpmInterface() {
                @Override
                public void onResponseFromCpmSuccess(SspObject sspObject) {
                    onResult(sspObject);
                }

                @Override
                public void onError(@NonNull RezolveError error) {
                    Log.d(TAG, "onError: "+error.getMessage()+" / "+error.getErrorMessage()+" / "+error.getErrorType());
                    showProgressBar(false);
                }
            });
        }

        @Override
        public void onImageResult(byte[] bytes) {
            RemoteScanResolverProvider.getInstance().getRemoteScanResolver().resolveImage(bytes, DESIRED_IMAGE_WIDTH, new SspFromCpmInterface() {
                @Override
                public void onResponseFromCpmSuccess(SspObject sspObject) {
                    onResult(sspObject);
                }

                @Override
                public void onError(@NonNull RezolveError error) {
                    Log.d(TAG, "onError: "+error.getMessage()+" / "+error.getErrorMessage()+" / "+error.getErrorType());
                    if (error.getErrorType() == RezolveError.RezolveErrorType.NOT_FOUND) {
                        if (error.getErrorMessage() == RezolveError.RezolveErrorMessage.WATERMARK_NOT_FOUND) {
                            imageCaptureHelper.captureNextFrame();
                            return;
                        }
                    }

                    if (error.getIoException() != null) {
                        error.getIoException().printStackTrace();
                    }

                    showProgressBar(false);
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_remote);

        prepareViews();

        audioRecorderHelper = new AudioRecorderHelper(this, resultInterface);
        imageCaptureHelper = new ImageCaptureHelper(this, resultInterface);

        if (allPermissionsGranted()) {
            imageCaptureHelper.startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void prepareViews() {
        progressBar = findViewById(R.id.progress_bar);
        Button cameraCaptureButton = findViewById(R.id.camera_capture_button);
        Button audioCaptureButton = findViewById(R.id.audio_capture_button);
        cameraCaptureButton.setOnClickListener(v -> {
            imageCaptureHelper.scanVideo();
        });
        audioCaptureButton.setOnClickListener(v -> {
            audioRecorderHelper.startRecording();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageCaptureHelper.onDestroy();
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void onResult(SspObject sspObject) {
        Log.d(TAG, "onResponseFromCpmSuccess:IMAGE: "+sspObject.entityToJson());
        Toast.makeText(this, "Engagement successfully resolved: "+sspObject.getEngagementName(), Toast.LENGTH_LONG).show();
        resultInterface.showProgressBar(false);
    }
}
