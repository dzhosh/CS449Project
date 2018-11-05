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

public class GameSettings extends Activity implements View.OnClickListener {

    private int difficultySelector;
    private int music_volume_value;
    private int sfx_volume_value;
    private String[] difficulties;
    private static final String file_name = "settings.txt";

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
        try {
            loadSettings();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

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
        difficulties = new String[]{"Very Easy","Easy","Normal","Hard","Very Hard"};
        difficulty_selected_text.setText(difficulties[difficultySelector]);

        SeekBar music_volume = findViewById(R.id.music_volume);
        music_volume.setMax(100);
        music_volume.setProgress(music_volume_value);

        SeekBar sfx_volume = findViewById(R.id.sfx_volume);
        sfx_volume.setMax(100);
        sfx_volume.setProgress(sfx_volume_value);

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
                try {
                    saveSettings();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
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

    public void loadSettings()
    throws IOException {
        int settings_array[] = new int[3];

        BufferedReader r = null;
        try {
            InputStream in = openFileInput(file_name);
            r = new BufferedReader(new InputStreamReader(in));
            for (int i = 0; i < 3; i++) {
                String line = r.readLine();
                settings_array[i] = Integer.parseInt(line);
            }
        }
        catch (FileNotFoundException e) {
            settings_array[0] = 2;
            settings_array[1] = 100;
            settings_array[2] = 100;
        }
        finally {
            if (r != null) {
                r.close();
            }
        }

        difficultySelector = settings_array[0];
        sfx_volume_value = settings_array[1];
        music_volume_value = settings_array[2];
    }

    public void saveSettings()
    throws IOException{
        SeekBar sfx_volume = findViewById(R.id.sfx_volume);
        SeekBar music_volume = findViewById(R.id.music_volume);

        Writer w = null;
        try {
            OutputStream out = openFileOutput(file_name, MODE_PRIVATE);
            w = new OutputStreamWriter(out);
            w.write(Integer.toString(difficultySelector));
            w.write('\n');
            w.write(Integer.toString(sfx_volume.getProgress()));
            w.write('\n');
            w.write(Integer.toString(music_volume.getProgress()));
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }
}