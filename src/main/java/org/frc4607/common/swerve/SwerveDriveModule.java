package org.frc4607.common.swerve;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;

/**
 * Represents one swerve drive module on the robot.
 */
public class SwerveDriveModule {
    private final SwerveMotorBase m_driveMotor;
    private final SwerveMotorBase m_turnMotor;
    private final Translation2d m_moduleLocation;
    private double m_lastDriveVelocity;
    private double m_lastTurnVelocity;

    /**
     * Constructs a new instance of {@code SwerveDriveModule}.
     *
     * @param drive A class that implements {@link org.frc4607.common.swerve.SwerveMotorBase} which
     controls the drive motor of the swerve module.
     * @param turn A class that implements {@link org.frc4607.common.swerve.SwerveMotorBase} which
     controls the turning motor of the swerve module.
     * @param moduleLocation A {@link edu.wpi.first.math.geometry.Translation2d} in the robot
     coordinate frame with units in meters representing the location of the swerve module relative
     to the center of the robot.
     */
    public SwerveDriveModule(SwerveMotorBase drive, SwerveMotorBase turn, 
        Translation2d moduleLocation) {
        m_driveMotor = drive;
        m_turnMotor = turn;
        m_moduleLocation = moduleLocation;
    }

    /**
     * Sets the swerve module to a given state.
     *
     * @param state A {@link edu.wpi.first.math.kinematics.SwerveModuleState} representing
     the desired state of the swerve module.
     */
    public void set(SwerveModuleState state) {
        SwerveModuleState newState = SwerveModuleState.optimize(state, m_turnMotor.getRotation2d());
        m_driveMotor.setTarget(newState.speedMetersPerSecond);
        m_turnMotor.setTarget(newState.angle.getDegrees());
    }


    public double getDriveMotorPosition() {
        return m_driveMotor.getEncoderPosition();
    }

    public double getDriveMotorVelocity() {
        return m_driveMotor.getEncoderVelocity();
    }

    public double getTurnMotorPosition() {
        return m_turnMotor.getEncoderPosition();
    }

    public double getTurnMotorVelocity() {
        return m_turnMotor.getEncoderVelocity();
    }

    public Translation2d getModuleLocation() {
        return m_moduleLocation;
    }

    public boolean isDriveMotorConnected() {
        return m_driveMotor.isConnected();
    }

    public boolean isTurnMotorConnected() {
        return m_turnMotor.isConnected();
    }
}