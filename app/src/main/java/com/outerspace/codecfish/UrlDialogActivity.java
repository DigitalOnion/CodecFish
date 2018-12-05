package com.outerspace.codecfish;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class UrlDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_dialog);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        String url = preferences.getString(MainActivity.KEY_PREFERENCE_URL, getString(R.string.no_url));
        EditText editUri = findViewById(R.id.url_edit);
        editUri.setText(url);
    }

    public void onClickUrlBtnCancel(View view) {
        this.finish();
    }

    public void onClickUrlBtnSave(View view) {
        EditText editUri = findViewById(R.id.url_edit);
        String uri = editUri.getText().toString();
        if(uri.equalsIgnoreCase("luis"))
            uri = Utils.getRtmpUrl();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MainActivity.KEY_PREFERENCE_URL, uri);
        editor.apply();
        this.finish();
    }
}
