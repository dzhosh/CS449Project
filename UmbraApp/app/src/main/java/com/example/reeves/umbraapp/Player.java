package com.example.reeves.umbraapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Player extends GameObject {
    private double ship_size_percentage;
    private double ship_height_width_ratio;
    private float speed; // in Pixels per Second
    private float intro_speed; // in Pixels per Second
    private float health_points;
    private float max_immune_frames;
    private float immune_frames;
    private double durability;
    private float hitbox_width;
    private float hitbox_height;
    private float laser_radius;
    private float laser_draw_radius;
    private float laser_x;
    private float laser_y;
    private boolean laser_avail;
    private boolean laser_charging;
    private float laser_speed;
    private double laser_angle;

    public Player(Context c, int s_width, int s_height) {
        screen_width = s_width;
        screen_height = s_height;
        ship_size_percentage = .20;
        ship_height_width_ratio = 1;
        height = (int)(screen_height * ship_size_percentage);
        width = (int)(ship_height_width_ratio * height);
        // (x,y) at (center, just off screen).
        x = screen_width / 2;
        y = screen_height + height / 2;
        speed = 400;
        turn_speed = 1.5;
        health_points = 100;
        durability = 2.7;
        max_immune_frames = 30;
        immune_frames = 0;
        Bitmap temp = BitmapFactory.decodeResource(c.getResources(), R.drawable.ship1);
        bitmap = Bitmap.createScaledBitmap(temp, (int)width, (int)height, false);
        opacity = 0;
        is_turning = false;
        turn_direction = 1;
        hitbox_height = height / 2;
        hitbox_width = width / 12;
        laser_x = x;
        laser_y = screen_height * 3 / 5 - height / 2;
        laser_avail = false;
        laser_charging = true;
        laser_radius = 7;
        laser_draw_radius = 0;
        laser_speed = speed * 2;
        laser_angle = Math.PI / 2 * 3;
    }

    public void updateIntro(long frame_rate) {
        if (opacity < 255) {
            opacity += 255 / frame_rate / 2;
        }
        if (opacity > 255) {
            opacity = 255;
        }

        if (y > screen_height * 3 / 5) {
            // Have player accelerate until halfway to the correct position
            if (y > screen_height * 4 / 5) {
                y -= intro_speed;
                intro_speed += .3;
            }
            // After halfway to correct position, decelerate
            else {
                y -= intro_speed;
                intro_speed -= .3;
            }
        }
    }

    public boolean introConcluded() {
        if (y < screen_height * 3 / 5) {
            y = screen_height * 3 / 5;
            return true;
        }
        else {
            return false;
        }
    }

    public void update(long frame_rate) {
        if (opacity < 255) {
            opacity += 255 / frame_rate / 2;
        }
        if (opacity > 255) {
            opacity = 255;
        }

        if (laser_draw_radius < laser_radius) {
            laser_draw_radius = laser_draw_radius + laser_radius / frame_rate;
        }
        else if (laser_draw_radius > laser_radius) {
            laser_draw_radius = laser_radius;
            laser_avail = true;
            laser_charging = false;
        }

        if (!(laser_avail || laser_charging)) {
            if (is_turning) {
                double theta = turn_speed / frame_rate * turn_direction;

                // Translate to origin
                laser_x -= x;
                laser_y -= y;
                // Rotate around origin
                laser_x = (float) (laser_x * Math.cos(theta) - laser_y * Math.sin(theta));
                laser_y = (float) (laser_x * Math.sin(theta) + laser_y * Math.cos(theta));
                // Translate back to original position
                laser_x += x;
                laser_y += y;

                // Update movement angle
                laser_angle += theta;
            }

            laser_x += Math.cos(laser_angle) * laser_speed / frame_rate;
            laser_y += Math.sin(laser_angle) * laser_speed / frame_rate;

            // If laser leaves screen size, reset laser
            if ((Math.abs(laser_x) > screen_width + 1) ||
                    (Math.abs(laser_y) > screen_height + 1)) {
                resetLaser();
            }
        }

        if (immune_frames > 0) {
            immune_frames--;
        }
    }

    public boolean isImmune() {
        return (immune_frames > 0);
    }

    public void drawShield(Canvas c, Paint p) {
        // Set opacity to pulse once per hit from 0 to 100 to 0
        // Sin curve stretched to max immune frames / pi and amplified by 100
        double opacity = 100 * Math.sin(Math.PI * immune_frames / max_immune_frames);
        p.setColor(Color.argb((int) opacity, 0, 200, 255));
        c.drawCircle(x, y, (Math.max(width, height)) / 2 + 10, p);
    }

    public void drawLaser(Canvas c, Paint p) {
        p.setColor(Color.argb(opacity, 255, 255, 255));
        c.drawCircle(laser_x, laser_y, laser_draw_radius, p);
    }

    public boolean detectCollision(GameObject object) {
        // Translate object to the origin
        float translated_x = object.getX() - x;
        float translated_y = object.getY() - y;
        return (Math.abs(translated_x) - object.getWidth() / 2 <= width / 2) &&
                (Math.abs(translated_y) - object.getHeight() / 2 <= height / 2);
    }

    public boolean detectHostileCollision(GameObject object) {
        // Translate object to the origin
        float translated_x = object.getX() - x;
        float translated_y = object.getY() - y;
        return (Math.abs(translated_x) - object.getWidth() / 2 <= hitbox_width / 2) &&
                (Math.abs(translated_y) - object.getHeight() / 2 <= hitbox_height / 2);

    }

    public boolean detectLaserCollision(GameObject object) {
        if (!(laser_avail || laser_charging)) {
            // Translate object to the origin
            float translated_x = object.getX() - laser_x;
            float translated_y = object.getY() - laser_y;
            return (Math.abs(translated_x) - object.getWidth() / 2 <= laser_radius) &&
                    (Math.abs(translated_y) - object.getHeight() / 2 <= laser_radius);
        }
        else
            return false;
    }

    public void resetLaser() {
        laser_x = x;
        laser_y = y - height / 2;
        laser_draw_radius = 0;
        laser_charging = true;
        laser_angle = Math.PI * 3 / 2;
    }

    public void increaseHealth() {
        double health_gain = 33 / durability;

        if (health_points < 100 - health_gain) {
            health_points += health_gain;
        } else {
            health_points = 100;
        }
    }

    public void decreaseHealth() {
        health_points -= 100 / durability;
        immune_frames = max_immune_frames;
    }

    public boolean hasHealth() {
        return health_points > 0;
    }

    public void turnLeft() {
        is_turning = true;
        turn_direction = 1;
    }

    public void turnRight() {
        is_turning = true;
        turn_direction = -1;
    }

    public boolean canFire() {
        return laser_avail;
    }

    public void fire() {
        laser_avail = false;
    }

    public void stopTurning() {
        is_turning = false;
    }

    public float getSpeed() {
        return speed;
    }

    public float getHealth() {
        return health_points;
    }
}
