package ru.samsung.gamestudio;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.TimeUtils;
import ru.samsung.gamestudio.managers.MemoryManager;

import java.util.ArrayList;


public class GameSession {

    public GameState state;
    public long nextTrashSpawnTime;
    long sessionStartTime;
    long pauseStartTime;
    private int score;
    int destructedSmesharikNumber;
    // Флаг, который сообщит экрану игры, что нужно запустить анимацию падения
    public boolean isGameOverTriggered = false;


    public GameSession() {
    }

    public void startGame() {
        state = GameState.PLAYING;
        score = 0;
        destructedSmesharikNumber = 0;
        sessionStartTime = TimeUtils.millis();
        nextTrashSpawnTime = sessionStartTime + (long) (GameSettings.STARTING_TRASH_APPEARANCE_COOL_DOWN
                * getTrashPeriodCoolDown());
    }

    public void pauseGame() {
        state = GameState.PAUSED;
        pauseStartTime = TimeUtils.millis();
    }

    public void resumeGame() {
        state = GameState.PLAYING;
        sessionStartTime += TimeUtils.millis() - pauseStartTime;
    }

    public void endGame() {
        state = GameState.ENDED;

        // Взводим флаг: игра закончена, пора ронять Смешариков!
        isGameOverTriggered = true;

        ArrayList<Integer> recordsTable = MemoryManager.loadRecordsTable();
        if (recordsTable == null) {
            recordsTable = new ArrayList<>();
        }
        int foundIdx = 0;
        for (; foundIdx < Math.min(recordsTable.size(), 5); foundIdx++) {
            if (recordsTable.get(foundIdx) < getScore()) break;
        }
        recordsTable.add(foundIdx, getScore());
        MemoryManager.saveTableOfRecords(recordsTable);
    }
    public int getScore() {
        return score;
    }

    public float getTrashPeriodCoolDown() {
        return (float) Math.exp(-0.0003 * (TimeUtils.millis() - sessionStartTime + 1) / 1000);
    }
    public void addScore(int value){
        score+=value;
    }
    public void updateScore() {

    }


}
