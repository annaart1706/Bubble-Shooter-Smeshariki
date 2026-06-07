package ru.samsung.gamestudio.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import static ru.samsung.gamestudio.GameSettings.SCALE;

public class GameObject {

    public short cBits;

    public int width;
    public int height;

    public Body body;
    Texture texture;

    GameObject(String texturePath, int x, int y, int width, int height, short cBits, World world) {
        this.width = width;
        this.height = height;
        this.cBits = cBits;

        texture = new Texture(texturePath);
        body = createBody(x, y, world);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture,
                getX() - (width / 2f),
                getY() - (height / 2f),
                width,
                height);
    }

    public void hit() {

    }

    private int fallbackX = 0;
    private int fallbackY = 0;

    public int getX() {
        if (body != null) {
            return (int) (body.getPosition().x / SCALE);
        }
        return fallbackX;
    }

    public int getY() {
        if (body != null) {
            return (int) (body.getPosition().y / SCALE);
        }
        return fallbackY;
    }

    public void setX(int x) {
        fallbackX = x;
        if (body != null) {
            body.setTransform(x * SCALE, body.getPosition().y, 0);
        }
    }

    public void setY(int y) {
        fallbackY = y;
        if (body != null) {
            body.setTransform(body.getPosition().x, y * SCALE, 0);
        }
    }

    private Body createBody(float x, float y, World world) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;

        def.fixedRotation = true;
        Body body = world.createBody(def);

        CircleShape circleShape = new CircleShape();

        float physicalRadius = (Math.max(width, height) * SCALE / 2f) * 0.85f;
        circleShape.setRadius(physicalRadius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 0.1f;

        fixtureDef.filter.categoryBits = cBits;
        fixtureDef.friction = 0.0f;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        circleShape.dispose();

        body.setTransform(x * SCALE, y * SCALE, 0);
        return body;
    }
}
