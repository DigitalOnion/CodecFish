package com.outerspace.codecfish;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import androidx.annotation.NonNull;

import net.butterflytv.rtmp_client.RTMPMuxer;

public class MyEncoder {
    private MainPresenterContract presenter;

    public MyEncoder(MainPresenterContract presenter) {
        this.presenter = presenter;
    }

    public static final String TAG = "MEDIA_ENCODING";

    public static final int IFRAME_INTERVAL = 2;               // 2 seconds between I-frames
    public static int FRAME_RATE = 25;                         // frames per second.
    public static int BIT_RATE = 850000;

    private static final String MIME_TYPE = "video/avc";       // H.264 Advanced Video Coding
    private MediaCodec encoder;
    private Handler workHandler;
    private Surface inputSurface;

    private static RTMPMuxer muxer = new RTMPMuxer();

    public void init(final String url, final int width, final int height) {
        MediaCodecInfo codecInfo = getCodecInfo(MIME_TYPE, true, "google", "264");
        int colorFormat = getColorFormat(codecInfo, MIME_TYPE);

        // handler to a worker thread
        HandlerThread handlerThread = new HandlerThread("encoderThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        workHandler = new Handler(looper);

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);

        // These settings are needed
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        // These settings are optional
        format.setString(MediaFormat.KEY_MIME, MIME_TYPE);
        format.setInteger(MediaFormat.KEY_WIDTH, width);
        format.setInteger(MediaFormat.KEY_HEIGHT, height);
        format.setInteger(MediaFormat.KEY_CAPTURE_RATE, FRAME_RATE);

        // opens the RTMP Muxer
        muxer.open(url, width, height);
        if( muxer.isConnected() == 1 ) {
            presenter.onMuxerIsConnected();

            // Create a MediaCodec encoder
            try {
                //NOTE: choosing the encoder by name was bringing the google's encoder which didn't work
                //encoder = MediaCodec.createByCodecName(codecInfo.getName());
                encoder = MediaCodec.createEncoderByType(MIME_TYPE);
                encoder.setCallback(new MyCallback(), workHandler);

                // surface is the output surface, can be null for encoding to a ByteBuffer
                encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                inputSurface = encoder.createInputSurface();
                // encoder.setInputSurface(inputSurface);
                workHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        encoder.start();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            presenter.onMuxerDidNotConnect();
        }
    }

    // This callback is given to the encoder to run asynchronous. since we set it with
    // setCallback(new MyCallback(), workHandler). It will run in the workThread.
    private static class MyCallback extends MediaCodec.Callback {

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            Log.d(TAG, "onInpubBufferAvailable index:" + index + " Thread:" + Thread.currentThread().getName());

            ByteBuffer buffer = codec.getInputBuffer(index);
            long timestamp = System.currentTimeMillis();
            codec.queueInputBuffer(index, 0, buffer.capacity(), timestamp, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            Log.d(TAG, "onOutpubBufferAvailable " + ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 ? (" size: " + info.size):" config flag"));
            byte[] bytes = new byte[info.size];
            ByteBuffer outBuffer = codec.getOutputBuffer(index);
            outBuffer.position(info.offset);
            outBuffer.limit(info.offset + info.size);
            outBuffer.get(bytes, 0, info.size);
            int timestamp = (int) System.currentTimeMillis();
            outBuffer.position(info.offset);

            muxer.writeVideo(bytes, 0, bytes.length, timestamp);
            codec.releaseOutputBuffer(index, false);
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            Log.d(TAG, "onError");
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
            Log.d(TAG, "onOutputFormatChanged format:" + format);
            ByteBuffer sps = format.getByteBuffer("csd-0");
            ByteBuffer pps = format.getByteBuffer("csd-1");
            byte[] config = new byte[sps.limit() + pps.limit()];
            sps.get(config, 0, sps.limit());
            pps.get(config, sps.limit(), pps.limit());

            muxer.writeVideo(config, 0, config.length, 0);
        }
    }

    public Surface getInputSurface() { return inputSurface; }

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
