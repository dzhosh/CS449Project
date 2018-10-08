/*
Referenced:
Basic Structure of GameView (Frame Rate Calculation, Draw, Resume, and Pause Methods)
        http://gamecodeschool.com/
*/

package com.example.reeves.umbraapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {
    // Player Variables
    private Bitmap player_bitmap_scaled;
    private float player_speed;
    private double player_turn_speed;
    private float player_health_points;
    private float player_max_immune_frames;
    private float player_immune_frames;
    private float player_durability;
    private int player_width;
    private int player_height;
    private float player_x;
    private float player_y;
    private boolean is_turning;
    // 1 for Left, -1 for Right
    private int turn_left_right;

    // Goal Variables
    private float goal_center_x;
    private float goal_center_y;
    private int goal_score;
    private float goal_radius;
    private float goal_max_distance;
    private float goal_min_distance;

    // Director Variables
    private double director_angle;
    private float director_x;
    private float director_y;
    private float director_radius;

    // Background Variables
    private double[] star_center_x;
    private double[] star_center_y;
    private float[] star_radius;

    // Hostile Variables
    int max_hostile_amount;
    int min_hostile_amount;
    int current_hostile_amount;
    private double[] hostile_x;
    private double[] hostile_y;
    private double[] hostile_movement_angle;
    private float[] hostile_size;

    // Game Variables
    private long frame_rate;
    private long score;
    private boolean introRunning;
    private boolean running;
    float screen_width;
    float screen_height;
    float fade_time;

    // Intro Variables
    int star_opacity;
    int goal_opacity;
    int player_opacity;
    int hud_opacity;
    float current_time;
    long start_time;
    float goal_intro_x;
    float goal_intro_y;
    float goal_intro_speed;
    float player_intro_speed;

    // HUD Variables
    private int hud_height;
    private int hud_width;
    private Bitmap hud_top_foreground_scaled;
    private Bitmap hud_healthbar;
    private Bitmap hud_healthbar_scaled;
    private float health_display;
    private Bitmap hud_actionbar;
    private Bitmap hud_actionbar_scaled;

    // Engine Variables
    SurfaceHolder holder;
    Thread gameThread = null;
    Canvas canvas;
    Paint paint;

    public GameView(Context c, boolean is_test, Display display){
        super(c);

        // Get current holder and create Paint for Drawing to Screen
        holder = getHolder();
        paint = new Paint();

        // Initialize All Necessary Variables
        try {
            Point size = new Point();
            display.getRealSize(size);
            screen_width = size.x;
            screen_height = size.y;
        } catch (NoSuchMethodError e) {
            screen_width = this.getResources().getDisplayMetrics().widthPixels;
            screen_height = this.getResources().getDisplayMetrics().heightPixels;
        }

        // Player Variables
        Bitmap player_bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ship1);
        player_speed = 300; // In Pixels per Second
        player_turn_speed = 1.5; // In Radians per Second
        player_health_points = 100;
        player_durability = (float)2.7;
        player_max_immune_frames = 30;
        player_immune_frames = 1;
        player_width = 240;
        player_height = 240;
        if (is_test) {
            player_x = 0;
            player_y = 0;
        }
        else {
            // Player (x,y) at (center, just off screen).
            player_x = screen_width / 2;
            player_y = screen_height;
        }
        is_turning = false;
        turn_left_right = 1;
        player_bitmap_scaled = Bitmap.createScaledBitmap(player_bitmap, player_width, player_height, false);

        // Goal Variables
        goal_max_distance = player_speed * 10;
        goal_min_distance = player_speed * 2;
        goal_score = 100;
        goal_radius = 20;
        // Goal placed randomly between goal_min_distance and goal_max_distance
        goal_center_x = 0;
        goal_center_y = goal_max_distance;

        // Director Variables
        // Director points towards the goal at all times using the angle found by arctan(goal_y,goal_x)
        director_angle = java.lang.Math.atan2(goal_center_y,goal_center_x);
        director_x = (float)java.lang.Math.sin(director_angle) * 100;
        director_y = (float)java.lang.Math.sin(director_angle) * 100;
        director_radius = 16;

        // Background Variables
        // 50 Stars that will rotate and move across screen with player movement
        star_center_x = new double[50];
        star_center_y = new double[50];
        star_radius = new float[50];
        if (is_test) {
            // For test have controlled position for stars
            for (int i = 0; i < 50; i++) {
                star_center_x[i] = i;
                star_center_y[i] = i;
                star_radius[i] = 1;
            }
        }
        else {
            for (int i = 0; i < 50; i++) {
                // Randomly place them around the screen with random radii between 0 and 3
                star_center_x[i] = Math.random() * (screen_width + 1);
                star_center_y[i] = Math.random() * (screen_height + 1);
                star_radius[i] = (float) (Math.random() * (3));
            }
        }

        // Hostile Variables
        // Speed is based on the size of the hostile
        min_hostile_amount = 20;
        max_hostile_amount = 70;
        current_hostile_amount = min_hostile_amount;
        hostile_x = new double[max_hostile_amount];
        hostile_y = new double[max_hostile_amount];
        hostile_movement_angle = new double[max_hostile_amount];
        hostile_size = new float[max_hostile_amount];
        if (!is_test){
            for (int i = 0; i < min_hostile_amount; i++) {
                // Randomly place hostiles offscreen
                // Size is random between 10 and player_height + 10
                // Movement angle is random between 0 and 2 * PI
                hostile_x[i] = Math.random() * (screen_width * 4) - screen_width * 2;
                hostile_y[i] = Math.random() * (screen_height * 4) - screen_height * 2;
                hostile_size[i] = (float) Math.random() * player_height + 10;
                hostile_movement_angle[i] = Math.random() * 2 * Math.PI;
            }
        }
        else {
            // For test have controlled position for hostiles
            for (int i = 0; i < max_hostile_amount; i++) {
                hostile_x[i] = i * 10;
                hostile_y[i] = i * 10;
                hostile_size[i] = 10;
                hostile_movement_angle[i] = 0;
            }
        }

        // HUD Variables
        hud_height = (int)screen_height / 10;
        hud_width = (int)(9.6 * hud_height);
        // hud_top_background = BitmapFactory.decodeResource(this.getResources(), R.drawable.hud_top_background);
       //hud_top_background_scaled = Bitmap.createScaledBitmap(hud_top_background, (int)screen_width, (int)screen_height/8, false);
        hud_actionbar = BitmapFactory.decodeResource(this.getResources(), R.drawable.hud_actionbar);
        hud_actionbar_scaled = Bitmap.createScaledBitmap(hud_actionbar, (int)(hud_width * .8), hud_height, false);
        hud_healthbar = BitmapFactory.decodeResource(this.getResources(), R.drawable.hud_healthbar);
        hud_healthbar_scaled = Bitmap.createScaledBitmap(hud_healthbar, (int)(hud_width * .8), hud_height, false);
        Bitmap hud_top_foreground = BitmapFactory.decodeResource(this.getResources(), R.drawable.hud_top_foreground);
        hud_top_foreground_scaled = Bitmap.createScaledBitmap(hud_top_foreground, hud_width, hud_height, false);
        health_display = 100;

        // Game Variables
        score = 0;
        introRunning = true;
        running = false;
        frame_rate = 30;
        fade_time = 1;

        // Intro Variables
        star_opacity = 0;
        goal_opacity = 0;
        hud_opacity = 0;
        player_opacity = 0;
        start_time = System.currentTimeMillis();
        current_time = 0;
        goal_intro_x = screen_width / 2;
        goal_intro_y = screen_height / 2;
        goal_intro_speed = 1;
        player_intro_speed = 1;
    }

    @Override
    public void run() {
        // Game Loop
        while (introRunning) {
            // Get Start Time
            long start_time = System.currentTimeMillis();
            updateIntro();
            drawIntro();

            long iteration_time = System.currentTimeMillis() - start_time;
            if (iteration_time > 0) {
                frame_rate = 1000 / iteration_time;
            }
        }
        while (running){
            // Get Start Time for iteration to Calculate frame_rate
            long start_time = System.currentTimeMillis();
            update();
            draw();

            long iteration_time = System.currentTimeMillis() - start_time;
            if (iteration_time > 0) {
                frame_rate = 1000 / iteration_time;
            }
        }
        // End Game
    }

    public void drawIntro() {
        // Check for valid surface to draw data
        if (holder.getSurface().isValid()){
            canvas = holder.lockCanvas();

            // Draw Background (Dark Violet)
            canvas.drawColor(Color.argb(255,10,0,25));

            // Draw Stars (White, Lower Opacity)
            paint.setColor(Color.argb(star_opacity,255, 255, 255));
            for(int i = 0; i < 50; i++) {
                canvas.drawCircle((float)star_center_x[i],(float)star_center_y[i],star_radius[i],paint);
            }

            // Draw Goal (Yellow)
            paint.setColor(Color.argb(goal_opacity,200, 200, 0));
            canvas.drawCircle(goal_intro_x,goal_intro_y, goal_radius, paint);

            // Draw Player
            paint.setAlpha(player_opacity);
            canvas.drawBitmap(player_bitmap_scaled, player_x - player_width / 2, player_y - player_height / 2, paint);
            paint.setAlpha(255);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void updateIntro() {
        if (current_time > 1) {
            updateGoalIntro();
            updateBackgroundIntro();
        }
        if (current_time > 3) {
            updatePlayerIntro();
        }
        float time_this_frame = (float)(System.currentTimeMillis() - start_time) / 1000;
        current_time += time_this_frame;
        start_time = System.currentTimeMillis();
    }

    public void updateGoalIntro() {
        if (goal_opacity < 255) {
            goal_opacity += 255 / fade_time / frame_rate;
        }
        if (goal_opacity > 255) {
            goal_opacity = 255;
        }

        if (current_time > 2) {
            goal_intro_y -= goal_intro_speed;
            goal_intro_speed++;
        }
    }

    public void updateBackgroundIntro() {
        if (star_opacity < 150) {
            star_opacity += 150 / fade_time / frame_rate;
        }
        if (star_opacity > 150) {
            star_opacity = 150;
        }
    }

    public void updatePlayerIntro() {
        if (player_opacity < 255) {
            player_opacity += 255 / fade_time / frame_rate / 2;
        }
        if (player_opacity > 255) {
            player_opacity = 255;
        }

        if (player_y > screen_height * 3 / 5) {
            // Have player accelerate until halfway to the correct position
            if (player_y > screen_height * 4 / 5) {
                player_y -= player_intro_speed;
                player_intro_speed += .3;
            }
            // After halfway to correct position, decelerate
            else {
                player_y -= player_intro_speed;
                player_intro_speed -= .3;
            }
        }
        if (player_y < screen_height * 3 / 5) {
            player_y = screen_height * 3 / 5;
            introRunning = false;
            running = true;
        }
    }

    public void draw() {
        // Check for valid surface to draw data
        if (holder.getSurface().isValid()){
            canvas = holder.lockCanvas();

            // Draw Background (Dark Violet)
            canvas.drawColor(Color.argb(255,10,0,25));

            // Draw Stars (White, Lower Opacity)
            paint.setColor(Color.argb(150,255, 255, 255));
            for(int i = 0; i < 50; i++) {
                canvas.drawCircle((float)star_center_x[i],(float)star_center_y[i],star_radius[i],paint);
            }

            // Draw Goal (Yellow)
            paint.setColor(Color.argb(255,200, 200, 0));
            float goal_drawable_x = player_x - goal_center_x;
            float goal_drawable_y = player_y - goal_center_y;
            canvas.drawCircle(goal_drawable_x,goal_drawable_y, goal_radius, paint);

            // Draw Player and Director
            canvas.drawBitmap(player_bitmap_scaled, player_x - player_width / 2, player_y - player_height / 2, paint);
            // Draw Shield if player is immune
            if (player_immune_frames > 0) {
                // Set opacity to pulse once per hit from 0 to 100 to 0
                // Sin curve stretched to max immune frames / pi and amplified by 100
                double opacity = 100*Math.sin(Math.PI * player_immune_frames / player_max_immune_frames);
                paint.setColor(Color.argb((int)opacity, 0, 200, 255));
                canvas.drawCircle(player_x, player_y, (Math.max(player_width, player_height))/2 + 10, paint);
            }
            paint.setColor(Color.argb(hud_opacity,0, 200, 255));
            float director_drawable_x = player_x - director_x;
            float director_drawable_y = player_y - director_y;
            paint.setTextSize(20);
            float distance = (float)Math.hypot(goal_center_x,goal_center_y);
            canvas.drawText("Distance: " + distance, director_drawable_x + director_radius*2, director_drawable_y, paint);
            canvas.drawCircle(director_drawable_x, director_drawable_y, director_radius, paint);

            // Draw Hostiles (Red)
            paint.setColor(Color.argb(255,255, 0, 0));
            for(int i = 0; i < current_hostile_amount; i++) {
                canvas.drawCircle((float)hostile_x[i],(float)hostile_y[i],hostile_size[i],paint);
            }

            // Draw Score (White)
            paint.setTextSize(100);
            paint.setColor(Color.argb(hud_opacity,255, 255, 255));
            float text_width = paint.measureText("Score: " + score);
            canvas.drawText("Score: " + score, player_x - text_width / 2, screen_height/8 + 100, paint);

            // Draw HUD
            canvas.drawBitmap(hud_healthbar_scaled, (int)(screen_width / 2 - hud_width / 2) + (int)(hud_width * .1), 0, paint);
            canvas.drawBitmap(hud_actionbar_scaled, (int)(screen_width / 2 - hud_width / 2) + (int)(hud_width * .1), 0, paint);
            canvas.drawBitmap(hud_top_foreground_scaled, screen_width / 2 - hud_width / 2, 0, paint);
            paint.setAlpha(255);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void setRunning(boolean r) {
        running = r;
    }

    public float getPlayerWidth() {
        return player_width;
    }

    public float getPlayerHeight() {
        return player_height;
    }

    public float getGoalX() {
        return goal_center_x;
    }

    public float getGoalY() {
        return goal_center_y;
    }

    public float getGoalDiameter() {
        return goal_radius * 2;
    }

    public double getPlayerTurnSpeed() {
        return player_turn_speed;
    }

    public float getPlayerSpeed() {
        return player_speed;
    }

    public double getDirectorAngle() {
        return director_angle;
    }

    public void update() {
        updateGoalPosition();
        updateDirector();
        updateGoal();
        updateBackground();
        updateHostilePosition();
        updateHostiles();
        updateHealthBar();
    }

    public void updateGoalPosition() {
        // If player is turning, rotate goal around (player_x,player_y)
        if (is_turning) {
            double theta = player_turn_speed / frame_rate * turn_left_right;
            float distance = (float)java.lang.Math.hypot(goal_center_x,goal_center_y);
            double original_angle = java.lang.Math.atan2(goal_center_y,goal_center_x);
            double new_angle = original_angle + theta;
            goal_center_x = (float)java.lang.Math.cos(new_angle) * distance;
            goal_center_y = (float)java.lang.Math.sin(new_angle) * distance;
        }
        // For every frame, move goal downwards by (speed/frame rate)
        double delta = player_speed / frame_rate;
        goal_center_y -= delta;
    }

    public void setGoalCenter(float x, float y){
        goal_center_x = x;
        goal_center_y = y;
    }

    public void setFrameRate(long f) {
        frame_rate = f;
    }

    public void setTurning(boolean t) {
        is_turning = t;
        turn_left_right = 1;
    }

    public void setPlayerSpeed(float s) {
        player_speed = s;
    }

    public boolean detectCollision(float x1, float y1, float width1, float height1,
                                   float x2, float y2, float width2, float height2) {
        // Translate (x2,y2) to where (x1,y1) is the origin
        x2 -= x1;
        y2 -= y1;
        return (Math.abs(x2) - width2 / 2 <= width1 / 2) &&
                Math.abs(y2) - height2 / 2 <= height1 / 2;
    }

    public void updateDirector() {
        // Director will point towards the goal
        double distance = Math.max(player_width, player_height) / 2 + 20;
        // Find arc tangent of (goal_x, goal_y) and use the same angle at a smaller distance for (director_x, director_y)
        director_angle = java.lang.Math.atan2(goal_center_y,goal_center_x);
        director_x = (float) (java.lang.Math.cos(director_angle) * distance);
        director_y = (float) (java.lang.Math.sin(director_angle) * distance);
    }

    public void updateGoal() {
        // If collision between player and goal, update score and health
        if (detectCollision(0,0,player_width,player_height,
                goal_center_x,goal_center_y,goal_radius*2,goal_radius*2)){
            score += goal_score;

            float player_health_gain = 33 / player_durability;

            if (player_health_points < 100 - player_health_gain) {
                player_health_points += 40 / player_durability;
            }
            else {
                player_health_points = 100;
            }

            // Create New Goal At Random Point between goal_max_distance and goal_min_distance
            goal_center_x = (float)(Math.random()*(goal_max_distance - goal_min_distance)+goal_min_distance);
            goal_center_y = (float)(Math.random()*(goal_max_distance - goal_min_distance)+goal_min_distance);
            // Randomize 50% chance of negative or positive for x and y
            if (Math.random() < .5) {
                goal_center_x *= -1;
            }
            if (Math.random() < .5) {
                goal_center_y *= -1;
            }

            // For every goal, add another hostile
            addHostile();
        }
    }

    public void updateBackground() {
        // Find new theta for stars (amount rotated in this frame)
        double theta = player_turn_speed / frame_rate * turn_left_right / 8;

        // For all stars update position
        for (int i = 0; i < 50; i++) {
            // If player is turning, rotate around the point (player_x, player_y)
            if (is_turning) {
                // Translate to origin
                star_center_x[i] -= player_x;
                star_center_y[i] -= player_y;
                // Rotate around origin
                star_center_x[i] = star_center_x[i] * Math.cos(theta) - star_center_y[i] * Math.sin(theta);
                star_center_y[i] = star_center_x[i] * Math.sin(theta) + star_center_y[i] * Math.cos(theta);
                // Translate back to original position
                star_center_x[i] += player_x;
                star_center_y[i] += player_y;
            }
            // Find delta (distance traveled by stars relative to player this frame)
            double delta = player_speed / frame_rate / 8;
            star_center_y[i] += delta;

            // If a star leaves visible area, update its position to the other side
            // This gives an illusion of a looping background
            if (star_center_y[i] > screen_height + 2)
                star_center_y[i] = -1;
            if (star_center_y[i] < -2)
                star_center_y[i] = screen_height + 1;
            if (star_center_x[i] > screen_width + 2)
                star_center_x[i] = -1;
            if (star_center_x[i] < -2)
                star_center_x[i] = screen_width + 1;
        }
    }

    public void updateHostilePosition() {
        // Find new theta (amount rotated in this frame)
        double theta = player_turn_speed / frame_rate * turn_left_right;

        // For all current hostiles, update position
        for (int i = 0; i < current_hostile_amount; i++) {
            // If player is turning, rotate around point (player_x, player_y)
            if (is_turning) {
                // Translate to origin
                hostile_x[i] -= player_x;
                hostile_y[i] -= player_y;
                // Rotate around origin
                hostile_x[i] = hostile_x[i] * Math.cos(theta) - hostile_y[i] * Math.sin(theta);
                hostile_y[i] = hostile_x[i] * Math.sin(theta) + hostile_y[i] * Math.cos(theta);
                // Translate back to original position
                hostile_x[i] += player_x;
                hostile_y[i] += player_y;

                // Update movement angle
                hostile_movement_angle[i] += theta;
            }

            // Find delta_x and delta_y (distance traveled in this frame)
            // Max speed is 2*player_speed, Min is .5*player_speed
            // Use size range (10,player_height+10) to move backwards to random variable range (0,1)
            double speed = (hostile_size[i] - 10) / player_height;
            // Flip the values to be between (1,0)
            speed = Math.abs(1-speed);
            speed = 1.5 * player_speed * speed + .5 * player_speed;
            double delta_x = Math.cos(hostile_movement_angle[i]) * speed / frame_rate;
            double delta_y = Math.sin(hostile_movement_angle[i]) * speed / frame_rate + player_speed / frame_rate;

            hostile_x[i] += delta_x;
            hostile_y[i] += delta_y;

            // If hostile leaves screen size * 2, update its position to the other side
            if (Math.abs(hostile_x[i]) > screen_width * 2 + 1) {
                hostile_x[i] *= -1;
            }
            if (Math.abs(hostile_y[i]) > screen_height * 2 + 1) {
                hostile_y[i] *= -1;
            }
        }
    }

    public void updateHostiles() {
        // Check for a collision for every hostile
        for (int i = 0; i < current_hostile_amount; i++) {
            if (detectCollision(player_x, player_y, player_width, player_height,
                    (float)hostile_x[i], (float)hostile_y[i], hostile_size[i], hostile_size[i])) {

                // If player is not immune, reduce health and make immune
                if (player_immune_frames == 0) {
                    player_health_points -= 100 / player_durability;
                    player_immune_frames = player_max_immune_frames;
                }
                // If health is below 0, end game
                if (player_health_points < 0) {
                    endGame();
                }

                // Create new hostile to replace destroyed one
                hostile_x[i] = Math.random() * (screen_width * 2) - screen_width * .5;
                hostile_y[i] = Math.random() * (screen_height * 1.5) + screen_height * 1.5;
                hostile_size[i] = (float) Math.random() * player_height + 10;
                hostile_movement_angle[i] = Math.random() * Math.PI + Math.PI;
            }
        }
        if (player_immune_frames > 0) {
            player_immune_frames--;
        }
    }

    public void addHostile() {
        if (current_hostile_amount < max_hostile_amount) {
                hostile_x[current_hostile_amount] = Math.random() * (screen_width * 2) - screen_width * .5;
                hostile_y[current_hostile_amount] = Math.random() * (screen_height * 1.5) + screen_height * 1.5;
                hostile_size[current_hostile_amount] = (float) Math.random() * player_height + 10;
                hostile_movement_angle[current_hostile_amount] = Math.random() * Math.PI + Math.PI;
            current_hostile_amount++;
        }
    }

    public void updateHealthBar() {
        // Use a displayed health instead of instantly showing actual health
        if (player_health_points < health_display) {
            health_display--;

            // Update Health Bar (width now is health points/100 * health width)
            hud_healthbar_scaled = Bitmap.createScaledBitmap(hud_healthbar, (int)(hud_width * .8) * (int)health_display / 100, hud_height, false);

            // If displayed health is now lower than actual health, update them to equal
            if (health_display < player_health_points) {
                health_display = player_health_points;
            }
        }
        else if (player_health_points > health_display) {
            health_display++;

            // Update Health Bar (width now is health points/100 * health width)
            hud_healthbar_scaled = Bitmap.createScaledBitmap(hud_healthbar, (int)(hud_width * .8) * (int)health_display / 100, hud_height, false);

            // If displayed health is now greater than actual health, update them to equal
            if (health_display > player_health_points) {
                health_display = player_health_points;
            }
        }

        if (hud_opacity < 255) {
            hud_opacity += 255 / fade_time / frame_rate;
        }
        if (hud_opacity > 255) {
            hud_opacity = 255;
        }
    }

    public void endGame() {
        // Add Ending Animations
        running = false;
    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // Left Turn
        if (motionEvent.getX() < screen_width / 3) {
            turn_left_right = 1;
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    is_turning = true;
                    break;
                case MotionEvent.ACTION_UP:
                    is_turning = false;
                    break;
            }
        }
        // Right Turn
        else if (motionEvent.getX() > screen_width / 3 * 2) {
            turn_left_right = -1;
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    is_turning = true;
                    break;
                case MotionEvent.ACTION_UP:
                    is_turning = false;
                    break;
            }
        }
        return true;
    }
}