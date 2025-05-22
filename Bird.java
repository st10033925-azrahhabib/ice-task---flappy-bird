package vcmsa.projects.flappybird;

// Bird.java
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Bird {
    private Bitmap birdBitmap;
    public int x, y, width, height;
    public float velocityY;
    private float gravity;
    private int screenHeight;

    public Bird(Bitmap bitmap, int startX, int startY, int screenHeight) {
        this.birdBitmap = bitmap;
        this.width = birdBitmap.getWidth();
        this.height = birdBitmap.getHeight();
        this.x = startX;
        this.y = startY;
        this.velocityY = 0;
        this.gravity = 0.8f; // Adjust for desired falling speed
        this.screenHeight = screenHeight;
    }

    public void update() {
        velocityY += gravity;
        y += velocityY;

        // Prevent bird from going off the top of the screen
        if (y < 0) {
            y = 0;
            velocityY = 0; // Stop upward momentum if hitting the ceiling
        }

        // Optional: Prevent bird from going off bottom (game over condition handled in GameView)
        // if (y + height > screenHeight) {
        //     y = screenHeight - height;
        //     velocityY = 0;
        // }
    }

    public void flap() {
        velocityY = -15; // Adjust for desired flap strength (negative is up)
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(birdBitmap, x, y, null);
    }

    public Rect getRect() {
        return new Rect(x, y, x + width, y + height);
    }
}
