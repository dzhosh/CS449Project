/*
Referenced:
Basic Structure of GameView (Frame Rate Calculation, Draw, Resume, and Pause Methods)
        http://gamecodeschool.com/
*/

package com.example.reeves.umbraapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

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
    private boolean paused;
    private float screen_width;
    private float screen_height;
    private int goal_score;
    private int music_volume;
    private int sfx_volume;
    private int difficulty;

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
        context = c;

        try {
            loadSettings();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // Get current holder and create Paint for Drawing to Screen
        holder = getHolder();
        paint = new Paint();
        audioManager = new GameAudioManager(c, music_volume, sfx_volume);

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
        paused = false;
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
            while (paused) {
                draw();
            }
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
            player.drawLaser(canvas, paint);
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

            if (paused) {
                canvas.drawColor(Color.argb(100, 0, 0, 0));
                paint.setTextSize(300);
                paint.setColor(Color.argb(255, 255, 255, 255));
                float text_width = paint.measureText("P A U S E D");
                int y = (int)(screen_height / 2 + 150);
                canvas.drawText("P A U S E D", (screen_width - text_width) / 2, y, paint);
            }

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
        checkLaserCollision();
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

    public void checkLaserCollision() {
        // Check for a collision for every hostile
        for (int i = 0; i < current_hostile_amount; i++) {
            if (player.detectLaserCollision(hostiles[i])) {
                // Create new hostile to replace destroyed one
                hostiles[i] = new Hostile(player);
                player.resetLaser();
                break;
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
        audioManager.clean();
        try {
            saveLastGame();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Activity a = (Activity)context;
        a.finish();
        context.startActivity(new Intent(context, GameOver.class));
    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
        audioManager.resumeBGM();
    }

    public void pause() {
        running = false;
        paused = true;
        player.stopTurning();
        audioManager.pauseBGM();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void loadSettings()
            throws IOException {
        int settings_array[] = new int[3];

        BufferedReader r = null;
        try {
            InputStream in = context.openFileInput("settings.txt");
            r = new BufferedReader(new InputStreamReader(in));
            for (int i = 0; i < 3; i++) {
                String line = r.readLine();
                settings_array[i] = Integer.parseInt(line);
            }
        }
        catch (FileNotFoundException e) {
            settings_array[0] = 2;
            settings_array[1] = 100;
            settings_array[2] = 100;
        }
        finally {
            if (r != null) {
                r.close();
            }
        }

        difficulty = settings_array[0];
        sfx_volume = settings_array[1];
        music_volume = settings_array[2];
    }

    public void saveLastGame()
        throws IOException {
        Writer w = null;
        try {
            OutputStream out = context.openFileOutput("last_game.txt", context.MODE_PRIVATE);
            w = new OutputStreamWriter(out);
            w.write(Long.toString(total_score));
            w.write('\n');
            w.write(Integer.toString(difficulty));
        } finally {
            if (w != null) {
                w.close();
            }
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
                if (paused) {
                    paused = false;
                }

                if (motionEvent.getX(pointerIndex) < screen_width / 5) {
                    player.turnLeft();
                } else if (motionEvent.getX(pointerIndex) > screen_width * 4 / 5) {
                    if (motionEvent.getY(pointerIndex) < screen_height / 4) {
                        paused = true;
                    }
                    else
                        player.turnRight();
                } else {
                    if (player.canFire()){
                        player.fire();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                player.stopTurning();
                break;
        }
        return true;
    }
}