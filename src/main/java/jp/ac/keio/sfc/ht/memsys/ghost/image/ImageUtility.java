package jp.ac.keio.sfc.ht.memsys.ghost.image;

/**
 * Created by aqram on 11/10/14.
 */
public class ImageUtility {

    public static int a(int c) {
        return c >>> 24;
    }

    public static int r(int c) {
        return c >> 16 & 0xff;
    }

    public static int g(int c) {
        return c >> 8 & 0xff;
    }

    public static int b(int c) {
        return c & 0xff;
    }

    public static int rgb
            (int r, int g, int b) {
        return 0xff000000 | r << 16 | g << 8 | b;
    }

    public static int argb
            (int a, int r, int g, int b) {
        return a << 24 | r << 16 | g << 8 | b;
    }
}
