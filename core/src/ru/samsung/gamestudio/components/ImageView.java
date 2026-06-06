package ru.samsung.gamestudio.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ImageView extends View {

    public Texture texture;

    public ImageView(float x, float y, String imagePath) {
        super(x, y);
        texture = new Texture(imagePath);
        this.width = texture.getWidth() ;
        this.height = texture.getHeight() ;
    }
    // === НАШ НОВЫЙ УМНЫЙ КОНСТРУКТОР ДЛЯ ОБЛАКОВ ===
    // Позволяет вручную задать ширину и высоту (например, растянуть во весь экран)
    public ImageView(float x, float y, float width, float height, String imagePath) {
        super(x, y);
        texture = new Texture(imagePath);
        this.width = width;
        this.height = height;
    }
    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

}