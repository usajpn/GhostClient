package jp.ac.keio.sfc.ht.memsys.ghost.sift;

import Jama.Matrix;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.data.OffloadableData;
import jp.ac.keio.sfc.ht.memsys.ghost.commonlib.tasks.OffloadableTask;
import jp.ac.keio.sfc.ht.memsys.ghost.image.ImageSample;
import jp.ac.keio.sfc.ht.memsys.ghost.image.SIFTUtil;

import java.awt.image.BufferedImage;
import java.util.Vector;

/**
 * Created by aqram on 11/2/14.
 */
public class SIFTTask implements OffloadableTask {

    private final static String NAME = "SIFT";
    private FloatArray2DScaleOctave mOctave;
    private int mSeq = 0;
    private int fdsize = 4;
    private int fdbins = 8;
    private int steps = 5;
    private float initial_sigma = 1.6f;
    private int min_size = 64;
    private int max_size = 1024;

    @Override
    public OffloadableData run(OffloadableData offloadableData) {
        Vector<Feature> features = new Vector<Feature>();
        mOctave = (FloatArray2DScaleOctave) offloadableData.getData("OCTAVE");
        mSeq = Integer.valueOf((String)offloadableData.getData("SEQ"));
        mOctave.build();
        Vector<float[]> candidates = detectCandidates(mOctave);

        for(float[] c : candidates) {
            processCandidate(c,features);
        }

        OffloadableData result = new OffloadableData("SIFT",String.valueOf(mSeq));
        result.putData("FEATURE", features);
        result.putData("SEQ", mSeq);

        return result;
    }

    @Override
    public String getName() {
        return NAME;
    }


    /**
     * IMPORT DOG CLASS
     */
    private static final float MIN_CONTRAST = 0.025f;

    /**
     * maximal curvature ratio, higher values allow more edge-like responses
     */
    private static final float MAX_CURVATURE = 10;
    private static final float MAX_CURVATURE_RATIO = (MAX_CURVATURE + 1) * (MAX_CURVATURE + 1) / MAX_CURVATURE;


    /**
     * detected candidates as float triples 0=>x, 1=>y, 2=>scale index
     */

    private Vector<float[]> detectCandidates(FloatArray2DScaleOctave octave) {
        FloatArray2D[] d = octave.getD();
        Vector<float[]> candidates = new Vector<float[]>();

        for (int i = d.length - 2; i >= 1; --i) {
            int ia = i - 1;
            int ib = i + 1;
            for (int y = d[i].height - 2; y >= 1; --y) {
                int r = y * d[i].width;
                int ra = r - d[i].width;
                int rb = r + d[i].width;

                X:
                for (int x = d[i].width - 2; x >= 1; --x) {
                    int ic = i;
                    int iac = ia;
                    int ibc = ib;
                    int yc = y;
                    int rc = r;
                    int rac = ra;
                    int rbc = rb;
                    int xc = x;
                    int xa = xc - 1;
                    int xb = xc + 1;
                    float e111 = d[ic].data[r + xc];

                    // check if d(x, y, i) is an extremum
                    // do it pipeline-friendly ;)

                    float e000 = d[iac].data[rac + xa];
                    boolean isMax = e000 < e111;
                    boolean isMin = e000 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e100 = d[iac].data[rac + xc];
                    isMax &= e100 < e111;
                    isMin &= e100 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e200 = d[iac].data[rac + xb];
                    isMax &= e200 < e111;
                    isMin &= e200 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }

                    float e010 = d[iac].data[rc + xa];
                    isMax &= e010 < e111;
                    isMin &= e010 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e110 = d[iac].data[rc + xc];
                    isMax &= e110 < e111;
                    isMin &= e110 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e210 = d[iac].data[rc + xb];
                    isMax &= e210 < e111;
                    isMin &= e210 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }

                    float e020 = d[iac].data[rbc + xa];
                    isMax &= e020 < e111;
                    isMin &= e020 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e120 = d[iac].data[rbc + xc];
                    isMax &= e120 < e111;
                    isMin &= e120 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e220 = d[iac].data[rbc + xb];
                    isMax &= e220 < e111;
                    isMin &= e220 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }

                    float e001 = d[ic].data[rac + xa];
                    isMax &= e001 < e111;
                    isMin &= e001 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e101 = d[ic].data[rac + xc];
                    isMax &= e101 < e111;
                    isMin &= e101 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e201 = d[ic].data[rac + xb];
                    isMax &= e201 < e111;
                    isMin &= e201 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }

                    float e011 = d[ic].data[rc + xa];
                    isMax &= e011 < e111;
                    isMin &= e011 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e211 = d[ic].data[rc + xb];
                    isMax &= e211 < e111;
                    isMin &= e211 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }

                    float e021 = d[ic].data[rbc + xa];
                    isMax &= e021 < e111;
                    isMin &= e021 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e121 = d[ic].data[rbc + xc];
                    isMax &= e121 < e111;
                    isMin &= e121 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e221 = d[ic].data[rbc + xb];
                    isMax &= e221 < e111;
                    isMin &= e221 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }

                    float e002 = d[ibc].data[rac + xa];
                    isMax &= e002 < e111;
                    isMin &= e002 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e102 = d[ibc].data[rac + xc];
                    isMax &= e102 < e111;
                    isMin &= e102 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e202 = d[ibc].data[rac + xb];
                    isMax &= e202 < e111;
                    isMin &= e202 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }

                    float e012 = d[ibc].data[rc + xa];
                    isMax &= e012 < e111;
                    isMin &= e012 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e112 = d[ibc].data[rc + xc];
                    isMax &= e112 < e111;
                    isMin &= e112 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e212 = d[ibc].data[rc + xb];
                    isMax &= e212 < e111;
                    isMin &= e212 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }

                    float e022 = d[ibc].data[rbc + xa];
                    isMax &= e022 < e111;
                    isMin &= e022 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e122 = d[ibc].data[rbc + xc];
                    isMax &= e122 < e111;
                    isMin &= e122 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }
                    float e222 = d[ibc].data[rbc + xb];
                    isMax &= e222 < e111;
                    isMin &= e222 > e111;
                    if (!(isMax || isMin)) {
                        continue;
                    }

                    // so it is an extremum, try to localize it with subpixel
                    // accuracy, if it has to be moved for more than 0.5 in at
                    // least one direction, try it again there but maximally 5
                    // times

                    boolean isLocalized = false;
                    boolean isLocalizable = true;

                    float dx;
                    float dy;
                    float di;

                    float dxx;
                    float dyy;
                    float dii;

                    float dxy;
                    float dxi;
                    float dyi;

                    float ox;
                    float oy;
                    float oi;

                    float od = Float.MAX_VALUE; // offset square distance

                    float fx = 0;
                    float fy = 0;
                    float fi = 0;

                    int t = 5; // maximal number of re-localizations
                    do {
                        --t;

                        // derive at (x, y, i) by center of difference
                        dx = (e211 - e011) / 2.0f;
                        dy = (e121 - e101) / 2.0f;
                        di = (e112 - e110) / 2.0f;

                        // create hessian at (x, y, i) by laplace
                        float e111_2 = 2.0f * e111;
                        dxx = e011 - e111_2 + e211;
                        dyy = e101 - e111_2 + e121;
                        dii = e110 - e111_2 + e112;

                        dxy = (e221 - e021 - e201 + e001) / 4.0f;
                        dxi = (e212 - e012 - e210 + e010) / 4.0f;
                        dyi = (e122 - e102 - e120 + e100) / 4.0f;

                        // invert hessian
                        Matrix H = new Matrix(new double[][]{{dxx, dxy, dxi}, {dxy, dyy, dyi}, {dxi, dyi, dii}}, 3, 3);
                        Matrix H_inv;

                        try {
                            H_inv = H.inverse();
                        } catch (RuntimeException e) {
                            continue X;
                        }
                        double[][] h_inv = H_inv.getArray();

                        // estimate the location of zero crossing being the
                        // offset of the extremum

                        ox = -(float) h_inv[0][0] * dx - (float) h_inv[0][1] * dy - (float) h_inv[0][0] * di;
                        oy = -(float) h_inv[1][0] * dx - (float) h_inv[1][1] * dy - (float) h_inv[1][0] * di;
                        oi = -(float) h_inv[2][0] * dx - (float) h_inv[2][1] * dy - (float) h_inv[2][0] * di;

                        float odc = ox * ox + oy * oy + oi * oi;

                        if (odc < 2.0f) {
                            if ((Math.abs(ox) > 0.5 || Math.abs(oy) > 0.5 || Math.abs(oi) > 0.5) && odc < od) {
                                od = odc;

                                xc = Math.round(xc + ox);
                                yc = Math.round(yc + oy);
                                ic = Math.round(ic + oi);

                                if (xc < 1 || yc < 1 || ic < 1 || xc > d[0].width - 2 || yc > d[0].height - 2 || ic > d.length - 2) {
                                    isLocalizable = false;
                                } else {
                                    xa = xc - 1;
                                    xb = xc + 1;
                                    rc = yc * d[ic].width;
                                    rac = rc - d[ic].width;
                                    rbc = rc + d[ic].width;
                                    iac = ic - 1;
                                    ibc = ic + 1;

                                    e000 = d[iac].data[rac + xa];
                                    e100 = d[iac].data[rac + xc];
                                    e200 = d[iac].data[rac + xb];

                                    e010 = d[iac].data[rc + xa];
                                    e110 = d[iac].data[rc + xc];
                                    e210 = d[iac].data[rc + xb];

                                    e020 = d[iac].data[rbc + xa];
                                    e120 = d[iac].data[rbc + xc];
                                    e220 = d[iac].data[rbc + xb];

                                    e001 = d[ic].data[rac + xa];
                                    e101 = d[ic].data[rac + xc];
                                    e201 = d[ic].data[rac + xb];

                                    e011 = d[ic].data[rc + xa];
                                    e111 = d[ic].data[rc + xc];
                                    e211 = d[ic].data[rc + xb];

                                    e021 = d[ic].data[rbc + xa];
                                    e121 = d[ic].data[rbc + xc];
                                    e221 = d[ic].data[rbc + xb];

                                    e002 = d[ibc].data[rac + xa];
                                    e102 = d[ibc].data[rac + xc];
                                    e202 = d[ibc].data[rac + xb];

                                    e012 = d[ibc].data[rc + xa];
                                    e112 = d[ibc].data[rc + xc];
                                    e212 = d[ibc].data[rc + xb];

                                    e022 = d[ibc].data[rbc + xa];
                                    e122 = d[ibc].data[rbc + xc];
                                    e222 = d[ibc].data[rbc + xb];
                                }
                            } else {
                                fx = xc + ox;
                                fy = yc + oy;
                                fi = ic + oi;

                                if (fx < 0 || fy < 0 || fi < 0 || fx > d[0].width - 1 || fy > d[0].height - 1 || fi > d.length - 1) {
                                    isLocalizable = false;
                                } else {
                                    isLocalized = true;
                                }
                            }
                        } else {
                            isLocalizable = false;
                        }
                    } while (!isLocalized && isLocalizable && t >= 0);
                    // reject detections that could not be localized properly

                    if (!isLocalized) {
                        // System.err.println( "Localization failed (x: " + xc +
                        // ", y: " + yc + ", i: " + ic + ") => (ox: " + ox +
                        // ", oy: " + oy + ", oi: " + oi + ")" );
                        // if ( ic < 1 || ic > d.length - 2 )
                        // System.err.println( "  Detection outside octave." );
                        continue;
                    }

                    // reject detections with very low contrast

                    if (Math.abs(e111 + 0.5f * (dx * ox + dy * oy + di * oi)) < MIN_CONTRAST) {
                        continue;
                    }

                    // reject edge responses

                    float det = dxx * dyy - dxy * dxy;
                    float trace = dxx + dyy;
                    if (trace * trace / det > MAX_CURVATURE_RATIO) {
                        continue;
                    }

                    candidates.addElement(new float[]{fx, fy, fi});
                }
            }
        }

        return candidates;
    }


    void processCandidate(float[] c,  Vector<Feature> features) {
        final int ORIENTATION_BINS = 36;
        final float ORIENTATION_BIN_SIZE = 2.0f * (float) Math.PI / ORIENTATION_BINS;
        float[] histogram_bins = new float[ORIENTATION_BINS];

        int scale = (int) Math.pow(2, mSeq);

        FloatArray2DScaleOctave octave = mOctave;

        float octave_sigma = octave.SIGMA[0] * (float) Math.pow(2.0f, c[2] / octave.STEPS);

        // create a circular gaussian window with sigma 1.5 times that of the
        // feature
        FloatArray2D gaussianMask = Filter.create_gaussian_kernel_2D_offset(octave_sigma * 1.5f, c[0] - (float) Math.floor(c[0]), c[1] - (float) Math.floor(c[1]), false);
        // FloatArrayToImagePlus( gaussianMask, "gaussianMask", 0, 0 ).show();

        // get the gradients in a region arround the keypoints location
        FloatArray2D[] src = octave.getL1(Math.round(c[2]));
        FloatArray2D[] gradientROI = new FloatArray2D[2];
        gradientROI[0] = new FloatArray2D(gaussianMask.width, gaussianMask.width);
        gradientROI[1] = new FloatArray2D(gaussianMask.width, gaussianMask.width);

        int half_size = gaussianMask.width / 2;
        int p = gaussianMask.width * gaussianMask.width - 1;
        for (int yi = gaussianMask.width - 1; yi >= 0; --yi) {
            int ra_y = src[0].width * Math.max(0, Math.min(src[0].height - 1, (int) c[1] + yi - half_size));
            int ra_x = ra_y + Math.min((int) c[0], src[0].width - 1);

            for (int xi = gaussianMask.width - 1; xi >= 0; --xi) {
                int pt = Math.max(ra_y, Math.min(ra_y + src[0].width - 2, ra_x + xi - half_size));
                gradientROI[0].data[p] = src[0].data[pt];
                gradientROI[1].data[p] = src[1].data[pt];
                --p;
            }
        }

        // and mask this region with the precalculated gaussion window
        for (int i = 0; i < gradientROI[0].data.length; ++i) {
            gradientROI[0].data[i] *= gaussianMask.data[i];
        }

        // TODO this is for test
        // ---------------------------------------------------------------------
        // ImageArrayConverter.FloatArrayToImagePlus( gradientROI[ 0 ],
        // "gaussianMaskedGradientROI", 0, 0 ).show();
        // ImageArrayConverter.FloatArrayToImagePlus( gradientROI[ 1 ],
        // "gaussianMaskedGradientROI", 0, 0 ).show();

        // build an orientation histogram of the region
        for (int i = 0; i < gradientROI[0].data.length; ++i) {
            int bin = Math.max(0, (int) ((gradientROI[1].data[i] + Math.PI) / ORIENTATION_BIN_SIZE));
            histogram_bins[bin] += gradientROI[0].data[i];
        }

        // find the dominant orientation and interpolate it with respect to its
        // two neighbours
        int max_i = 0;
        for (int i = 0; i < ORIENTATION_BINS; ++i) {
            if (histogram_bins[i] > histogram_bins[max_i]) {
                max_i = i;
            }
        }

        /**
         * interpolate orientation estimate the offset from center of the
         * parabolic extremum of the taylor series through env[1], derivatives
         * via central difference and laplace
         */
        float e0 = histogram_bins[(max_i + ORIENTATION_BINS - 1) % ORIENTATION_BINS];
        float e1 = histogram_bins[max_i];
        float e2 = histogram_bins[(max_i + 1) % ORIENTATION_BINS];
        float offset = (e0 - e2) / 2.0f / (e0 - 2.0f * e1 + e2);
        float orientation = (max_i + offset) * ORIENTATION_BIN_SIZE - (float) Math.PI;

        // assign descriptor and add the Feature instance to the collection
        features.addElement(new Feature(octave_sigma * scale, orientation, new float[]{c[0] * scale, c[1] * scale},
                // new float[]{ ( c[ 0 ] + 0.5f ) * scale - 0.5f, ( c[ 1 ] + 0.5f ) *
                // scale - 0.5f },
                createDescriptor(c,  octave_sigma, orientation)));

        // TODO this is for test
        // ---------------------------------------------------------------------
        // ImageArrayConverter.FloatArrayToImagePlus( pattern, "test", 0f, 1.0f
        // ).show();

        /**
         * check if there is another significant orientation ( > 80% max ) if
         * there is one, duplicate the feature and
         */
        for (int i = 0; i < ORIENTATION_BINS; ++i) {
            if (i != max_i && (max_i + 1) % ORIENTATION_BINS != i && (max_i - 1 + ORIENTATION_BINS) % ORIENTATION_BINS != i && histogram_bins[i] > 0.8 * histogram_bins[max_i]) {
                /**
                 * interpolate orientation estimate the offset from center of
                 * the parabolic extremum of the taylor series through env[1],
                 * derivatives via central difference and laplace
                 */
                e0 = histogram_bins[(i + ORIENTATION_BINS - 1) % ORIENTATION_BINS];
                e1 = histogram_bins[i];
                e2 = histogram_bins[(i + 1) % ORIENTATION_BINS];

                if (e0 < e1 && e2 < e1) {
                    offset = (e0 - e2) / 2.0f / (e0 - 2.0f * e1 + e2);
                    orientation = (i + 0.5f + offset) * ORIENTATION_BIN_SIZE - (float) Math.PI;

                    features.addElement(new Feature(octave_sigma * scale, orientation, new float[]{c[0] * scale, c[1] * scale},
                            // new float[]{ ( c[ 0 ] + 0.5f ) * scale - 0.5f, ( c[ 1 ] +
                            // 0.5f ) * scale - 0.5f },
                            createDescriptor(c,  octave_sigma, orientation)));

                    // TODO this is for test
                    // ---------------------------------------------------------------------
                    // ImageArrayConverter.FloatArrayToImagePlus( pattern,
                    // "test", 0f, 1.0f ).show();
                }
            }
        }
        return;
    }

    /**
     * number of orientation histograms per axis of the feature descriptor
     * square
     */
    private int FEATURE_DESCRIPTOR_SIZE;
    private int FEATURE_DESCRIPTOR_WIDTH;

    /**
     * number of bins per orientation histogram of the feature descriptor
     */
    private int FEATURE_DESCRIPTOR_ORIENTATION_BINS = 0;

    private float FEATURE_DESCRIPTOR_ORIENTATION_BIN_SIZE = 0;

    /**
     * evaluation mask for the feature descriptor square
     */
    private float[][] descriptorMask;


    private float[] createDescriptor(float[] c, float octave_sigma, float orientation) {
        FloatArray2DScaleOctave octave = mOctave;
        FloatArray2D[] gradients = octave.getL1(Math.round(c[2]));
        FloatArray2D[] region = new FloatArray2D[2];

        region[0] = new FloatArray2D(FEATURE_DESCRIPTOR_WIDTH, FEATURE_DESCRIPTOR_WIDTH);
        region[1] = new FloatArray2D(FEATURE_DESCRIPTOR_WIDTH, FEATURE_DESCRIPTOR_WIDTH);
        float cos_o = (float) Math.cos(orientation);
        float sin_o = (float) Math.sin(orientation);

        // TODO this is for test
        // ---------------------------------------------------------------------
        // FloatArray2D image = octave.getL( Math.round( c[ 2 ] ) );
        // pattern = new FloatArray2D( FEATURE_DESCRIPTOR_WIDTH,
        // FEATURE_DESCRIPTOR_WIDTH );

        // ! sample the region arround the keypoint location
        for (int y = FEATURE_DESCRIPTOR_WIDTH - 1; y >= 0; --y) {
            float ys = (y - 2.0f * FEATURE_DESCRIPTOR_SIZE + 0.5f) * octave_sigma; // !<
            // scale
            // y
            // around
            // 0,0
            for (int x = FEATURE_DESCRIPTOR_WIDTH - 1; x >= 0; --x) {
                float xs = (x - 2.0f * FEATURE_DESCRIPTOR_SIZE + 0.5f) * octave_sigma; // !<
                // scale
                // x
                // around
                // 0,0
                float yr = cos_o * ys + sin_o * xs; // !< rotate y around 0,0
                float xr = cos_o * xs - sin_o * ys; // !< rotate x around 0,0

                // flip_range at borders
                // TODO for now, the gradients orientations do not flip outside
                // the image even though they should do it. But would this
                // improve the result?

                // translate ys to sample y position in the gradient image
                int yg = Filter.flipInRange(Math.round(yr + c[1]), gradients[0].height);

                // translate xs to sample x position in the gradient image
                int xg = Filter.flipInRange(Math.round(xr + c[0]), gradients[0].width);

                // get the samples
                int region_p = FEATURE_DESCRIPTOR_WIDTH * y + x;
                int gradient_p = gradients[0].width * yg + xg;

                // weigh the gradients
                region[0].data[region_p] = gradients[0].data[gradient_p] * descriptorMask[y][x];

                // rotate the gradients orientation it with respect to the
                // features orientation
                region[1].data[region_p] = gradients[1].data[gradient_p] - orientation;

                // TODO this is for test
                // ---------------------------------------------------------------------
                // pattern.data[ region_p ] = image.data[ gradient_p ];
            }
        }

        float[][][] hist = new float[FEATURE_DESCRIPTOR_SIZE][FEATURE_DESCRIPTOR_SIZE][FEATURE_DESCRIPTOR_ORIENTATION_BINS];

        // build the orientation histograms of 4x4 subregions
        for (int y = FEATURE_DESCRIPTOR_SIZE - 1; y >= 0; --y) {
            int yp = FEATURE_DESCRIPTOR_SIZE * 16 * y;
            for (int x = FEATURE_DESCRIPTOR_SIZE - 1; x >= 0; --x) {
                int xp = 4 * x;
                for (int ysr = 3; ysr >= 0; --ysr) {
                    int ysrp = 4 * FEATURE_DESCRIPTOR_SIZE * ysr;
                    for (int xsr = 3; xsr >= 0; --xsr) {
                        float bin_location = (region[1].data[yp + xp + ysrp + xsr] + (float) Math.PI) / FEATURE_DESCRIPTOR_ORIENTATION_BIN_SIZE;

                        int bin_b = (int) bin_location;
                        int bin_t = bin_b + 1;
                        float d = bin_location - bin_b;

                        bin_b = (bin_b + 2 * FEATURE_DESCRIPTOR_ORIENTATION_BINS) % FEATURE_DESCRIPTOR_ORIENTATION_BINS;
                        bin_t = (bin_t + 2 * FEATURE_DESCRIPTOR_ORIENTATION_BINS) % FEATURE_DESCRIPTOR_ORIENTATION_BINS;

                        float t = region[0].data[yp + xp + ysrp + xsr];

                        hist[y][x][bin_b] += t * (1 - d);
                        hist[y][x][bin_t] += t * d;
                    }
                }
            }
        }

        float[] desc = new float[FEATURE_DESCRIPTOR_SIZE * FEATURE_DESCRIPTOR_SIZE * FEATURE_DESCRIPTOR_ORIENTATION_BINS];

        // normalize, cut above 0.2 and renormalize
        float max_bin_val = 0;
        int i = 0;
        for (int y = FEATURE_DESCRIPTOR_SIZE - 1; y >= 0; --y) {
            for (int x = FEATURE_DESCRIPTOR_SIZE - 1; x >= 0; --x) {
                for (int b = FEATURE_DESCRIPTOR_ORIENTATION_BINS - 1; b >= 0; --b) {
                    desc[i] = hist[y][x][b];
                    if (desc[i] > max_bin_val) {
                        max_bin_val = desc[i];
                    }
                    ++i;
                }
            }
        }
        max_bin_val /= 0.2;
        for (i = 0; i < desc.length; ++i) {
            desc[i] = (float) Math.min(1.0, desc[i] / max_bin_val);
        }

        return desc;
    }


}
