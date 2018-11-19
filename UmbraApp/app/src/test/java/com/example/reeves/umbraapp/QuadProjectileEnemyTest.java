package com.example.reeves.umbraapp;

import android.content.Context;

import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;

public class QuadProjectileEnemyTest {

    private QuadProjectileEnemy test_enemy;
    @Mock
    private Player test_player;
    @Mock
    Context context;

    @Test
    public void updateProjectiles() {
        test_enemy = new QuadProjectileEnemy(context, 100);
        test_enemy.setPosition(0, 0);
        // Beat 0 Fires
        // Beat 1 Rotates
        // Beat 2&3 Build New Projectiles
        Enemy.increaseBeat();
        Enemy.increaseBeat();
        test_enemy.updateProjectiles(test_player);

        assert (test_enemy.charging_projectiles[0].getX() == 0);
        assert (test_enemy.charging_projectiles[0].getY() == -100);
    }
}