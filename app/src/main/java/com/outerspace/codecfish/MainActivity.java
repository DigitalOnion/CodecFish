package com.outerspace.codecfish;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity implements MainViewContract {
    public static final String KEY_PREFERENCE_URL = "URL_PREFERENCES";

    SurfaceView targetSurfaceView;

    Button btnEncoderTest;
    Button btnProcessTest;
    TextView display;

    String exerciseUrl;

    MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEncoderTest = findViewById(R.id.btn_codec_encoder_test);
        btnProcessTest = findViewById(R.id.btn_surface_process_test);
        targetSurfaceView = findViewById(R.id.target_surface_view);
        display = findViewById(R.id.counter_display);

        presenter = new MainPresenter(this);

        exerciseUrl = Utils.getRtmpUrl();
    }

    @Override
    protected void onStart() {
        super.onStart();
        btnProcessTest.setVisibility(View.GONE);
        btnEncoderTest.setVisibility(View.VISIBLE);
    }

    public void onClickBtnCodecEncoderTest(View view) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
        exerciseUrl = preferences.getString(KEY_PREFERENCE_URL, getString(R.string.no_url));
        presenter.init(exerciseUrl, mainHandler);
    }

    public void onClickBtnSurfaceProcessTest(View view) {
        presenter.startStream();
    }

    public void onClickUrl(View view) {
        Intent intent = new Intent(this, UrlDialogActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMuxerDidNotConnect() {
        Toast.makeText(this, R.string.muxer_did_not_connect, Toast.LENGTH_SHORT).show();
        btnProcessTest.setVisibility(View.GONE);
        btnEncoderTest.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMuxerIsConnected() {
        btnProcessTest.setVisibility(View.VISIBLE);
        btnEncoderTest.setVisibility(View.GONE);
    }

    @Override
    public void onFrameSent() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Integer count = Integer.valueOf(display.getText().toString()) + 1;
                display.setText(count.toString());
            }
        });
    }

    @Override
    public Surface getSurface() {
        return targetSurfaceView.getHolder().getSurface();
    }
}
