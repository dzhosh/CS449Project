package com.example.reeves.umbraapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public abstract class GameObject {
    // All game objects will derive some variables based on screen size
    // All game objects will need to rotate at a set angular speed
    protected static int screen_width;
    protected static int screen_height;
    protected static double turn_speed; // in Radians per Second
    protected static boolean is_turning;
    protected static int turn_direction; // 1 for Left, -1 for Right
    protected static long frame_rate;

    protected float width;
    protected float height;
    protected float radius;
    protected float x;
    protected float y;
    protected int opacity;
    protected int color;
    protected Bitmap bitmap;

    GameObject() {
    }

    public void drawBitmap(Canvas c, Paint p) {
        p.setAlpha(opacity);
        c.drawBitmap(bitmap,x - width / 2, y - height / 2, p);
    }

    public void drawCircle(Canvas c, Paint p) {
        p.setColor(color);
        c.drawCircle(x, y, radius, p);
    }

    public void drawEndCircle(Canvas c, Paint p, int opacity) {
        p.setAlpha(opacity);
        c.drawCircle(x, y, radius, p);
    }

    public void drawEndBitmap(Canvas c, Paint p, int opacity) {
        p.setAlpha(opacity);
        c.drawBitmap(bitmap, x - width / 2, y - height / 2, p);
    }

    public void updateBitmap(Bitmap new_bitmap){
        bitmap = new_bitmap;
    }

    public void setOpacity(int new_opacity) {
        opacity = new_opacity;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setPosition(int _x, int _y) {
        x = _x;
        y = _y;
    }

    public static void setFrameRate(long _frame_rate) {
        frame_rate = _frame_rate;
    }

    public static long getFrameRate() {
        return frame_rate;
    }
}
