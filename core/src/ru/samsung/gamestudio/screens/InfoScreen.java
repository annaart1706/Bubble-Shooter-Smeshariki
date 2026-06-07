package ru.samsung.gamestudio.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import ru.samsung.gamestudio.GameResources;
import ru.samsung.gamestudio.GameSettings;
import ru.samsung.gamestudio.MyGdxGame;
import ru.samsung.gamestudio.components.ButtonView;
import ru.samsung.gamestudio.components.ImageView;
import ru.samsung.gamestudio.components.MovingBackgroundView;
import ru.samsung.gamestudio.components.TextView;

public class InfoScreen extends ScreenAdapter {

    MyGdxGame myGdxGame;
    MovingBackgroundView backgroundView;
    ImageView blackoutView;
    ButtonView backButton;

    // Массивы для иконок и текстов
    ImageView[] iconViews;
    TextView[] descViews;

    // === ВНУТРИ InfoScreen.java ===
    private final String[] descriptions = {
            "КРОШ: ураганный снос половины ряда 🌪️",
            "НЮША: пробивает стрелой 3 ячейки вверх 💘",
            "ЁЖИК: феноменальный взрыв всей матрицы! 👓",
            "КОПАТЫЧ: сбор урожая одного цвета с поля 🐻",
            "БАРАШ: красит соседей под цвет пушки 🐑",
            "КАРЫЧ: заморозка времени на 7 секунд 🐧",
            "ЛОСЯШ: тотальный снос нижнего ряда 🦌",
            "ПИН: осколочный взрыв 3-х соседей 🔧",
            "СОВУНЬЯ: даёт в пушку самый частый на поле шар! 🦉", // ОБНОВЛЕНО 🌟
            "БИБИ: легендарная мега-бомба (шанс 1%) 🤖"
    };


    public InfoScreen(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;

        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_IMG_PATH);
        blackoutView = new ImageView(50, 150, 620, 950, GameResources.BLACKOUT_MIDDLE_IMG_PATH);

        backButton = new ButtonView(280, 70, 160, 60, myGdxGame.commonBlackFont, GameResources.BUTTON_SHORT_BG_IMG_PATH, "назад");

        iconViews = new ImageView[10];
        descViews = new TextView[10];

        int startY = 1020;
        int stepY = 85;

        for (int i = 0; i < 10; i++) {
            iconViews[i] = new ImageView(80, startY - (i * stepY), 50, 50, GameSettings.BUBBLE_TEXTURES[i]);
            descViews[i] = new TextView(myGdxGame.commonWhiteFont, 150, startY - (i * stepY) + 35, descriptions[i]);
        }
    }

    @Override
    public void show() {
        if (myGdxGame != null && myGdxGame.audioManager != null) {
            myGdxGame.audioManager.playMusicForState(3);
        }
    }

    @Override
    public void render(float delta) {
        handleInput();

        myGdxGame.camera.update();
        myGdxGame.batch.setProjectionMatrix(myGdxGame.camera.combined);
        ScreenUtils.clear(Color.CLEAR);

        myGdxGame.batch.begin();

        backgroundView.draw(myGdxGame.batch);
        blackoutView.draw(myGdxGame.batch);
        backButton.draw(myGdxGame.batch);

        for (int i = 0; i < 10; i++) {
            if (iconViews[i] != null) iconViews[i].draw(myGdxGame.batch);
            if (descViews[i] != null) descViews[i].draw(myGdxGame.batch);
        }

        myGdxGame.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            myGdxGame.touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (backButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                myGdxGame.setScreen(myGdxGame.menuScreen);
            }
        }
    }
}
