package com.outerspace.codecfish;

import android.view.Surface;

public interface MainViewContract {
    public void onMuxerDidNotConnect();
    public void onMuxerIsConnected();
    public void onFrameSent();
    public Surface getSurface();
}
