package com.example.reeves.umbraapp;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class HUD {
    private int health_width;
    private int health_height;
    private float health_display;
    private int action_width;
    private int action_height;
    private int opacity;

    private Bitmap health_unscaled;
    private Bitmap action_unscaled;

    private HUDElement top;
    private HUDElement health_bar;
    private HUDElement action_bar;
    private HUDElement left_action;
    private HUDElement right_action;

    public HUD(Context c, int screen_height, int screen_width) {
        health_height = screen_height / 10;
        health_width = (int)(9.6 * health_height);
        health_display = 100;
        action_height = screen_height / 5;
        action_width = screen_height / 5;
        opacity = 0;

        Bitmap top_unscaled = BitmapFactory.decodeResource(c.getResources(), R.drawable.hud_top_foreground);
        int hud_x = screen_width / 2;
        int hud_y = health_height / 2;
        top = new HUDElement(top_unscaled, health_height, health_width, hud_x, hud_y);

        health_unscaled = BitmapFactory.decodeResource(c.getResources(), R.drawable.hud_healthbar);
        health_bar = new HUDElement(health_unscaled, health_height, (int)(health_width * .8), hud_x, hud_y);

        action_unscaled = BitmapFactory.decodeResource(c.getResources(), R.drawable.hud_actionbar);
        action_bar = new HUDElement(action_unscaled, health_height, (int)(health_width * .8), hud_x, hud_y);

        Bitmap left_unscaled = BitmapFactory.decodeResource(c.getResources(), R.drawable.control_arrow_left);
        int left_x = screen_width / 10;
        int action_y = screen_height / 2;
        left_action = new HUDElement(left_unscaled, action_height, action_width, left_x, action_y);

        Bitmap right_unscaled = BitmapFactory.decodeResource(c.getResources(), R.drawable.control_arrow_right);
        int right_x = screen_width / 10 * 9;
        right_action = new HUDElement(right_unscaled, action_height, action_width, right_x, action_y);
    }

    public void drawControls(Canvas c, Paint p) {
        if (GameObject.is_turning)  {
            if (GameObject.turn_direction == 1) {
                left_action.setOpacity(opacity - 100);
                right_action.setOpacity(opacity);
            }
            else {
                right_action.setOpacity(opacity - 100);
                left_action.setOpacity(opacity);
            }
        }
        else {
            right_action.setOpacity(opacity);
            left_action.setOpacity(opacity);
        }
        left_action.drawBitmap(c, p);
        right_action.drawBitmap(c, p);
    }

    public void drawHealthBar(Canvas c, Paint p) {
        health_bar.drawBitmap(c, p);
        action_bar.drawBitmap(c, p);
        top.drawBitmap(c, p);
    }

    public void drawScore(Canvas c, Paint p, long score, float screen_width) {
        p.setTextSize(100);
        p.setColor(Color.argb(opacity, 255, 255, 255));
        float text_width = p.measureText("Score: " + score);
        int y = (int)(top.getY() + top.getHeight()) + 20;
        c.drawText("Score: " + score, (screen_width - text_width) / 2, y, p);
    }

    public void updateHealthBar(float health_points, long frame_rate) {
        // Use a displayed health instead of instantly showing actual health
        if (health_points < health_display) {
            health_display--;

            // If displayed health is now lower than actual health, update them to equal
            if (health_display < health_points) {
                health_display = health_points;
            }
        } else if (health_points > health_display) {
            health_display++;

            // If displayed health is now greater than actual health, update them to equal
            if (health_display > health_points) {
                health_display = health_points;
            }
        }
        // Update Health Bar (width now is health points/100 * health width)
        health_bar.updateBitmap(Bitmap.createScaledBitmap(health_unscaled, (int)(health_width * .8 * health_display / 100), health_height, false));

        if (opacity < 255) {
            opacity += 255 / frame_rate;
            updateOpacities();
        }
        if (opacity > 255) {
            opacity = 255;
            updateOpacities();
        }
    }

    public void updateOpacities() {
        left_action.setOpacity(opacity);
        right_action.setOpacity(opacity);
        top.setOpacity(opacity);
        health_bar.setOpacity(opacity);
        action_bar.setOpacity(opacity);
    }
}
