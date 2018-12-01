package com.outerspace.codecfish;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.Timer;
import java.util.TimerTask;

public class MainPresenter implements MainPresenterContract {

    MainViewContract mainView;
    Handler mainHandler, workHandler;
    Timer frameTimer;

    public MainPresenter(MainViewContract mainView) {
        this.mainView = mainView;

        // handler to a worker thread
        HandlerThread handlerThread = new HandlerThread("encoderThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        workHandler = new Handler(looper);

    }

    MyEncoder encoder;
    String url;

    public void init(String url, Handler mainHandler) {
        this.url = url;
        this.mainHandler = mainHandler;
        encoder = new MyEncoder(this);
        encoder.init(this.url, workHandler);
    }

    public void startStream() {
        frameTimer = new Timer();
        long period = 1000L / MyEncoder.FRAME_RATE;
        frameTimer.scheduleAtFixedRate(new StreamTask(), 0L, period);
    }

    private class StreamTask extends TimerTask {
        @Override
        public void run() {
            workHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(encoder != null) {
                        encoder.requestCanvas();
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mainView.onFrameSent();
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onMuxerDidNotConnect() { mainView.onMuxerDidNotConnect(); }

    @Override
    public void onMuxerIsConnected() { mainView.onMuxerIsConnected(); }

    @Override
    public void onResponseCanvas(Canvas canvas) {  // run in work thread
        canvas.drawColor(Color.RED);
        encoder.streamCanvas(canvas);
    }
}
