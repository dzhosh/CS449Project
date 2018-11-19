package com.example.reeves.umbraapp;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

public class GameAudioManager {
    private SoundPool soundPool;
    private MediaPlayer mediaPlayer;
    private float music_volume;
    private float sfx_volume;
    private int[] sounds;
    private boolean isPlayingBGM;

    public GameAudioManager(Context c, int m_volume, int s_volume) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .build();
        } else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        sounds = new int[5];
        sounds[0] = soundPool.load(c, R.raw.goal, 1);
        sounds[1] = soundPool.load(c, R.raw.fire, 1);
        sounds[2] = soundPool.load(c, R.raw.hit, 1);
        sounds[3] = soundPool.load(c, R.raw.end, 1);

        mediaPlayer = MediaPlayer.create(c, R.raw.space_chase);
        isPlayingBGM = false;

        music_volume = (float)m_volume / 100;
        sfx_volume = (float)s_volume / 100;
    }

    public void playSound(int soundID) {
        soundPool.play(sounds[soundID], sfx_volume, sfx_volume, 1, 0, 1f);
    }

    public void playBGM() {
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(music_volume, music_volume);
        isPlayingBGM = true;
    }

    public void stopBGM() {
        mediaPlayer.stop();
        isPlayingBGM = false;
    }

    public void pauseBGM() {
        mediaPlayer.pause();
        isPlayingBGM = false;
    }

    public void resumeBGM() {
        mediaPlayer.start();
    }

    public void clean() {
        sounds = null;
        soundPool.release();
        soundPool = null;
        mediaPlayer.stop();
    }

    public boolean isPlayingBGM() {
        return isPlayingBGM;
    }
}
