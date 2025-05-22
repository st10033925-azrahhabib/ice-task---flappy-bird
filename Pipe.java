package vcmsa.projects.flappybird;

// Pipe.java
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Pipe {
    private Bitmap topPipeBitmap;
    private Bitmap bottomPipeBitmap; // Or draw with rectangles
    public int x;
    public int topPipeY;      // Bottom Y-coordinate of the top pipe
    public int bottomPipeY;   // Top Y-coordinate of the bottom pipe
    public int pipeWidth;
    private int speed;
    public boolean passed; // For scoring

    // Constructor if using Bitmaps
    public Pipe(Bitmap topPipe, Bitmap bottomPipe, int startX, int gapY, int gapHeight, int screenWidth, int screenHeight) {
        this.topPipeBitmap = topPipe;
        this.bottomPipeBitmap = bottomPipe;
        this.pipeWidth = topPipe.getWidth(); // Assume same width
        this.x = startX;
        this.topPipeY = gapY - topPipe.getHeight(); // Position top pipe correctly
        this.bottomPipeY = gapY + gapHeight;
        this.speed = 7; // Adjust for desired pipe speed
        this.passed = false;
    }

    // Alternative constructor if drawing with rectangles (simpler)
    public Pipe(int startX, int gapY, int gapHeight, int pipeWidth, int screenHeight) {
        this.pipeWidth = pipeWidth;
        this.x = startX;
        this.topPipeY = 0; // Top pipe extends from 0 to gapY
        this.bottomPipeY = gapY + gapHeight; // Bottom pipe from gapY + gapHeight to screenHeight
        this.speed = 7;
        this.passed = false;

    }


    public void update() {
        x -= speed;
    }

    public void draw(Canvas canvas) {
        if (topPipeBitmap != null && bottomPipeBitmap != null) {
            canvas.drawBitmap(topPipeBitmap, x, topPipeY, null);
            canvas.drawBitmap(bottomPipeBitmap, x, bottomPipeY, null);
        } else {

        }
    }

    public Rect getTopRect() {

        if (topPipeBitmap != null) {
            return new Rect(x, topPipeY, x + pipeWidth, topPipeY + topPipeBitmap.getHeight());
        } else {
            // For rectangle drawing, if topPipeY is the position of the *gap's top*
            return new Rect(x, 0, x + pipeWidth, this.topPipeY); // Assuming this.topPipeY is end of top pipe
        }
    }

    public Rect getBottomRect() {
        if (bottomPipeBitmap != null) {
            return new Rect(x, bottomPipeY, x + pipeWidth, bottomPipeY + bottomPipeBitmap.getHeight());
        } else {
            // For rectangle drawing, if bottomPipeY is the position of the *gap's bottom*
            return new Rect(x, this.bottomPipeY, x + pipeWidth, GameView.screenHeight); // Use actual screenHeight
        }
    }

    public boolean isOffScreen() {
        return x + pipeWidth < 0;
    }
}