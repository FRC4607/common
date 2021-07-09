package org.frc4607.common.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import org.apache.logging.log4j.CloseableThreadContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CISLogger {
    // Store the logger's name and the size of its data.
    private String name;
    private int dataSize;

    // Store the instance of the Log4J2 logger.
    private Logger log;

    /**
     * Creates a new CISLogger. You must wait for CISLogger.isReady() to be true before creating, or an IllegalStateException will be thrown.
     * @param name The name of the logger, which will be the filename of the logger's data.
     * @param labels The labels for the telemetry data. Can be empty, in which case sending telemetry will thrown an IllegalStateException.
     * @throws IllegalStateException An IllegalStateException will be thrown if CISLogger.isReady() isn't true, meaning the robot isn't connected to the driver's station.
     * @throws IllegalArgumentException An IllegalArgumentException will be thrown if an invalid name is picked for the logger.
     */
    public CISLogger(String name, String[] labels) {
        if (LocalDate.now().getYear() < 2021) {
            throw new IllegalStateException("Loggers cannot be created before CISLogger.ready() returns true.");
        }
        if (name == "Messages") {
            throw new IllegalArgumentException("You cannot name a logger \"Messages\".");
        }
        if (name.matches("[/\\\\?%*:|\"<>.,;= ]")) {
            throw new IllegalArgumentException("Logger names must be valid Windows filenames.");
        }
        this.name = name;
        dataSize = labels.length;
        log = LogManager.getLogger("CISLogger");
        setTimeIfNeeded();
        if (dataSize > 0) {
            logLabels(labels);
        }
    }

	public void logTelemetry(Object[] data) {
        if (data.length == 0) {
            throw new IllegalStateException("Loggers created with no labels cannot send telemetry.");
        }
        if (data.length != dataSize) {
            throw new IllegalArgumentException("The number of objects passed to Logger.logTelemetry() must be the same as the number of labels that were created.");
        }
        StringBuilder sb = new StringBuilder("0,");
        sb.append(LocalTime.now().toString());
        for (Object item : data) {
            sb.append(',');
            sb.append(item.toString());
        }
        logInternal(sb.toString());
    }
    
    public void logMessage(String message) {
        logInternal("1," + LocalTime.now().toString() + ",message");
    }

    public static boolean isReady() {
        // Check if the time is up to date.
        return LocalDate.now().getYear() >= 2021;
    }

    private void logLabels(String[] labels) {
        StringBuilder sb = new StringBuilder("0,Timestamp");
        for (String label : labels) {
            sb.append(',');
            sb.append(label);
        }
        logInternal(sb.toString());
    }

    private void setTimeIfNeeded() {
        // Have we set the time yet? We don't want to make a new log every second so we will set the time when the logger is created.
        LocalDateTime now = LocalDateTime.now();
        if (ThreadContext.get("datetime") == null && now.getYear() == 1970) {
            ThreadContext.put("datetime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
        }
    }

    private void logInternal(String msg) {
        try (final CloseableThreadContext.Instance ctc = CloseableThreadContext.put("filename", name)) {
            log.info(msg);
        }
    }
}
