import org.junit.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.frc4607.common.util.Zip;
import org.frc4607.common.logging.CISLogger;

// import org.frc4607.common.logging.LogTest;

public class LoggingTest {
    /**
     * Compares a CSV table with a refrence.
     * 
     * @param input  A CSVParser from a CSV file.
     * @param target A List containing Lists of Strings corresponding to the cells
     *               in the table.
     * @return
     * @throws IOException
     */
    public static boolean compareTables(CSVParser input, List<List<String>> target) throws IOException {
        List<Map.Entry<CSVRecord, List<String>>> rows = Zip.zip(input.getRecords(), target);
        int line = 0;
        for (Map.Entry<CSVRecord, List<String>> pair : rows) {
            List<Map.Entry<String, String>> values = Zip.zip(pair.getKey().toList(), pair.getValue());
            int entry = 0;
            for (Map.Entry<String, String> pair2 : values) {
                if (entry != 1) {
                    if (!pair2.getKey().equals(pair2.getValue())) {
                        System.out.println("On line " + line + " and entry " + entry);
                        System.out.println("Expected: " + pair2.getValue());
                        System.out.println("Got: " + pair2.getKey());
                        return false;
                    }
                } else {
                    if (!pair2.getValue().equals("Timestamp")) {
                        LocalTime inputTime = LocalTime.parse(pair2.getKey());
                        LocalTime targetTime = LocalTime.parse(pair2.getValue());
                        if (!inputTime.isBefore(targetTime.plusSeconds(1))) {
                            System.out.println("Expected: " + pair2.getValue() + " within 1 second");
                            System.out.println("Got: " + pair2.getKey());
                            return false;
                        }
                    } else {
                        if (!pair2.getKey().equals("Timestamp")) {
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

    /**
     * Tests if the logger will reject names with special characters.
     */
    @Test
    public void testReservedCharacters() {
        // https://en.wikipedia.org/wiki/Filename#In_Windows
        String badNames = "/\\?%*:|\"<>.,;= ";
        for (char badChar : badNames.toCharArray()) {
            String badString = Character.toString(badChar);
            try {
                CISLogger log = new CISLogger(badString, new String[] { "test" });
                fail("A Logger with the character " + badString + " was allowed to be created.");
            } catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), "Logger names must be valid Windows filenames.");
            }
        }
    }

    /**
     * Tests if the logger will reject the name "Messages"
     */
    @Test
    public void testReservedName() {
        try {
            CISLogger log = new CISLogger("Messages", new String[] { "test" });
            fail("Creating a logger named \"Messages\" should throw an error.");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "You cannot name a logger \"Messages\".");
        }
    }

    /**
     * Tests if the logger will prohibit sending telemetry if no labels were given
     * to it.
     */
    @Test
    public void testEmptyLabels() {
        try {
            CISLogger log = new CISLogger("Test", new String[] {});
            log.logTelemetry(new Object[] {});
        } catch (IllegalStateException e) {
            assertEquals(e.getMessage(), "Loggers created with no labels cannot send telemetry.");
        }
    }

    /**
     * Tests to see if the logger will reject telemetry data if it contains the
     * wrong number of objects.
     */
    @Test
    public void testInvalidDataSize() {
        try {
            CISLogger log = new CISLogger("Test", new String[] { "a", "b", "c" });
            log.logTelemetry(new Object[] { "a" });
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(),
                    "The number of objects passed to Logger.logTelemetry() must be the same as the number of labels that were created.");
        }
        try {
            CISLogger log = new CISLogger("Test", new String[] { "a", "b", "c" });
            log.logTelemetry(new Object[] { "a", "b", "c", "d", "e" });
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(),
                    "The number of objects passed to Logger.logTelemetry() must be the same as the number of labels that were created.");
        }
    }

    /**
     * Tests the logger's ability to write telemetry.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test
    public void testTelemetry() throws FileNotFoundException, IOException {
        String csvdir = System.getenv("CSVDIR");
        LocalDateTime now = LocalDateTime.now();
        String filename = csvdir + "/" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "_"
                + "TelemetryTest.csv";
        String time = now.toLocalTime().truncatedTo(ChronoUnit.MILLIS).toString();

        CISLogger log = new CISLogger("TelemetryTest", new String[] { "a", "b", "c" });
        Object[] test1 = new Object[] { 1, 2, 3 };
        Object[] test2 = new Object[] { "x", "y", "z" };
        Object[] test3 = new Object[] { false, true, false };
        Object[] test4 = new Object[] { 1, "y", false };
        log.logTelemetry(test1);
        log.logTelemetry(test2);
        log.logTelemetry(test3);
        log.logTelemetry(test4);

        String[] result0 = new String[] { "0", "Timestamp", "a", "b", "c" };
        String[] result1 = new String[] { "0", time, "1", "2", "3" };
        String[] result2 = new String[] { "0", time, "x", "y", "z" };
        String[] result3 = new String[] { "0", time, "false", "true", "false" };
        String[] result4 = new String[] { "0", time, "1", "y", "false" };
        List<List<String>> table = new ArrayList<List<String>>();
        table.add(Arrays.asList(result0));
        table.add(Arrays.asList(result1));
        table.add(Arrays.asList(result2));
        table.add(Arrays.asList(result3));
        table.add(Arrays.asList(result4));

        assertTrue(compareTables(CSVFormat.DEFAULT.parse(new FileReader(filename)), table));
    }

    /**
     * Tests the logger's ability to write messages.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test
    public void testMessages() throws FileNotFoundException, IOException {
        String csvdir = System.getenv("CSVDIR");
        LocalDateTime now = LocalDateTime.now();
        String filename = csvdir + "/" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "_"
                + "MessageTest.csv";
        String time = now.toLocalTime().truncatedTo(ChronoUnit.MILLIS).toString();

        CISLogger log = new CISLogger("MessageTest", new String[] {});
        log.logMessage("Test Message 1");
        log.logMessage("Test Message 2");

        String[] result0 = new String[] { "1", time, "Test Message 1" };
        String[] result1 = new String[] { "1", time, "Test Message 2" };
        List<List<String>> table = new ArrayList<List<String>>();
        table.add(Arrays.asList(result0));
        table.add(Arrays.asList(result1));

        assertTrue(compareTables(CSVFormat.DEFAULT.parse(new FileReader(filename)), table));
    }

    /**
     * Tests the logger's ability to escape the characters " and ,.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test
    public void testEscape() throws FileNotFoundException, IOException {
        String csvdir = System.getenv("CSVDIR");
        LocalDateTime now = LocalDateTime.now();
        String filename = csvdir + "/" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "_"
                + "EscapeTest.csv";
        String time = now.toLocalTime().truncatedTo(ChronoUnit.MILLIS).toString();

        CISLogger log = new CISLogger("EscapeTest", new String[] { "test", "te,st", "te\"st" });
        log.logTelemetry(new Object[] { "test", "te,st", "te\"st" });
        log.logMessage("This message contains a , and a \".");
        log.logMessage("This message contains no special characters.");

        // Commons CSV will automatically escape for us, so there's no need to put extra
        // quote marks.
        String[] result0 = new String[] { "0", "Timestamp", "test", "te,st", "te\"st" };
        String[] result1 = new String[] { "0", time, "test", "te,st", "te\"st" };
        String[] result2 = new String[] { "1", time, "This message contains a , and a \"." };
        String[] result3 = new String[] { "1", time, "This message contains no special characters." };
        List<List<String>> table = new ArrayList<List<String>>();
        table.add(Arrays.asList(result0));
        table.add(Arrays.asList(result1));
        table.add(Arrays.asList(result2));
        table.add(Arrays.asList(result3));

        assertTrue(compareTables(CSVFormat.DEFAULT.parse(new FileReader(filename)), table));
    }
}
