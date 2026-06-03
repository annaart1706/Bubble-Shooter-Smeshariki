package ru.samsung.gamestudio;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main (String[] arg) {
        try {
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

            // Настраиваем заголовок окна вашей игры про Смешариков
            config.setTitle("Smeshariki Bubble Shooter");

            // Жестко задаем стандартное портретное разрешение (как на телефоне)
            config.setWindowedMode(720, 1280);

            // Защита: отключаем аудио-поток, если на компьютере не подключены наушники/колонки,
            // чтобы OpenAL движок LibGDX не крашил запуск окна с кодом 1
            config.setAudioConfig(64, 512, 9);

            // Запускаем саму игру
            new Lwjgl3Application(new MyGdxGame(), config);

        } catch (Exception e) {
            System.err.println("!!! КРИТИЧЕСКИЙ СБОЙ ПРИ ИНИЦИАЛИЗАЦИИ ОКНА LWJGL3 !!!");
            e.printStackTrace();
        }
    }
}
