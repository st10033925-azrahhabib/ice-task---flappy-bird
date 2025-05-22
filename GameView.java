package vcmsa.projects.flappybird;

// GameView.java
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Make sure to import your R file if you are in a different package
// import com.yourpackage.R; // Replace com.yourpackage with your actual package name

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder holder;
    private Thread gameThread;
    private boolean isRunning;

    private Bird bird;
    private Bitmap birdBitmap; // Load this in constructor

    private List<Pipe> pipes;
    private Bitmap topPipeBitmap, bottomPipeBitmap; // Load these
    private Paint pipePaint; // For drawing pipes as rectangles

    private int score;
    private long startTime; // For timing (can be used for difficulty or just display)
    private Paint scorePaint;
    private Paint gameOverPaint;

    private Random random;
    private int pipeGapHeight = 400; // Adjust
    private int pipeWidth = 150;     // Adjust
    private int pipeSpacing = 700;   // Distance between pipe pairs

    public static int screenWidth, screenHeight; // Made static for Pipe to access
    private boolean gameOver = false;

    public GameView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        random = new Random();


        birdBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bird); // Replace bird_icon with your bird image name


        // If drawing pipes as rectangles:
        pipePaint = new Paint();
        pipePaint.setColor(Color.GREEN); // Example color

        scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(80);
        scorePaint.setTextAlign(Paint.Align.LEFT);

        gameOverPaint = new Paint();
        gameOverPaint.setColor(Color.RED);
        gameOverPaint.setTextSize(100);
        gameOverPaint.setTextAlign(Paint.Align.CENTER);


    }

    private void initGame() {
        // Bird initialized after screen dimensions are known
        bird = new Bird(birdBitmap, screenWidth / 4, screenHeight / 2, screenHeight);
        pipes = new ArrayList<>();
        score = 0;
        startTime = System.currentTimeMillis();
        gameOver = false;
        addInitialPipes();
    }

    private void addInitialPipes() {
        pipes.clear();
        // Add a few pipes to start, spaced out
        for (int i = 0; i < 3; i++) {
            int startX = screenWidth + i * pipeSpacing;
            addPipe(startX);
        }
    }

    private void addPipe(int startX) {
        int minGapY = (int) (screenHeight * 0.2); // Gap can't be too high
        int maxGapY = (int) (screenHeight * 0.8) - pipeGapHeight; // Gap can't be too low
        int gapY = random.nextInt(maxGapY - minGapY + 1) + minGapY; // Random Y position for the top of the gap


        Pipe newPipe = new Pipe(startX, gapY, pipeGapHeight, pipeWidth, screenHeight);
        pipes.add(newPipe);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Screen dimensions are not reliably available here yet.
        // Game thread start will be managed in surfaceChanged or onResume.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
        initGame(); // Initialize game elements now that we have screen dimensions
        resumeGame();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pauseGame();
    }

    public void resumeGame() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pauseGame() {
        isRunning = false;
        boolean retry = true;
        while (retry) {
            try {
                if (gameThread != null) {
                    gameThread.join();
                }
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        gameThread = null;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0; // Target FPS
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                if (!gameOver) {
                    update();
                }
                delta--;
            }

            if (isRunning) {
                draw();
            }

            frames++;
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                // Log.d("GameView", "FPS: " + frames); // Optional FPS counter
                frames = 0;
            }
        }
    }

    private void update() {
        bird.update();

        // Bird hits top or bottom boundary (bottom is game over)
        if (bird.y + bird.height > screenHeight || bird.y < 0) {
            gameOver = true;
            return; // Stop further updates if game over
        }


        List<Pipe> pipesToRemove = new ArrayList<>();
        boolean newPipeNeeded = false;
        int lastPipeX = 0;

        for (Pipe pipe : pipes) {
            pipe.update();

            // Check for collision
            if (Rect.intersects(bird.getRect(), pipe.getTopRect()) ||
                    Rect.intersects(bird.getRect(), pipe.getBottomRect())) {
                gameOver = true;
                return; // Stop further updates
            }

            // Scoring: bird passes the pipe
            if (!pipe.passed && bird.x > pipe.x + pipe.pipeWidth) {
                pipe.passed = true;
                score++;
            }

            if (pipe.isOffScreen()) {
                pipesToRemove.add(pipe);
                newPipeNeeded = true; // A pipe went off-screen, so we'll need a new one
            }
            lastPipeX = Math.max(lastPipeX, pipe.x); // Track the rightmost pipe's position
        }

        pipes.removeAll(pipesToRemove);

        // Generate new pipes to maintain a continuous flow
        // Check if the last pipe is far enough to the left to add a new one
        if (pipes.size() < 3 && lastPipeX < screenWidth - pipeSpacing + 200 ) { // Ensure enough pipes are on screen
            addPipe(screenWidth + pipeSpacing/2); // Add slightly off screen
        } else if (newPipeNeeded && pipes.size() < 5) { // Keep a certain number of pipes
            // Determine the x-coordinate for the new pipe based on the last pipe's position
            int newPipeX = screenWidth; // Default to screenWidth if no pipes
            if (!pipes.isEmpty()) {
                newPipeX = pipes.get(pipes.size() - 1).x + pipeSpacing;
            }
            addPipe(newPipeX);
        }
    }


    private void draw() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                try {
                    // Background
                    canvas.drawColor(Color.parseColor("#70c5ce")); // Sky blue

                    // Draw Pipes
                    for (Pipe pipe : pipes) {

                        canvas.drawRect(pipe.x, 0, pipe.x + pipe.pipeWidth, pipe.topPipeY, pipePaint);
                        // Bottom Pipe (gap starts at pipe.topPipeY, ends at pipe.bottomPipeY)
                        canvas.drawRect(pipe.x, pipe.bottomPipeY, pipe.x + pipe.pipeWidth, screenHeight, pipePaint);
                    }

                    // Draw Bird
                    bird.draw(canvas);

                    // Draw Score
                    canvas.drawText("Score: " + score, 50, 100, scorePaint);


                    long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                    // canvas.drawText("Time: " + elapsedTime, 50, 200, scorePaint);

                    if (gameOver) {
                        canvas.drawText("GAME OVER", screenWidth / 2, screenHeight / 2 - 50, gameOverPaint);
                        canvas.drawText("Tap to Restart", screenWidth / 2, screenHeight / 2 + 50, scorePaint);
                    }

                } finally {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gameOver) {
                initGame(); // Restart the game
            } else {
                bird.flap();
            }
            return true; // Event handled
        }
        return super.onTouchEvent(event);
    }
}