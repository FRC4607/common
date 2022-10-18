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

    /**
     * Constructs a new instance of {@code SwerveDriveModule}.
     *
     * @param drive A class that extends {@link org.frc4607.common.swerve.SwerveMotorBase} which
     controls the drive motor of the swerve module.
     * @param turn A class that extends {@link org.frc4607.common.swerve.SwerveMotorBase} which
     controls the turning motor of the swerve module.
     * @param moduleLocation A {@link edu.wpi.first.math.geometry.Translation2d} in the robot
     coordinate frame with units in meters representing the location of the swerve module's wheel's
     turning axis relative to the center of the robot.
     * @param maxTurnVelocity The maximum turning speed of the module in CCW positive degrees
     per second.
     */
    public SwerveDriveModule(SwerveMotorBase drive, SwerveMotorBase turn, 
        Translation2d moduleLocation, double maxTurnVelocity) {
        m_driveMotor = drive;
        m_turnMotor = turn;
        m_moduleLocation = moduleLocation;

        m_turnConstraints = new TrapezoidProfile.Constraints(maxTurnVelocity,
            m_turnMotor.getMaxAcceleration(maxTurnVelocity));
    }

    /**
     * Sets the swerve module to a given state.
     *
     * @param state A {@link edu.wpi.first.math.kinematics.SwerveModuleState} representing
     the desired state of the swerve module.
     */
    public void set(SwerveModuleState state) {
        /*
        1. Get the current position and turning velocity of the wheel to create a new profile state.
        2. Construct the profile and use it to get the next state in time.
        3. Calculate the feedforward for the turning motor with the two velocities of the states.
        4. Calculate the feedfoward for the drive motor based on the state's velocity and the
        current velocity.
        5. Set both motors.
        */
        TrapezoidProfile.State currentTurn =
            new TrapezoidProfile.State(getTurnMotorPosition(), getTurnMotorVelocity());

        TrapezoidProfile.State targetTurn =
            new TrapezoidProfile.State(state.angle.getDegrees(), 0);
        TrapezoidProfile profile = new TrapezoidProfile(m_turnConstraints, currentTurn, targetTurn);
        TrapezoidProfile.State next = profile.calculate(0.001);

        double turnFeedforward =
            m_turnMotor.calculateFeedforward(currentTurn.velocity, next.velocity, 0.001);

        double currentVelocity = getDriveMotorVelocity();
        
        double driveFeedforward =
            m_driveMotor.calculateFeedforward(currentVelocity, state.speedMetersPerSecond, 0.001);

        m_turnMotor.setTarget(state.angle.getDegrees(), turnFeedforward);
        m_driveMotor.setTarget(state.speedMetersPerSecond, driveFeedforward);
    }

    /**
     * Gets the position of the drive motor in meters.
     *
     * @return The position of the drive motor in meters.
     */
    public double getDriveMotorPosition() {
        return m_driveMotor.getEncoderPosition();
    }

    /**
     * Gets the position of the drive motor in meters per second.
     *
     * @return The position of the drive motor in meters per second.
     */
    public double getDriveMotorVelocity() {
        return m_driveMotor.getEncoderVelocity();
    }

    /**
     * Gets the position of the drive motor in CCW positive degrees.
     *
     * @return The position of the drive motor in CCW positive degrees.
     */
    public double getTurnMotorPosition() {
        return m_turnMotor.getEncoderPosition();
    }

    /**
     * Gets the position of the drive motor in CCW positive degrees per second.
     *
     * @return The position of the drive motor in CCW positive degrees per second.
     */
    public double getTurnMotorVelocity() {
        return m_turnMotor.getEncoderVelocity();
    }

    /**
     * Gets the location of the swerve module's wheel's turning axis relative to
     the center of the robot.
     *
     * @return The location of the swerve module's wheel's turning axis relative to
     the center of the robot.
     */
    public Translation2d getModuleLocation() {
        return m_moduleLocation;
    }

    /**
     * Checks if the drive motor of this module is connected to the robot.
     *
     * @return Whether or not the drive motor is connected to the robot.
     */
    public boolean isDriveMotorConnected() {
        return m_driveMotor.isConnected();
    }

    /**
     * Checks if the turning motor of this module is connected to the robot.
     *
     * @return Whether or not the turning motor is connected to the robot.
     */
    public boolean isTurnMotorConnected() {
        return m_turnMotor.isConnected();
    }
}