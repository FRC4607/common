package org.frc4607.common.swerve;

import edu.wpi.first.math.geometry.Rotation2d;

public abstract class SwerveMotorBase {

    /**
     * Returns the CCW-positive angle of either the drive or turn motor.
     * @return The CCW-positive angle of either the drive or turn motor in degrees.
     */
    public abstract double getEncoderPosition();

    /**
     * Returns the velocity of either the drive or turn motor.
     * @return The velocity of either the drive or turn motor in meters/second.
     */
    public abstract double getEncoderVelocity();

    /**
     * Sets the CCW-positive angle of either the drive or turn motor.
     * @param value The CCW-positive angle of either the drive or turn motor.
     */
    public abstract void setEncoder(double value);

    /**
     * Returns the CCW-positive angle of either the drive or turn motor as a Rotation2d.
     * @return The CCW-positive angle of either the drive or turn motor as a Rotation2d.
     */
    public Rotation2d getRotation2d() {
        return Rotation2d.fromDegrees(getEncoderPosition());
    }

    /**
     * Sets the CCW-positive angle of either the drive or turn motor as a Rotation2d.
     * @param value The CCW-positive angle of either the drive or turn motor as a Rotation2d.
     */
    public void getRotation2d(Rotation2d value) {
        setEncoder(value.getDegrees());
    }

    /**
     * Commands the motor to either a velocity or position setpoint.
     * @param value The value to command the motor to. Will be in meters/second if commanding the drive motor, and in CCW positive degrees if the turn motor.
     */
    public abstract void setTarget(double value);

    /**
     * Checks if the motor is connected.
     * @return True if the motor is connected, false otherwise.
     */
    public abstract boolean isConnected();
}
