package com.example.reeves.umbraapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class HighScores extends Activity implements View.OnClickListener {

    private String scores[];
    private static final String file_name = "scores.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        setContentView(R.layout.activity_game_scores);
        try {
            scores = new String[10];
            loadScores();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        TextView title = findViewById(R.id.scores_title);
        title.setText("High Scores");

        TextView score_10 = findViewById(R.id.score_10);
        score_10.setText(scores[9]);

        TextView score_9 = findViewById(R.id.score_9);
        score_9.setText(scores[8]);

        TextView score_8 = findViewById(R.id.score_8);
        score_8.setText(scores[7]);

        TextView score_7 = findViewById(R.id.score_7);
        score_7.setText(scores[6]);

        TextView score_6 = findViewById(R.id.score_6);
        score_6.setText(scores[5]);

        TextView score_5 = findViewById(R.id.score_5);
        score_5.setText(scores[4]);

        TextView score_4 = findViewById(R.id.score_4);
        score_4.setText(scores[3]);

        TextView score_3 = findViewById(R.id.score_3);
        score_3.setText(scores[2]);

        TextView score_2 = findViewById(R.id.score_2);
        score_2.setText(scores[1]);

        TextView score_1 = findViewById(R.id.score_1);
        score_1.setText(scores[0]);

        Button exit_button = findViewById(R.id.score_exit_button);
        exit_button.setText("Back");
        exit_button.setOnClickListener(this);
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
            case R.id.score_exit_button:
                finish();
                break;
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

    public void loadScores()
    throws IOException {

        BufferedReader r = null;
        try {
            InputStream in = openFileInput(file_name);
            r = new BufferedReader(new InputStreamReader(in));
            for (int i = 0; i < 10; i++) {
                String line = r.readLine();
                scores[i] = Integer.toString(10 - i) + ".  " + line;
            }
        }
        catch (FileNotFoundException e) {
            for (int i = 0; i < 10; i++) {
                int score = (i + 1) * 1000;
                scores[i] = Integer.toString(10 - i) + ".  " + Integer.toString(score);
            }
        }
        finally {
            if (r != null) {
                r.close();
            }
        }
    }
}