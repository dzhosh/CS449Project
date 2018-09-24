package com.example.reeves.umbraapp;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class GameViewTest {

    @Mock
    Context mockContext;

    GameView gameView = new GameView(mockContext, true);

    @Test
    public void updateGoalPosition() {
        // PLAYER TURNING
        gameView.setRunning(false);
        gameView.setFrameRate(1);
        gameView.setTurning(true);
        gameView.setGoalCenter(100, 0);
        double delta = 0.1;

        // After 1 frame update, goal should have rotated player turn speed radians and fallen player speed pixels
        double expected_radians = gameView.getPlayerTurnSpeed();
        // Find Expected X and Y values
        float expected_x = 100 * (float) java.lang.Math.cos(expected_radians);
        float expected_y = 100 * (float) java.lang.Math.sin(expected_radians);
        expected_y -= gameView.getPlayerSpeed();

        gameView.updateGoalPosition();
        float actual_x = gameView.getGoalX();
        float actual_y = gameView.getGoalY();

        assertEquals(expected_x, actual_x, delta);
        assertEquals(expected_y, actual_y, delta);

        // PLAYER STATIONARY
        gameView.setPlayerSpeed(10);
        gameView.setGoalCenter(0, 100);
        gameView.setTurning(false);
        for (int i = 0; i < 10; i++) {
            gameView.updateGoalPosition();
        }
        expected_y = 0;
        actual_y = gameView.getGoalY();

        assertEquals(expected_y, actual_y, delta);
    }

    @Test
    public void detectCollision() {
        gameView.setRunning(false);
        gameView.setGoalCenter(40, 0);

        float x1 = 0;
        float y1 = 0;
        float h1 = 10;
        float w1 = 10;
        float x2 = 10;
        float y2 = 0;
        float h2 = 5;
        float w2 = 5;

        assertFalse(gameView.detectCollision(x1, y1, w1, h1, x2, y2, w2, h2));

        w2 = 30;
        assertTrue(gameView.detectCollision(x1, y1, w1, h1, x2, y2, w2, h2));

        assertTrue(gameView.detectCollision(0, 0, gameView.getPlayerWidth(), gameView.getPlayerHeight(),
                gameView.getGoalX(), gameView.getGoalY(), gameView.getGoalDiameter(), gameView.getGoalDiameter()));

        gameView.setGoalCenter(100, 100);
        assertFalse(gameView.detectCollision(0, 0, gameView.getPlayerWidth(), gameView.getPlayerHeight(),
                gameView.getGoalX(), gameView.getGoalY(), gameView.getGoalDiameter(), gameView.getGoalDiameter()));
    }

    @Test
    public void updateDirector() {
        gameView.setRunning(false);
        gameView.setGoalCenter(1, 0);
        double expected = 0;
        double delta = 0.1;

        gameView.updateDirector();
        double actual = gameView.getDirectorAngle();
        assertEquals(expected, actual, delta);

        gameView.setGoalCenter(0, 1);
        expected = java.lang.Math.PI / 2;
        delta = 0.1;

        gameView.updateDirector();
        actual = gameView.getDirectorAngle();
        assertEquals(expected, actual, delta);
    }
}