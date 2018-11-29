package com.outerspace.codecfish;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    MyEncoder encoder;
    Button btnEncoderTest;
    Button btnProcessTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEncoderTest = findViewById(R.id.btn_codec_encoder_test);
        btnProcessTest = findViewById(R.id.btn_surface_process_test);
        btnProcessTest.setEnabled(false);
    }

    public void onClickBtnCodecEncoderTest(View view) {
        btnProcessTest.setEnabled(true);
        btnEncoderTest.setEnabled(false);
        encoder = new MyEncoder();
        encoder.init();
    }

    public void onClickBtnSurfaceProcessTest(View view) {
        if(encoder != null) {
            encoder.processImage();
        }
    }
}
