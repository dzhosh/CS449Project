package com.example.reeves.umbraapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

public class Enemy extends GameObject{
    protected static int beat;
    protected int next_projectile;
    protected int current_projectiles;
    protected int projectiles_per_shot;
    protected int projectile_count;
    protected boolean fired_this_beat;
    protected Projectile projectiles[];
    protected Projectile charging_projectiles[];
    protected boolean projectiles_placed;
    protected double angle;
    protected Matrix matrix;
    protected Bitmap unscaled_bitmap;


    public Enemy(Context c) {
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

        matrix = new Matrix();

        opacity = 255;
        color = Color.argb(opacity, 255, 0, 0);
    }

    public static void increaseBeat() {
        // Beat goes with music, 4 beats per measure
        beat = (beat + 1) % 4;
    }

    public void drawProjectiles(Canvas canvas, Paint paint) {
        for (int i = 0; i < current_projectiles; i++) {
            projectiles[i].drawCircle(canvas, paint);
        }
        for (int i = 0; i < Math.min(current_projectiles, projectiles_per_shot); i++) {
            charging_projectiles[i].drawCircle(canvas, paint);
        }
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

            angle -= theta;
        }

        y = y + p.getSpeed() / frame_rate;

        // If hostile leaves screen size, update its position to the other side
        if (Math.abs(x) > screen_width * 2 + 1) {
            x *= -1;
        }
        if (Math.abs(y) > screen_height * 2 + 1) {
            y *= -1;
        }

        updateProjectiles(p);
    }

    public void updateProjectiles(Player p) {
        for (int i = 0; i < projectile_count; i++) {
            projectiles[i].update(p);
        }
    }

    protected void rotate(double amount) {
        angle += amount / frame_rate * 126.0 / 60.0;
        angle = angle % (Math.PI * 2);
    }

    protected void fireNewProjectiles() {
        for (int i = 0; i < projectiles_per_shot; i++) {
            projectiles[next_projectile + i] = charging_projectiles[i];
            projectiles[next_projectile + i].fire();
            charging_projectiles[i] = new Projectile(x, y, 0, 0, 0, 0);
        }
        fired_this_beat = true;
        next_projectile = (next_projectile + projectiles_per_shot) % projectile_count;
        current_projectiles = Math.min(current_projectiles + projectiles_per_shot, projectile_count);
    }

    @Override
    public void drawBitmap(Canvas canvas, Paint paint) {
        canvas.save();
        float degrees = (float)(angle * 180 / Math.PI);
        canvas.rotate(-degrees, x, y);
        canvas.drawBitmap(bitmap,x - width / 2, y - height / 2, paint);
        canvas.restore();
    }
}
