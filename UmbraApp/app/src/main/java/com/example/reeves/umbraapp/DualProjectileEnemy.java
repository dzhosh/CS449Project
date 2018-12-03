package com.example.reeves.umbraapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class DualProjectileEnemy extends Enemy {
    DualProjectileEnemy(Context c) {
        super(c);
        width = screen_height / 4;
        height = screen_height / 4;
        radius = width / 2;
        unscaled_bitmap = BitmapFactory.decodeResource(c.getResources(), R.drawable.enemy2);
        bitmap = Bitmap.createScaledBitmap(unscaled_bitmap, (int)width, (int)height, false);
        projectiles_per_shot = 2;
        projectile_count = 16;
        projectiles = new Projectile[projectile_count];
        charging_projectiles = new Projectile[projectiles_per_shot];
        fired_this_beat = false;
        angle = 0;
        next_projectile = 0;
        current_projectiles = 0;
        projectiles_placed = false;
        for (int i = 0; i < projectiles_per_shot; i++) {
            charging_projectiles[i] = new Projectile(x, y, 0, 0, 0, 75);
        }
    }

    DualProjectileEnemy(Context c, float _radius) {
        super(c);
        radius = _radius;
        projectiles_per_shot = 2;
        projectile_count = 16;
        projectiles = new Projectile[projectile_count];
        charging_projectiles = new Projectile[projectiles_per_shot];
        fired_this_beat = false;
        angle = 0;
        next_projectile = 0;
        current_projectiles = 0;
        projectiles_placed = false;
    }

    @Override
    public void updateProjectiles(Player p) {
        if (beat == 0) {
            if (!fired_this_beat) {
                if (projectiles_placed) {
                    fireNewProjectiles();
                    projectiles_placed = false;
                }
            }
        }
        else if (beat == 1) {
            fired_this_beat = false;
            rotate(Math.PI / 2.0);
        }
        else {
            buildNewProjectiles();
        }

        for (int i = 0; i < current_projectiles; i++) {
            projectiles[i].update(p);
        }
        for (int i = 0; i < projectiles_per_shot; i++) {
            charging_projectiles[i].update(p);
        }
    }

    private void buildNewProjectiles() {
        if (!projectiles_placed) {
            int speed = 100;
            int damage = 75;

            float top_x = (float) (x + radius * Math.cos(angle));
            float top_y = (float) (y - radius * Math.sin(angle));
            float bottom_x = (float) (x - radius * Math.cos(angle));
            float bottom_y = (float) (y + radius * Math.sin(angle));

            charging_projectiles[0] = new Projectile(top_x, top_y, 0, speed, angle, damage);
            charging_projectiles[1] = new Projectile(bottom_x, bottom_y, 0, speed, angle + Math.PI, damage);
            projectiles_placed = true;
        }
        else {
            for (int i = 0; i < projectiles_per_shot; i++) {
                charging_projectiles[i].increaseRadius(126.0 / 30, (int)radius);
            }
        }
    }

}
