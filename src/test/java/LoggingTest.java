import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.frc4607.common.logging.CISLogger;


public class LoggingTest {

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
                CISLogger log = new CISLogger(badString, new String[]{"test"});
                fail("A Logger with the character " + badString + " was allowed to be created.");
            }
            catch (IllegalArgumentException e) {
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
            CISLogger log = new CISLogger("Messages", new String[]{"test"});
            fail("Creating a logger named \"Messages\" should throw an error.");
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "You cannot name a logger \"Messages\".");
        }
    }

    /**
     * Tests if the logger will prohibit sending telemetry if no labels were given to it.
     */
    @Test
    public void testEmptyLabels() {
        try {
            CISLogger log = new CISLogger("Test", new String[]{});
            log.logTelemetry(new Object[]{});
        }
        catch (IllegalStateException e) {
            assertEquals(e.getMessage(), "Loggers created with no labels cannot send telemetry.");
        }
    }

    /**
     * Tests to see if the logger will reject telemetry data if it contains the wrong number of objects.
     */
    @Test
    public void testInvalidDataSize() {
        try {
            CISLogger log = new CISLogger("Test", new String[]{"a", "b", "c"});
            log.logTelemetry(new Object[]{"a"});
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "The number of objects passed to Logger.logTelemetry() must be the same as the number of labels that were created.");
        }
        try {
            CISLogger log = new CISLogger("Test", new String[]{"a", "b", "c"});
            log.logTelemetry(new Object[]{"a", "b", "c", "d", "e"});
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "The number of objects passed to Logger.logTelemetry() must be the same as the number of labels that were created.");
        }
    }
}
