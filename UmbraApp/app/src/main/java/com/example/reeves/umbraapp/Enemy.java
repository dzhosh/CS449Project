package com.example.reeves.umbraapp;

import android.graphics.Color;

public class Enemy extends GameObject{

    public Enemy(Player p) {
        // Randomly Place Offscreen
        x = (float)(Math.random() * screen_width + screen_width);
        y = (float)(Math.random() * screen_height + screen_height);
        // Randomize 50% chance of negative or positive for x and y
        if (Math.random() < .5) {
            x *= -1;
        }
        if (Math.random() < .5) {
            y *= -1;
        }

        radius = (int)(Math.random() * 240 + 10);
        width = radius * 2;
        height = radius * 2;
        opacity = 255;
        color = Color.argb(opacity, 255, 0, 0);
    }
}
