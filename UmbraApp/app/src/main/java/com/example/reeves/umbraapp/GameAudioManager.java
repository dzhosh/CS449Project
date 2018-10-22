package com.example.reeves.umbraapp;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

public class GameAudioManager {
    private SoundPool soundPool;
    private MediaPlayer mediaPlayer;
    private int[] sounds;
    private boolean isPlayingBGM;

    public GameAudioManager(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .build();
        } else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        sounds = new int[5];

        mediaPlayer = MediaPlayer.create(c, R.raw.space_chase);
        isPlayingBGM = false;
    }

    public void playSound(int soundID) {
        soundPool.play(sounds[soundID], 1, 1, 1, 0, 1f);
    }

    public void playBGM() {
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
        isPlayingBGM = true;
    }

    public void stopBGM() {
        mediaPlayer.stop();
        isPlayingBGM = false;
    }

    public void clean() {
        sounds = null;
        soundPool.release();
        soundPool = null;
    }

    public boolean isPlayingBGM() {
        return isPlayingBGM;
    }
}
