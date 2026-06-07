    package ru.samsung.gamestudio;

    import com.badlogic.gdx.Gdx;
    import com.badlogic.gdx.graphics.Color;
    import com.badlogic.gdx.graphics.g2d.BitmapFont;
    import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

    public class FontBuilder {

        public static BitmapFont generate(int size, Color color, String fontPath) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontPath));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = size;
            parameter.color = color;
            parameter.characters = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
                    "абвгдеёжзийклмнопрстуфхцчшщъыьэюя" +
                    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "0123456789.,!?:;'\"()-+=/%";

            BitmapFont font = generator.generateFont(parameter);
            generator.dispose();
            return font;
        }

    }
