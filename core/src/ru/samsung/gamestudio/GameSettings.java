package ru.samsung.gamestudio;

public class GameSettings {

    // Device settings

    public static final int SCREEN_WIDTH = 720;
    public static final int SCREEN_HEIGHT = 1280;

    // Physics settings

    public static final float STEP_TIME = 1f / 60f;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 6;
    public static final float SCALE = 0.03f;

    public static long STARTING_TRASH_APPEARANCE_COOL_DOWN = 7000; // in [ms] - milliseconds
    public static int BULLET_VELOCITY = 200; // in [m/s] - meter per second
    public static int SHOOTING_COOL_DOWN = 1000; // in [ms] - milliseconds

    public static final short TRASH_BIT = 2;
    public static final short SHIP_BIT = 4;

    // Object sizes

    public static final int SHIP_WIDTH = 150;
    public static final int SHIP_HEIGHT = 150;
    public static final int BUBBLE_RADIUS = 45;
    public static final int BUBBLE_DIAMETER = BUBBLE_RADIUS * 2;
    public static final int GRID_ROWS = 8;
    public static final int GRID_COLS = 8;
    public static final float ROW_HEIGHT = BUBBLE_DIAMETER * 0.866f;
    public static final short FILTER_WALLS = 0x0004; // Новый бит СТРОГО для левой и правой стен
    public static final short FILTER_FIXED_BUBBLES = 0x0002; // Оставляем ТОЛЬКО для шаров в сетке и потолка


    public static final String[] BUBBLE_TEXTURES = {
            "Smeshariki/krosh.png",
            "Smeshariki/nusha.png",
            "Smeshariki/egik.png",
            "Smeshariki/kapatich.png",
            "Smeshariki/barash.png",
            "Smeshariki/karych.png",
            "Smeshariki/losaysh.png",

            "Smeshariki/pin.png",
            "Smeshariki/sovynya.png",

            // НАШ СКРЫТЫЙ БОНУС: Биби под индексом 9!
            "Smeshariki/bibi.png"       // 9
    };

    public static final float CANNON_CANCEL_ZONE = 100f;
    public static final float SHOT_SPEED = 15f;

    // === НАШИ НОВЫЕ НАСТРОЙКИ ДЛЯ МИЛЫХ ОБЛАКОВ-ГРАНИЦЫ ===
    // Координата Y, ниже которой Смешарикам опускаться нельзя (конец 8-го ряда)
    public static final int CRITICAL_Y_LINE = 650;

    // Путь к картинке облачной гряды (не забудь положить её в assets/images/)
    public static final String CLOUD_LINE_IMG_PATH = "textures/cloud_border.png";

}
