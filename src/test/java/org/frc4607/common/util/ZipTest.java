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
    public void testZipNormal() {
        // Equal type and size
        List<String> a = Arrays.asList("A", "B", "C");
        List<String> b = Arrays.asList("D", "E", "F");
        List<Map.Entry<String, String>> c = Arrays.asList(Map.entry("A", "D"), 
            Map.entry("B", "E"), Map.entry("C", "F"));
        assertEquals("Equal type and size test failed.", Zip.zip(a, b), c);
    }

    @Test
    public void testZipUnequalType() {
        // Unequal types, but equal sizes
        List<String> a = Arrays.asList("A", "B", "C");
        List<Integer> b = Arrays.asList(1, 2, 3);
        List<Map.Entry<String, Integer>> c = Arrays.asList(Map.entry("A", 1), 
            Map.entry("B", 2), Map.entry("C", 3));
        assertEquals("Unequal type test failed.", Zip.zip(a, b), c);
    }

    @Test
    public void testZipUnequalTypeAndSizeLargerListFirst() {
        // Unequal type and size
        List<String> a = Arrays.asList("D", "E", "F");
        List<Integer> b = Arrays.asList(1, 2, 3, 4, 5, 6);
        List<Map.Entry<Integer, String>> c = Arrays.asList(Map.entry(1, "D"), 
            Map.entry(2, "E"), Map.entry(3, "F"));
        assertEquals("Unequal size test failed with larger list first.", Zip.zip(b, a), c);
    }

    @Test
    public void testZipUnequalTypeAndSizeLargerListSecond() {
        // Unequal type and size
        List<String> a = Arrays.asList("A", "B", "C");
        List<Integer> b = Arrays.asList(1, 2, 3, 4, 5, 6);
        List<Map.Entry<String, Integer>> c = Arrays.asList(Map.entry("A", 1), 
            Map.entry("B", 2), Map.entry("C", 3));
        assertEquals("Unequal size test failed with larger list second.", Zip.zip(a, b), c);
    }
}
