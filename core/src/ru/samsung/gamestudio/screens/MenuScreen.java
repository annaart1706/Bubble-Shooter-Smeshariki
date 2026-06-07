package ru.samsung.gamestudio.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import ru.samsung.gamestudio.GameResources;
import ru.samsung.gamestudio.MyGdxGame;
import ru.samsung.gamestudio.components.ButtonView;
import ru.samsung.gamestudio.components.MovingBackgroundView;
import ru.samsung.gamestudio.components.TextView;

public class MenuScreen extends ScreenAdapter {

    MyGdxGame myGdxGame;

    MovingBackgroundView backgroundView;
    TextView titleView;
    ButtonView startButtonView;
    ButtonView settingsButtonView;
    ButtonView exitButtonView;
    MovingBackgroundView foreground;
    public ButtonView infoButtonView;

    public MenuScreen(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;

        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_IMG_PATH);
        foreground = new MovingBackgroundView(GameResources.FOREGROUND_IMG_PATH_MENU);

        titleView = new TextView(myGdxGame.largeWhiteFont, 100, 1100, "Смешарики: Большой Бабах!");
        startButtonView = new ButtonView(140, 400, 440, 65, myGdxGame.commonBlackFont, GameResources.BUTTON_LONG_BG_IMG_PATH, "старт");
        settingsButtonView = new ButtonView(140, 310, 440, 65, myGdxGame.commonBlackFont, GameResources.BUTTON_LONG_BG_IMG_PATH, "настройки");
        infoButtonView = new ButtonView(140, 220, 440, 65, myGdxGame.commonBlackFont, GameResources.BUTTON_LONG_BG_IMG_PATH, "справка");
        exitButtonView = new ButtonView(140, 130, 440, 65, myGdxGame.commonBlackFont, GameResources.BUTTON_LONG_BG_IMG_PATH, "выход");
    }

    @Override
    public void show() {
        if (myGdxGame != null && myGdxGame.audioManager != null) {
            myGdxGame.audioManager.playMusicForState(1);
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
        foreground.draw(myGdxGame.batch);
        titleView.draw(myGdxGame.batch);
        exitButtonView.draw(myGdxGame.batch);
        settingsButtonView.draw(myGdxGame.batch);
        startButtonView.draw(myGdxGame.batch);
        infoButtonView.draw(myGdxGame.batch);

        myGdxGame.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            myGdxGame.touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (startButtonView.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                myGdxGame.setScreen(myGdxGame.gameScreen);
                if (settingsButtonView.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                    myGdxGame.setScreen(myGdxGame.settingsScreen);
                }

                if (exitButtonView.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                    if (myGdxGame.audioManager != null) {
                        myGdxGame.audioManager.stopMusic();
                    }
                    Gdx.app.exit();
                }
                if (infoButtonView.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                    myGdxGame.setScreen(myGdxGame.infoScreen);
                }
            }
        }
    }
}