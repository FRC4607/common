package org.frc4607.common.swerve;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.TrapezoidProfile;

/**
 * Represents one swerve drive module on the robot.
 */
public class SwerveDriveModule {
    private final SwerveMotorBase m_driveMotor;
    private final SwerveMotorBase m_turnMotor;
    private final Translation2d m_moduleLocation;
    private final TrapezoidProfile.Constraints m_turnConstraints;
    private double m_lastDriveVelocity = 0;

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
     * @param maxVelocity The maximum turning speed of the module in meters per second.
     */
    public SwerveDriveModule(SwerveMotorBase drive, SwerveMotorBase turn, 
        Translation2d moduleLocation, double maxVelocity) {
        m_driveMotor = drive;
        m_turnMotor = turn;
        m_moduleLocation = moduleLocation;

        m_turnConstraints = new TrapezoidProfile.Constraints(maxVelocity,
            m_turnMotor.getMaxAcceleration(maxVelocity));
    }

    /**
     * Sets the swerve module to a given state.
     *
     * @param state A {@link edu.wpi.first.math.kinematics.SwerveModuleState} representing
     the desired state of the swerve module.
     */
    public void set(SwerveModuleState state) {
        /*
        1. Get the current position and turning velocity of the wheel to create a new state.
        2. Construct the profile and use it to get the next state in time.
        3. Calculate the feedforward for the turning motor with the two velocities.
        4. Do the feedforward for the drive motor by extrapolating the next velocity.
        5. Set both motors.
        */
        TrapezoidProfile.State current =
            new TrapezoidProfile.State(getTurnMotorPosition(), getTurnMotorVelocity());

        TrapezoidProfile.State target =
            new TrapezoidProfile.State(state.angle.getDegrees(), 0);
        TrapezoidProfile profile = new TrapezoidProfile(m_turnConstraints, current, target);
        TrapezoidProfile.State next = profile.calculate(0.001);

        double turnFeedforward =
            m_turnMotor.calculateFeedforward(current.velocity, next.velocity, 0.001);

        double currentVelocity = getDriveMotorVelocity();
        double nextVelocity = currentVelocity + (currentVelocity - m_lastDriveVelocity);
        
        double driveFeedforward =
            m_driveMotor.calculateFeedforward(currentVelocity, nextVelocity, 0.001);

        m_turnMotor.setTarget(state.angle.getDegrees(), turnFeedforward);
        m_driveMotor.setTarget(state.speedMetersPerSecond, driveFeedforward);

        m_lastDriveVelocity = currentVelocity;
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