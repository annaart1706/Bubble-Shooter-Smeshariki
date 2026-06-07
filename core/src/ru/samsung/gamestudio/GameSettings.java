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
    public static final short FILTER_WALLS = 0x0004;
    public static final short FILTER_FIXED_BUBBLES = 0x0002;


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
            "Smeshariki/bibi.png"
    };

    public static final float CANNON_CANCEL_ZONE = 100f;
    public static final float SHOT_SPEED = 15f;
    public static final int CRITICAL_Y_LINE = 650;
}
