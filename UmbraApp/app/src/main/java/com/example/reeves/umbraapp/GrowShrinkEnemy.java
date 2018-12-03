package com.example.reeves.umbraapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

public class GrowShrinkEnemy extends Enemy {
    private float max_radius;
    private float min_radius;

    GrowShrinkEnemy(Context c) {
        super(c);
        width = screen_height / 8;
        height = screen_height / 8;
        radius = screen_height / 8;
        max_radius = radius;
        min_radius = radius / 4;
        projectile_count = 1;
        projectiles = new Projectile[projectile_count];
        angle = Math.random() * 2 * Math.PI;
        projectiles[0] = new Projectile(x, y, (int)max_radius, 200, angle, 100);
        projectiles[0].fire();
        current_projectiles = 1;
    }

    GrowShrinkEnemy(Context c, float _radius) {
        super(c);
        radius = _radius;
        projectile_count = 1;
        projectiles = new Projectile[projectile_count];
        angle = 0;
        next_projectile = 0;
        current_projectiles = 0;
        projectiles_placed = false;
    }

    @Override
    public void updateProjectiles(Player p) {
        if (beat <= 1) {
            projectiles[0].decreaseRadius(126.0 / 120, (int)min_radius);
        }
        else {
            projectiles[0].increaseRadius(126.0 / 120, (int)max_radius);
        }

        for (int i = 0; i < current_projectiles; i++) {
            projectiles[i].update(p);
        }

        // If projectile leaves screen size, update its position to the other side
        if (Math.abs(projectiles[0].x) > screen_width * 2 + 1) {
            projectiles[0].x *= -1;
        }
        if (Math.abs(projectiles[0].y) > screen_height * 2 + 1) {
            projectiles[0].y *= -1;
        }

        x = projectiles[0].x;
        y = projectiles[0].y;
        radius = projectiles[0].radius;
    }

    @Override
    public void drawBitmap(Canvas canvas, Paint paint) {
    }

    @Override
    public void drawEndBitmap(Canvas canvas, Paint paint, int opacity) {
    }
}
