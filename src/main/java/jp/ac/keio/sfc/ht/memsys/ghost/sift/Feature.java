package jp.ac.keio.sfc.ht.memsys.ghost.sift;

import java.io.Serializable;

/**
 * Created by aqram on 11/1/14.
 */
public class Feature implements Comparable<Feature>, Serializable {
    public float scale;
    public float orientation;
    public float[] location;
    public float[] descriptor;

    public Feature() {
    }

    public Feature(float s, float o, float[] l, float[] d) {
        scale = s;
        orientation = o;
        location = l;
        descriptor = d;
    }

    /**
     * comparator for making Features sortable please note, that the comparator
     * returns -1 for this.scale &gt; o.scale, to sort the features in a
     * descending order
     */
    public int compareTo(Feature f) {
        return scale < f.scale ? 1 : scale == f.scale ? 0 : -1;
    }

    public float descriptorDistance(Feature f) {
        float d = 0;
        for (int i = 0; i < descriptor.length; ++i) {
            float a = descriptor[i] - f.descriptor[i];
            d += a * a;
        }
        return (float) Math.sqrt(d);
    }

}
