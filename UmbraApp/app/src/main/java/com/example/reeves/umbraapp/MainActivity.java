package com.example.reeves.umbraapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0,0);

        super.onCreate(savedInstanceState);

        // Set Fullscreen Immersive to Remove Navigation Bar, etc.
        // Referenced https://developer.android.com/training/system-ui/immersive
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        Button start_button = findViewById(R.id.start_button);
        start_button.setText("Start");
        start_button.setOnClickListener(this);

        Button settings_button = findViewById(R.id.settings_button);
        settings_button.setText("Settings");
        settings_button.setOnClickListener(this);

        Button triumphs_button = findViewById(R.id.triumphs_button);
        triumphs_button.setText("Triumphs");
        triumphs_button.setOnClickListener(this);
    }

    // Remove System UI When Playing
    // Referenced https://developer.android.com/training/system-ui/immersive
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            hideSystemUI();
        }
    }

    public void hideSystemUI() {
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

    }

    @Override
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.start_button:
                startActivity(new Intent(this, GameLauncher.class));
                break;
            case R.id.settings_button:
                startActivity(new Intent(this, GameSettings.class));
                break;
            case R.id.triumphs_button:
                startActivity(new Intent(this, HighScores.class));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                hideSystemUI();
        }
        return true;
    }
}