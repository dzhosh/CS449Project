package com.example.reeves.umbraapp;

import android.graphics.Color;

public class Hostile extends GameObject {
    private double movement_angle;
    private double speed;

    public Hostile(Player p) {
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
        movement_angle = Math.random() * Math.PI + Math.PI;
        opacity = 255;
        color = Color.argb(opacity, 255, 0, 0);

        // Speed Based on Size
        // Go back to random variable between [0,1]
        speed = (double)(radius - 10) / 240;
        // Invert the random variable so it is now [1,0]
        speed = Math.abs(1 - speed);
        speed = 1.4 * p.getSpeed() * speed + .1 * p.getSpeed();
    }

    public void update(long frame_rate, Player p) {
        if (is_turning) {
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

            // Update movement angle
            movement_angle += theta;
        }

        double delta_x = Math.cos(movement_angle) * speed / frame_rate;
        double delta_y = Math.sin(movement_angle) * speed / frame_rate + p.getSpeed() / frame_rate;

        x += delta_x;
        y += delta_y;

        // If hostile leaves screen size * 2, update its position to the other side
        if (Math.abs(x) > screen_width * 2 + 1) {
            x *= -1;
        }
        if (Math.abs(y) > screen_height * 2 + 1) {
            y *= -1;
        }
    }
}
