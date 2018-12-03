package com.example.reeves.umbraapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class QuadProjectileEnemy extends Enemy {

    QuadProjectileEnemy(Context c) {
        super(c);
        width = screen_height / 4;
        height = screen_height / 4;
        radius = width / 2;
        unscaled_bitmap = BitmapFactory.decodeResource(c.getResources(), R.drawable.enemy1);
        bitmap = Bitmap.createScaledBitmap(unscaled_bitmap, (int)width, (int)height, false);
        projectiles_per_shot = 4;
        projectile_count = 16;
        projectiles = new Projectile[projectile_count];
        charging_projectiles = new Projectile[projectiles_per_shot];
        fired_this_beat = false;
        angle = 0;
        next_projectile = 0;
        current_projectiles = 0;
        projectiles_placed = false;
        for (int i = 0; i < projectiles_per_shot; i++) {
            charging_projectiles[i] = new Projectile(x, y, 0, 0, 0, 50);
        }
    }

    QuadProjectileEnemy(Context c, float _radius) {
        super(c);
        radius = _radius;
        projectiles_per_shot = 4;
        projectile_count = 8;
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
            rotate(Math.PI / 4.0);
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
            int speed = 400;
            int damage = 50;

            double angle_from_origin = angle % (Math.PI / 2);
            float quad1_x = (float) (x + radius * Math.cos(angle_from_origin));
            float quad1_y = (float) (y - radius * Math.sin(angle_from_origin));
            float quad2_x = (float) (x - radius * Math.sin(angle_from_origin));
            float quad2_y = (float) (y - radius * Math.cos(angle_from_origin));
            float quad3_x = (float) (x - radius * Math.cos(angle_from_origin));
            float quad3_y = (float) (y + radius * Math.sin(angle_from_origin));
            float quad4_x = (float) (x + radius * Math.sin(angle_from_origin));
            float quad4_y = (float) (y + radius * Math.cos(angle_from_origin));

            charging_projectiles[0] = new Projectile(quad1_x, quad1_y, 0, speed, angle_from_origin, damage);
            charging_projectiles[1] = new Projectile(quad2_x, quad2_y, 0, speed, angle_from_origin + Math.PI / 2, damage);
            charging_projectiles[2] = new Projectile(quad3_x, quad3_y, 0, speed, angle_from_origin + Math.PI, damage);
            charging_projectiles[3] = new Projectile(quad4_x, quad4_y, 0, speed, angle_from_origin - Math.PI / 2, damage);
            projectiles_placed = true;
        }
        else {
            for (int i = 0; i < projectiles_per_shot; i++) {
                charging_projectiles[i].increaseRadius(126.0 / 30, (int)(radius / 2));
            }
        }
    }
}
