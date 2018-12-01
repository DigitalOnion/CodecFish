package com.outerspace.codecfish;

public interface MainViewContract {
    public void onMuxerDidNotConnect();
    public void onMuxerIsConnected();
    public void onFrameSent();
}
