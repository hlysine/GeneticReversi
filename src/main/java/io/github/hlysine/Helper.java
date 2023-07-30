package io.github.hlysine;

import java.util.Random;

public class Helper {
    private static final Random random = new Random();

    public static float random(float min, float max) {
        return random.nextFloat(min, max);
    }

    public static float random(float max) {
        return random(0, max);
    }

    public static float randomGaussian() {
        return (float) random.nextGaussian();
    }

    public static int floor(float x) {
        return (int) Math.floor(x);
    }

    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    public static int max(int a, int b, int c) {
        return max(max(a, b), c);
    }

    public static String padZero(int num, int length) {
        return String.format("%0" + length + "d", num);
    }

    public static String padZero(float num, int left, int right) {
        return String.format("%0" + (left + right + 1) + "." + right + "f", num);
    }

    public static String settingsPath(String path) {
        return Main.settingsDir + path;
    }

    public static String dataPath(String path) {
        return Main.dataDir + path;
    }
}
