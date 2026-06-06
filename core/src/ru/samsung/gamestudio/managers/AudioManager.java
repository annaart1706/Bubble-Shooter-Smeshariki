package ru.samsung.gamestudio.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {

    // Секретное оружие против какофонии: единственная копия на всю игру!
    private static AudioManager instance;

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public boolean isSoundOn;
    public boolean isMusicOn;

    public Music menuMusic;
    public Music gameMusic;
    public Music settingsMusic;
    public Music bibiMusic;
    public Music karychMusic;

    public Music shootSound;
    public Music explosionSound;

    private Music currentMusic;

    // Приватный конструктор: теперь НИКТО в других классах не сможет написать new AudioManager()!
    private AudioManager() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/menu_theme.mp3"));
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/game_theme.mp3"));
        settingsMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/settings_theme.mp3"));
        bibiMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/bibi_theme.mp3"));
        karychMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/karych_theme.mp3"));

        // ИСПРАВЛЕНО: Загружаем твои файлы как потоковую музыку, чтобы не ломать буферы OpenAL
        shootSound = Gdx.audio.newMusic(Gdx.files.internal("sounds/shoot_signal.mp3"));
        explosionSound = Gdx.audio.newMusic(Gdx.files.internal("sounds/bubble_stick.mp3"));

        menuMusic.setVolume(0.15f);
        menuMusic.setLooping(true);

        gameMusic.setVolume(0.12f);
        gameMusic.setLooping(true);

        settingsMusic.setVolume(0.15f);
        settingsMusic.setLooping(true);

        bibiMusic.setVolume(0.4f);
        karychMusic.setVolume(0.3f);

        updateSoundFlag();
        isMusicOn = MemoryManager.loadIsMusicOn();
    }

    public void updateSoundFlag() {
        isSoundOn = MemoryManager.loadIsSoundOn();
    }

    public void playMusicForState(int stateIndex) {
        // 1. СТРОЖАЙШЕЕ ГЛУШЕНИЕ: останавливаем вообще всё перед выбором трека
        if (menuMusic != null) menuMusic.stop();
        if (gameMusic != null) gameMusic.stop();
        if (settingsMusic != null) settingsMusic.stop();
        if (bibiMusic != null) bibiMusic.stop();
        if (karychMusic != null) karychMusic.stop();

        // 2. Назначаем текущий трек
        if (stateIndex == 1) {
            currentMusic = menuMusic;
        } else if (stateIndex == 2) {
            currentMusic = gameMusic;
        } else if (stateIndex == 3) {
            currentMusic = settingsMusic;
        }

        // 3. ЖЕЛЕЗНЫЙ ЗАМОК ОТ КАКОФОНИИ 🛡️
        // Если код пытается включить музыку меню (stateIndex == 1), но при этом
        // активен экран игры или раунд еще не закрыт в меню, мы принудительно
        // подменяем музыку на ИГРОВУЮ, не давая менюшному треку прорваться в эфир!
        if (stateIndex == 1 && ru.samsung.gamestudio.screens.GameScreen.class.isInstance(Gdx.app.getApplicationListener())) {
            currentMusic = gameMusic;
        }

        // 4. Запускаем чистый одиночный трек
        if (isMusicOn && currentMusic != null) {
            currentMusic.play();
        }
    }


    public void playSpecialMusic(Music specialTrack) {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
        if (isMusicOn && specialTrack != null) {
            specialTrack.play();
        }
    }

    public void restoreBackgroundMusic(Music specialTrack) {
        if (specialTrack != null) {
            specialTrack.stop();
        }
        if (isMusicOn && currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }

    public void updateMusicFlag() {
        isMusicOn = MemoryManager.loadIsMusicOn();
        if (isMusicOn) {
            if (currentMusic != null && !currentMusic.isPlaying()) {
                currentMusic.play();
            }
        } else {
            stopMusic();
        }
    }

    // 3. ПЕРЕПИСЫВАЕМ МЕТОД ВЫСТРЕЛА С АВТО-ВЫКЛЮЧАТЕЛЕМ ЧЕРЕЗ 0.4 СЕКУНДЫ ⏱️⚡
    public void playShoot() {
        if (isSoundOn && shootSound != null) {
            shootSound.setVolume(0.25f);
            shootSound.stop(); // На всякий случай сбрасываем в начало, если стреляем часто
            shootSound.play(); // Запускаем чистый трек

            // ХИТРЫЙ ТАЙМЕР: Сказать игре выключить звук, не проигрывая его до конца!
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    if (shootSound != null) {
                        shootSound.stop(); // Ровно через 0.4 секунды музыка СТОПАЕТСЯ, имитируя короткий щелчок!
                    }
                }
            }, 0.4f); // Время звучания в секундах (можешь поменять на 0.3f или 0.5f, как красивее звучит)
        }
    }

    // 4. ПЕРЕПИСЫВАЕМ МЕТОД ПРИКЛЕИВАНИЯ С АВТО-ВЫКЛЮЧАТЕЛЕМ
    public void playExplosion() {
        if (isSoundOn && explosionSound != null) {
            explosionSound.setVolume(0.30f);
            explosionSound.stop();
            explosionSound.play();

            // Таймер для звука прилипания к сетке (пусть звучит чуть короче, например 0.3 секунды)
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    if (explosionSound != null) {
                        explosionSound.stop(); // Глушим длинный файл, оставляя только милое начало
                    }
                }
            }, 0.3f);
        }
    }

    public void stopMusic() {
        if (currentMusic != null) currentMusic.stop();
        if (bibiMusic != null) bibiMusic.stop();
        if (karychMusic != null) karychMusic.stop();
    }
}
