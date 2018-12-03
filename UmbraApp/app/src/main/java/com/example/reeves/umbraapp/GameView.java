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
    private int enemy_type;
    private int enemy_type_count;
    private Enemy enemies[];

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
    private GameAudioManager audioManager;

    // Engine Variables
    private SurfaceHolder holder;
    private Thread gameThread = null;
    private Canvas canvas;
    private Paint paint;
    private int beat;

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

        // Enemy Variables
        min_hostile_amount = 6;
        max_hostile_amount = 18;
        current_hostile_amount = 0;
        enemy_type = 0;
        enemy_type_count = 3;
        enemies = new Enemy[max_hostile_amount];
        for (int i = 0; i < min_hostile_amount; i++) {
            addHostile();
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
        beat = 0;

        // Intro Variables
        start_time = System.currentTimeMillis();
        current_time = 0;
    }

    @Override
    public void run() {
        // Game Loop
        while (introRunning) {
            long start_time = System.currentTimeMillis();

            GameObject.setFrameRate(frame_rate);
            updateIntro();
            drawIntro();

            long iteration_time = System.currentTimeMillis() - start_time;
            if (iteration_time > 0) {
                frame_rate = 1000 / iteration_time;
            }
        }

        // Reset time for in game timer
        current_time = 0;

        while (running) {
            while (paused) {
                draw();
            }
            // Get Start Time for iteration to Calculate frame_rate
            long start_time = System.currentTimeMillis();

            GameObject.setFrameRate(frame_rate);
            update();
            draw();

            long iteration_time = System.currentTimeMillis() - start_time;
            if (iteration_time > 0) {
                frame_rate = 1000 / iteration_time;
                current_time += iteration_time / 1000.0;
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
            player.updateIntro();
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
                enemies[i].drawBitmap(canvas, paint);
                enemies[i].drawProjectiles(canvas, paint);
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

    public void drawEnd(int opacity) {
        // Check for valid surface to draw data
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();

            // Draw Background (Dark Violet)
            canvas.drawColor(Color.argb(255, 10, 0, 25));

            // Draw Stars (White, Lower Opacity)
            paint.setColor(Color.argb(150 * opacity / 255, 255, 255, 255));
            for (int i = 0; i < 50; i++) {
                stars[i].drawCircle(canvas, paint);
            }

            // Draw Goal
            goal.drawEndCircle(canvas, paint, opacity);

            // Draw Player
            player.drawBitmap(canvas, paint);
            player.drawLaser(canvas, paint);
            // Draw Shield if player is immune
            if (player.isImmune()) {
                player.drawShield(canvas, paint);
            }

            // Draw Hostiles (Red)
            paint.setColor(Color.argb(255, 255, 0, 0));
            for (int i = 0; i < current_hostile_amount; i++) {
                enemies[i].drawEndBitmap(canvas, paint, opacity);
                enemies[i].drawEndProjectiles(canvas, paint, opacity);
            }

            paint.setAlpha(255);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void update() {
        // Update All Positions
        goal.update(player);
        director.update(goal, player);
        player.update();
        updateBackground();
        updateHostiles();
        hud.updateHealthBar(player.getHealth(), frame_rate);

        // Check For Collisions
        checkGoalCollision();
        checkHostileCollision();
        checkLaserCollision();

        // If beat changed during time, increment beat
        // 126 / 60 is song's Beats per Second
        if ((int)(current_time * 126 / 60 % 4) != beat) {
            beat = (beat + 1) % 4;
            Enemy.increaseBeat();
        }
    }

    public void checkGoalCollision() {
        // If collision between player and goal, update score and health
        if (player.detectCollision(goal)) {
            total_score += goal_score;
            player.increaseHealth();
            audioManager.playSound(0);

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
            enemies[i].update(player);
        }
    }

    public void checkHostileCollision() {
        // Check for a collision for every hostile
        boolean hit = false;
        int damage = 0;

        for (int i = 0; i < current_hostile_amount; i++) {
            if (player.detectHostileCollision(enemies[i])) {
                hit = true;
                damage = 100;
            }
            for (int j = 0; j < enemies[i].current_projectiles; j++) {
                if (player.detectHostileCollision(enemies[i].projectiles[j])) {
                    hit = true;
                    damage = enemies[i].projectiles[j].getDamage();
                    enemies[i].projectiles[j].destroyProjectile();
                }
            }

            if (hit) {
                // If player is not immune, reduce health and make immune
                if (!player.isImmune()) {
                    audioManager.playSound(2);
                    player.decreaseHealth(damage);
                    checkPlayerHealth();
                }
            }
        }
    }

    public void checkLaserCollision() {
        // Check for a collision for every hostile
        for (int i = 0; i < current_hostile_amount; i++) {
            if (player.detectLaserCollision(enemies[i])) {
                // Create new hostile to replace destroyed one
                enemies[i] = new QuadProjectileEnemy(context);
                player.resetLaser();
                break;
            }
        }
    }

    public void addHostile() {
        if (current_hostile_amount < max_hostile_amount) {
            if (enemy_type == 0) {
                enemies[current_hostile_amount] = new QuadProjectileEnemy(context);
            }
            else if (enemy_type == 1) {
                enemies[current_hostile_amount] = new DualProjectileEnemy(context);
            }
            else {
                enemies[current_hostile_amount] = new GrowShrinkEnemy(context);
            }

            enemy_type = (enemy_type + 1) % enemy_type_count;
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
        audioManager.playSound(3);
        running = false;
        int opacity = 255;
        while (player.opacity > 0) {
            player.updateEnd();
            drawEnd(opacity);
        }
        audioManager.stopBGM();
        audioManager.clean();
        while (opacity > 0) {
            opacity -= 255 / frame_rate;
            if (opacity < 0) {
                opacity = 0;
            }
            drawEnd(opacity);
        }
        drawEnd(0);

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
        audioManager.playBGM();
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

    public void stop() {
        paused = true;
        audioManager.pauseBGM();
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
                    audioManager.resumeBGM();
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
                        audioManager.playSound(1);
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