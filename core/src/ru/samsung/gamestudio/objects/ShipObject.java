package ru.samsung.gamestudio.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.TimeUtils;
import ru.samsung.gamestudio.GameSettings;

public class ShipObject extends GameObject {

    public int livesLeft;
    private float rotation = 0f;
    public ShipObject(int x, int y, int width, int height, String texturePath, World world) {
        super(texturePath, x, y, width, height, GameSettings.SHIP_BIT, world);
        body.setLinearDamping(0);
        livesLeft = 3;

    }
    public float getRotation(){
        return rotation;
    }
    public void setRotation(float rotation){
        this.rotation = rotation;
    }

    @Override
    public void draw(SpriteBatch batch) {
        float originX = width / 2f;
        float originY = height / 2f;
        batch.draw(texture,
                getX() - originX,
                getY() - originY,
                originX,
                originY,
                width,
                height,
                1f,
                1f,
                rotation,
                0, 0,
                texture.getWidth(),
                texture.getHeight(),
                false, false
        );
    }

    @Override
    public void hit() {
        livesLeft -= 1;
    }

    public boolean isAlive() {
        return livesLeft > 0;
    }
}
