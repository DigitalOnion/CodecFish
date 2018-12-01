package com.outerspace.codecfish;

import android.graphics.Canvas;


public interface MainPresenterContract {
    public void onMuxerDidNotConnect();
    public void onMuxerIsConnected();
    public void onResponseCanvas(Canvas canvas);
}
