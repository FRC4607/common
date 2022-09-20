package org.frc4607.common.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.frc4607.common.util.Zip;
import org.junit.Test;

/**
 * Performs unit tests on {@link org.frc4607.common.util.Zip}.
 */
public class ZipTest {
    @Test
    public void testZip() {
        // Equal type and size
        List<String> a = Arrays.asList("A", "B", "C");
        List<String> b = Arrays.asList("D", "E", "F");
        List<Map.Entry<String, String>> c = Arrays.asList(Map.entry("A", "D"), 
            Map.entry("B", "E"), Map.entry("C", "F"));
        assertEquals("Equal type and size test failed.", Zip.zip(a, b), c);

        // Unequal types, but equal sizes
        List<Integer> d = Arrays.asList(1, 2, 3);
        List<Map.Entry<String, Integer>> e = Arrays.asList(Map.entry("A", 1), 
            Map.entry("B", 2), Map.entry("C", 3));
        assertEquals("Unequal type test failed.", Zip.zip(a, d), e);

        // Unequal type and size
        List<Integer> f = Arrays.asList(1, 2, 3, 4, 5, 6);
        List<Map.Entry<Integer, String>> g = Arrays.asList(Map.entry(1, "D"), 
            Map.entry(2, "E"), Map.entry(3, "F"));
        assertEquals("Unequal size test failed with larger list second.", Zip.zip(a, f), e);
        assertEquals("Unequal size test failed with larger list first.", Zip.zip(f, b), g);
    }
}
