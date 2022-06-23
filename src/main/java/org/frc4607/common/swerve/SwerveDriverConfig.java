package org.frc4607.common.swerve;

/**
 * Contains the configuration settings for objects inheriting from
 * {@link org.frc4607.common.swerve.SwerveMotorBase}.
 */
public class SwerveDriverConfig {
    /** Determines what kind of motor the driver is representing. */
    public static enum MotorType {
        DRIVE,
        TURNING
    }

    /** The CAN ID of the motor the driver is representing. */
    public int m_id;

    /** The value to multiply the native position units by to convert them to CCW positive
     * degrees.
     */
    public float m_positionCoefficient;

    /** The value to multiply the native position units by to convert them to meters per
     * second.
     */
    public float m_velocityCoefficient;

    /** The voltage needed to overcome the static friction of the motor. Unit will depend on
     * motor type.
     */
    public float m_ks;
    
    /** The {@link org.frc4607.common.swerve.SwerveDriverConfig.MotorType} of this motor. */
    public MotorType m_motorType;

    /** The P term of the hardware PID controller of the motor for velocity control.
     * Unit will depend on motor type. */
    public float m_kp;

    /** The I term of the hardware PID controller of the motor for velocity control.
     * Unit will depend on motor type. */
    public float m_ki;

    /** The D term of the hardware PID controller of the motor for velocity control.
     * Unit will depend on motor type. */
    public float m_kd;

    /** The F term of the hardware PID controller of the motor for velocity control.
     * Unit will depend on motor type. */
    public float m_kf;

    /** The IZone term of the hardware PID controller of the motor for velocity control.
     * Unit will depend on motor type.
     */
    public float m_kiZone;

    /** TURNING ONLY: The maximum velocity of the motor when turning in m/s. */
    public float m_maxVelocity;

    /** TURNING ONLY: The maximum acceleration of the motor when turning in m/s. */
    public float m_maxAcceleration;

    /** TURNING ONLY: The A channel DIO of the quad encoder for the turning motor. */
    public int m_quadChannelA;

    /** TURNING ONLY: The B channel DIO of the quad encoder for the turning motor. */
    public int m_quadChannelB;

    /** TURNING ONLY: The PWM channel DIO of the quad encoder for the turning motor. */
    public int m_pwmChannel;

    /** TURNING ONLY: The CPR of the external encoder used on the swerve module. */
    public int m_encoderCpr;
}
