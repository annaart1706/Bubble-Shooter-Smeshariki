package ru.samsung.gamestudio;

import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

import ru.samsung.gamestudio.managers.MemoryManager;


public class GameSession {

    public GameState state;
    public long nextTrashSpawnTime;
    long sessionStartTime;
    long pauseStartTime;
    private int score;
    int destructedSmesharikNumber;
    public boolean isGameOverTriggered = false;
    public int lastRecordIndex = -1;

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

        isGameOverTriggered = true;

        ArrayList<Integer> recordsTable = MemoryManager.loadRecordsTable();
        if (recordsTable == null) {
            recordsTable = new ArrayList<>();
        }
        int foundIdx = 0;
        for (; foundIdx < Math.min(recordsTable.size(), 5); foundIdx++) {
            if (recordsTable.get(foundIdx) < getScore()) break;
        }

        if (foundIdx < 5) {
            lastRecordIndex = foundIdx;
        } else {
            lastRecordIndex = -1;
        }

        recordsTable.add(foundIdx, getScore());
        MemoryManager.saveTableOfRecords(recordsTable);
    }
    public int getScore() {
        return score;
    }

    public float getTrashPeriodCoolDown() {
        return (float) Math.exp(-0.0012 * (TimeUtils.millis() - sessionStartTime + 1) / 1000);
    }
    public void addScore(int value){
        score+=value;
    }
    public void updateScore() {

    }
}
