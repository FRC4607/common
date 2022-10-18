package org.frc4607.common.swerve;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;

/**
 * Contains the configuration settings for objects inheriting from
 {@link org.frc4607.common.swerve.SwerveMotorBase}.
 */
public class SwerveDriverConfig {
    /** Determines what kind of motor the driver is representing. */
    public static enum MotorType {
        DRIVE,
        TURNING
    }

    /** The CAN ID of the motor the driver is representing. */
    public int m_id = -1;

    /** The maximum voltage that can be applied to the motor. */
    public double m_maxVoltage = 12;

    /** The value to multiply the native position units by to convert them to CCW-positive
     degrees in the case of the turning motor, or meters in the case of the drive motor.
     */
    public double m_positionCoefficient = 1;

    /** The value to multiply the native velocity units by to convert them to meters per
     second for the drive motor, and CCW-positive degrees per second for the turning motor.
     */
    public double m_velocityCoefficient = 1;

    /** A feedforward with constants corresponding to the motor. Units should be meters for
     drive motors and CCW-positive degrees for turning motors.
     */
    public SimpleMotorFeedforward m_feedforward;
    
    /** The {@link org.frc4607.common.swerve.SwerveDriverConfig.MotorType} of this motor. */
    public MotorType m_motorType;

    /** The P term of the hardware PID controller of the motor for velocity control.
     Unit will depend on motor type.
     */
    public double m_kp;

    /** The I term of the hardware PID controller of the motor for velocity control.
     Unit will depend on motor type.
     */
    public double m_ki;

    /** The D term of the hardware PID controller of the motor for velocity control.
     Unit will depend on motor type.
     */
    public double m_kd;

    /** The IZone term of the hardware PID controller of the motor for velocity control.
     Unit will depend on motor type.
     */
    public double m_kiZone;

    /** The maximum integral value of the hardware PID controller for velocity control.
     Unit will depend on motor type.
     */
    public double m_maxI;
    
    /** Whether or not to invert the motor. */
    public boolean m_invertMotor;

    /** TURNING ONLY: The A channel DIO of the quad encoder for the turning motor. */
    public int m_quadChannelA;

    /** TURNING ONLY: The B channel DIO of the quad encoder for the turning motor. */
    public int m_quadChannelB;

    /** TURNING ONLY: The PWM channel DIO of the quad encoder for the turning motor. */
    public int m_pwmChannel = -1;

    /** TURNING ONLY: The value to multiply the native position units of the external
     encoder by to convert them to CCW-positive degrees.
     */
    public double m_turnPositionCoefficient = 1;

    /** TURNING ONLY: The absolute value of the external encoder in CCW-positive degrees when
     the wheel of the module is facing towards positve X so that applying positive voltage to the
     motor would cause movement in the positive X direction.
     */
    public double m_wheelOffset;

    /** TURNING ONLY: Whether or not to invert the external encoder. */
    public boolean m_turningInvert;
}
