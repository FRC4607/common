package org.frc4607.common.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.RobotController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.frc4607.common.logging.CisLogger;
import org.frc4607.common.logging.CisLogger.SubLogger;
import org.frc4607.common.util.Zip;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

// import org.frc4607.common.logging.LogTest;

/**
 * Performs unit tests on {@link org.frc4607.common.logging.CisLogger}.
 */
@SuppressWarnings({"PMD.SystemPrintln"}) // println is ok here because it is a test
public class CisLoggerTest {
    private static final int MESSAGE_ID = 2;
    private static final String TIMESTAMP = "Timestamp";
    private static final String CSVDIR = "CSVDIR";
    private static final String TEST = "Test";
    private static final String TELEMETRY_TEST = "TelemetryTest";
    private static final String TE_COMMA_ST = "te,st";
    private static final String TE_QUOTE_ST = "te\"st";
    private static final String TEST_LOWERCASE = "test";
    private static final String ESCAPE_TEST = "EscapeTest";

    /**
     * Compares a CSV table with a refrence.
     *
     * @param input  A CSVParser from a CSV file.
     * @param target A List containing Lists of Strings corresponding to the cells
     *               in the table.
     */
    public static boolean compareTables(CSVParser input, List<List<String>> target)
        throws IOException {
        List<Map.Entry<CSVRecord, List<String>>> rows = Zip.zip(input.getRecords(), target);
        int line = 0;
        for (Map.Entry<CSVRecord, List<String>> pair : rows) {
            List<Map.Entry<String, String>> values
                = Zip.zip(pair.getKey().toList(), pair.getValue());
            int entry = 0;
            for (Map.Entry<String, String> pair2 : values) {
                if (entry != MESSAGE_ID) {
                    if (!pair2.getKey().equals(pair2.getValue())) {
                        System.out.println("On line " + line + " and entry " + entry);
                        System.out.println("Expected: " + pair2.getValue());
                        System.out.println("Got: " + pair2.getKey());
                        return false;
                    }
                } else {
                    if (!TIMESTAMP.equals(pair2.getKey())
                            && !TIMESTAMP.equals(pair2.getValue())) {
                        long inputTime = Long.parseLong(pair2.getKey());
                        long targetTime = Long.parseLong(pair2.getValue());
                        if (!((targetTime - 1000000 <= inputTime) 
                                && (targetTime + 1000000 >= inputTime))) {
                            System.out.println("Expected: "
                                + pair2.getValue() + " within 1 second");
                            System.out.println("Got: " + pair2.getKey());
                            return false;
                        }
                    } else {
                        if (!pair2.getKey().equals(pair2.getValue())) {
                            System.out.println("On line " + line + " and entry " + entry);
                            System.out.println("Expected: " + pair2.getValue());
                            System.out.println("Got: " + pair2.getKey());
                            return false;
                        }
                    }
                }
                entry++;
            }
        }
        return true;
    }

    /** Cleans up the CSV directory before tests begin. */
    @BeforeClass
    public static void cleanFolder() {
        File csvdir = new File(System.getenv(CSVDIR));
        File[] files = csvdir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    fail("File delete failed.");
                }
            }
        }
    }

    /** Initializes the HAL. */
    @Before
    public void setupHal() {
        assertTrue("HAL initialization failed.", HAL.initialize(500, 0));
    }

    /**
     * Tests if the logger will reject names with special characters.
     */
    @Test
    public void testReservedCharacters() {
        // https://en.wikipedia.org/wiki/Filename#In_Windows
        String badNames = "/\\?%*:|\"<>.,;= ";
        for (int i = 0; i < badNames.length(); i++) {
            char badChar = badNames.charAt(i);
            String badString = Character.toString(badChar);
            try {
                new CisLogger(badString); // NOPMD: A clean logger is needed to test properly.
                fail("A Logger with the character " + badString + " was allowed to be created.");
            } catch (IllegalArgumentException e) {
                assertEquals("The error message when invalid characters are used failed to match.",
                    e.getMessage(), "Logger names must be valid Windows filenames.");
            }
        }
    }

    /**
     * Tests if the logger will prohibit sending telemetry if no labels were given
     * to it.
     */
    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    // This way of catching errors suggested by Error Prone.
    public void testEmptyLabels() {
        CisLogger log = new CisLogger(TEST);
        SubLogger sublog = log.createSubLogger(TEST, new String[] {});
        IllegalStateException e = assertThrows(IllegalStateException.class,
            () -> {
                sublog.logTelemetry(new Object[] {});
            });
        assertEquals("The error message when trying to use telemetry with no labels failed to " 
            + "match.",
            e.getMessage(), "Loggers created with no labels cannot send telemetry.");
    }

    /** Tests the logger's ability to write telemetry. */
    @Test
    public void testTelemetry() throws FileNotFoundException, IOException, InterruptedException {
        String time = Long.toString(RobotController.getFPGATime());

        CisLogger log = new CisLogger("0_TelemetryTest");
        SubLogger sublog = log.createSubLogger(TELEMETRY_TEST, new String[] { "a", "b", "c" });
        Object[] test1 = new Object[] { 1, 2, 3 };
        Object[] test2 = new Object[] { "x", "y", "z" };
        Object[] test3 = new Object[] { false, true, false };
        Object[] test4 = new Object[] { 1, "y", false };
        sublog.logTelemetry(test1);
        sublog.logTelemetry(test2);
        sublog.logTelemetry(test3);
        sublog.logTelemetry(test4);

        String[] result0 = new String[] { "0", TELEMETRY_TEST, "Timestamp", "a", "b", "c" };
        String[] result1 = new String[] { "0", TELEMETRY_TEST, time, "1", "2", "3" };
        String[] result2 = new String[] { "0", TELEMETRY_TEST, time, "x", "y", "z" };
        String[] result3 = new String[] { "0", TELEMETRY_TEST, time, "false", "true", "false" };
        String[] result4 = new String[] { "0", TELEMETRY_TEST, time, "1", "y", "false" };
        List<List<String>> table = new ArrayList<List<String>>();
        table.add(Arrays.asList(result0));
        table.add(Arrays.asList(result1));
        table.add(Arrays.asList(result2));
        table.add(Arrays.asList(result3));
        table.add(Arrays.asList(result4));

        Thread.sleep(500);

        String csvdir = System.getenv(CSVDIR);
        String filename = csvdir + "/" + "0_TelemetryTest.csv";
        assertTrue("The tables in the telemetry test failed to match.",
            compareTables(CSVFormat.DEFAULT.parse(Files.newBufferedReader(Paths.get(filename))),
                table));
    }

    /** Tests the logger's ability to write messages. */
    @Test
    public void testMessages() throws FileNotFoundException, IOException, InterruptedException {
        CisLogger log = new CisLogger("1_MessageTest");
        SubLogger sublog = log.createSubLogger("MessageTest", new String[] {});
        sublog.logMessage("Test Message 1");
        sublog.logMessage("Test Message 2");

        String time = Long.toString(RobotController.getFPGATime());

        String[] result0 = new String[] { "1", "MessageTest", time, "Test Message 1" };
        String[] result1 = new String[] { "1", "MessageTest", time, "Test Message 2" };
        List<List<String>> table = new ArrayList<List<String>>();
        table.add(Arrays.asList(result0));
        table.add(Arrays.asList(result1));

        Thread.sleep(500);

        String csvdir = System.getenv(CSVDIR);
        String filename = csvdir + "/" + "1_MessageTest.csv";
        assertTrue("The tables in the message test failed to match.", compareTables(
            CSVFormat.DEFAULT.parse(Files.newBufferedReader(Paths.get(filename))), table));
    }

    /** Tests the logger's ability to escape the characters {@code "} and {@code ,}.*/
    @Test
    public void testEscape() throws FileNotFoundException, IOException, InterruptedException {
        String time = Long.toString(RobotController.getFPGATime());

        CisLogger log = new CisLogger("2_EscapeTest");
        SubLogger sublog = log.createSubLogger(ESCAPE_TEST,
            new String[] { TEST_LOWERCASE, TE_COMMA_ST, TE_QUOTE_ST });
        sublog.logTelemetry(new Object[] { TEST_LOWERCASE, TE_COMMA_ST, TE_QUOTE_ST });
        sublog.logMessage("This message contains a , and a \".");
        sublog.logMessage("This message contains no special characters.");

        // Commons CSV will automatically escape for us, so there's no need to put extra
        // quote marks.
        String[] result0 = new String[] { "0", ESCAPE_TEST, "Timestamp",
            TEST_LOWERCASE, TE_COMMA_ST, TE_QUOTE_ST };
        String[] result1 = new String[] { "0", ESCAPE_TEST, time, TEST_LOWERCASE,
            TE_COMMA_ST, TE_QUOTE_ST };
        String[] result2 = new String[] { "1", ESCAPE_TEST, time, "This message "
                + "contains a , and a \"." };
        String[] result3 = new String[] { "1", ESCAPE_TEST, time,
            "This message contains no special characters." };
        List<List<String>> table = new ArrayList<List<String>>();
        table.add(Arrays.asList(result0));
        table.add(Arrays.asList(result1));
        table.add(Arrays.asList(result2));
        table.add(Arrays.asList(result3));

        Thread.sleep(500);

        String csvdir = System.getenv(CSVDIR);
        String filename = csvdir + "/" + "2_EscapeTest.csv";
        assertTrue("The tables in the escape test failed to match.",
            compareTables(CSVFormat.DEFAULT.parse(
                Files.newBufferedReader(Paths.get(filename))), table));
    }
}
