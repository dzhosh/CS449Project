package com.example.reeves.umbraapp;

import android.graphics.Color;

public class Goal extends GameObject {
    private int score;
    private int max_distance;
    private int min_distance;
    private float intro_speed;

    public Goal(){
        x = screen_width / 2;
        y = screen_height / 2;
        radius = 20;
        height = 40;
        width = 40;
        opacity = 0;
        intro_speed = 1;
        max_distance = 4000;
        min_distance = 800;
        color = Color.argb(opacity, 200, 200, 0);
    }

    public void updateIntro(float current_time, long frame_rate) {
        if (opacity < 255) {
            opacity += 255 / frame_rate;
            color = Color.argb(opacity, 200, 200, 0);
        }
        if (opacity > 255) {
            opacity = 255;
            color = Color.argb(opacity, 200, 200, 0);
        }

        if (current_time > 2) {
            // If goal is off screen, move to starting position
            if (y < height * -1) {
                y = max_distance * -1;
            }
            // Else, accelerate upwards
            else {
                y -= intro_speed;
                intro_speed++;
            }
        }
    }

    public void update(long frame_rate, Player p) {
        if (is_turning) {
            // Rotate goal around (player_x,player_y)
            double theta = turn_speed / frame_rate * turn_direction;
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
        // For every frame, move goal downwards by (player speed/frame rate)
        double delta = p.getSpeed() / frame_rate;
        y += delta;
    }

    public void makeNewGoal(float player_x, float player_y) {
        x = (float)(Math.random() * (max_distance - min_distance) + min_distance) + player_x;
        y = (float) (Math.random() * (max_distance - min_distance) + min_distance) + player_y;
        // Randomize 50% chance of negative or positive for x and y
        if (Math.random() < .5) {
            x *= -1;
        }
        if (Math.random() < .5) {
            y *= -1;
        }
    }

    public double findAngle(Player p) {
        double angleX = x - p.getX();
        double angleY = y - p.getY();
        return Math.atan2(angleY, angleX);
    }
}
