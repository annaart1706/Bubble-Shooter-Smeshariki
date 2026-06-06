package ru.samsung.gamestudio.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;


import ru.samsung.gamestudio.GameSettings;

import java.util.Random;

import javax.swing.plaf.nimbus.State;

public class Smesharik extends GameObject {
    private int livesLeft;

    public enum State {NEW, FLYING, FIXED, FULLING}

    ;
    public State currentState;
    private int colorType;
    private int row = -1, col = -1;
    // Добавь к переменным класса Smesharik:
    public boolean hitCeilingDirectly = false;


    public Smesharik(String texturePath, int x, int y, int colorType, World world, boolean isProjectile) {
        super(
                texturePath,
                x,
                y,
                GameSettings.BUBBLE_DIAMETER,
                GameSettings.BUBBLE_DIAMETER,
                GameSettings.TRASH_BIT,
                world
        );

        this.livesLeft = 1; // Теперь каждый Смешарик официально "жив" при создании
        this.colorType = colorType;
        body.setGravityScale(0);

        if (isProjectile) {
            this.currentState = State.NEW;
            this.body.setType(BodyDef.BodyType.DynamicBody);
            this.body.setLinearDamping(0f);
        } else {
            this.currentState = State.FIXED;
            this.body.setType(BodyDef.BodyType.StaticBody);
            this.body.setLinearVelocity(0, 0);
        }

    }


    public State getCurrentState() {
        return currentState;
    }

    public void setState(State bubbleState) {
        currentState = bubbleState;
    }

    public int getColorType() {
        return colorType;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    // И добавь метод:
    public void changeColor(int newColorId) {
        this.colorType = newColorId;
        // Сам забирает путь к текстуре из твоего массива настроек по индексу!
        String texturePath = GameSettings.BUBBLE_TEXTURES[newColorId];
        this.texture = new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal(texturePath));
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {

        this.col = col;
    }

    public boolean isAlive() {
        return livesLeft > 0;
    }

    public boolean isInFrame() {
        return getY() + height / 2 > 0;
    }

    @Override
    public void hit() {
        livesLeft -= 1;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, getX() - (width / 2f), getY() - (height / 2f), width, height);
    }
}
