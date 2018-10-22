package com.example.reeves.umbraapp;

import android.graphics.Color;

public class Star extends GameObject {

    public Star() {
        x = (float)Math.random() * (screen_width + 1);
        y = (float)Math.random() * (screen_height + 1);
        radius = (float)Math.random() * 3;
        height = radius * 2;
        width = radius * 2;
        color = Color.argb(opacity, 255, 255, 255);
        opacity = 0;
    }

    public void updateIntro(long frame_rate) {
        if (opacity < 150) {
            opacity += 150 / frame_rate;
            color = Color.argb(opacity, 255, 255, 255);
        }
        if (opacity > 150) {
            opacity = 150;
            color = Color.argb(opacity, 255, 255, 255);
        }
    }

    public void update(long frame_rate, Player p) {
        // Find new theta for stars (amount rotated in this frame)
        double theta = turn_speed / frame_rate * turn_direction / 8;

        // If player is turning, rotate around the point (player_x, player_y)
        if (is_turning) {
            // Translate to origin
            x -= p.getX();
            y -= p.getY();
            // Rotate around origin
            x = (float)(x * Math.cos(theta) - y * Math.sin(theta));
            y = (float)(x * Math.sin(theta) + y * Math.cos(theta));
            // Translate back to original position
            x += p.getX();
            y += p.getY();
        }
        // Find delta (distance traveled by stars relative to player this frame)
        double delta = p.getSpeed() / frame_rate / 8;
        y += delta;

        // If a star leaves visible area, update its position to the other side
        // This gives an illusion of a looping background
        if (y > screen_height + 2)
            y = -1;
        if (y < -2)
            y = screen_height + 1;
        if (x > screen_width + 2)
            x = -1;
        if (x < -2)
            x = screen_width + 1;
    }
}
