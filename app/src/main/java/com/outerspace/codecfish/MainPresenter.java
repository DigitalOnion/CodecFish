package com.outerspace.codecfish;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.Surface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainPresenter implements MainPresenterContract {

    private int cRed = 0xFF;
    private int cBlue = 0xFF;
    private int cGreen = 0xFF;

    private Surface targetSurface, encoderSurface;
    private MainViewContract mainView;
    private Handler workHandler;
    private Handler mainHandler;
    private Timer frameTimer;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private float xTime, yTime;
    private Paint paint;

    private MyEncoder encoder;
    private String url;

    public MainPresenter(MainViewContract mainView) {
        this.mainView = mainView;

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(100F);
    }

    public void init(String url, Handler mainHandler) {
        this.url = url;
        this.mainHandler = mainHandler;
        encoder = new MyEncoder(this);
        targetSurface = mainView.getSurface();

        Canvas canvas = targetSurface.lockCanvas(null);
        xTime = canvas.getWidth() * 0.05F;
        yTime = canvas.getHeight() * 0.95F;

        encoder.init(this.url, canvas.getWidth(), canvas.getHeight());
        targetSurface.unlockCanvasAndPost(canvas);

        encoderSurface = encoder.getInputSurface();
    }

    public void startStream() {
        frameTimer = new Timer();
        long period = 1000L / MyEncoder.FRAME_RATE;
        frameTimer.scheduleAtFixedRate(new StreamTask(), 0L, period);
    }

    private class StreamTask extends TimerTask {
        @Override
        public void run() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(encoder != null) {
                        updateTargetSurface(targetSurface, encoderSurface);
                        mainView.onFrameSent();
                    }
                }
            });
        }
    }

    private void updateTargetSurface(Surface... surfaces) {
        cGreen += 1 % 0x100;
        cBlue += 2 % 0x100;
        cRed += 3 % 0x100;
        int currentColor = Color.rgb(cRed, cGreen, cBlue);
        paint.setColor(Color.rgb(0x100 - cRed, 0x100 - cGreen, 0x100 - cBlue));

        for(Surface surface : surfaces) {
            if(surface != null) {
                Canvas canvas = surface.lockCanvas(null);
                canvas.drawColor(currentColor);

                String currentTime = sdf.format(new Date());
                canvas.drawText(currentTime, xTime, yTime, paint);

                surface.unlockCanvasAndPost(canvas);       // for encoder it causes a onOutputBufferAvailable.
            }
        }
    }

    @Override
    public void onMuxerDidNotConnect() { mainView.onMuxerDidNotConnect(); }

    @Override
    public void onMuxerIsConnected() { mainView.onMuxerIsConnected(); }

}
