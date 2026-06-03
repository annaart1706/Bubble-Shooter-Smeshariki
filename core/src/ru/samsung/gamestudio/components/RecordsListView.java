package ru.samsung.gamestudio.components;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ru.samsung.gamestudio.GameSettings;

import java.util.ArrayList;

public class RecordsListView extends TextView {

    private ArrayList<String> recordsLines = new ArrayList<>();
    private float startY;

    public RecordsListView(BitmapFont font, float y) {
        super(font, 0, y, "");
        this.startY = y;
    }

    public void setRecords(ArrayList<Integer> recordsList) {
        recordsLines.clear();

        if (recordsList == null || recordsList.isEmpty()) {
            recordsLines.add("No records yet!");
            return;
        }

        int countOfRows = Math.min(recordsList.size(), 5);
        for (int i = 0; i < countOfRows; i++) {
            String rowText = (i + 1) + ". - " + recordsList.get(i);
            recordsLines.add(rowText);
        }
    }

    // ИСПРАВЛЕНО: Изменили имя на drawRecords и убрали @Override,
    // чтобы полностью исключить конфликты со старым TextView из шутера!
    public void drawRecords(SpriteBatch batch) {
        float currentY = startY;

        for (String line : recordsLines) {
            GlyphLayout layout = new GlyphLayout(font, line);
            float currentX = (GameSettings.SCREEN_WIDTH - layout.width) / 2;

            // Рисуем чистым шрифтом прямо в открытый батч из GameScreen
            font.draw(batch, line, currentX, currentY);
            currentY -= 45f;
        }
    }
}
