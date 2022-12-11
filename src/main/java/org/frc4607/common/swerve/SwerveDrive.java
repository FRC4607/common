package org.frc4607.common.swerve;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.frc4607.common.util.Zip;

/**
 * Handles the the kinematics and odometry of a coaxial swerve drive and provides methods to get 
 * information about the state of the swerve drive.
 */
public class SwerveDrive {
    private static final Translation2d m_center = new Translation2d(0, 0);

    private final double m_maxWheelVelocity;

    private List<SwerveDriveModule> m_activeModules;
    private SwerveDriveKinematics m_kinematics;

    private final Gyro m_gyro;
    private SwerveDriveOdometry m_odometry;

    private int m_kinematicModuleCount;

    /**
     * Constructs a new {@code SwerveDrive}.
     *
     * @param maxWheelVelocity The maximum wheel velocity of the slowest wheel in the swerve drive
     in meters per second.
     * @param gyro A class implementing {@link edu.wpi.first.wpilibj.interfaces.Gyro} used
     to get the heading of the robot.
     * @param modules A array of {@link org.frc4607.common.swerve.SwerveDriveModule} objects
     with one object for each swerve drive module on the robot.
     */
    public SwerveDrive(double maxWheelVelocity, Gyro gyro, SwerveDriveModule... modules) {
        m_gyro = gyro;
        m_activeModules = List.of(modules);
        reconstructKinematics(m_activeModules.stream());
        m_maxWheelVelocity = maxWheelVelocity;
    }

    /**
     * Updates the swerve drive modules given a desired
     {@link edu.wpi.first.math.kinematics.ChassisSpeeds} object and a desired center of rotation.
     *
     * @param speeds The desired chassis speeds.
     * @param centerRotationMeters A {@link edu.wpi.first.math.geometry.Translation2d} in the robot
     coordinate frame with units in meters representing the desired center of rotation.
     */
    public void update(ChassisSpeeds speeds, Translation2d centerRotationMeters) {
        List<SwerveDriveModule> validModules = validate(m_activeModules);

        SwerveModuleState[] states = 
            m_kinematics.toSwerveModuleStates(speeds, centerRotationMeters);
        SwerveDriveKinematics.desaturateWheelSpeeds(states, m_maxWheelVelocity);
        
        List<SwerveModuleState> currentStates = new ArrayList<SwerveModuleState>();
        Zip.zip(validModules, List.of(states)).stream().forEachOrdered((pair) -> {
            SwerveDriveModule module = pair.getKey();
            SwerveModuleState state = pair.getValue();
            state = SwerveModuleState.optimize(state,
                Rotation2d.fromDegrees(module.getTurnMotorPosition()));
            module.set(state);
            currentStates.add(module.get());
            /* 2023 WPILib: odometry/pose estimation requires a SwerveModulePosition (same thing as
            SwerveModuleState but it uses position instead of velocity). */
        });

        /* If we only have 0 or 1 motors left, fill up the states odometry is expecting with zeroed
         * states.
         */
        SwerveModuleState[] odometryStates =
            currentStates.toArray(new SwerveModuleState[m_kinematicModuleCount]);
        for (int i = currentStates.size(); i < odometryStates.length; i++) {
            odometryStates[i] = new SwerveModuleState(0, new Rotation2d());
        }

        m_odometry.update(m_gyro.getRotation2d(), odometryStates);
    }

    /**
     * Updates the swerve drive modules given a desired
     {@link edu.wpi.first.math.kinematics.ChassisSpeeds} object, assuming the robot's center
     as the center of rotation.
     *
     * @param speeds The desired chassis speeds.
     */
    public void update(ChassisSpeeds speeds) {
        update(speeds, m_center);
    }

    /**
     * Updates the swerve drive modules given a desired
     {@link edu.wpi.first.math.kinematics.ChassisSpeeds} object in the field coordinate
     system.
     *
     * @param fieldOrientedSpeeds The desired chassis speeds in the field coordinate system.
     * @param centerRotationMeters A {@link edu.wpi.first.math.geometry.Translation2d} in the robot
     coordinate frame with units in meters representing the desired center of rotation.
     */
    public void updateFieldOriented(ChassisSpeeds fieldOrientedSpeeds,
        Translation2d centerRotationMeters) {
        // 2023 WPILib: This can be greatly simplified with the new overload.
        update(ChassisSpeeds.fromFieldRelativeSpeeds(
            fieldOrientedSpeeds.vxMetersPerSecond, fieldOrientedSpeeds.vyMetersPerSecond,
            fieldOrientedSpeeds.omegaRadiansPerSecond, m_gyro.getRotation2d()),
            centerRotationMeters);
    }

    /**
     * Updates the swerve drive modules given a desired
     {@link edu.wpi.first.math.kinematics.ChassisSpeeds} object in the field coordinate
     system, assuming the robot's center as the center of rotation.
     *
     * @param fieldOrientedSpeeds The desired chassis speeds in the field coordinate system.
     */
    public void updateFieldOriented(ChassisSpeeds fieldOrientedSpeeds) {
        // 2023 WPILib: This can be greatly simplified with the new overload.
        update(ChassisSpeeds.fromFieldRelativeSpeeds(
            fieldOrientedSpeeds.vxMetersPerSecond, fieldOrientedSpeeds.vyMetersPerSecond,
            fieldOrientedSpeeds.omegaRadiansPerSecond, m_gyro.getRotation2d()));
    }

    /**
     * Validates that the motors in the current
     {@link org.frc4607.common.swerve.SwerveDriveModule} objects are connected to the robot and
     updates the kinematics and valid module list if any motors are found to be disconnected.
     *
     * @param modules A list containing all of the currently valid
     {@link org.frc4607.common.swerve.SwerveDriveModule} objects.
     * @return A new list containing only valid
     {@link org.frc4607.common.swerve.SwerveDriveModule} objects. {@code m_kinematics} will also
     be updated with this list if there are enough modules.
    */
    private List<SwerveDriveModule> validate(List<SwerveDriveModule> modules) {
        // Based on https://stackoverflow.com/a/14832470
        Stream<SwerveDriveModule> validModules = modules.stream()
            .filter((module) -> {
                return module.isDriveMotorConnected() && module.isTurnMotorConnected();
            });
        List<SwerveDriveModule> validModulesList = validModules.collect(Collectors.toList());
        if (validModulesList.size() > 1 && validModulesList.size() < modules.size()) {
            reconstructKinematics(validModulesList.stream());
        }
        return validModulesList;
    }

    /**
     * Reconstruct the {@link edu.wpi.first.math.kinematics.SwerveDriveKinematics} object given a
     * stream of valid modules. Designed to be used in {@code validate}.
     *
     * @param modules A stream of valid modules.
     */
    private void reconstructKinematics(Stream<SwerveDriveModule> modules) {
        Translation2d[] positions = (Translation2d[]) modules
            .map((module) -> {
                return module.getModuleLocation();
            })
            .toArray(Translation2d[]::new);

        System.out.println("Modules in kinematics constructor: " + positions.length);
        m_kinematicModuleCount = positions.length;
        m_kinematics = new SwerveDriveKinematics(positions);
        // m_odometry is null on first run
        if (m_odometry != null) {
            m_odometry = new SwerveDriveOdometry(m_kinematics, m_gyro.getRotation2d(),
                m_odometry.getPoseMeters());
        } else {
            m_odometry = new SwerveDriveOdometry(m_kinematics, m_gyro.getRotation2d());
        }
    }

    // Getters and setters for gyro and odometry

    public Rotation2d getRotation2d() {
        return m_gyro.getRotation2d();
    }

    public void resetGyro() {
        m_gyro.reset();
        m_odometry.resetPosition(m_odometry.getPoseMeters(), m_gyro.getRotation2d());
    }

    public void calibrateGyro() {
        m_gyro.calibrate();
        m_odometry.resetPosition(m_odometry.getPoseMeters(), m_gyro.getRotation2d());
    }

    public Pose2d getPose2d() {
        return m_odometry.getPoseMeters();
    }

    public void resetPosition(Pose2d pose) {
        m_odometry.resetPosition(pose, m_gyro.getRotation2d());
    }
}
