package com.example.reeves.umbraapp;

import android.graphics.Bitmap;
import android.graphics.Color;

public class HUDElement extends GameObject {
    public HUDElement(Bitmap unscaled_bitmap, float Height, float Width, float X, float Y){
        height = Height;
        width = Width;
        x = X;
        y = Y;
        bitmap = Bitmap.createScaledBitmap(unscaled_bitmap, (int)width, (int)height, false);
        opacity = 0;
        color = Color.argb(255,255,255,255);
    }
}
