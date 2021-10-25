package org.frc4607.common.logging;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import edu.wpi.first.wpilibj.RobotController;

import org.apache.logging.log4j.LogManager;

public class CISLogger {
    public class SubLogger {
        // Store the logger's name and the size of its data.
        private String name;
        private int numColumns;

        // Store the parent of the logger.
        private CISLogger parent;

        private SubLogger(String name, String[] labels, CISLogger parent) {
            this.name = name;
            this.parent = parent;
            numColumns = labels.length;
            if (numColumns > 0) {
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
        public void logTelemetry(Object[] data) {
            // Throw exception here because like the name of the logger, labels should be
            // defined at compile time.
            if (numColumns == 0) {
                throw new IllegalStateException("Loggers created with no labels cannot send telemetry.");
            }
            StringBuilder sb = new StringBuilder("0,");
            sb.append(csvEscape(name));
            sb.append(",");
            sb.append(RobotController.getFPGATime());
            for (Object item : data) {
                sb.append(',');
                sb.append(csvEscape(item.toString()));
            }
            parent.logInternal(sb.toString());
        }

        /**
         * Logs a message to the log file.
         * 
         * @param message The message to log.
         */
        public void logMessage(String message) {
            StringBuilder sb = new StringBuilder("1,");
            sb.append(csvEscape(name));
            sb.append(",");
            sb.append(RobotController.getFPGATime());
            sb.append(",");
            sb.append(csvEscape(message));
            parent.logInternal(sb.toString());
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
                out = "\"" + out + "\"";
                return out;
            }
            if (in.contains(",")) {
                out = "\"" + out + "\"";
                return out;
            }
            return out;
        }

        /**
         * Logs the labels for telemetry.
         * 
         * @param labels The labels to log.
         */
        private void logLabels(String[] labels) {
            StringBuilder sb = new StringBuilder("0,");
            sb.append(csvEscape(name));
            sb.append(",Timestamp");
            for (String label : labels) {
                sb.append(',');
                sb.append(csvEscape(label));
            }
            parent.logInternal(sb.toString());
        }
    }

    // Store the filename.
    private String fileName;

    // Store the instance of the Log4J2 logger.
    private Logger log;

    /**
     * Creates a new CISLogger. It is not recommended to create more than one
     * instance of this class, as the filename of the instance created most recently
     * 
     * @param filename The filename of the log.
     * @param labels   The labels for the telemetry data. Can be empty, in which
     *                 case sending telemetry will thrown an IllegalStateException.
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown
     *                                  if an invalid name is picked for the logger.
     */
    public CISLogger(String fileName) {
        // Throw an exception here because this should be a compile time error.
        String badNames = "/\\?%*:|\"<>.,;= ";
        for (char badChar : badNames.toCharArray()) {
            String badString = Character.toString(badChar);
            if (fileName.contains(badString)) {
                throw new IllegalArgumentException("Logger names must be valid Windows filenames.");
            }
        }
        this.fileName = fileName;
        ThreadContext.put("filename", this.fileName);
        log = LogManager.getLogger("CISLogger");
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
    public SubLogger createSubLogger(String name, String[] labels) {
        return new SubLogger(name, labels, this);
    }

    /**
     * Writes a string to the file.
     * 
     * @param msg The string to write.
     */
    private void logInternal(String msg) {
        System.out.println(ThreadContext.get("filename"));
        log.info(msg);
    }
}