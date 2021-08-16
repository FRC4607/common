package org.frc4607.common.logging;

import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class CISLogger {
    // Store the logger's name and the size of its data.
    private String name;
    private int dataSize;
    private String datetime;

    // Store the instance of the Log4J2 logger.
    private Logger log;

    /**
     * Creates a new CISLogger. You must wait for CISLogger.isReady() to be true
     * before creating, or an IllegalStateException will be thrown.
     * 
     * @param name   The name of the logger, which will be the filename of the
     *               logger's data.
     * @param labels The labels for the telemetry data. Can be empty, in which case
     *               sending telemetry will thrown an IllegalStateException.
     * @throws IllegalStateException    An IllegalStateException will be thrown if
     *                                  CISLogger.isReady() isn't true, meaning the
     *                                  robot isn't connected to the driver's
     *                                  station.
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown
     *                                  if an invalid name is picked for the logger.
     */
    public CISLogger(String name, String[] labels) {
        if (!isReady()) {
            throw new IllegalStateException("Loggers cannot be created before CISLogger.ready() returns true.");
        }
        if (name == "Messages") {
            throw new IllegalArgumentException("You cannot name a logger \"Messages\".");
        }
        String badNames = "/\\?%*:|\"<>.,;= ";
        for (char badChar : badNames.toCharArray()) {
            String badString = Character.toString(badChar);
            if (name.contains(badString)) {
                throw new IllegalArgumentException("Logger names must be valid Windows filenames.");
            }
        }
        this.name = name;
        dataSize = labels.length;
        datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        log = LogManager.getLogger("CISLogger");
        if (dataSize > 0) {
            logLabels(labels);
        }
    }

    /**
     * Logs telemetry data to the log file.
     * 
     * @param data A list of object that will be turned into strings and placed into
     *             the log
     * @throws IllegalStateException    An IllegalStateException will be created if
     *                                  no labels were provided when the logger was
     *                                  created.
     * @throws IllegalArgumentException An IllegalArgumentException if the number of
     *                                  data points passed in is not the same as the
     *                                  number of labels provided.
     */
    public void logTelemetry(Object[] data) {
        if (data.length == 0) {
            throw new IllegalStateException("Loggers created with no labels cannot send telemetry.");
        }
        if (data.length != dataSize) {
            throw new IllegalArgumentException(
                    "The number of objects passed to Logger.logTelemetry() must be the same as the number of labels that were created.");
        }
        StringBuilder sb = new StringBuilder("0,");
        sb.append(LocalTime.now().truncatedTo(ChronoUnit.MILLIS).toString());
        for (Object item : data) {
            sb.append(',');
            sb.append(csvEscape(item.toString()));
        }
        logInternal(sb.toString());
    }

    /**
     * Logs a message to the log file.
     * 
     * @param message The message to log.
     */
    public void logMessage(String message) {
        logInternal("1," + LocalTime.now().truncatedTo(ChronoUnit.MILLIS).toString() + "," + csvEscape(message));
    }

    /**
     * Checks if the logger is ready to be created, or rather if the roborio is
     * connected to the driver's station or not,
     * 
     * @return Returns true if loggers can be created.
     */
    public static boolean isReady() {
        // Check if the time is up to date.
        return LocalDate.now().getYear() >= 2021;
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
        StringBuilder sb = new StringBuilder("0,Timestamp");
        for (String label : labels) {
            sb.append(',');
            sb.append(csvEscape(label));
        }
        logInternal(sb.toString());
    }

    /**
     * Writes a string to the file.
     * 
     * @param msg The string to write.
     */
    private void logInternal(String msg) {
        try (final CloseableThreadContext.Instance ctc = CloseableThreadContext
                .putAll(Map.of("filename", name, "datetime", datetime))) {
            log.info(msg);
        }
    }
}