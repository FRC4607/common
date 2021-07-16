import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.frc4607.common.logging.CISLogger;
// import org.frc4607.common.logging.LogTest;

public class LoggingTest {

    /**
     * Compares the text in a file to the provided string.
     * 
     * @param file The path to the file to get the text from.
     * @param text The text to compare to.
     * @return Returns true if the strings match.
     */
    public boolean compareFile(String file, String text) {
        try {
            // https://stackoverflow.com/a/14169729
            String str = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);

            System.out.println(str);
            System.out.println(text);

            return str.equals(text);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * @Test public void aaa() { LogTest.main(); }
     */

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
            } catch (IOException e) {
                e.printStackTrace();
                fail("An IOException was encountered.");
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
        } catch (IOException e) {
            e.printStackTrace();
            fail("An IOException was encountered.");
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
        } catch (IOException e) {
            e.printStackTrace();
            fail("An IOException was encountered.");
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
        } catch (IOException e) {
            e.printStackTrace();
            fail("An IOException was encountered.");
        }
        try {
            CISLogger log = new CISLogger("Test", new String[] { "a", "b", "c" });
            log.logTelemetry(new Object[] { "a", "b", "c", "d", "e" });
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(),
                    "The number of objects passed to Logger.logTelemetry() must be the same as the number of labels that were created.");
        } catch (IOException e) {
            e.printStackTrace();
            fail("An IOException was encountered.");
        }
    }

    /**
     * Tests the logger's ability to write telemetry.
     */
    @Test
    public void testTelemetry() {
        try {
            String csvdir = System.getenv("CSVDIR");
            LocalDateTime now = LocalDateTime.now();
            String filename = csvdir + "/" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "_"
                    + "TelemetryTest.csv";
            String time = now.toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString();

            CISLogger log = new CISLogger("TelemetryTest", new String[] { "a", "b", "c" });
            Object[] test1 = new Object[] { 1, 2, 3 };
            Object[] test2 = new Object[] { "x", "y", "z" };
            Object[] test3 = new Object[] { false, true, false };
            Object[] test4 = new Object[] { 1, "y", false };
            log.logTelemetry(test1);
            log.logTelemetry(test2);
            log.logTelemetry(test3);
            log.logTelemetry(test4);
            log.finishWriting();

            String fileContent = "0,Timestamp,a,b,c\r\n" + "0," + time + ",1,2,3\r\n" + "0," + time + ",x,y,z\r\n"
                    + "0," + time + ",false,true,false\r\n" + "0," + time + ",1,y,false\r\n";
            assertTrue(compareFile(filename, fileContent));
        } catch (IOException e) {
            e.printStackTrace();
            fail("An IOException was encountered.");
        }
    }

    /**
     * Tests the logger's ability to write messages.
     */
    @Test
    public void testMessages() {
        try {
            String csvdir = System.getenv("CSVDIR");
            LocalDateTime now = LocalDateTime.now();
            String filename = csvdir + "/" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "_"
                    + "MessageTest.csv";
            String time = now.toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString();

            CISLogger log = new CISLogger("MessageTest", new String[] {});
            log.logMessage("Test Message 1");
            log.logMessage("Test Message 2");
            log.finishWriting();

            String fileContent = "1," + time + ",Test Message 1\r\n" + "1," + time + ",Test Message 2\r\n";
            assertTrue(compareFile(filename, fileContent));
        } catch (IOException e) {
            e.printStackTrace();
            fail("An IOException was encountered.");
        }
    }

    /**
     * Tests the logger's ability to escape the characters " and ,.
     */
    @Test
    public void testEscape() {
        try {
            String csvdir = System.getenv("CSVDIR");
            LocalDateTime now = LocalDateTime.now();
            String filename = csvdir + "/" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "_"
                    + "EscapeTest.csv";
            String time = now.toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString();

            CISLogger log = new CISLogger("EscapeTest", new String[] { "test", "te,st", "te\"st" });
            log.logTelemetry(new Object[] { "test", "te,st", "te\"st" });
            log.logMessage("This message contains a , and a \".");
            log.logMessage("This message contains no special characters.");
            log.finishWriting();

            String fileContent = "0,Timestamp,test,\"te,st\",\"te\"\"st\"\r\n" + "0," + time
                    + ",test,\"te,st\",\"te\"\"st\"\r\n" + "1," + time
                    + ",\"This message contains a , and a \"\".\"\r\n" + "1," + time
                    + ",This message contains no special characters.\r\n";
            assertTrue(compareFile(filename, fileContent));
        } catch (IOException e) {
            e.printStackTrace();
            fail("An IOException was encountered.");
        }
    }
}
