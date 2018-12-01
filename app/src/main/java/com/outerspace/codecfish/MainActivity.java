package com.outerspace.codecfish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity implements MainViewContract {
    private static final String KEY_PREFERENCE_URL = "URL_PREFERENCES";
    Button btnEncoderTest;
    Button btnProcessTest;
    TextInputEditText txtUrl;
    TextView display;
    SharedPreferences preferences;

    MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEncoderTest = findViewById(R.id.btn_codec_encoder_test);
        btnProcessTest = findViewById(R.id.btn_surface_process_test);
        btnProcessTest.setEnabled(false);

        txtUrl = findViewById(R.id.url);
        display = findViewById(R.id.counter_display);

        presenter = new MainPresenter(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        preferences = this.getPreferences(Context.MODE_PRIVATE);

        txtUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(KEY_PREFERENCE_URL, s.toString());
                editor.commit();
            }
        });

        if(preferences.contains(KEY_PREFERENCE_URL)) {
            String priorUrl = preferences.getString(KEY_PREFERENCE_URL, "");
            txtUrl.setText(priorUrl);
        } else {
            txtUrl.setText(Utils.getRtmpUrl());
        }
    }

    public void onClickBtnCodecEncoderTest(View view) {
        String url = txtUrl.getText().toString();
        Handler mainHandler = new Handler(Looper.getMainLooper());
        presenter.init(url, mainHandler);
    }

    public void onClickBtnSurfaceProcessTest(View view) {
        presenter.startStream();
    }

    public void onClickRestartUrl(View view) {
        txtUrl.setText(Utils.getRtmpUrl());
    }

    @Override
    public void onMuxerDidNotConnect() {
        Toast.makeText(this, R.string.muxer_did_not_connect, Toast.LENGTH_SHORT).show();
        btnProcessTest.setEnabled(false);
        btnEncoderTest.setEnabled(true);
    }

    @Override
    public void onMuxerIsConnected() {
        btnProcessTest.setEnabled(true);
        btnEncoderTest.setEnabled(false);
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
}
