package org.frc4607.common.logging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class NIOLogger {
    // Store the logger's name and the size of its data.
    private String name;
    private String datetime;
    private int dataSize;

    // Store a refrence to our file.
    private AsynchronousFileChannel file;
    private int cursor = 0;
    private Future future = null;

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
     * @throws IOException              An IOException will be thrown if the log
     *                                  file is unable to be opened.
     * 
     * @deprecated This logger uses java.nio and is not stable enough for
     *             competition use. Please use
     *             {@link CISLogger#CISLogger(String, String[]) CISLogger} instead.
     */
    public NIOLogger(String name, String[] labels) throws IOException {
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
        file = openFile();
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
     * Finishes waiting for the most recent write to the log file to complete or
     * error out. Will complete instantly if no writing has been done.
     */
    public void finishWriting() {
        if (future != null) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
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
     * Opens the log file.
     * 
     * @return Returns the log file as an AsynchronousFileChannel.
     * @throws IOException An IOException will be thrown if the file cannot be
     *                     opened.
     */
    private AsynchronousFileChannel openFile() throws IOException {
        Path path;
        String csvdir = System.getenv("CSVDIR");
        if (csvdir == null) {
            path = Paths.get("/home/lvuser/logs/" + datetime + "_" + name + ".csv");
        } else {
            path = Paths.get(csvdir + "/" + datetime + "_" + name + ".csv");
        }
        return AsynchronousFileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    /**
     * Writes a string to the file.
     * 
     * @param msg The string to write.
     */
    private void logInternal(String msg) {
        // https://www.baeldung.com/java-nio2-async-file-channel
        int length = msg.length() + 2;
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put((msg + "\r\n").getBytes());
        buffer.flip();

        // No need to do anything else, since we want writes to run in the background.
        future = file.write(buffer, cursor);
        cursor += length;
    }
}
