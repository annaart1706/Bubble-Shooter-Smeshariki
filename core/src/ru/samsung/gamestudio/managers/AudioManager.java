package ru.samsung.gamestudio.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {

    private static AudioManager instance;

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

    private AudioManager() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/menu_theme.mp3"));
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/game_theme.mp3"));
        settingsMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/settings_theme.mp3"));
        bibiMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/bibi_theme.mp3"));
        karychMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/karych_theme.mp3"));

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

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void updateSoundFlag() {
        isSoundOn = MemoryManager.loadIsSoundOn();
    }

    public void playMusicForState(int stateIndex) {

        if (menuMusic != null) menuMusic.stop();
        if (gameMusic != null) gameMusic.stop();
        if (settingsMusic != null) settingsMusic.stop();
        if (bibiMusic != null) bibiMusic.stop();
        if (karychMusic != null) karychMusic.stop();

        if (stateIndex == 1) {
            currentMusic = menuMusic;
        } else if (stateIndex == 2) {
            currentMusic = gameMusic;
        } else if (stateIndex == 3) {
            currentMusic = settingsMusic;
        }

        if (stateIndex == 1 && ru.samsung.gamestudio.screens.GameScreen.class.isInstance(Gdx.app.getApplicationListener())) {
            currentMusic = gameMusic;
        }

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

    public void playShoot() {
        if (isSoundOn && shootSound != null) {
            shootSound.setVolume(0.25f);
            shootSound.stop();
            shootSound.play();

            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    if (shootSound != null) {
                        shootSound.stop();
                    }
                }
            }, 0.4f);
        }
    }

    public void playExplosion() {
        if (isSoundOn && explosionSound != null) {
            explosionSound.setVolume(0.30f);
            explosionSound.stop();
            explosionSound.play();

            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    if (explosionSound != null) {
                        explosionSound.stop();
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