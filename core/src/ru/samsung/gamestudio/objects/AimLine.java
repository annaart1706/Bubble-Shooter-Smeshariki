package ru.samsung.gamestudio.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import ru.samsung.gamestudio.GameSettings;

public class AimLine {

    private ShapeRenderer shapeRenderer;
    private float dotSpacing = 20f;
    private int maxDots = 200;

    public AimLine() {
        shapeRenderer = new ShapeRenderer();
    }

    public void drawRecords(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, com.badlogic.gdx.math.Matrix4 projectionMatrix,
                            Smesharik[][] bubbleGrid, ShipObject shipObject, Smesharik currentBall) {

        if (shipObject == null || currentBall == null || bubbleGrid == null) return;

        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        int colorId = currentBall.getColorType();
        switch (colorId) {
            case 0: shapeRenderer.setColor(Color.CYAN); break;
            case 1: shapeRenderer.setColor(Color.RED); break;
            case 2: shapeRenderer.setColor(Color.PURPLE); break;
            case 3: shapeRenderer.setColor(Color.ORANGE); break;
            case 4: shapeRenderer.setColor(Color.MAGENTA); break;
            case 5: shapeRenderer.setColor(Color.BLUE); break;
            case 6: shapeRenderer.setColor(Color.YELLOW); break;
            case 7: shapeRenderer.setColor(Color.GRAY); break;
            case 8: shapeRenderer.setColor(Color.VIOLET); break;
            default: shapeRenderer.setColor(Color.WHITE); break;
        }

        float currentX = shipObject.getX();
        float currentY = shipObject.getY() + 50;

        float angleRadians = (shipObject.getRotation() + 90) * MathUtils.degreesToRadians;
        float dirX = MathUtils.cos(angleRadians);
        float dirY = MathUtils.sin(angleRadians);

        int dotsDrawn = 0;
        float stepAccumulator = 0f;
        boolean hitSomething = false;

        while (dotsDrawn < maxDots && currentY < GameSettings.SCREEN_HEIGHT && currentY > 0 && !hitSomething) {

            float nextWallX = (dirX > 0) ? GameSettings.SCREEN_WIDTH : 0;
            float distanceX = nextWallX - currentX;
            float tWall = (dirX != 0) ? (distanceX / dirX) : Float.MAX_VALUE;

            float distanceY = GameSettings.SCREEN_HEIGHT - currentY;
            float tCeiling = (dirY > 0) ? (distanceY / dirY) : Float.MAX_VALUE;

            float tMin = Math.min(tWall, tCeiling);
            float segmentLength = tMin;

            while (stepAccumulator < segmentLength && dotsDrawn < maxDots) {
                float dotX = currentX + dirX * stepAccumulator;
                float dotY = currentY + dirY * stepAccumulator;

                int r = (int) ((GameSettings.SCREEN_HEIGHT - dotY) / GameSettings.ROW_HEIGHT);
                if (r >= 0 && r < GameSettings.GRID_ROWS) {
                    boolean isOddRow = (r % 2 != 0);
                    float targetX = dotX;
                    if (isOddRow) {
                        targetX -= GameSettings.BUBBLE_RADIUS;
                    }
                    int c = (int) (targetX / GameSettings.BUBBLE_DIAMETER);

                    if (c >= 0 && c < GameSettings.GRID_COLS && bubbleGrid[r][c] != null) {
                        hitSomething = true;
                        break;
                    }
                }

                shapeRenderer.circle(dotX, dotY, 4f);
                dotsDrawn++;
                stepAccumulator += dotSpacing;
            }

            if (hitSomething) break;

            stepAccumulator -= segmentLength;
            currentX += dirX * tMin;
            currentY += dirY * tMin;

            if (tMin == tWall) {
                dirX = -dirX;
            } else {
                break;
            }
        }

        shapeRenderer.end();
    }

    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}