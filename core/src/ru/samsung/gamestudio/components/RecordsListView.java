package ru.samsung.gamestudio.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

import ru.samsung.gamestudio.GameSettings;

public class RecordsListView extends TextView {

    private ArrayList<String> recordsLines = new ArrayList<>();
    private float startY;
    private int highlightedIdx = -1;

    public RecordsListView(BitmapFont font, float y) {
        super(font, 0, y, "");
        this.startY = y;
    }

    public void setRecordsWithHighlight(ArrayList<Integer> recordsList, int highlightIndex) {
        this.highlightedIdx = highlightIndex;
        recordsLines.clear();

        if (recordsList == null || recordsList.isEmpty()) {
            recordsLines.add("Ещё нет рекордов!");
            return;
        }

        int countOfRows = Math.min(recordsList.size(), 5);
        for (int i = 0; i < countOfRows; i++) {
            String rowText = (i + 1) + ". - " + recordsList.get(i);

            if (i == highlightedIdx) {
                rowText += "  ★ ТВОЙ РЕЗУЛЬТАТ ★";
            }
            recordsLines.add(rowText);
        }
    }

    public void drawRecords(SpriteBatch batch) {
        float currentY = startY;

        for (int i = 0; i < recordsLines.size(); i++) {
            String line = recordsLines.get(i);
            GlyphLayout layout = new GlyphLayout(font, line);
            float currentX = (GameSettings.SCREEN_WIDTH - layout.width) / 2;

            if (i == highlightedIdx) {
                font.setColor(Color.YELLOW);
            } else {
                font.setColor(Color.WHITE);
            }

            font.draw(batch, line, currentX, currentY);
            currentY -= 55f;
        }

        font.setColor(Color.WHITE);
    }
}