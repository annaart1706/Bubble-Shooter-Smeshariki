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
import ru.samsung.gamestudio.objects.BulletObject;
import ru.samsung.gamestudio.objects.ShipObject;
import ru.samsung.gamestudio.objects.Smesharik;

import java.util.ArrayList;

public class GameScreen extends ScreenAdapter {

    private MyGdxGame myGdxGame;
    GameSession gameSession;
    private ShipObject shipObject;

    ArrayList<Smesharik> smeshariks;
    ArrayList<BulletObject> bulletArray;

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



    public GameScreen(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;
        gameSession = new GameSession();

        contactManager = new ContactManager(myGdxGame.world);

        smeshariks = new ArrayList<>();
        bulletArray = new ArrayList<>();

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

    }

    @Override
    public void show() {
        restartGame();
    }

    @Override
    public void render(float delta) {

        handleInput();

        if (gameSession.state == PLAYING) {
            if (!shipObject.isAlive()) {
                gameSession.endGame();
                recordsListView.setRecords(MemoryManager.loadRecordsTable());
            }

            backgroundView.move();
            gameSession.updateScore();
            scoreTextView.setText("Score: " + gameSession.getScore());
            liveView.setLeftLives(shipObject.getLiveLeft());

            myGdxGame.stepWorld();

            if (bubbleGrid != null) {
                for (int i = 0; i < GameSettings.GRID_ROWS; i++) {
                    for (int j = 0; j < GameSettings.GRID_COLS; j++) {
                        if (bubbleGrid[i][j] != null) {

                            bubbleGrid[i][j].body.setLinearVelocity(0, 0);
                            bubbleGrid[i][j].body.setAngularVelocity(0);
                        }
                    }
                }
            }

            ArrayList<Smesharik> ballsToFix = contactManager.getBallsToFix();
            if (ballsToFix != null && !ballsToFix.isEmpty()){
                for(Smesharik ball : ballsToFix){
                    fixBallInGrid(ball);
                    flyingBalls.remove(ball);
                    ball.body.setAwake(true);

                    ArrayList<Smesharik> matches = findMatches(ball);
                    if(matches.size() >= 3){
                        gameSession.addScore(matches.size()* 10);
                       for(Smesharik matchBall : matches){
                           bubbleGrid[matchBall.getRow()][matchBall.getCol()] = null;
                           smeshariks.remove(matchBall);
                           myGdxGame.world.destroyBody(matchBall.body);
                       }
                    }
                }
                contactManager.clearBallsToFix();
            }

//            ArrayList<Smesharik> ballsToFix = contactManager.getBallsToFix();
//            if (ballsToFix != null && !ballsToFix.isEmpty()) {
//                for (Smesharik ball : ballsToFix) {
//                    fixBallInGrid(ball); // Оставляем ТОЛЬКО вызов фиксации
//                    flyingBalls.remove(ball);
//                    ball.body.setAwake(true);
//                }
//                contactManager.clearBallsToFix();
//            }
//            if (ballsToRemove != null && !ballsToRemove.isEmpty()) {
//                for (Smesharik deadBall : ballsToRemove) {
//                    if (deadBall.body != null) {
//                        try {
//                            myGdxGame.world.destroyBody(deadBall.body);
//                            deadBall.body = null; // Зануляем ссылку, чтобы не удалить дважды
//                        } catch (Exception e) {
//                            Gdx.app.log("BOX2D", "Не удалось удалить тело смертника");
//                        }
//                    }
//                }
//                // Опустошаем список смертников, они полностью стерты из вселенной игры!
//                ballsToRemove.clear();
//            }


        }

        draw();
    }

    private void handleInput() {

        if (Gdx.input.isTouched()) {
            myGdxGame.touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        }

        switch (gameSession.state) {
            case PLAYING:
                if(Gdx.input.isTouched()) {
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
                    if(Gdx.input.isTouched()) {
                        if (continueButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                            gameSession.resumeGame();
                        }
                        if (homeButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                            myGdxGame.setScreen(myGdxGame.menuScreen);
                        }
                    }
                    break;

                case ENDED:
                    if(Gdx.input.isTouched()) {
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

        myGdxGame.batch.begin();

        backgroundView.draw(myGdxGame.batch);
        for (int i = 0; i < GameSettings.GRID_ROWS; i++) {
            for (int j = 0; j < GameSettings.GRID_COLS; j++) {
                if (bubbleGrid[i][j] != null) {
                    bubbleGrid[i][j].draw(myGdxGame.batch);
                }
            }
        }
        for (Smesharik ball : flyingBalls) {
            ball.draw(myGdxGame.batch);
        }

        shipObject.draw(myGdxGame.batch);
        if(currentBall != null){
            currentBall.draw(myGdxGame.batch);
        }
        for (BulletObject bullet : bulletArray) bullet.draw(myGdxGame.batch);
        topBlackoutView.draw(myGdxGame.batch);
        scoreTextView.draw(myGdxGame.batch);
        liveView.draw(myGdxGame.batch);
        pauseButton.draw(myGdxGame.batch);

        if (gameSession.state == PAUSED) {
            fullBlackoutView.draw(myGdxGame.batch);
            pauseTextView.draw(myGdxGame.batch);
            homeButton.draw(myGdxGame.batch);
            continueButton.draw(myGdxGame.batch);
        } else if (gameSession.state == GameState.ENDED) {
            fullBlackoutView.draw(myGdxGame.batch);
            recordsTextView.draw(myGdxGame.batch);
            recordsListView.draw(myGdxGame.batch);
            homeButton2.draw(myGdxGame.batch);
        }

        myGdxGame.batch.end();

    }

    private void restartGame() {
        flyingBalls.clear();

        if (shipObject != null) {
            myGdxGame.world.destroyBody(shipObject.body);
        }

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

                Smesharik newSmesharik = new Smesharik(randomTexturePath, x, y, randomColorId, myGdxGame.world, false);
                newSmesharik.body.setType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody);

                bubbleGrid[i][j] = newSmesharik;
                newSmesharik.setRow(i);
                newSmesharik.setCol(j);

                smeshariks.add(newSmesharik);
            }

        }

        shipObject = new ShipObject(
                GameSettings.SCREEN_WIDTH / 2, 150,
                GameSettings.SHIP_WIDTH, GameSettings.SHIP_HEIGHT,
                GameResources.SHIP_IMG_PATH,
                myGdxGame.world
        );
        createNewBall();

        bulletArray.clear();
        gameSession.startGame();

        BodyDef wallDef = new BodyDef();
        wallDef.type = BodyDef.BodyType.StaticBody;
        Body wallBody = myGdxGame.world.createBody(wallDef);

        FixtureDef wallFixtureDef = new FixtureDef();
        wallFixtureDef.restitution = 1.0f;
        wallFixtureDef.friction = 0.0f;
        wallFixtureDef.filter.categoryBits = GameSettings.FILTER_WALLS;

        EdgeShape edge = new EdgeShape();

        // 1. ЛЕВАЯ ГРАНИЦА ЭКРАНА (от низа до потолка)
        edge.set(0 * GameSettings.SCALE, 0 * GameSettings.SCALE,
                0 * GameSettings.SCALE, GameSettings.SCREEN_HEIGHT * GameSettings.SCALE);
        wallFixtureDef.shape = edge;
        wallBody.createFixture(wallFixtureDef);

// 2. ПРАВАЯ ГРАНИЦА ЭКРАНА (от низа до потолка)
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
        topFixtureDef.filter.categoryBits = GameSettings.FILTER_FIXED_BUBBLES; // ПАСПОРТ СЕТКИ!

        EdgeShape topEdge = new EdgeShape();
        topEdge.set(0 * GameSettings.SCALE, GameSettings.SCREEN_HEIGHT * GameSettings.SCALE,
                GameSettings.SCREEN_WIDTH * GameSettings.SCALE, GameSettings.SCREEN_HEIGHT * GameSettings.SCALE);
        topFixtureDef.shape = topEdge;
        topWallBody.createFixture(topFixtureDef);
        topEdge.dispose();
    }
    private void createNewBall(){
        int randomId = (int)(Math.random() * GameSettings.BUBBLE_TEXTURES.length);
        String texturePath = GameSettings.BUBBLE_TEXTURES[randomId];
        int spawnY = shipObject.getY() + GameSettings.BUBBLE_DIAMETER;
        currentBall = new Smesharik(texturePath, shipObject.getX(),spawnY, randomId, myGdxGame.world, true);
    }


    private void fixBallInGrid(Smesharik flyingBall){
        flyingBall.body.setLinearVelocity(0, 0);
        flyingBall.body.setAngularVelocity(0);
        flyingBall.body.setType(BodyDef.BodyType.StaticBody);
        flyingBall.setState(Smesharik.State.FIXED);

        int row = (int) ((GameSettings.SCREEN_HEIGHT - flyingBall.getY()) / GameSettings.ROW_HEIGHT);
        if (row < 0) row = 0;
        if (row >= GameSettings.GRID_ROWS) row = GameSettings.GRID_ROWS - 1;

        boolean isOddRow = (row % 2 != 0);
        int targetX = flyingBall.getX();
        if(isOddRow){
            targetX -= GameSettings.BUBBLE_RADIUS;
        }
        int col = (int)(targetX / GameSettings.BUBBLE_DIAMETER);
        if(col < 0){
            col = 0;
        }
        int maxCol = isOddRow ? (GameSettings.GRID_COLS - 2) : (GameSettings.GRID_COLS - 1);
        if(col > maxCol){
            col = maxCol;
        }
//        // ====================================================
//        // ПРОВЕРКА НА ЗАВИСАНИЕ В ВОЗДУХЕ
//        // ====================================================
//        // Получаем список соседей для ячейки, куда прилетел шар
//        ArrayList<Smesharik> currentNeighbors = getNeighbors(row, col);
//
//        // Если это НЕ самый верхний ряд (row > 0) И вокруг этой ячейки вообще НЕТ соседей (список пустой)
//        if (row > 0 && currentNeighbors.isEmpty()) {
//            // Мы принудительно возвращаем Смешарику статус летящего снаряда
//            flyingBall.setState(Smesharik.State.FLYING);
//            flyingBall.body.setType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody);
//
//            // Добавляем его обратно в список летящих, чтобы игра продолжала его двигать
//            if (!flyingBalls.contains(flyingBall)) {
//                flyingBalls.add(flyingBall);
//            }
//
//            return; // СРОЧНО выходим из метода! Замораживать этот шар нельзя!
//        }
//        // ====================================================




        while (bubbleGrid[row][col] != null) {
            row++;

            if (row >= GameSettings.GRID_ROWS) {
                row = GameSettings.GRID_ROWS - 1;
                break;
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

        if (bubbleGrid[row][col] != null) {
            gameSession.endGame();
            flyingBall.body.getWorld().destroyBody(flyingBall.body);
            return; // СРОЧНО выходим из метода, ничего не записывая в матрицу!
        }

        bubbleGrid[row][col] = flyingBall;
        flyingBall.setCol(col);
        flyingBall.setRow(row);

        boolean finalIsOdd = (row % 2 != 0);
        int finalX = GameSettings.BUBBLE_RADIUS + (col * GameSettings.BUBBLE_DIAMETER) + (finalIsOdd ? GameSettings.BUBBLE_RADIUS : 0);
        int finalY = (int) (GameSettings.SCREEN_HEIGHT - GameSettings.BUBBLE_RADIUS - (row * GameSettings.ROW_HEIGHT));


        flyingBall.setX(finalX);
        flyingBall.setY(finalY);

//        ArrayList<Smesharik> ballsToFix = contactManager.getBallsToFix();
//            if (ballsToFix != null && !ballsToFix.isEmpty()){
//                for(Smesharik ball : ballsToFix){
//                    fixBallInGrid(ball);
//                    flyingBalls.remove(ball);
//                    ball.body.setAwake(true);
//
//                    ArrayList<Smesharik> matches = findMatches(ball);
//                    if(matches.size() >= 3){
//                        gameSession.addScore(matches.size()* 10);
//                       for(Smesharik matchBall : matches){
//                           bubbleGrid[matchBall.getRow()][matchBall.getCol()] = null;
//                           smeshariks.remove(matchBall);
////                           myGdxGame.world.destroyBody(matchBall.body);
//                           if (!ballsToRemove.contains(matchBall)) {
//                               ballsToRemove.add(matchBall);
//                           }
//                       }
//                    }
//                }
//                contactManager.clearBallsToFix();
//            }
    }

    private ArrayList<Smesharik> getNeighbors(int row, int col) {
        ArrayList<Smesharik> neighbors = new ArrayList<Smesharik>();

        boolean isOdd = (row % 2 != 0);
        int[][] offsets;

        if (isOdd) {
            offsets = new int[][]{
                    {0, -1}, {0, 1},
                    {-1, 0}, {-1, 1},
                    {1, 0}, {1, 1}
            };
        } else {
            offsets = new int[][]{
                    {0, -1}, {0, 1},
                    {-1, -1}, {-1, 0},
                    {1, -1}, {1, 0}
            };
        }
        for(int[] offset : offsets){
            int iRow = row + offset[0];
            int iCol = col + offset[1];

            if(iRow > -1 && iRow < GameSettings.GRID_ROWS && iCol > -1 && iCol < GameSettings.GRID_COLS && bubbleGrid[iRow][iCol] != null){
                neighbors.add(bubbleGrid[iRow][iCol]);
            }

        }
        return neighbors;
    }

    private ArrayList<Smesharik> findMatches(Smesharik startBall){
        ArrayList<Smesharik> matchList = new ArrayList<Smesharik>();
        if (startBall == null) return matchList;
        ArrayList<Smesharik> queue = new ArrayList<Smesharik>();
        boolean[][] visited = new boolean[GameSettings.GRID_ROWS][GameSettings.GRID_COLS];

        queue.add(startBall);
        matchList.add(startBall);
        visited[startBall.getRow()][startBall.getCol()] = true;

        while(!queue.isEmpty()){
            Smesharik current = queue.remove(0);
            ArrayList<Smesharik> neighbors = getNeighbors(current.getRow(), current.getCol());
            for(Smesharik neighbor : neighbors){
                int r = neighbor.getRow();
                int c = neighbor.getCol();
                if(!visited[r][c] && neighbor.getColorType() == startBall.getColorType()){
                    visited[r][c]  = true;
                    queue.add(neighbor);
                    matchList.add(neighbor);
                }
            }
        }
        return matchList;
    }
}
