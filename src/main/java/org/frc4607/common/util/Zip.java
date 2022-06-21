package org.frc4607.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A static method designed to take two lists {@code a} and {@code b} and create a resulting list of
 * {@code [[a[0], b[0]], [a[1], b[1]], ..., [a[-1], b[-1]]]}.
 */
public class Zip {
    /**
     * Zips two lists together.
     *
     * @param a A list.
     * @param b Another list.
     * @return The two lists zipped together.
     */
    // Based on https://stackoverflow.com/a/31964093
    public static <A, B> List<Map.Entry<A, B>> zip(List<A> a, List<B> b) {
        List<Map.Entry<A, B>> out = new ArrayList<Map.Entry<A, B>>();
        for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
            out.add(Map.entry(a.get(i), b.get(i)));
        }
        return out;
    }
}
