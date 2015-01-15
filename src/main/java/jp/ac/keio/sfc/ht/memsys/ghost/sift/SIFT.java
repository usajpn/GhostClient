package jp.ac.keio.sfc.ht.memsys.ghost.sift;

import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by aqram on 11/1/14.
 * imported some classes from
 * http://fly.mpi-cbg.de/~saalfeld/Projects/javasift.html
 */
public class SIFT implements Serializable{

    private final String NAME = "SIFT";

    /*
    public OffloadableData run(OffloadableData offloadableData) {

        int[] pics = (int[]) offloadableData.getData("PICTURE");


        return null;
    }

    public String getName() {
        return NAME;
    }
    */

    private static int steps = 5;
    // initial sigma
    private static float initial_sigma = 1.6f;
    // feature descriptor size
    private static int fdsize = 4;

    // feature descriptor orientation bins
    private static int fdbins = 8;
    // size restrictions for scale octaves, use octaves < max_size and >
    // min_size only
    private static int min_size = 64;
    private static int max_size = 1024;

    public static int fdbins() {
        return fdbins;
    }

    public static void fdbins(int fb) {
        fdbins = fb;
    }

    public static int fdsize() {
        return fdsize;
    }

    public static Vector<Feature> getFeatures(FloatArray2D fa) {
        Vector<Feature> fs1;

        long start_time = System.currentTimeMillis();
        FloatArray2DSIFT sift = new FloatArray2DSIFT(fdsize, fdbins);
        long track1 = System.currentTimeMillis();

        Filter.enhance(fa, 1.0f);

        long track2 = System.currentTimeMillis();
        fa = Filter.computeGaussianFastMirror(fa, (float) Math.sqrt(initial_sigma * initial_sigma - 0.25));
        long track3 = System.currentTimeMillis();

        // System.out.print( "processing SIFT ..." );
        sift.init(fa, steps, initial_sigma, min_size, max_size);
        long track4 = System.currentTimeMillis();
        fs1 = sift.run(max_size);
        long track5 = System.currentTimeMillis();

        System.out.println(" -----------SAMPLE1 :-------------- " +  String.valueOf(track1 - start_time));
        System.out.println(" -----------SAMPLE2 :-------------- " +  String.valueOf(track2 - track1));
        System.out.println(" -----------SAMPLE3 :-------------- " +  String.valueOf(track3 - track2));
        System.out.println(" -----------SAMPLE4 :-------------- " +  String.valueOf(track4 - track3));
        System.out.println(" -----------SAMPLE5 :-------------- " +  String.valueOf(track5 - track4));

        Collections.sort(fs1);
        // System.out.println( " took " + ( System.currentTimeMillis() -
        // start_time ) + "ms" );

        // System.out.println( fs1.size() + " features identified and processed"
        // );

        return fs1;
    }

    /**
     * @param w      width of the picture
     * @param h      height of the picture
     * @param pixels [] tab of pixels rgb color (ex: red 0xff0000)
     * @return vector of features of the picture
     * @author Jonathan ODUL 2009
     * @link http://www.jidul.com
     * @version 1.0
     */

    public static Vector<Feature> getFeatures(int w, int h, int pixels[]) {
        FloatArray2D fa = ArrayToFloatArray2D(w, h, pixels);

        return getFeatures(fa);
    }

    public static float initial_sigma() {
        return initial_sigma;
    }

    public static int max_size() {
        return max_size();
    }

    public static int min_size() {
        return min_size();
    }

    public static void set_fdsize(int fs) {
        fdsize = fs;
    }

    public static void set_initial_sigma(float is) {
        initial_sigma = is;
    }

    public static void set_max_size(int ms) {
        max_size = ms;
    }

    public static void set_min_size(int ms) {
        min_size = ms;
    }

    public static void set_steps(int s) {
        steps = s;
    }

    public static int steps() {
        return steps;
    }

    public static FloatArray2D ArrayToFloatArray2D(int w, int h, int pixels[]) {
        FloatArray2D image = new FloatArray2D(w, h);

        int rgb, r, g, b;
        for (int i = 0; i < pixels.length; i++) {
            rgb = pixels[i];
            b = rgb & 0xff;
            rgb = rgb >> 8;
            g = rgb & 0xff;
            rgb = rgb >> 8;
            r = rgb & 0xff;
            image.data[i] = 0.3f * r + 0.6f * g + 0.1f * b;
        }

        return image;
    }
}
