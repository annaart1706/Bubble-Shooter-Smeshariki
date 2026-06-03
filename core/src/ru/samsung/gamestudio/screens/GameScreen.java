package ru.samsung.gamestudio.screens;

import static ru.samsung.gamestudio.GameSettings.BUBBLE_DIAMETER;
import static ru.samsung.gamestudio.GameState.PAUSED;
import static ru.samsung.gamestudio.GameState.PLAYING;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.ScreenUtils;

import ru.samsung.gamestudio.*;
import ru.samsung.gamestudio.components.*;
import ru.samsung.gamestudio.managers.ContactManager;
import ru.samsung.gamestudio.managers.MemoryManager;
import ru.samsung.gamestudio.objects.AimLine;
import ru.samsung.gamestudio.objects.ShipObject;
import ru.samsung.gamestudio.objects.Smesharik;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class GameScreen extends ScreenAdapter {

    private MyGdxGame myGdxGame;
    GameSession gameSession;
    private ShipObject shipObject;

    ArrayList<Smesharik> smeshariks;


    ContactManager contactManager;

    // PLAY state UI
    MovingBackgroundView backgroundView;
    ImageView topBlackoutView;
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

    // Инструмент для рисования линий и лазеров
    private AimLine aimLine;



    public GameScreen(MyGdxGame myGdxGame) {
        try {
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
            topBlackoutView = new ImageView(0, 1180, GameResources.BLACKOUT_TOP_IMG_PATH);
            liveView = new LiveView(305, 1215);
            scoreTextView = new TextView(myGdxGame.commonWhiteFont, 50, 1215);
            pauseButton = new ButtonView(
                    605, 1200,
                    46, 54,
                    GameResources.PAUSE_IMG_PATH
            );

            fullBlackoutView = new ImageView(0, 0, GameResources.BLACKOUT_FULL_IMG_PATH);
            pauseTextView = new TextView(myGdxGame.largeWhiteFont, 282, 842, "Pause");
            homeButton = new ButtonView(
                    138, 695,
                    200, 70,
                    myGdxGame.commonBlackFont,
                    GameResources.BUTTON_SHORT_BG_IMG_PATH,
                    "Home"
            );
            continueButton = new ButtonView(
                    393, 695,
                    200, 70,
                    myGdxGame.commonBlackFont,
                    GameResources.BUTTON_SHORT_BG_IMG_PATH,
                    "Continue"
            );

            recordsListView = new RecordsListView(myGdxGame.commonWhiteFont, 690);
            recordsTextView = new TextView(myGdxGame.largeWhiteFont, 206, 842, "Last records");
            homeButton2 = new ButtonView(
                    280, 365,
                    160, 70,
                    myGdxGame.commonBlackFont,
                    GameResources.BUTTON_SHORT_BG_IMG_PATH,
                    "Home"
            );

            // Наш новый класс луча
            aimLine = new ru.samsung.gamestudio.objects.AimLine();

        } catch (Exception e) {
            // Если в конструкторе падает шрифт или картинка — мы увидим это в системной ошибке!
            System.err.println("!!! КРИТИЧЕСКИЙ СБОЙ В КОНСТРУКТОРЕ GAMESCREEN !!!");
            e.printStackTrace();
        }
    }


    @Override
    public void show() {
        try {
            restartGame();
        } catch (Exception e) {
            System.err.println("!!! КРИТИЧЕСКИЙ СБОЙ В МЕТОДЕ SHOW !!!");
            e.printStackTrace();
        }
    }


    @Override
    public void render(float delta) {
        handleInput();



        // ХИТ: Если игра уже закончилась, сразу выходим на draw() и не выполняем код ниже!
        if (gameSession.state == GameState.ENDED) {
            draw();
            return;
        }

        if (gameSession.state == PLAYING) {
            if (!shipObject.isAlive()) {
                gameSession.endGame();
               // recordsListView.setRecords(MemoryManager.loadRecordsTable());
            }

            backgroundView.move();
            gameSession.updateScore();
            scoreTextView.setText("Score: " + gameSession.getScore());
            liveView.setLeftLives(shipObject.getLiveLeft());

            myGdxGame.stepWorld();

            // Принудительно останавливаем ТОЛЬКО тех Смешариков, которые стоят в сетке и НЕ падают
            if (bubbleGrid != null) {
                for (int i = 0; i < GameSettings.GRID_ROWS; i++) {
                    for (int j = 0; j < GameSettings.GRID_COLS; j++) {
                        if (bubbleGrid[i][j] != null && bubbleGrid[i][j].getCurrentState() != Smesharik.State.FULLING) {
                            bubbleGrid[i][j].body.setLinearVelocity(0, 0);
                            bubbleGrid[i][j].body.setAngularVelocity(0);
                        }
                    }
                }
            }

                      // --- БЕЗОПАСНАЯ ФИКСАЦИЯ И ЗАЩИТА ОТ ИНДЕКСОВ -1 ---
            ArrayList<Smesharik> ballsToFix = contactManager.getBallsToFix();
            if (ballsToFix != null && !ballsToFix.isEmpty()) {
                for (Smesharik ball : ballsToFix) {

                    fixBallInGrid(ball); // Пытаемся зафиксировать

                    // ХИТ: Если после фиксации игра завершилась (проигрыш),
                    // мы СРОЧНО выходим и не запускаем findMatches со сломанными координатами -1!
                    if (gameSession.state == GameState.ENDED) {
                        contactManager.clearBallsToFix();
                        break;
                    }

                    flyingBalls.remove(ball);
                    ball.body.setAwake(true);

                    ArrayList<Smesharik> matches = findMatches(ball);
                    if (matches.size() >= 3) {
                        int pointsForMatches = matches.size() * 100;
                        gameSession.addScore(pointsForMatches);

                        for (Smesharik matchBall : matches) {
                            bubbleGrid[matchBall.getRow()][matchBall.getCol()] = null;
                            matchBall.setState(Smesharik.State.FULLING);

                            if (matchBall.body != null) {
                                matchBall.body.setActive(false);
                            }
                        }

                        int ballsBeforeDrop = countFixedBalls();
                        checkAndDropFloatingBalls();
                        int ballsAfterDrop = countFixedBalls();
                        int droppedCount = ballsBeforeDrop - ballsAfterDrop;

                        if (droppedCount > 0) {
                            gameSession.addScore(droppedCount * 200);
                        }
                    }
                }
                contactManager.clearBallsToFix();
            }


            // НАСТОЯЩИЙ ЛИСТОПАД СМЕШАРИКОВ 🍁
            for (int i = smeshariks.size() - 1; i >= 0; i--) {
                Smesharik s = smeshariks.get(i);

                if (s.getCurrentState() == Smesharik.State.FULLING) {

                    // 1. Создаем уникальное число для каждого Смешарика на основе его хэша.
                    // Это даст нам постоянную, но уникальную скорость для каждого персонажа!
                    int seed = Math.abs(s.hashCode());

                    // Скорость по вертикали будет случайной в диапазоне от 400 до 650 пикселей
                    float verticalSpeed = 400 + (seed % 250);

                    // Вычисляем новый Y
                    int newY = s.getY() - (int) (verticalSpeed * delta);
                    s.setY(newY);

                    // 2. ЭФФЕКТ ЛИСТОПАДА: Добавляем покачивание влево-вправо!
                    // С помощью синуса от времени (TimeUtils) шарик будет плавно вилять в бока.
                    // seed % 10 дает случайный сдвиг фазы, чтобы они не качались синхронно.
                    float wave = com.badlogic.gdx.utils.TimeUtils.millis() / 150f + (seed % 10);

                    // Амплитуда качания — примерно 2 пикселя за кадр
                    float horizontalShift = MathUtils.sin(wave) * 120f * delta;

                    // Применяем сдвиг к координате X
                    s.setX(s.getX() + (int) horizontalShift);

                    // Если Смешарик улетел ниже экрана — полностью стираем из памяти
                    if (newY < -100) {
                        smeshariks.remove(i);
                        if (s.body != null) {
                            myGdxGame.world.destroyBody(s.body);
                        }
                    }
                }
            }


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
                    if (!pauseButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        if (currentBall != null) {
                            float distanceFromCannonY = myGdxGame.touch.y - shipObject.getY();
                            if (distanceFromCannonY > GameSettings.CANNON_CANCEL_ZONE) {
                                float angleRadians = shipObject.getRotation() * MathUtils.degreesToRadians;

                                float directionX = -MathUtils.sin(angleRadians);
                                float directionY = MathUtils.cos(angleRadians);

                                float impulseX = directionX * GameSettings.SHOT_SPEED;
                                float impulseY = directionY * GameSettings.SHOT_SPEED;

                                currentBall.body.applyLinearImpulse(
                                        impulseX, impulseY,
                                        currentBall.body.getWorldCenter().x,
                                        currentBall.body.getWorldCenter().y,
                                        true
                                );
                                currentBall.setState(Smesharik.State.FLYING);
                                currentBall.body.setBullet(true);
                                flyingBalls.add(currentBall);
                                currentBall = null;
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


            // НАЙДИ БЛОК case ENDED: В МЕТОДЕ handleInput() И ЗАМЕНИ ЕГО:
            case ENDED:
                // Используем justTouched(), чтобы клик срабатывал только от НОВОГО нажатия,
                // а не от старого пальца, который остался на экране после выстрела!
                if (Gdx.input.justTouched()) {
                    // Обновляем координаты тача строго для нового клика
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
        ScreenUtils.clear(Color.CLEAR);

        // ====================================================
        // ЧАСТЬ 1: Отрисовка всех игровых спрайтов и текстур Смешариков
        // ====================================================
        myGdxGame.batch.begin(); // ОТКРЫЛИ БАТЧ №1

        backgroundView.draw(myGdxGame.batch);

        if (smeshariks != null) {
            for (Smesharik ball : smeshariks) {
                ball.draw(myGdxGame.batch);
            }
        }
        if (flyingBalls != null) {
            for (Smesharik ball : flyingBalls) {
                ball.draw(myGdxGame.batch);
            }
        }
        if (shipObject != null) {
            shipObject.draw(myGdxGame.batch);
        }
        if (currentBall != null) {
            currentBall.draw(myGdxGame.batch);
        }


        myGdxGame.batch.end(); // ЖЕСТКО ЗАКРЫЛИ БАТЧ №1, рисование картинок окончено!
        // ====================================================


        // ====================================================
        // ЧАСТЬ 2: Отрисовка лазерного прицела геометрией ShapeRenderer
        // ====================================================
        // Прицел рисуется ТОЛЬКО когда палец прижат к экрану, строго вне батча!
        // Внутри GameScreen.java в методе draw() ЧАСТЬ 2 должна быть такой:
        if (com.badlogic.gdx.Gdx.input.isTouched() && aimLine != null) {
            aimLine.drawRecords(myGdxGame.batch, myGdxGame.camera.combined, bubbleGrid, shipObject, currentBall);
        }

        // ====================================================


        // ====================================================
        // ЧАСТЬ 3: Отрисовка верхнего интерфейса и экранов Паузы / Финала
        // ====================================================
        myGdxGame.batch.begin(); // ОТКРЫЛИ БАТЧ №2 для текста и интерфейса

        if (topBlackoutView != null) topBlackoutView.draw(myGdxGame.batch);
        if (scoreTextView != null) scoreTextView.draw(myGdxGame.batch);
        if (liveView != null) liveView.draw(myGdxGame.batch);
        if (pauseButton != null) pauseButton.draw(myGdxGame.batch);

        if (gameSession.state == PAUSED) {
            if (fullBlackoutView != null) fullBlackoutView.draw(myGdxGame.batch);
            if (pauseTextView != null) pauseTextView.draw(myGdxGame.batch);
            if (homeButton != null) homeButton.draw(myGdxGame.batch);
            if (continueButton != null) continueButton.draw(myGdxGame.batch);
        }
        else if (gameSession.state == GameState.ENDED) {
            if (fullBlackoutView != null) fullBlackoutView.draw(myGdxGame.batch);
            if (recordsTextView != null) recordsTextView.draw(myGdxGame.batch);

            // Отрисовываем рекорды нашим новым безопасным методом
            if (recordsListView != null) {
                recordsListView.drawRecords(myGdxGame.batch);
            }

            if (homeButton2 != null) homeButton2.draw(myGdxGame.batch);
        }

        myGdxGame.batch.end(); // ЖЕСТКО ЗАКРЫЛИ БАТЧ №2
        // ====================================================
    }



    private void restartGame() {
        try {
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
                int y = (int) (GameSettings.SCREEN_HEIGHT - GameSettings.BUBBLE_RADIUS - (i * GameSettings.ROW_HEIGHT));
                boolean isOddRow = i % 2 != 0;
                int currentCols = isOddRow ? (GameSettings.GRID_COLS - 1) : GameSettings.GRID_COLS;

                for (int j = 0; j < currentCols; j++) {
                    int x = GameSettings.BUBBLE_RADIUS + (j * BUBBLE_DIAMETER);
                    if (isOddRow) {
                        x += GameSettings.BUBBLE_RADIUS;
                    }
                    int randomColorId = (int) (Math.random() * GameSettings.BUBBLE_TEXTURES.length);
                    String randomTexturePath = GameSettings.BUBBLE_TEXTURES[randomColorId];

                    if (myGdxGame != null && myGdxGame.world != null) {
                        Smesharik newSmesharik = new Smesharik(randomTexturePath, x, y, randomColorId, myGdxGame.world, false);
                        newSmesharik.body.setType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);

                        bubbleGrid[i][j] = newSmesharik;
                        newSmesharik.setRow(i);
                        newSmesharik.setCol(j);

                        smeshariks.add(newSmesharik);
                    }
                }
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
                topFixtureDef.restitution = 1.0f;
                topFixtureDef.friction = 0.0f;
                topFixtureDef.filter.categoryBits = GameSettings.FILTER_FIXED_BUBBLES;

                EdgeShape topEdge = new EdgeShape();
                topEdge.set(0 * GameSettings.SCALE, GameSettings.SCREEN_HEIGHT * GameSettings.SCALE,
                        GameSettings.SCREEN_WIDTH * GameSettings.SCALE, GameSettings.SCREEN_HEIGHT * GameSettings.SCALE);
                topFixtureDef.shape = topEdge;
                topWallBody.createFixture(topFixtureDef);
                topEdge.dispose();
            }

            // Безопасно инициализируем таблицу рекордов при самом первом старте
            if (recordsListView != null) {
                try {
                    ArrayList<Integer> table = MemoryManager.loadRecordsTable();
                    if (table == null) table = new ArrayList<>();
                    recordsListView.setRecords(table);
                } catch (Exception e) {
                    ArrayList<Integer> emptyTable = new ArrayList<>();
                    recordsListView.setRecords(emptyTable);
                }
            }

        } catch (Exception e) {
            // Если упала абсолютно любая строчка — ловушка поймает её и выведет ошибку в консоль,
            // но само приложение при этом продолжит работать!
            Gdx.app.log("RESTART_CATCHER", "Произошел сбой при старте: " + e.getMessage());
            e.printStackTrace();
        }
    }




    private void createNewBall() {
        int randomId = (int) (Math.random() * GameSettings.BUBBLE_TEXTURES.length);
        String texturePath = GameSettings.BUBBLE_TEXTURES[randomId];
        int spawnY = shipObject.getY() + GameSettings.BUBBLE_DIAMETER;
        currentBall = new Smesharik(texturePath, shipObject.getX(), spawnY, randomId, myGdxGame.world, true);
    }


    private void fixBallInGrid(Smesharik flyingBall) {
        flyingBall.body.setLinearVelocity(0, 0);
        flyingBall.body.setAngularVelocity(0);
        flyingBall.body.setType(BodyDef.BodyType.StaticBody);
        flyingBall.setState(Smesharik.State.FIXED);

        // 1. Рассчитываем строку
        int row = (int) ((GameSettings.SCREEN_HEIGHT - flyingBall.getY()) / GameSettings.ROW_HEIGHT);

        // ====================================================
        // ЖЕЛЕЗНЫЙ ЗАМОК ОТ ИНДЕКСОВ СБОЯ И КРАША ПОЛЯ 🛡️
        // Если строка улетела в минус или Смешарики забили поле до самого низа
        // ====================================================
        if (row < 0 || row >= GameSettings.GRID_ROWS) {
            gameSession.endGame(); // Включаем финал игры
            if (recordsListView != null) {
                recordsListView.setRecords(MemoryManager.loadRecordsTable());
            }
            return; // Мгновенно выходим, не запуская циклы findMatches со сломанными индексами!
        }
        // ====================================================

        boolean isOddRow = (row % 2 != 0);
        int targetX = flyingBall.getX();
        if (isOddRow) {
            targetX -= GameSettings.BUBBLE_RADIUS;
        }
        int col = (int) (targetX / GameSettings.BUBBLE_DIAMETER);

        // Проверяем границы столбцов, чтобы там тоже не вылезло -1
        if (col < 0) col = 0;
        int maxCol = isOddRow ? (GameSettings.GRID_COLS - 2) : (GameSettings.GRID_COLS - 1);
        if (col > maxCol) col = maxCol;

        // Поиск свободной ячейки ниже (только внутри безопасных границ)
        while (row < GameSettings.GRID_ROWS && bubbleGrid[row][col] != null) {
            row++;

            // Если в процессе сдвига мы опять переполнили сетку — это проигрыш
            if (row >= GameSettings.GRID_ROWS) {
                gameSession.endGame();
                if (recordsListView != null) {
                    recordsListView.setRecords(MemoryManager.loadRecordsTable());
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

        // Записываем Смешарика на безопасное место
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

        // ИСПРАВЛЕНО: Правильный обход двумерного массива смещений (offset[0] и offset[1])
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

        // Шаг 1. Ищем Смешариков на потолке
        for (int j = 0; j < cols; j++) {
            Smesharik ball = bubbleGrid[0][j];
            if (ball != null) {
                queue.add(ball);
                visited[0][j] = true;
            }
        }

        // Шаг 2. Запускаем BFS цепную проверку от потолка
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

        // Шаг 3. Безопасный перевод оторвавшихся шаров в режим падения на Java-математике
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Smesharik ball = bubbleGrid[i][j];
                if (ball != null && !visited[i][j]) {
                    int backupX = ball.getX();
                    int backupY = ball.getY();

                    bubbleGrid[i][j] = null; // Стираем из сетки
                    ball.setState(Smesharik.State.FULLING); // Включаем падение

                    if (!smeshariks.contains(ball)) {
                        smeshariks.add(ball);
                    }

                    // НАДЕЖНОСТЬ: Выключаем Box2D тело, чтобы оно не мешало нам плавно ронять пиксели
                    if (ball.body != null) {
                        ball.body.setActive(false);
                    }

                    ball.setX(backupX);
                    ball.setY(backupY);
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

} // Самая последняя закрывающая скобка всего файла GameScreen






