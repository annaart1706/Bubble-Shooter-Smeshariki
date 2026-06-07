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
        foreground= new MovingBackgroundView(GameResources.FOREGROUND_IMG_PATH_MENU);
//        titleView = new TextView(myGdxGame.largeWhiteFont, 180, 960, "Смешарики");
//        startButtonView = new ButtonView(140, 646, 440, 70, myGdxGame.commonBlackFont, GameResources.BUTTON_LONG_BG_IMG_PATH, "start");
//        settingsButtonView = new ButtonView(140, 551, 440, 70, myGdxGame.commonBlackFont, GameResources.BUTTON_LONG_BG_IMG_PATH, "settings");
//        exitButtonView = new ButtonView(140, 456, 440, 70, myGdxGame.commonBlackFont, GameResources.BUTTON_LONG_BG_IMG_PATH, "exit");

        // Объяви переменную InfoScreen infoScreen сверху класса, а в create() инициализируй:

        titleView = new TextView(myGdxGame.largeWhiteFont, 160, 1100, "Смешарики: В Облака!"); // Новое крутое русское название!
        startButtonView = new ButtonView(140, 400, 440, 65, myGdxGame.commonBlackFont, GameResources.BUTTON_LONG_BG_IMG_PATH, "старт");
        settingsButtonView = new ButtonView(140, 310, 440, 65, myGdxGame.commonBlackFont, GameResources.BUTTON_LONG_BG_IMG_PATH, "настройки");

// Наша новая кнопка справки! 📖✨

        infoButtonView = new ButtonView(140, 220, 440, 65, myGdxGame.commonBlackFont, GameResources.BUTTON_LONG_BG_IMG_PATH, "справка");

        exitButtonView = new ButtonView(140, 130, 440, 65, myGdxGame.commonBlackFont, GameResources.BUTTON_LONG_BG_IMG_PATH, "выход");
    }

    @Override
    public void show() {
        // Как только игрок зашел в Главное меню — включается тема меню (индекс 1)
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

            // === ВНУТРИ handleInput() В MenuScreen.java ===
            if (startButtonView.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                myGdxGame.setScreen(myGdxGame.gameScreen); // Просто меняем экран, без лишних вызовов!
            }
            if (settingsButtonView.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                myGdxGame.setScreen(myGdxGame.settingsScreen); // Просто меняем экран
            }


            // КЛИК ПО КНОПКЕ ВЫХОДА
            if (exitButtonView.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                // ИСПРАВЛЕНИЕ: Перед выходом глушим музыку, чтобы она не зависала на секунду в системе
                if (myGdxGame.audioManager != null) {
                    myGdxGame.audioManager.stopMusic();
                }
                Gdx.app.exit();
            }
            if (infoButtonView.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                myGdxGame.setScreen(myGdxGame.infoScreen); // Переходим на экран способностей
            }

        }
    }

}
