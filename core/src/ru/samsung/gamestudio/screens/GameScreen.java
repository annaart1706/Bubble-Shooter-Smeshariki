package ru.samsung.gamestudio.screens;

import static ru.samsung.gamestudio.GameState.PLAYING;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import ru.samsung.gamestudio.GameResources;
import ru.samsung.gamestudio.GameSession;
import ru.samsung.gamestudio.GameSettings;
import ru.samsung.gamestudio.GameState;
import ru.samsung.gamestudio.MyGdxGame;
import ru.samsung.gamestudio.components.ButtonView;
import ru.samsung.gamestudio.components.ImageView;
import ru.samsung.gamestudio.components.LiveView;
import ru.samsung.gamestudio.components.MovingBackgroundView;
import ru.samsung.gamestudio.components.RecordsListView;
import ru.samsung.gamestudio.components.TextView;
import ru.samsung.gamestudio.managers.ContactManager;
import ru.samsung.gamestudio.managers.MemoryManager;
import ru.samsung.gamestudio.objects.AimLine;
import ru.samsung.gamestudio.objects.ShipObject;
import ru.samsung.gamestudio.objects.Smesharik;

public class GameScreen extends ScreenAdapter {

    private MyGdxGame myGdxGame;
    GameSession gameSession;
    private ShipObject shipObject;
    ArrayList<Smesharik> smeshariks;
    ContactManager contactManager;

    // PLAY state UI
    MovingBackgroundView backgroundView;
   LiveView liveView;
    TextView scoreTextView;
    ButtonView pauseButton;

    // PAUSED state UI
    ImageView fullBlackoutView;
    TextView pauseTextView;
    ButtonView homeButton;
    ButtonView continueButton;

    // ENDED state UI
    TextView recordsTextView;
    RecordsListView recordsListView;
    ButtonView homeButton2;
    private Smesharik[][] bubbleGrid;
    private Smesharik currentBall;

    private boolean wasTouchedLastFrame = false;

    private ArrayList<Smesharik> flyingBalls = new java.util.ArrayList<>();
    private ArrayList<Smesharik> ballsToRemove = new ArrayList<>();
    private AimLine aimLine;

    private float shakeTimer = 0f;    // Текущее время тряски
    private float shakeDuration = 0f; // Общая длительность эффекта
    private float shakeIntensity = 0f;// Сила тряски (в пикселях)

    ImageView cloudLineView;
    private boolean isBibiReady = false;

    // Переменные для летающих фраз Смешариков
    private String popupText = "";
    private float popupTimer = 0f;
    private final float POPUP_DURATION = 1.8f; // Фраза будет висеть на экране 1.8 секунды
    private float popupY = 0f; // Для плавной анимации взлета текста вверх


    public GameScreen(MyGdxGame myGdxGame) {

            this.myGdxGame = myGdxGame;
            gameSession = new GameSession();

            contactManager = new ContactManager(myGdxGame.world);

            smeshariks = new ArrayList<>();


            shipObject = new ShipObject(
                    GameSettings.SCREEN_WIDTH / 2, 150,
                    GameSettings.SHIP_WIDTH, GameSettings.SHIP_HEIGHT,
                    GameResources.SHIP_IMG_PATH,
                    myGdxGame.world
            );

            backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_IMG_PATH);
            cloudLineView = new ImageView(0, 0, GameSettings.SCREEN_WIDTH, GameSettings.CRITICAL_Y_LINE,"textures/cloud_border.png");
            scoreTextView = new TextView(myGdxGame.commonWhiteFont, 80, 60);
            pauseButton = new ButtonView(620, 70, 46, 54, GameResources.PAUSE_IMG_PATH);

            fullBlackoutView = new ImageView(0, 0, GameResources.BLACKOUT_FULL_IMG_PATH);
            pauseTextView = new TextView(myGdxGame.largeWhiteFont, 282, 842, "Пауза");
            homeButton = new ButtonView(
                    138, 695,
                    200, 70,
                    myGdxGame.commonBlackFont,
                    GameResources.BUTTON_SHORT_BG_IMG_PATH,
                    "Домой"
            );
            continueButton = new ButtonView(
                    393, 695,
                    200, 70,
                    myGdxGame.commonBlackFont,
                    GameResources.BUTTON_SHORT_BG_IMG_PATH,
                    "Продолжить"
            );

            recordsListView = new RecordsListView(myGdxGame.commonWhiteFont, 690);
            recordsTextView = new TextView(myGdxGame.largeWhiteFont, 206, 842, "Last records");
            homeButton2 = new ButtonView(
                    280, 365,
                    160, 70,
                    myGdxGame.commonBlackFont,
                    GameResources.BUTTON_SHORT_BG_IMG_PATH,
                    "Домой"
            );
            aimLine = new ru.samsung.gamestudio.objects.AimLine();
        }

    @Override
    public void show() {
            if (myGdxGame != null && myGdxGame.audioManager != null) {
                myGdxGame.audioManager.playMusicForState(2);
            }
            restartGame();
    }

    @Override
    public void render(float delta) {
        handleInput();

        if (shakeTimer > 0) {
            shakeTimer -= delta;

            float currentShakeIntensity = shakeIntensity * (shakeTimer / shakeDuration);
            float shakeX = (float) (Math.random() * 2 - 1) * currentShakeIntensity;
            float shakeY = (float) (Math.random() * 2 - 1) * currentShakeIntensity;

            myGdxGame.camera.position.set(GameSettings.SCREEN_WIDTH / 2f + shakeX, GameSettings.SCREEN_HEIGHT / 2f + shakeY, 0);
        } else {
            myGdxGame.camera.position.set(GameSettings.SCREEN_WIDTH / 2f, GameSettings.SCREEN_HEIGHT / 2f, 0);
        }

        if (gameSession.state == GameState.ENDED) {
            if (gameSession.isGameOverTriggered) {
                triggerSmesharikoPad();
                gameSession.isGameOverTriggered = false;
            }

            updateSmesharikoPad(delta);
            draw();
            return;
        }

        if (gameSession.state == GameState.PLAYING) {

            if (com.badlogic.gdx.utils.TimeUtils.millis() > gameSession.nextTrashSpawnTime) {
                shiftGridDownAndSpawnRow();
                checkAndDropFloatingBalls();

                long coolDownDelta = (long) (GameSettings.STARTING_TRASH_APPEARANCE_COOL_DOWN
                        * gameSession.getTrashPeriodCoolDown());

                gameSession.nextTrashSpawnTime = com.badlogic.gdx.utils.TimeUtils.millis() + coolDownDelta;
            }

            if (!shipObject.isAlive()) {
                gameSession.endGame();
            }

            backgroundView.move();
            gameSession.updateScore();
            scoreTextView.setText("Очки: " + gameSession.getScore());

            myGdxGame.stepWorld();

            if (bubbleGrid != null) {
                for (int i = 0; i < GameSettings.GRID_ROWS; i++) {
                    for (int j = 0; j < GameSettings.GRID_COLS; j++) {
                        if (bubbleGrid[i][j] != null && bubbleGrid[i][j].getCurrentState() != Smesharik.State.FALLING) {
                            bubbleGrid[i][j].body.setLinearVelocity(0, 0);
                            bubbleGrid[i][j].body.setAngularVelocity(0);
                        }
                    }
                }
            }

            ArrayList<Smesharik> ballsToFix = contactManager.getBallsToFix();
            if (ballsToFix != null && !ballsToFix.isEmpty()) {
                for (Smesharik ball : ballsToFix) {

                    myGdxGame.audioManager.playExplosion();
                    fixBallInGrid(ball);

                    if (gameSession.state == GameState.ENDED) {
                        contactManager.clearBallsToFix();
                        break;
                    }

                    flyingBalls.remove(ball);
                    ball.body.setAwake(true);

                    if (ball.getColorType() == 9) {
                        if (ball.getRow() >= 0 && ball.getCol() >= 0) {
                            bubbleGrid[ball.getRow()][ball.getCol()] = null;
                        }
                        triggerPhrasePopup(9);
                        ArrayList<Smesharik> bibiList = new ArrayList<>();
                        bibiList.add(ball);
                        checkAndActivateSuperPowers(bibiList);
                        makeBallFall(ball);
                        checkAndDropFloatingBalls();
                        continue;
                    }

                    // Обработка обычных Смешариков "три в ряд"
                    ArrayList<Smesharik> matches = findMatches(ball);
                    if (matches.size() >= 3) {
                        int pointsForMatches = matches.size() * 100;
                        gameSession.addScore(pointsForMatches);

                        for (Smesharik matchBall : matches) {
                            bubbleGrid[matchBall.getRow()][matchBall.getCol()] = null;
                            makeBallFall(matchBall);
                        }

                        checkAndActivateSuperPowers(matches);

                        int ballsBeforeDrop = countFixedBalls();
                        checkAndDropFloatingBalls();
                        int ballsAfterDrop = countFixedBalls();
                        int droppedCount = ballsBeforeDrop - ballsAfterDrop;

                        if (droppedCount > 0) {
                            gameSession.addScore(droppedCount * 200);
                        }

                        // Если после всех взрывов и падений на поле НЕ ОСТАЛОСЬ ни одного шара
                        if (countFixedBalls() == 0) {

                            gameSession.addScore(5000);

                            triggerPhrasePopup(9);

                            if (myGdxGame.audioManager != null) {
                                myGdxGame.audioManager.playSpecialMusic(myGdxGame.audioManager.bibiMusic);

                                com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                                    @Override
                                    public void run() {
                                        if (gameSession.state == GameState.PLAYING) {
                                            myGdxGame.audioManager.restoreBackgroundMusic(myGdxGame.audioManager.bibiMusic);
                                        }
                                    }
                                }, 7.0f);
                            }

                            startScreenShake(0.6f, 9f);
                        }
                    }
                }
                contactManager.clearBallsToFix();
            }

            if (flyingBalls != null && !flyingBalls.isEmpty()) {
                for (int i = flyingBalls.size() - 1; i >= 0; i--) {
                    Smesharik flyingBall = flyingBalls.get(i);

                    if (flyingBall != null && flyingBall.getY() >= (GameSettings.SCREEN_HEIGHT - GameSettings.BUBBLE_DIAMETER)) {

                        flyingBalls.remove(i);
                        myGdxGame.audioManager.playExplosion();
                        fixBallInGrid(flyingBall);

                        if (flyingBall.getColorType() == 9) {
                            if (flyingBall.getRow() >= 0 && flyingBall.getCol() >= 0) {
                                bubbleGrid[flyingBall.getRow()][flyingBall.getCol()] = null;
                            }
                            ArrayList<Smesharik> bibiList = new ArrayList<>();
                            bibiList.add(flyingBall);
                            checkAndActivateSuperPowers(bibiList);
                            makeBallFall(flyingBall);
                            checkAndDropFloatingBalls();
                            continue;
                        }

                        // Проверка "три в ряд" для обычных шаров на потолке
                        ArrayList<Smesharik> matches = findMatches(flyingBall);
                        if (matches.size() >= 3) {
                            gameSession.addScore(matches.size() * 100);
                            for (Smesharik matchBall : matches) {
                                bubbleGrid[matchBall.getRow()][matchBall.getCol()] = null;
                                makeBallFall(matchBall);
                            }
                            checkAndActivateSuperPowers(matches);
                            checkAndDropFloatingBalls();
                            if (matches.size() >= 5) {
                                isBibiReady = true;
                            }
                        }
                    }
                }
            }
            updateSmesharikoPad(delta);
        }
        if (popupTimer > 0) {
            popupTimer -= delta;
            popupY += 35 * delta;
        }
        draw();
    }

    private void handleInput() {
           if (Gdx.input.isTouched()) {
        myGdxGame.touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    }

        switch (gameSession.state) {
        case PLAYING:
            if (Gdx.input.isTouched()) {
                if (pauseButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                    gameSession.pauseGame();
                    return;
                }
                float screenX = Gdx.input.getX();
                float screenY = Gdx.input.getY();

                Vector3 touchPoint = new Vector3(screenX, screenY, 0);
                myGdxGame.camera.unproject(touchPoint);

                float dx = touchPoint.x - shipObject.getX();
                float dy = touchPoint.y - shipObject.getY();
                float angle = (float) Math.atan2(dy, dx) * MathUtils.radiansToDegrees;
                angle -= 90;
                angle = MathUtils.clamp(angle, -75f, 75f);
                shipObject.setRotation(angle);
                if (currentBall != null) {
                    float angleRadians = MathUtils.degreesToRadians * angle;
                    currentBall.body.setTransform(currentBall.body.getPosition(), angleRadians);
                }
            }

            if (!Gdx.input.isTouched() && wasTouchedLastFrame) {
                if (myGdxGame.touch != null && !pauseButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                    if (currentBall != null) {
                        float distanceFromCannonY = myGdxGame.touch.y - shipObject.getY();
                        if (distanceFromCannonY > GameSettings.CANNON_CANCEL_ZONE) {

                            float angleRadians = shipObject.getRotation() * MathUtils.degreesToRadians;
                            float directionX = -MathUtils.sin(angleRadians);
                            float directionY = MathUtils.cos(angleRadians);

                            currentBall.body.applyLinearImpulse(
                                    directionX * GameSettings.SHOT_SPEED, directionY * GameSettings.SHOT_SPEED,
                                    currentBall.body.getWorldCenter().x, currentBall.body.getWorldCenter().y,
                                    true
                            );
                            currentBall.setState(Smesharik.State.FLYING);
                            currentBall.body.setBullet(true);

                            myGdxGame.audioManager.playShoot();
                            flyingBalls.add(currentBall);
                            currentBall = null;

                            myGdxGame.touch.set(0, 0, 0);

                            createNewBall();
                        }
                    }
                }
            }
            wasTouchedLastFrame = Gdx.input.isTouched();
break;


            case PAUSED:
            if (Gdx.input.justTouched()) {
                myGdxGame.touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

                if (continueButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                    gameSession.resumeGame();
                }
                if (homeButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                    myGdxGame.setScreen(myGdxGame.menuScreen);
                }
            }
            break;


        case ENDED:
            if (Gdx.input.justTouched()) {

                myGdxGame.touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

                if (homeButton2.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                    myGdxGame.setScreen(myGdxGame.menuScreen);
                }
            }
            break;
    }
}

    private void draw() {
        myGdxGame.camera.update();
        myGdxGame.batch.setProjectionMatrix(myGdxGame.camera.combined);
        com.badlogic.gdx.utils.ScreenUtils.clear(com.badlogic.gdx.graphics.Color.CLEAR);

        myGdxGame.batch.begin();

        if (backgroundView != null) backgroundView.draw(myGdxGame.batch);

        if (smeshariks != null) {
            for (Smesharik ball : smeshariks) {
                if (ball != null) ball.draw(myGdxGame.batch);
            }
        }
        if (flyingBalls != null) {
            for (Smesharik ball : flyingBalls) {
                if (ball != null) ball.draw(myGdxGame.batch);
            }
        }

        if (cloudLineView != null) {
            if (isSmesharikTooCloseToDanger()) {
                myGdxGame.batch.setColor(1.0f, 0.75f, 0.75f, 1.0f);
            } else {
                myGdxGame.batch.setColor(1.0f, 1.0f, 1.0f, 0.8f);
            }

            cloudLineView.draw(myGdxGame.batch);
            myGdxGame.batch.setColor(com.badlogic.gdx.graphics.Color.WHITE); // Сброс цвета
        }

        if (shipObject != null) shipObject.draw(myGdxGame.batch);
        if (currentBall != null) currentBall.draw(myGdxGame.batch);

        if (scoreTextView != null) scoreTextView.draw(myGdxGame.batch);
        if (liveView != null) liveView.draw(myGdxGame.batch);
        if (pauseButton != null) pauseButton.draw(myGdxGame.batch);

        if (gameSession.state == GameState.PAUSED) {
            if (fullBlackoutView != null) fullBlackoutView.draw(myGdxGame.batch);
            if (pauseTextView != null) pauseTextView.draw(myGdxGame.batch);
            if (homeButton != null) homeButton.draw(myGdxGame.batch);
            if (continueButton != null) continueButton.draw(myGdxGame.batch);
        }else if (gameSession.state == GameState.ENDED) {
        if (fullBlackoutView != null) fullBlackoutView.draw(myGdxGame.batch);

        String currentScoreText = "<<< ТВОЙ РЕЗУЛЬТАТ: " + gameSession.getScore();
        GlyphLayout scoreLayout = new GlyphLayout(myGdxGame.largeWhiteFont, currentScoreText);
        float scoreX = (GameSettings.SCREEN_WIDTH - scoreLayout.width) / 2;

        myGdxGame.largeWhiteFont.setColor(Color.GOLD);
        myGdxGame.largeWhiteFont.draw(myGdxGame.batch, currentScoreText, scoreX, 980);
        myGdxGame.largeWhiteFont.setColor(Color.WHITE); // сброс

        if (recordsTextView != null) {

            recordsTextView.setText("ТАБЛИЦА РЕКОРДОВ");

            GlyphLayout titleLayout = new GlyphLayout(myGdxGame.largeWhiteFont, "ТАБЛИЦА РЕКОРДОВ");
            float titleX = (GameSettings.SCREEN_WIDTH - titleLayout.width) / 2;
            myGdxGame.largeWhiteFont.draw(myGdxGame.batch, "ТАБЛИЦА РЕКОРДОВ", titleX, 860);
        }

        if (recordsListView != null) {
            recordsListView.drawRecords(myGdxGame.batch);
        }

        if (homeButton2 != null) homeButton2.draw(myGdxGame.batch);
    }
        if (popupTimer > 0 && popupText != null && !popupText.isEmpty()) {
            float alpha = Math.min(popupTimer / 0.5f, 1f);

            myGdxGame.largeWhiteFont.setColor(1f, 0.85f, 0.1f, alpha);

            myGdxGame.largeWhiteFont.draw(myGdxGame.batch, popupText, 160, popupY);

            myGdxGame.largeWhiteFont.setColor(Color.WHITE);
        }
        myGdxGame.batch.end();

        if (com.badlogic.gdx.Gdx.input.isTouched() && aimLine != null && currentBall != null) {
            aimLine.drawRecords(myGdxGame.batch, myGdxGame.camera.combined, bubbleGrid, shipObject, currentBall);
        }
    }

    private void restartGame() {

            if (contactManager != null) {
                contactManager.clearBallsToFix();
            }

            if (flyingBalls != null) flyingBalls.clear();
            if (smeshariks != null) smeshariks.clear();

            if (ballsToRemove != null) ballsToRemove.clear();
            currentBall = null;

            if (myGdxGame != null && myGdxGame.world != null) {
                com.badlogic.gdx.utils.Array<com.badlogic.gdx.physics.box2d.Body> bodies =
                        new com.badlogic.gdx.utils.Array<>();
                myGdxGame.world.getBodies(bodies);

                if (bodies.size > 0) {
                    for (com.badlogic.gdx.physics.box2d.Body b : bodies) {
                        myGdxGame.world.destroyBody(b);
                    }
                }
            }

            shipObject = null;
            bubbleGrid = new Smesharik[GameSettings.GRID_ROWS][GameSettings.GRID_COLS];

            for (int i = 0; i < GameSettings.GRID_ROWS / 2; i++) {
                spawnRow(i);
            }

            if (myGdxGame != null && myGdxGame.world != null) {
                shipObject = new ShipObject(
                        GameSettings.SCREEN_WIDTH / 2, 150,
                        GameSettings.SHIP_WIDTH, GameSettings.SHIP_HEIGHT,
                        GameResources.SHIP_IMG_PATH,
                        myGdxGame.world
                );
                createNewBall();
            }

            if (gameSession != null) {
                gameSession.startGame();
            }

            if (myGdxGame != null && myGdxGame.world != null) {
                BodyDef wallDef = new BodyDef();
                wallDef.type = BodyDef.BodyType.StaticBody;
                Body wallBody = myGdxGame.world.createBody(wallDef);

                FixtureDef wallFixtureDef = new FixtureDef();
                wallFixtureDef.restitution = 1.0f;
                wallFixtureDef.friction = 0.0f;
                wallFixtureDef.filter.categoryBits = GameSettings.FILTER_WALLS;

                EdgeShape edge = new EdgeShape();
                edge.set(0 * GameSettings.SCALE, 0 * GameSettings.SCALE,
                        0 * GameSettings.SCALE, GameSettings.SCREEN_HEIGHT * GameSettings.SCALE);
                wallFixtureDef.shape = edge;
                wallBody.createFixture(wallFixtureDef);

                edge.set(GameSettings.SCREEN_WIDTH * GameSettings.SCALE, 0 * GameSettings.SCALE,
                        GameSettings.SCREEN_WIDTH * GameSettings.SCALE, GameSettings.SCREEN_HEIGHT * GameSettings.SCALE);
                wallFixtureDef.shape = edge;
                wallBody.createFixture(wallFixtureDef);
                edge.dispose();

                BodyDef topWallDef = new BodyDef();
                topWallDef.type = BodyDef.BodyType.StaticBody;
                Body topWallBody = myGdxGame.world.createBody(topWallDef);

                FixtureDef topFixtureDef = new FixtureDef();
                topFixtureDef.restitution = 0.0f;
                topFixtureDef.friction = 0.0f;
                topFixtureDef.filter.categoryBits = GameSettings.FILTER_FIXED_BUBBLES;

                EdgeShape topEdge = new EdgeShape();
                topEdge.set(0 * GameSettings.SCALE, GameSettings.SCREEN_HEIGHT * GameSettings.SCALE,
                        GameSettings.SCREEN_WIDTH * GameSettings.SCALE, GameSettings.SCREEN_HEIGHT * GameSettings.SCALE);
                topFixtureDef.shape = topEdge;
                topWallBody.createFixture(topFixtureDef);
                topEdge.dispose();
            }

            if (recordsListView != null) {
                    ArrayList<Integer> table = MemoryManager.loadRecordsTable();
                    if (table == null) table = new ArrayList<>();
                    recordsListView.setRecordsWithHighlight(MemoryManager.loadRecordsTable(), gameSession.lastRecordIndex);
            }
    }

    private void createNewBall() {
        int randomId;

        if (Math.random() < 0.005f) {
            randomId = 9;
        } else {
            randomId = (int) (Math.random() * (GameSettings.BUBBLE_TEXTURES.length - 1));
        }

        String texturePath = GameSettings.BUBBLE_TEXTURES[randomId];
        int spawnY = shipObject.getY() + GameSettings.BUBBLE_DIAMETER;
        currentBall = new Smesharik(texturePath, shipObject.getX(), spawnY, randomId, myGdxGame.world, true);
    }

    private void fixBallInGrid(Smesharik flyingBall) {
        flyingBall.body.setLinearVelocity(0, 0);
        flyingBall.body.setAngularVelocity(0);
        flyingBall.body.setType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);
        flyingBall.setState(Smesharik.State.FIXED);

        int row;

        if (flyingBall.hitCeilingDirectly) {
            row = 0;
            flyingBall.hitCeilingDirectly = false;
        } else {
            float calculatedRow = (GameSettings.SCREEN_HEIGHT - flyingBall.getY()) / GameSettings.ROW_HEIGHT;
            row = (int) calculatedRow;

            if (row >= GameSettings.GRID_ROWS) {
                row = GameSettings.GRID_ROWS - 1;
            }
        }

        if (flyingBall.getY() < GameSettings.CRITICAL_Y_LINE || row < 0) {
            gameSession.endGame();
            if (recordsListView != null) {
                recordsListView.setRecordsWithHighlight(MemoryManager.loadRecordsTable(), gameSession.lastRecordIndex);
            }
            return;
        }

        if (flyingBall.getY() >= GameSettings.SCREEN_HEIGHT - GameSettings.BUBBLE_DIAMETER) {
            row = 0;
        }

        boolean isOddRow = (row % 2 != 0);
        int targetX = flyingBall.getX();
        if (isOddRow) {
            targetX -= GameSettings.BUBBLE_RADIUS;
        }
        int col = (int) (targetX / GameSettings.BUBBLE_DIAMETER);

        if (col < 0) col = 0;
        int maxCol = isOddRow ? (GameSettings.GRID_COLS - 2) : (GameSettings.GRID_COLS - 1);
        if (col > maxCol) col = maxCol;

        while (row < GameSettings.GRID_ROWS && bubbleGrid[row][col] != null) {
            row++;

            if (row >= GameSettings.GRID_ROWS) {
                gameSession.endGame();
                if (recordsListView != null) {
                    recordsListView.setRecordsWithHighlight(MemoryManager.loadRecordsTable(), gameSession.lastRecordIndex);
                }
                return;
            }

            boolean isOddRowNow = (row % 2 != 0);
            float adjustedX = flyingBall.getX();
            if (isOddRowNow) {
                adjustedX -= GameSettings.BUBBLE_RADIUS;
            }
            col = (int) (adjustedX / GameSettings.BUBBLE_DIAMETER);

            if (col < 0) col = 0;
            int maxColsNow = isOddRowNow ? (GameSettings.GRID_COLS - 2) : (GameSettings.GRID_COLS - 1);
            if (col > maxColsNow) col = maxColsNow;
        }

        bubbleGrid[row][col] = flyingBall;
        flyingBall.setCol(col);
        flyingBall.setRow(row);

        if (!smeshariks.contains(flyingBall)) {
            smeshariks.add(flyingBall);
        }

        boolean finalIsOdd = (row % 2 != 0);
        int finalX = GameSettings.BUBBLE_RADIUS + (col * GameSettings.BUBBLE_DIAMETER) + (finalIsOdd ? GameSettings.BUBBLE_RADIUS : 0);
        int finalY = (int) (GameSettings.SCREEN_HEIGHT - GameSettings.BUBBLE_RADIUS - (row * GameSettings.ROW_HEIGHT));

        flyingBall.setX(finalX);
        flyingBall.setY(finalY);
    }

    private ArrayList<Smesharik> getNeighbors(int row, int col) {
        ArrayList<Smesharik> neighbors = new ArrayList<Smesharik>();
        boolean isOdd = (row % 2 != 0);
        int[][] offsets;

        if (isOdd) {
            offsets = new int[][]{{0, -1}, {0, 1}, {-1, 0}, {-1, 1}, {1, 0}, {1, 1}};
        } else {
            offsets = new int[][]{{0, -1}, {0, 1}, {-1, -1}, {-1, 0}, {1, -1}, {1, 0}};
        }

        for (int[] offset : offsets) {
            int iRow = row + offset[0];
            int iCol = col + offset[1];

            if (iRow > -1 && iRow < GameSettings.GRID_ROWS && iCol > -1 && iCol < GameSettings.GRID_COLS && bubbleGrid[iRow][iCol] != null) {
                neighbors.add(bubbleGrid[iRow][iCol]);
            }
        }
        return neighbors;
    }

    private ArrayList<Smesharik> findMatches(Smesharik startBall) {
        ArrayList<Smesharik> matchList = new ArrayList<Smesharik>();
        if (startBall == null) return matchList;
        ArrayList<Smesharik> queue = new ArrayList<Smesharik>();
        boolean[][] visited = new boolean[GameSettings.GRID_ROWS][GameSettings.GRID_COLS];

        queue.add(startBall);
        matchList.add(startBall);
        visited[startBall.getRow()][startBall.getCol()] = true;

        while (!queue.isEmpty()) {
            Smesharik current = queue.remove(0);
            ArrayList<Smesharik> neighbors = getNeighbors(current.getRow(), current.getCol());
            for (Smesharik neighbor : neighbors) {
                int r = neighbor.getRow();
                int c = neighbor.getCol();
                if (!visited[r][c] && neighbor.getColorType() == startBall.getColorType()) {
                    visited[r][c] = true;
                    queue.add(neighbor);
                    matchList.add(neighbor);
                }
            }
        }
        return matchList;
    }

    private void checkAndDropFloatingBalls() {
        int rows = GameSettings.GRID_ROWS;
        int cols = GameSettings.GRID_COLS;
        boolean[][] visited = new boolean[rows][cols];
        Queue<Smesharik> queue = new LinkedList<>();

        for (int j = 0; j < cols; j++) {
            Smesharik ball = bubbleGrid[0][j];
            if (ball != null) {
                queue.add(ball);
                visited[0][j] = true;
            }
        }

        while (!queue.isEmpty()) {
            Smesharik curr = queue.poll();
            ArrayList<Smesharik> neighbors = getNeighbors(curr.getRow(), curr.getCol());

            for (Smesharik neighbor : neighbors) {
                int r = neighbor.getRow();
                int c = neighbor.getCol();
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    if (!visited[r][c]) {
                        visited[r][c] = true;
                        queue.add(neighbor);
                    }
                }
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Smesharik ball = bubbleGrid[i][j];

                if (ball != null && !visited[i][j]) {
                    bubbleGrid[i][j] = null;
                    makeBallFall(ball);
                }
            }
        }
    }

    private int countFixedBalls() {
        int count = 0;
        for (int i = 0; i < GameSettings.GRID_ROWS; i++) {
            for (int j = 0; j < GameSettings.GRID_COLS; j++) {
                if (bubbleGrid[i][j] != null && bubbleGrid[i][j].getCurrentState() == Smesharik.State.FIXED) {
                    count++;
                }
            }
        }
        return count;
    }

    private void shiftGridDownAndSpawnRow() {
        int rows = GameSettings.GRID_ROWS;
        int cols = GameSettings.GRID_COLS;
        int lastRowIndex = rows - 1;

        boolean lastRowIsOdd = (lastRowIndex % 2 != 0);
        int lastRowCols = lastRowIsOdd ? (cols - 1) : cols;

        for (int j = 0; j < lastRowCols; j++) {
            if (bubbleGrid[lastRowIndex][j] != null) {
                gameSession.endGame();
                if (recordsListView != null) {
                    recordsListView.setRecordsWithHighlight(MemoryManager.loadRecordsTable(), gameSession.lastRecordIndex);
                }
                return;
            }
        }

        Smesharik[] nextRowBuffer = new Smesharik[cols];

        for (int i = lastRowIndex; i > 0; i--) {
            boolean currentRowIsOdd = (i % 2 != 0);
            boolean prevRowIsOdd = !currentRowIsOdd;

            int currentMaxCols = currentRowIsOdd ? (cols - 1) : cols;
            int prevMaxCols = prevRowIsOdd ? (cols - 1) : cols;

            java.util.Arrays.fill(nextRowBuffer, null);

            for (int j = 0; j < prevMaxCols; j++) {
                if (j < currentMaxCols) {
                    Smesharik checkingBall = bubbleGrid[i - 1][j];

                    if (checkingBall != null && checkingBall.getColorType() == 9) {
                        nextRowBuffer[j] = null;
                    } else {
                        nextRowBuffer[j] = checkingBall;
                    }
                }
            }

            if (!prevRowIsOdd && currentRowIsOdd) {
                Smesharik extraBall = bubbleGrid[i - 1][cols - 1];

                if (extraBall != null) {
                    int emptyJ = -1;
                    for (int targetJ = currentMaxCols - 1; targetJ >= 0; targetJ--) {
                        if (nextRowBuffer[targetJ] == null) {
                            emptyJ = targetJ;
                            break;
                        }
                    }

                    if (emptyJ != -1) {
                        for (int moveJ = emptyJ; moveJ < currentMaxCols - 1; moveJ++) {
                            nextRowBuffer[moveJ] = nextRowBuffer[moveJ + 1];
                        }
                        nextRowBuffer[currentMaxCols - 1] = extraBall;
                    } else {
                        smeshariks.remove(extraBall);
                        if (extraBall.body != null) {
                            myGdxGame.world.destroyBody(extraBall.body);
                        }
                    }
                }
            }

            for (int j = 0; j < cols; j++) {
                bubbleGrid[i][j] = nextRowBuffer[j];

                if (bubbleGrid[i][j] != null) {

                    bubbleGrid[i][j].setRow(i);
                    bubbleGrid[i][j].setCol(j);

                    int newX = GameSettings.BUBBLE_RADIUS + (j * GameSettings.BUBBLE_DIAMETER) + (currentRowIsOdd ? GameSettings.BUBBLE_RADIUS : 0);
                    int newY = (int) (GameSettings.SCREEN_HEIGHT - GameSettings.BUBBLE_RADIUS - (i * GameSettings.ROW_HEIGHT));

                    bubbleGrid[i][j].setX(newX);
                    bubbleGrid[i][j].setY(newY);

                    if (bubbleGrid[i][j].body != null) {
                        float scale = GameSettings.SCALE;
                        bubbleGrid[i][j].body.setTransform(newX * scale, newY * scale, 0);
                    }
                }
            }
        }

        spawnRow(0);
        startScreenShake(0.25f, 3f);
    }

    private void spawnRow(int rowIdx) {
        int cols = GameSettings.GRID_COLS;
        boolean isOddRow = (rowIdx % 2 != 0);
        int currentCols = isOddRow ? (cols - 1) : cols;

        int y = (int) (GameSettings.SCREEN_HEIGHT - GameSettings.BUBBLE_RADIUS - (rowIdx * GameSettings.ROW_HEIGHT));

        for (int j = 0; j < currentCols; j++) {
            int x = GameSettings.BUBBLE_RADIUS + (j * GameSettings.BUBBLE_DIAMETER);
            if (isOddRow) {
                x += GameSettings.BUBBLE_RADIUS;
            }

            int randomColorId = (int) (Math.random() * (GameSettings.BUBBLE_TEXTURES.length - 1));

            String randomTexturePath = GameSettings.BUBBLE_TEXTURES[randomColorId];

            if (myGdxGame != null && myGdxGame.world != null) {
                Smesharik newSmesharik = new Smesharik(randomTexturePath, x, y, randomColorId, myGdxGame.world, false);
                newSmesharik.body.setType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);

                bubbleGrid[rowIdx][j] = newSmesharik;
                newSmesharik.setRow(rowIdx);
                newSmesharik.setCol(j);

                smeshariks.add(newSmesharik);
            }
        }
    }

    private void startScreenShake(float duration, float intensity) {
        this.shakeDuration = duration;
        this.shakeIntensity = intensity;
        this.shakeTimer = duration;
    }

    private void makeBallFall(Smesharik ball) {
        if (ball == null) return;

        ball.setState(Smesharik.State.FALLING);

        if (ball.body != null) {
            ball.body.setActive(false);
        }

        if (!smeshariks.contains(ball)) {
            smeshariks.add(ball);
        }
    }

    private void updateSmesharikoPad(float delta) {
        for (int i = smeshariks.size() - 1; i >= 0; i--) {
            Smesharik s = smeshariks.get(i);

            if (s.getCurrentState() == Smesharik.State.FALLING) {

                int seed = Math.abs(s.hashCode());
                float verticalSpeed = 400 + (seed % 250);
                int newY = s.getY() - (int) (verticalSpeed * delta);
                s.setY(newY);

                float wave = com.badlogic.gdx.utils.TimeUtils.millis() / 150f + (seed % 10);
                float horizontalShift = MathUtils.sin(wave) * 120f * delta;

                s.setX(s.getX() + (int) horizontalShift);

                if (newY < -100) {
                    smeshariks.remove(i);
                    if (s.body != null) {
                        myGdxGame.world.destroyBody(s.body);
                    }
                }
            }
        }
    }

    private void triggerSmesharikoPad() {
        int rows = GameSettings.GRID_ROWS;
        int cols = GameSettings.GRID_COLS;

        for (int i = 0; i < rows; i++) {
            boolean rowIsOdd = (i % 2 != 0);
            int currentCols = rowIsOdd ? (cols - 1) : cols;

            for (int j = 0; j < currentCols; j++) {
                if (bubbleGrid[i][j] != null) {
                    Smesharik ball = bubbleGrid[i][j];
                    bubbleGrid[i][j] = null;
                    makeBallFall(ball);
                }
            }
        }
        if (currentBall != null) {
            makeBallFall(currentBall);
            currentBall = null;
        }

        if (flyingBalls != null) {
            for (Smesharik flyingBall : flyingBalls) {
                makeBallFall(flyingBall);
            }
            flyingBalls.clear();
        }
    }

    private void checkAndActivateSuperPowers(ArrayList<Smesharik> matches) {

        if (matches == null || matches.isEmpty()) return;

        int rows = GameSettings.GRID_ROWS;
        int cols = GameSettings.GRID_COLS;
        ArrayList<Smesharik> extraToBlast = new ArrayList<>();

        int activeColorId = matches.get(0).getColorType();
        triggerPhrasePopup(activeColorId);

        for (Smesharik ball : matches) {
            int colorId = ball.getColorType();
            int currentRow = ball.getRow();


            int currentCol = ball.getCol();

            switch (colorId) {
                case 0:
                    int maxColsInRow = (currentRow % 2 != 0) ? (cols - 1) : cols;

                    int rowCount = 0;
                    for (int j = 0; j < maxColsInRow; j++) {
                        Smesharik rowBall = bubbleGrid[currentRow][j];
                        if (rowBall != null && !matches.contains(rowBall) && !extraToBlast.contains(rowBall)) {
                            extraToBlast.add(rowBall);
                            rowCount++;
                            if (rowCount >= 4) break;
                        }
                    }
                    break;

                case 1:
                    int colCount = 0;
                    for (int r = currentRow; r >= 0; r--) {
                        int maxColsAtRow = (r % 2 != 0) ? (cols - 1) : cols;
                        if (currentCol >= 0 && currentCol < maxColsAtRow) {
                            Smesharik colBall = bubbleGrid[r][currentCol];
                            if (colBall != null && !matches.contains(colBall) && !extraToBlast.contains(colBall)) {
                                extraToBlast.add(colBall);
                                colCount++;
                                if (colCount >= 3) break;
                            }
                        }
                    }
                    break;


                case 2: // 🦔 ЕЖИК: Колючий взрыв (Лопает 3 случайных шара на поле)
                    ArrayList<Smesharik> allFixedBalls = new ArrayList<>();
                    for (int r = 0; r < rows; r++) {
                        int maxColsAtRow = (r % 2 != 0) ? (cols - 1) : cols;
                        for (int j = 0; j < maxColsAtRow; j++) {
                            Smesharik currentGridBall = bubbleGrid[r][j];
                            if (currentGridBall != null && !matches.contains(currentGridBall) && !extraToBlast.contains(currentGridBall)) {
                                allFixedBalls.add(currentGridBall);
                            }
                        }
                    }
                    int count = 0;
                    while (!allFixedBalls.isEmpty() && count < 3) {
                        int randIdx = (int) (Math.random() * allFixedBalls.size());
                        extraToBlast.add(allFixedBalls.remove(randIdx));
                        count++;
                    }
                    break;

                case 3:

                    int harvestColorId = (int) (Math.random() * (GameSettings.BUBBLE_TEXTURES.length - 1));

                    for (int r = 0; r < rows; r++) {
                        boolean rowIsOdd = (r % 2 != 0);
                        int currentMaxCols = rowIsOdd ? (cols - 1) : cols;

                        for (int j = 0; j < currentMaxCols; j++) {
                            Smesharik gridBall = bubbleGrid[r][j];

                            if (gridBall != null && gridBall.getColorType() == harvestColorId && !matches.contains(gridBall)) {

                                bubbleGrid[r][j] = null;
                                extraToBlast.add(gridBall);
                            }
                        }
                    }

                    if (!extraToBlast.isEmpty()) {
                        gameSession.addScore(extraToBlast.size() * 300);
                    }

                    gameSession.nextTrashSpawnTime += 3000;
                    checkAndDropFloatingBalls();
                    startScreenShake(0.35f, 4.5f);
                    break;

                case 4:
                    ArrayList<Smesharik> bNeighbors = getNeighbors(currentRow, currentCol);

                    if (currentBall != null) {
                        int targetColor = currentBall.getColorType();

                        for (Smesharik n : bNeighbors) {
                            if (n != null && !matches.contains(n)) {
                                n.changeColor(targetColor);
                            }
                        }
                    }
                    break;

                case 5:

                    if (myGdxGame.audioManager.gameMusic != null) {
                        myGdxGame.audioManager.playSpecialMusic(myGdxGame.audioManager.karychMusic);

                        gameSession.nextTrashSpawnTime += 7000;

                        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                            @Override
                            public void run() {
                                if (gameSession.state == PLAYING) {
                                    myGdxGame.audioManager.restoreBackgroundMusic(myGdxGame.audioManager.karychMusic);
                                }
                            }
                        }, 7.0f);
                    }
                    break;

                case 6:
                    removeLowestRow();
                    break;

                case 7:
                    ArrayList<Smesharik> pNeighbors = getNeighbors(currentRow, currentCol);
                    int pinCount = 0;
                    for (Smesharik n : pNeighbors) {
                        if (n != null && !matches.contains(n) && !extraToBlast.contains(n)) {
                            extraToBlast.add(n);
                            pinCount++;
                            if (pinCount >= 3) break;
                        }
                    }
                    break;

                case 8:

                    int[] colorCounts = new int[9];
                    for (int r = 0; r < rows; r++) {
                        int maxCols = (r % 2 != 0) ? (cols - 1) : cols;
                        for (int j = 0; j < maxCols; j++) {
                            if (bubbleGrid[r][j] != null) {
                                int c = bubbleGrid[r][j].getColorType();
                                if (c >= 0 && c < 9) colorCounts[c]++;
                            }
                        }
                    }

                    int mostFrequentColorId = 0;
                    int maxCount = 0;
                    for (int c = 0; c < 9; c++) {
                        if (colorCounts[c] > maxCount) {
                            maxCount = colorCounts[c];
                            mostFrequentColorId = c;
                        }
                    }

                    if (currentBall != null && maxCount > 0) {
                        currentBall.changeColor(mostFrequentColorId);
                    }

                    gameSession.addScore(matches.size() * 300);
                    break;

                case 9:

                    myGdxGame.audioManager.playSpecialMusic(myGdxGame.audioManager.bibiMusic);

                    for (int r = 0; r < rows; r++) {
                        boolean rowIsOdd = (r % 2 != 0);
                        int currentMaxCols = rowIsOdd ? (cols - 1) : cols;

                        for (int j = 0; j < currentMaxCols; j++) {
                            Smesharik gridBall = bubbleGrid[r][j];

                            if (gridBall != null && !matches.contains(gridBall) && !extraToBlast.contains(gridBall)) {
                                extraToBlast.add(gridBall);
                            }
                        }
                    }

                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                        @Override
                        public void run() {
                            if (gameSession.state == PLAYING) {
                                myGdxGame.audioManager.restoreBackgroundMusic(myGdxGame.audioManager.bibiMusic);
                            }
                        }
                    }, 7.0f);
                    startScreenShake(0.6f, 10f);
                    break;
                default:
                    break;
            }
        }

        if (!extraToBlast.isEmpty()) {
            gameSession.addScore(extraToBlast.size() * 150);

            for (Smesharik blastBall : extraToBlast) {
                bubbleGrid[blastBall.getRow()][blastBall.getCol()] = null;
                makeBallFall(blastBall);
            }
        }
    }

    private void removeLowestRow() {
        int rows = GameSettings.GRID_ROWS;
        int cols = GameSettings.GRID_COLS;

        int lowestRowIdx = -1;
        for (int i = rows - 1; i >= 0; i--) {
            int maxCols = (i % 2 != 0) ? (cols - 1) : cols;
            for (int j = 0; j < maxCols; j++) {
                if (bubbleGrid[i][j] != null) {
                    lowestRowIdx = i;
                    break;
                }
            }
            if (lowestRowIdx != -1) break;
        }

        if (lowestRowIdx != -1) {
            int maxCols = (lowestRowIdx % 2 != 0) ? (cols - 1) : cols;
            for (int j = 0; j < maxCols; j++) {
                Smesharik ball = bubbleGrid[lowestRowIdx][j];
                if (ball != null) {
                    bubbleGrid[lowestRowIdx][j] = null;
                    makeBallFall(ball);
                }
            }
            startScreenShake(0.4f, 6f);
        }
    }

    private int countBallsInRow(int rowIdx) {
        int count = 0;
        int cols = GameSettings.GRID_COLS;
        int maxCols = (rowIdx % 2 != 0) ? (cols - 1) : cols;

        for (int j = 0; j < maxCols; j++) {
            if (bubbleGrid != null && bubbleGrid[rowIdx][j] != null) {
                count++;
            }
        }
        return count;
    }

    private boolean isSmesharikTooCloseToDanger() {
        int lastRow = GameSettings.GRID_ROWS - 1;
        return countBallsInRow(lastRow) > 0 || countBallsInRow(lastRow - 1) > 0;
    }

    public void triggerPhrasePopup(int colorId) {
        popupTimer = POPUP_DURATION;
        popupY = 750f;

        String[] phrases;

        switch (colorId) {
            case 0: // Крош
                phrases = new String[]{"Ёлки-иголки!", "От винта!", "Ура-а! Половина ряда долой!"};
                break;
            case 1: // Nusha
                phrases = new String[]{"Кузинатра!", "Ой, я такая воздушная!", "Пробиваю путь вверх! 💘"};
                break;
            case 2: // Ежик
                phrases = new String[]{"Подумать только", "Феноменальный взрыв!", "Ой, мамочки, всё лопнуло!"};
                break;
            case 3: // Копатыч
                phrases = new String[]{"Укуси меня пчела!", "Собираем урожай! 🌾", "Запасы на зиму готовы!"};
                break;
            case 4: // Бараш
                phrases = new String[]{"Ах, вдохновение!", "Крашу под цвет пушки! 🎨", "Творческий порыв!"};
                break;
            case 5: // Карыч
                phrases = new String[]{"Карамба!", "Музыкальная пауза! 🎵", "Время, замри!"};
                break;
            case 6: // Лосяш
                phrases = new String[]{"Феноменально!", "Интересный артефакт", "Нижний ряд ликвидирован! 🦌"};
                break;
            case 7: // Пин
                phrases = new String[]{"Компрессия!", "О майн гот!", "Дас ист фантастиш! 🔧"};
                break;
            case 8: // Совунья
                phrases = new String[]{"Куда катится мир?!", "Вот тебе лучший смешарик! 🩺", "Витаминный заряд!"};
                break;
            case 9: // Робот Биби
                phrases = new String[]{"Полный рассинхрон!", "Биби-буст! 🤖⚡", "Квантовый бабах!"};
                break;
            default:
                phrases = new String[]{"Комбо! ✨"};
                break;
        }

        int randIdx = (int) (Math.random() * phrases.length);
        popupText = phrases[randIdx];
    }
}
