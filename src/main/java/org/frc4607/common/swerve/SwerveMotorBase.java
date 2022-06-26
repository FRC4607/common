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

        if (m_config.m_motorType == SwerveDriverConfig.MotorType.DRIVE) {
            DutyCycleEncoder pwm = new DutyCycleEncoder(m_config.m_pwmChannel);
            m_offset = pwm.getAbsolutePosition() - m_config.m_wheelOffset;
            pwm.close();

            m_quadEncoder
                = new Encoder(m_config.m_quadChannelA, m_config.m_quadChannelB,
                m_config.m_turningInvert, EncodingType.k4X);

            m_quadEncoder.setDistancePerPulse(m_config.m_turnPositionCoefficientt);
        }
    }

    /**
     * Returns the CCW-positive angle of either the drive or turn motor.
     *
     * @return Returns the CCW-positive angle of either the drive or turn motor in degrees.
     */
    public abstract double getEncoderPosition();

    /**
     * Returns the velocity of either the drive or turn motor.
     *
     * @return Returns the velocity of either the drive or turn motor in meters/second.
     */
    public abstract double getEncoderVelocity();

    /**
     * Sets the CCW-positive angle of either the drive or turn motor.
     *
     * @param value The CCW-positive angle of either the drive or turn motor.
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
    public void getRotation2d(Rotation2d value) {
        setEncoder(value.getDegrees());
    }

    /**
     * Passthrough of {@link edu.wpi.first.math.controller.SimpleMotorFeedforward}'s
     * {@code calculate} method.
     *
     * @param current The current velocity.
     * @param next The next desired velocity.
     * @param dt The time in seconds between current and next.
     * @return The voltage to apply to achieve the next velocity.
     */
    public double calculateFeedforward(double current, double next, double dt) {
        return m_config.m_feedforward.calculate(current, next, dt);
    }

    public double get

    /**
     * Commands the motor to a velocity setpoint.
     *
     * @param value The velocity to command the motor to in meters per second.
     */
    public abstract void setTarget(double value);

    /**
     * Checks if the motor is connected.
     *
     * @return Returns true if the motor is connected, false otherwise.
     */
    public abstract boolean isConnected();
}
