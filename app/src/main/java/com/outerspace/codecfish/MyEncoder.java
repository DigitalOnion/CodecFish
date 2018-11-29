package com.outerspace.codecfish;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class MyEncoder {
    public static final String TAG = "MEDIA_ENCODING";

    private static final int TIMEOUT_1SEC = 1000;
    private static final int TIMEOUT_100MS = 100;
    public static final int IFRAME_INTERVAL = 2;               // 2 seconds between I-frames
    public static int FRAME_RATE = 25;                         // frames per second.
    public static int BIT_RATE = 850000;

    private static final String MIME_TYPE = "video/avc";       // H.264 Advanced Video Coding
    private MediaCodec encoder;

    public void init() {
        MediaCodecInfo codecInfo = getCodecInfo(MIME_TYPE, true, "google", "264");
        int colorFormat = getColorFormat(codecInfo, MIME_TYPE);

        int width = 854;
        int height = 480;

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);

        format.setString(MediaFormat.KEY_MIME, MIME_TYPE);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        format.setInteger(MediaFormat.KEY_WIDTH, width);
        format.setInteger(MediaFormat.KEY_HEIGHT, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_CAPTURE_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        // handler to a worker thread
        HandlerThread handlerThread = new HandlerThread("encoderThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);

        // Create a MediaCodec encoder
        try {
            encoder = MediaCodec.createByCodecName(codecInfo.getName());
            encoder.setCallback(new MyCallback(), handler);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    encoder.start();
                }
            });
            //inputSurface = encoder.createInputSurface();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MyCallback extends MediaCodec.Callback {
        CharSequence chars[] = {"csd-0", "csd-1"};

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            Log.d(TAG, "onInpubBufferAvailable index:" + index + " Thread:" + Thread.currentThread().getName());

            if(index <= 1) {
                ByteBuffer buffer = codec.getInputBuffer(index);
                buffer.put(((String) chars[index]).getBytes());
                long timestamp = System.currentTimeMillis();
                codec.queueInputBuffer(index, 0, chars[index].length(), timestamp, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            Log.d(TAG, "onOutpubBufferAvailable");
            codec.releaseOutputBuffer(index, false);
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            Log.d(TAG, "onError");
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
            Log.d(TAG, "onOutputFormatChanged");
            ByteBuffer sps = format.getByteBuffer("csd-0");
            ByteBuffer pps = format.getByteBuffer("csd-1");
            byte[] config = new byte[sps.limit() + pps.limit()];
            sps.get(config, 0, sps.limit());
            pps.get(config, sps.limit(), pps.limit());
            Log.d(TAG, format.toString());
        }
    }

    private MediaCodecInfo getCodecInfo(String mimeType, boolean wantEncoder, String... containsKeywords) throws IllegalStateException {
        ArrayList<MediaCodecInfo> codecInfoList = new ArrayList<>();
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        for(MediaCodecInfo mediaCodecInfo : codecList.getCodecInfos()) {
            if(mediaCodecInfo.isEncoder() == wantEncoder) {
                for(String codecMimeType : mediaCodecInfo.getSupportedTypes()) {
                    if(codecMimeType.equalsIgnoreCase(MIME_TYPE)) {
                        codecInfoList.add(mediaCodecInfo);
                        break;
                    }
                }
            }
        }
        for(int i = 0; i < containsKeywords.length; i++) {
            containsKeywords[i] = containsKeywords[i].toLowerCase();
        }
        if(codecInfoList.size() > 0) {
            for(MediaCodecInfo codecInfo : codecInfoList) {
                String codecName = codecInfo.getName().toLowerCase();
                boolean allKeywords = true;
                for(String keyword : containsKeywords) {
                    allKeywords = allKeywords && codecName.contains(keyword);
                }
                if(allKeywords) return codecInfo;
            }
            return codecInfoList.get(0);
        }
        throw new IllegalStateException("Couldn't find an encoder for Mime Type = " + mimeType);
    }

    private int getColorFormat(MediaCodecInfo codecInfo, String mimeType) throws IllegalStateException {
        if(codecInfo != null) {
            MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
            int[] colorFormats = capabilities.colorFormats;
            int[] colorFormatTargets = {
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible,
            };
            for(int target: colorFormatTargets) {
                for(int cf : colorFormats) {
                    if(cf == target) {
                        return target;
                    }
                }
            }
        }
        throw new IllegalStateException("Couldn't find color-format for Mime Type = " + mimeType);
    }


}
