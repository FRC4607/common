package org.frc4607.common.swerve;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.Encoder;

/**
 * Abstracts the functionality of a motor in a swerve drive module allowing for multiple different
 * kinds of motors to be used without causing vendor lock in.
 */
public abstract class SwerveMotorBase {
    protected final SwerveDriverConfig m_config;
    protected Encoder m_quadEncoder;
    protected double m_offset;

    /**
     * Constructs a new instance of this class.
     *
     * @param config The settings for this module.
     */
    public SwerveMotorBase(SwerveDriverConfig config) {
        m_config = config;

        if (m_config.m_motorType == SwerveDriverConfig.MotorType.TURNING) {
            try (DutyCycleEncoder pwm = new DutyCycleEncoder(m_config.m_pwmChannel)) {
                m_offset = pwm.getAbsolutePosition() - m_config.m_wheelOffset;
            }

            m_quadEncoder
                = new Encoder(m_config.m_quadChannelA, m_config.m_quadChannelB,
                m_config.m_turningInvert, EncodingType.k4X);
            m_quadEncoder.setDistancePerPulse(m_config.m_turnPositionCoefficient);
        }
    }

    /**
     * Gets the CCW-positive angle of the extermal encoder in degrees if this is a turning motor
     or gets the distance traveled by the wheel in meters if this is a drive motor.
     *
     * @return The position of the motor in either CCW-positive degrees or meters depending on
     motor type.
     */
    public abstract double getEncoderPosition();

    /**
     * Returns the velocity of the motor in CCW-positive degrees per second if this is a turning
     motor or in meters per second if it is a drive motor.
     *
     * @return The position of the motor in either CCW-positive degrees per second or meters
     per second depending on motor type.
     */
    public abstract double getEncoderVelocity();

    /**
     * Sets the CCW-positive angle of the encoder if this a turning motor or the encoder position in
     meters if this is a drive motor.
     *
     * @param value The position to set the encoder to in either CCW-positive degrees or meters 
     depending on motor type.
     */
    public abstract void setEncoder(double value);

    /**
     * Returns the CCW-positive angle of either the drive or turn motor as a Rotation2d.
     *
     * @return Returns the CCW-positive angle of either the drive or turn motor as a Rotation2d.
     */
    public Rotation2d getRotation2d() {
        return Rotation2d.fromDegrees(getEncoderPosition());
    }

    /**
     * Sets the CCW-positive angle of either the drive or turn motor as a Rotation2d.
     *
     * @param value The CCW-positive angle of either the drive or turn motor as a Rotation2d.
     */
    public void setRotation2d(Rotation2d value) {
        setEncoder(value.getDegrees());
    }

    /**
     * Passthrough of {@link edu.wpi.first.math.controller.SimpleMotorFeedforward}'s
     {@code maxAchievableAcceleration} method.
     *
     * @param velocity The desired velocity in meters per second.
     * @return The voltage to apply to achieve the desired velocity.
     */
    public double getMaxAcceleration(double velocity) {
        return m_config.m_feedforward.maxAchievableAcceleration(m_config.m_maxVoltage, velocity);
    }

    /**
     * Passthrough of {@link edu.wpi.first.math.controller.SimpleMotorFeedforward}'s
     {@code calculate} method.
     *
     * @param velocity The desired velocity in meters per second.
     * @return The voltage to apply to achieve the desired velocity.
     */
    public double calculateFeedforward(double velocity) {
        return m_config.m_feedforward.calculate(velocity);
    }

    /**
     * Passthrough of {@link edu.wpi.first.math.controller.SimpleMotorFeedforward}'s
     {@code calculate} method.
     *
     * @param velocity The desired velocity in meters per second.
     * @param acceleration The desired acceleration in meters per second squared.
     * @return The voltage to apply to achieve the desired velocity.
     */
    public double calculateFeedforward(double velocity, double acceleration) {
        return m_config.m_feedforward.calculate(velocity, acceleration);
    }

    /**
     * Passthrough of {@link edu.wpi.first.math.controller.SimpleMotorFeedforward}'s
     {@code calculate} method.
     *
     * @param current The current velocity.
     * @param next The next desired velocity.
     * @param dt The time in seconds between current and next.
     * @return The voltage to apply to achieve the next velocity in the desired time.
     */
    public double calculateFeedforward(double current, double next, double dt) {
        return m_config.m_feedforward.calculate(current, next, dt);
    }

    /**
     * Commands the motor to either a velocity setpoint or position setpoint with a feedforward.
     *
     * @param value Either the velocity to command the motor to in meters per second or the position
     to command the motor to in CCW positive degrees.
     * @param ffVolts The feedforward to apply to the motor in volts.
     */
    public abstract void setTarget(double value, double ffVolts);

    /**
     * Checks if the motor is connected.
     *
     * @return Returns true if the motor is connected, false otherwise.
     */
    public abstract boolean isConnected();
}
