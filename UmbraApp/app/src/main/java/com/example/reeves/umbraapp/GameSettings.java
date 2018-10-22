package com.example.reeves.umbraapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GameSettings extends Activity implements View.OnClickListener {

    private int difficultySelector;
    private String[] difficulties;

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

        setContentView(R.layout.activity_game_settings);

        TextView title = findViewById(R.id.title);
        title.setText("Settings");

        TextView volume_title = findViewById(R.id.volume_title);
        volume_title.setText("Volume");

        TextView music_text = findViewById(R.id.music_text);
        music_text.setText("Music:");

        TextView sound_effects_text = findViewById(R.id.sound_effects_text);
        sound_effects_text.setText("SFX:");

        TextView difficulty_title = findViewById(R.id.difficulty_text);
        difficulty_title.setText("Difficulty");

        TextView difficulty_selected_text = findViewById(R.id.difficulty_selected_text);
        difficulty_selected_text.setText("Normal");
        difficultySelector = 2;
        difficulties = new String[]{"Very Easy","Easy","Normal","Hard","Very Hard"};

        Button left_button = findViewById(R.id.button_left);
        left_button.setText("<<");
        left_button.setOnClickListener(this);

        Button right_button = findViewById(R.id.button_right);
        right_button.setText(">>");
        right_button.setOnClickListener(this);

        Button save_button = findViewById(R.id.save_button);
        save_button.setText("Save Changes");
        save_button.setOnClickListener(this);

        Button discard_button = findViewById(R.id.discard_button);
        discard_button.setText("Discard Changes");
        discard_button.setOnClickListener(this);
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
            case R.id.button_left:
                decreaseDifficulty();
                break;
            case R.id.button_right:
                increaseDifficulty();
                break;
            case R.id.save_button:
                finish();
                break;
            case R.id.discard_button:
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

    public void increaseDifficulty() {
        difficultySelector++;
        String new_difficulty_text = difficulties[difficultySelector % 5];
        TextView difficulty_selected_text = findViewById(R.id.difficulty_selected_text);
        difficulty_selected_text.setText(new_difficulty_text);
    }

    public void decreaseDifficulty() {
        difficultySelector--;
        String new_difficulty_text = difficulties[difficultySelector % 5];
        TextView difficulty_selected_text = findViewById(R.id.difficulty_selected_text);
        difficulty_selected_text.setText(new_difficulty_text);
    }
}