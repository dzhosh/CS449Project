package com.example.reeves.umbraapp;

import android.graphics.Color;

public class Director extends GameObject {
    private double angle;
    private float distance;

    public Director(int player_width, int player_height) {
        angle = Math.PI / 2;
        distance = Math.max(player_width, player_height) / 2 + 20;
        width = 32;
        height = 32;
        radius = 16;
        opacity = 255;
        color = Color.argb(opacity, 0, 200, 255);
    }

    public void update(Goal g, Player p) {
        // Director will point towards the goal
        angle = g.findAngle(p);
        x = (float) (java.lang.Math.cos(angle) * distance) + p.getX();
        y = (float) (java.lang.Math.sin(angle) * distance) + p.getY();
    }
}