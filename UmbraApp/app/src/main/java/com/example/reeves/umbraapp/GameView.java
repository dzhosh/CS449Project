/*
Referenced:
Basic Structure of GameView (Frame Rate Calculation, Draw, Resume, and Pause Methods)
        http://gamecodeschool.com/
*/

package com.example.reeves.umbraapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {
    // Player Variables
    private float player_speed;
    private double player_turn_speed;
    private float player_health_points;
    private float player_width;
    private float player_height;
    private float player_radius;
    private float player_x;
    private float player_y;
    private boolean is_turning;
    // -1 for Left, 1 for Right
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

    // Game Variables
    private long frame_rate;
    private long score;
    private boolean running;

    // Engine Variables
    SurfaceHolder holder;
    Thread gameThread = null;
    Canvas canvas;
    Paint paint;

    public GameView(Context c, boolean is_test){
        super(c);

        // Get current holder and create Paint for Drawing to Screen
        holder = getHolder();
        paint = new Paint();

        // Initialize All Necessary Variables
        // Player Variables
        player_speed = 300; // In Pixels per Second
        player_turn_speed = 2; // In Radians per Second
        player_health_points = 100;
        player_radius = 60;
        player_width = player_radius*2;
        player_height = player_radius*2;
        if (is_test) {
            player_x = 0;
            player_y = 0;
        }
        else {
            // Player (x,y) at (center, 3/4 down the screen).
            player_x = this.getResources().getDisplayMetrics().widthPixels / 2;
            player_y = this.getResources().getDisplayMetrics().heightPixels / 4 * 3;
        }
        is_turning = false;
        turn_left_right = 1;

        // Goal Variables
        goal_max_distance = player_speed * 10;
        goal_min_distance = player_speed * 2;
        goal_score = 100;
        goal_radius = 20;
        // Goal placed randomly between goal_min_distance and goal_max_distance
        goal_center_x = (float)(Math.random()*(goal_max_distance - goal_min_distance))+goal_min_distance;
        goal_center_y = (float)(Math.random()*(goal_max_distance - goal_min_distance))+goal_min_distance;

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
            for (int i = 0; i < 50; i++) {
                star_center_x[i] = i;
                star_center_y[i] = i;
                star_radius[i] = 1;
            }
        }
        else {
            for (int i = 0; i < 50; i++) {
                // Randomly place them around the screen with random radii between 0 and 3
                star_center_x[i] = (float) Math.random() * (this.getResources().getDisplayMetrics().widthPixels + 1);
                star_center_y[i] = (float) Math.random() * (this.getResources().getDisplayMetrics().heightPixels + 1);
                star_radius[i] = (float) (Math.random() * (3));
            }
        }

        // Game Variables
        score = 0;
        running = true;
        frame_rate = 30;
    }

    @Override
    public void run() {
        // Game Loop
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

            // Draw Player and Director (Light Blue)
            paint.setColor(Color.argb(255,0, 200, 255));
            float director_drawable_x = player_x - director_x;
            float director_drawable_y = player_y - director_y;
            paint.setTextSize(20);
            float distance = (float)Math.hypot(goal_center_x,goal_center_y);
            canvas.drawText("Distance: " + distance, director_drawable_x + director_radius*2, director_drawable_y, paint);
            canvas.drawCircle(director_drawable_x, director_drawable_y, director_radius, paint);
            canvas.drawCircle(player_x, player_y, player_radius, paint);

            // Draw Hostiles (Red)
            // Implement in later stage

            // Draw Score (White)
            paint.setTextSize(100);
            paint.setColor(Color.argb(255,255, 255, 255));
            canvas.drawText("Score: " + score, player_x - 100, 180, paint);

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
        // Find arctangent of (goal_x, goal_y) and use the same angle at a smaller distance for (director_x, director_y)
        director_angle = java.lang.Math.atan2(goal_center_y,goal_center_x);
        director_x = (float)java.lang.Math.cos(director_angle) * 100;
        director_y = (float)java.lang.Math.sin(director_angle) * 100;
    }

    public void updateGoal() {
        // If collision between player and goal, update score
        if (detectCollision(0,0,player_width,player_height,
                goal_center_x,goal_center_y,goal_radius*2,goal_radius*2)){
            score += goal_score;

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
        }
    }

    public void updateBackground() {
        // Find new theta for stars (amount rotated in this frame)
        double theta = player_turn_speed / frame_rate * turn_left_right / 8;

        // For all stars, rotate around the point (player_x, player_y)
        for (int i = 0; i < 50; i++) {
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
            if (star_center_y[i] > this.getResources().getDisplayMetrics().heightPixels + 2)
                star_center_y[i] = -1;
            if (star_center_y[i] < -2)
                star_center_y[i] = this.getResources().getDisplayMetrics().heightPixels + 1;
            if (star_center_x[i] > this.getResources().getDisplayMetrics().widthPixels + 2)
                star_center_x[i] = -1;
            if (star_center_x[i] < -2)
                star_center_x[i] = this.getResources().getDisplayMetrics().widthPixels + 1;
        }
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
        if (motionEvent.getX() < this.getResources().getDisplayMetrics().widthPixels / 3) {
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
        // Right Turn
        else if (motionEvent.getX() > this.getResources().getDisplayMetrics().widthPixels / 3 * 2) {
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
        return true;
    }
}