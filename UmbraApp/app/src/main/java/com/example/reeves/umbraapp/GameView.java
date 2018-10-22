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
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class GameView extends SurfaceView implements Runnable {

    private Player player;
    private Goal goal;
    private Director director;
    private Context context;

    private int star_count;
    private Star[] stars;

    private int max_hostile_amount;
    private int min_hostile_amount;
    private int current_hostile_amount;
    private Hostile hostiles[];

    // Game Variables
    private long frame_rate;
    private long total_score;
    private boolean introRunning;
    private boolean running;
    private float screen_width;
    private float screen_height;
    private int goal_score;

    // Intro Variables
    private float current_time;
    private long start_time;

    // HUD Variables
    private HUD hud;

    // Audio Variables
    GameAudioManager audioManager;

    // Engine Variables
    private SurfaceHolder holder;
    private Thread gameThread = null;
    private Canvas canvas;
    private Paint paint;

    public GameView(Context c, Display display) {
        super(c);

        // Get current holder and create Paint for Drawing to Screen
        holder = getHolder();
        paint = new Paint();
        audioManager = new GameAudioManager(c);

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
        player = new Player(c, (int)screen_width, (int)screen_height);

        // Goal Variables
        goal = new Goal();

        // Director Variables
        // Director points towards the goal at all times using the angle found by arctan(goal_y,goal_x)
        director = new Director((int)player.getWidth(), (int)player.getHeight());

        // Background Variables
        // 50 Stars that will rotate and move across screen with player movement
        star_count = 50;
        stars = new Star[star_count];
        for (int i = 0; i < star_count; i++) {
            stars[i] = new Star();
        }

        // Hostile Variables
        min_hostile_amount = 20;
        max_hostile_amount = 70;
        current_hostile_amount = min_hostile_amount;
        hostiles = new Hostile[max_hostile_amount];
        for (int i = 0; i < min_hostile_amount; i++) {
            hostiles[i] = new Hostile(player);
        }

        // HUD Variables
        hud = new HUD(c, (int)screen_height, (int)screen_width);

        // Game Variables
        total_score = 0;
        introRunning = true;
        running = false;
        frame_rate = 30;
        goal_score = 100;

        // Intro Variables
        start_time = System.currentTimeMillis();
        current_time = 0;
    }

    @Override
    public void run() {
        // Game Loop
        while (introRunning) {
            long start_time = System.currentTimeMillis();
            updateIntro();
            drawIntro();

            long iteration_time = System.currentTimeMillis() - start_time;
            if (iteration_time > 0) {
                frame_rate = 1000 / iteration_time;
            }
        }
        while (running) {
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
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();

            // Draw Background (Dark Violet)
            canvas.drawColor(Color.argb(255, 10, 0, 25));

            // Draw Stars (White, Lower Opacity)
            for (int i = 0; i < 50; i++) {
                stars[i].drawCircle(canvas, paint);
            }

            goal.drawCircle(canvas, paint);
            player.drawBitmap(canvas, paint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void updateIntro() {
        if (current_time > 1) {
            goal.updateIntro(current_time, frame_rate);
            for (int i = 0; i < star_count; i++) {
                stars[i].updateIntro(frame_rate);
            }
        }
        if (current_time > 3) {
            player.updateIntro(frame_rate);
            if (!audioManager.isPlayingBGM()) {
                audioManager.playBGM();
            }
        }
        if (player.introConcluded()) {
            introRunning = false;
            running = true;
        }

        float time_this_frame = (float) (System.currentTimeMillis() - start_time) / 1000;
        current_time += time_this_frame;
        start_time = System.currentTimeMillis();
    }

    public void draw() {
        // Check for valid surface to draw data
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();

            // Draw Background (Dark Violet)
            canvas.drawColor(Color.argb(255, 10, 0, 25));

            // Draw Controls
            hud.drawControls(canvas, paint);

            // Draw Stars (White, Lower Opacity)
            paint.setColor(Color.argb(150, 255, 255, 255));
            for (int i = 0; i < 50; i++) {
                stars[i].drawCircle(canvas, paint);
            }

            // Draw Goal
            goal.drawCircle(canvas, paint);

            // Draw Player
            player.drawBitmap(canvas, paint);
            // Draw Shield if player is immune
            if (player.isImmune()) {
                player.drawShield(canvas, paint);
            }

            // Draw Director (Light Blue)
            director.drawCircle(canvas, paint);

            // Draw Hostiles (Red)
            paint.setColor(Color.argb(255, 255, 0, 0));
            for (int i = 0; i < current_hostile_amount; i++) {
                hostiles[i].drawCircle(canvas, paint);
            }

            // Draw Score (White)
            hud.drawScore(canvas, paint, total_score, screen_width);

            // Draw HUD
            hud.drawHealthBar(canvas, paint);

            paint.setAlpha(255);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void update() {
        // Update All Positions
        goal.update(frame_rate, player);
        director.update(goal, player);
        player.update(frame_rate);
        updateBackground();
        updateHostiles();
        hud.updateHealthBar(player.getHealth(), frame_rate);

        // Check For Collisions
        checkGoalCollision();
        checkHostileCollision();
    }

    public void checkGoalCollision() {
        // If collision between player and goal, update score and health
        if (player.detectCollision(goal)) {
            total_score += goal_score;
            player.increaseHealth();

            // Create New Goal At Random Point between goal_max_distance and goal_min_distance
            goal.makeNewGoal(player.getX(), player.getY());
            // For every goal, add another hostile
            addHostile();
        }
    }

    public void updateBackground() {
        for (int i = 0; i < star_count; i++) {
            stars[i].update(frame_rate, player);
        }
    }

    public void updateHostiles() {
        for (int i = 0; i < current_hostile_amount; i++) {
            hostiles[i].update(frame_rate, player);
        }
    }

    public void checkHostileCollision() {
        // Check for a collision for every hostile
        for (int i = 0; i < current_hostile_amount; i++) {
            if (player.detectHostileCollision(hostiles[i])) {

                // If player is not immune, reduce health and make immune
                if (!player.isImmune()) {
                    player.decreaseHealth();
                    checkPlayerHealth();
                }

                // Create new hostile to replace destroyed one
                hostiles[i] = new Hostile(player);
            }
        }
    }

    public void addHostile() {
        if (current_hostile_amount < max_hostile_amount) {
            hostiles[current_hostile_amount] = new Hostile(player);
            current_hostile_amount++;
        }
    }

    public void checkPlayerHealth() {
        if (!player.hasHealth()) {
            endGame();
        }
    }

    public void endGame() {
        // Add Ending Animations
        running = false;
        audioManager.stopBGM();
    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        running = false;
        player.stopTurning();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int pointerIndex = motionEvent.getActionIndex();

        // Check if screen was pressed or released
        // This needs to be checked first to always stop action when screen is released
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
                if (motionEvent.getX(pointerIndex) < screen_width / 3) {
                    player.turnLeft();
                } else if (motionEvent.getX(pointerIndex) > screen_width * 2 / 3) {
                    player.turnRight();
                } else {

                }
                break;

            case MotionEvent.ACTION_UP:
                player.stopTurning();
                break;
        }
        return true;
    }
}