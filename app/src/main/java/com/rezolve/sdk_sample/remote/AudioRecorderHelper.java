package com.rezolve.sdk_sample.remote;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioRecorderHelper {

    private final static String TAG = AudioRecorderHelper.class.getSimpleName();

    private final ResultInterface callback;

    final File rawFile = new File(Environment.getExternalStorageDirectory(), "raw_recording");
    final File wavFile = new File(Environment.getExternalStorageDirectory(), "recording.wav");

    private static final int AUDIO_RECORDING_DURATION = (int) TimeUnit.SECONDS.toMillis(5);
    private static final int RECORDER_BPP = 16;
    private static final int SAMPLING_RATE_IN_HZ = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int NUMBER_OF_CHANNELS = CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE_FACTOR = 3;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_FACTOR;
    private final AtomicBoolean recordingInProgress = new AtomicBoolean(false);
    private AudioRecord recorder;
    private Thread recordingThread;

    public AudioRecorderHelper(ResultInterface callback) {
        this.callback = callback;
    }

    public void startRecording() {
        rawFile.delete();
        wavFile.delete();
        this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
        this.recordingThread = new Thread(new RecordingRunnable(), "Recording Thread");
        callback.showProgressBar(true);
        recorder.startRecording();
        recordingInProgress.set(true);
        recordingThread.start();
        new Handler(Looper.myLooper()).postDelayed(this::stopRecording, AUDIO_RECORDING_DURATION);
    }

    private void stopRecording() {
        if (null == recorder) {
            return;
        }

        recordingInProgress.set(false);
        recorder.stop();
        recorder.release();
        recorder = null;
        recordingThread = null;

        convertRawToWaveFile();
    }

    private void convertRawToWaveFile() {
        int byteRate = RECORDER_BPP * SAMPLING_RATE_IN_HZ * NUMBER_OF_CHANNELS/8;

        byte[] data = new byte[byteRate];

        try {
            FileInputStream in = new FileInputStream(rawFile);
            FileOutputStream out = new FileOutputStream(wavFile);
            long totalAudioLen = in.getChannel().size();
            long totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen, SAMPLING_RATE_IN_HZ, NUMBER_OF_CHANNELS, BUFFER_SIZE);

            while(in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
            readAudioFileToBytesAndResolve();
        } catch (IOException e) {
            e.printStackTrace();
        }
        rawFile.delete();
    }

    private void readAudioFileToBytesAndResolve() {
        try {
            RandomAccessFile f = new RandomAccessFile(wavFile, "r");
            byte[] bytes = new byte[(int)f.length()];
            f.readFully(bytes);

            callback.onAudioResult(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            callback.showProgressBar(false);
        }
    }

    private void writeWaveFileHeader(
            FileOutputStream out,
            long totalAudioLen,
            long totalDataLen,
            long longSampleRate,
            int channels,
            long byteRate
    ) throws IOException {
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * RECORDER_BPP / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    private class RecordingRunnable implements Runnable {

        @Override
        public void run() {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            try (final FileOutputStream outStream = new FileOutputStream(rawFile)) {
                while (recordingInProgress.get()) {
                    int result = recorder.read(buffer, BUFFER_SIZE);
                    if (result < 0) {
                        throw new RuntimeException("Reading of audio buffer failed: " + result);
                    }
                    outStream.write(buffer.array(), 0, BUFFER_SIZE);
                    buffer.clear();
                }
            } catch (IOException e) {
                throw new RuntimeException("Writing of recorded audio failed", e);
            }
        }
    }
}
