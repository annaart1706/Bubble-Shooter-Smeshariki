package ru.samsung.gamestudio.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import ru.samsung.gamestudio.GameSettings;

public class AimLine {

    private ShapeRenderer shapeRenderer;
    private float dotSpacing = 20f; // Делаем точки чуть плотнее и красивее
    private int maxDots = 120;       // Увеличили до 120, чтобы луча точно хватало на 3-4 рикошета!

    public AimLine() {
        shapeRenderer = new ShapeRenderer();
    }

    // Имя метода изменено на drawRecords, чтобы оно строго совпадало с вызовом из твоего GameScreen
    public void drawRecords(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, com.badlogic.gdx.math.Matrix4 projectionMatrix,
                            Smesharik[][] bubbleGrid, ShipObject shipObject, Smesharik currentBall) {

        // ЗАЩИТА: Если пушки или шарика еще нет в памяти, тихо выходим
        if (shipObject == null || currentBall == null || bubbleGrid == null) return;

        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // --- УМНЫЙ ЦВЕТ ПРИЦЕЛА ПОД ЦВЕТ СМЕШАРИКА ---
        int colorId = currentBall.getColorType();
        switch (colorId) {
            case 0: shapeRenderer.setColor(Color.CYAN); break;    // Бирюзовый для Кроша
            case 1: shapeRenderer.setColor(Color.RED); break;     // Красный/Розовый для Нюши
            case 2: shapeRenderer.setColor(Color.PURPLE); break;  // Фиолетовый для Ежика
            case 3: shapeRenderer.setColor(Color.ORANGE); break;  // Оранжевый для Копатыча
            case 4: shapeRenderer.setColor(Color.MAGENTA); break; // Сиреневый для Бараша
            case 5: shapeRenderer.setColor(Color.BLUE); break;    // Синий для Карыча
            case 6: shapeRenderer.setColor(Color.YELLOW); break;  // Желтый для Лосяша
            case 7: shapeRenderer.setColor(Color.GRAY); break;    // Серый для Пина
            case 8: shapeRenderer.setColor(Color.VIOLET); break;  // Темно-фиолетовый для Совуньи
            default: shapeRenderer.setColor(Color.WHITE); break;
        }

        // Начальная точка — нос пушки
        float currentX = shipObject.getX();
        float currentY = shipObject.getY() + 50;

        // Направление луча
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

                // Перевод пикселей в индексы сетки для умной остановки луча
                int r = (int) ((GameSettings.SCREEN_HEIGHT - dotY) / GameSettings.ROW_HEIGHT);
                if (r >= 0 && r < GameSettings.GRID_ROWS) {
                    boolean isOddRow = (r % 2 != 0);
                    float targetX = dotX;
                    if (isOddRow) {
                        targetX -= GameSettings.BUBBLE_RADIUS;
                    }
                    int c = (int) (targetX / GameSettings.BUBBLE_DIAMETER);

                    // Если луч коснулся существующего Смешарика, взводим стоп-кран
                    if (c >= 0 && c < GameSettings.GRID_COLS && bubbleGrid[r][c] != null) {
                        hitSomething = true;
                        break;
                    }
                }

                // Рисуем круглую точку прицела (радиус 4 пикселя)
                shapeRenderer.circle(dotX, dotY, 4f);
                dotsDrawn++;
                stepAccumulator += dotSpacing;
            }

            if (hitSomething) break;

            stepAccumulator -= segmentLength;
            currentX += dirX * tMin;
            currentY += dirY * tMin;

            if (tMin == tWall) {
                dirX = -dirX; // Зеркальный рикошет от боковой стены
            } else {
                break;
            }
        }

        shapeRenderer.end(); // Жестко закрыли ShapeRenderer, батч не трогаем!
    }

    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
