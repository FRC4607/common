package org.frc4607.common.logging;

import edu.wpi.first.wpilibj.RobotController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 * A logger designed to take telemetry data from multiple subsystems and store
 * it in one file for parsing later.
 */
@Deprecated
public class CisLogger {
    /**
     * An individual instance of the logger that is passed to each subsystem.
     */
    public class SubLogger {
        // Store the logger's name and the size of its data.
        private String m_name;
        private int m_numColumns;

        private SubLogger(String name, String... labels) {
            this.m_name = name;
            m_numColumns = labels.length;
            if (m_numColumns > 0) {
                logLabels(labels);
            }
        }

        /**
         * Logs telemetry data to the log file. If there is not the same number of data
         * points as columns, this method will do nothing, rather than throw an
         * exception.
         *
         * @param data A list of object that will be turned into strings and placed into
         *             the log
         * @throws IllegalStateException An IllegalStateException will be created if no
         *                               labels were provided when the logger was
         *                               created.
         */
        public void logTelemetry(Object... data) {
            // Throw exception here because like the name of the logger, labels should be
            // defined at compile time.
            if (m_numColumns == 0) {
                throw new IllegalStateException("Loggers created with no labels cannot send "
                + "telemetry.");
            }
            StringBuilder sb = new StringBuilder("0,")
                .append(csvEscape(m_name))
                .append(',')
                .append(RobotController.getFPGATime());
            for (Object item : data) {
                sb.append(',')
                    .append(csvEscape(item.toString()));
            }
            logInternal(sb.toString());
        }

        /**
         * Logs a message to the log file.
         *
         * @param message The message to log.
         */
        public void logMessage(String message) {
            StringBuilder sb = new StringBuilder("1,")
                .append(csvEscape(m_name))
                .append(',')
                .append(RobotController.getFPGATime())
                .append(',')
                .append(csvEscape(message));
            logInternal(sb.toString());
        }

        /**
         * Escapes a CSV item.
         *
         * @param in The unescaped string.
         * @return Returns the escaped string.
         */
        private String csvEscape(String in) {
            String out = in;
            if (in.contains("\"")) {
                out = out.replace("\"", "\"\"");
                return new StringBuilder("\"")
                    .append(out)
                    .append('\"')
                    .toString();
            }
            if (in.contains(",")) {
                return new StringBuilder("\"")
                    .append(out)
                    .append('\"')
                    .toString();
            }
            return out;
        }

        /**
         * Logs the labels for telemetry.
         *
         * @param labels The labels to log.
         */
        private void logLabels(String... labels) {
            StringBuilder sb = new StringBuilder("0,")
                .append(csvEscape(m_name))
                .append(",Timestamp");
            for (String label : labels) {
                sb.append(',')
                    .append(csvEscape(label));
            }
            logInternal(sb.toString());
        }
    }

    // Store the filename.
    private String m_fileName;

    // Store the instance of the Log4J2 logger.
    private Logger m_log;

    /**
     * Creates a new CisLogger.
     *
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown
     *                                  if an invalid name is picked for the logger.
     * @deprecated Use {@link edu.wpi.first.wpilibj.DataLogManager} instead.
     */
    @Deprecated
    public CisLogger(String fileName) {
        // Throw an exception here because this should be a compile time error.
        String badNames = "/\\?%*:|\"<>.,;= ";
        for (int i = 0; i < badNames.length(); i++) { 
            char badChar = badNames.charAt(i);
            String badString = Character.toString(badChar);
            if (fileName.contains(badString)) {
                throw new IllegalArgumentException("Logger names must be valid Windows filenames.");
            }
        }
        this.m_fileName = fileName;
        ThreadContext.put("filename", this.m_fileName);
        m_log = LogManager.getLogger("CISLogger");
    }

    /**
     * Creates a new sublogger with the specified name and labels.
     *
     * @param name   The name of the logger.
     * @param labels The labels to use for telemetry. Can be empty, in which case
     *               {@link SubLogger#logTelemetry(Object[])} will throw an
     *               IllegalStateException.
     * @return The sublogger.
     */
    public SubLogger createSubLogger(String name, String... labels) {
        return new SubLogger(name, labels);
    }

    /**
     * Writes a string to the file.
     *
     * @param msg The string to write.
     */
    private void logInternal(String msg) {
        m_log.info(msg);
    }
}