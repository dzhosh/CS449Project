package com.example.reeves.umbraapp;

import android.graphics.Color;

public class Projectile extends GameObject {
    private double movement_angle;
    private double speed;
    private boolean fired;
    private int damage;

    public Projectile(float _x, float _y, int _radius, double _speed, double _angle, int _damage) {
        x = _x;
        y = _y;
        radius = _radius;
        damage = _damage;
        width = radius * 2;
        height = radius * 2;
        opacity = 255;
        color = Color.argb(opacity, 255, 0, 0);
        speed = _speed;
        movement_angle = _angle;
        fired = false;
    }

    public void update(Player p) {
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
            movement_angle -= theta;
        }

        double delta_x;
        double delta_y;

        if (fired) {
            delta_x = Math.cos(movement_angle) * speed / frame_rate;
            delta_y = -Math.sin(movement_angle) * speed / frame_rate + p.getSpeed() / frame_rate;
        }
        else {
            delta_x = 0;
            delta_y = p.getSpeed() / frame_rate;
        }

        x += delta_x;
        y += delta_y;
    }

    public void increaseRadius(double beats_per_second, int new_radius) {
        radius = (float)(radius + new_radius / frame_rate / beats_per_second);
    }

    public void decreaseRadius(double seconds_per_beat, int new_radius) {
        radius = (float)(radius - new_radius / seconds_per_beat / frame_rate);
    }

    public void fire() {
        fired = true;
    }

    public int getDamage() {
        return damage;
    }

    public void destroyProjectile() {
        // Set all vars to 0
        x = 0;
        y = 0;
        radius = 0;
        damage = 0;
        speed = 0;
    }
}
