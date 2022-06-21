package org.frc4607.common.swerve;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.frc4607.common.util.Zip;

/**
 * Handles the the kinematics and odometry of a coaxial swerve drive and provides methods to get 
 * information about the state of the swerve drive.
 */
public class SwerveDrive {

    private static final Translation2d mCenter = new Translation2d(0, 0);

    private List<SwerveDriveModule> m_activeModules;
    private SwerveDriveKinematics m_kinematics;

    /**
     * Constructs a new {@code SwerveDrive}.
     *
     * @param modules A list of {@link org.frc4607.common.swerve.SwerveDriveModule} objects.
     */
    public SwerveDrive(SwerveDriveModule... modules) {
        m_activeModules = List.of(modules);
        m_kinematics = reconstructKinematics(m_activeModules.stream());
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

        Zip.zip(validModules, List.of(states)).stream().forEach((pair) -> {
            pair.getKey().set(pair.getValue());
        });
    }

    /**
     * Updates the swerve drive modules given a desired
     {@link edu.wpi.first.math.kinematics.ChassisSpeeds} object, assuming the robot's center
     as the center of rotation.
     *
     * @param speeds The desired chassis speeds.
     */
    public void update(ChassisSpeeds speeds) {
        update(speeds, mCenter);
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
     be updated with this list.
    */
    private List<SwerveDriveModule> validate(List<SwerveDriveModule> modules) {
        // Based on https://stackoverflow.com/a/14832470
        Stream<SwerveDriveModule> validModules = modules.stream()
            .filter((module) -> {
                return module.isDriveMotorConnected() && module.isTurnMotorConnected();
            });
        if (validModules.count() < modules.size()) {
            m_kinematics = reconstructKinematics(validModules);
        }
        return validModules.collect(Collectors.toList());
    }

    /**
     * Reconstruct the {@link edu.wpi.first.math.kinematics.SwerveDriveKinematics} object given a
     * stream of valid modules. Designed to be used in {@code validate}.
     *
     * @param modules A stream of valid modules.
     * @return A new {@link edu.wpi.first.math.kinematics.SwerveDriveKinematics} made of the
     positions of the modules in the stream.
     */
    private SwerveDriveKinematics reconstructKinematics(Stream<SwerveDriveModule> modules) {
        Translation2d[] positions = (Translation2d[]) modules
            .map((module) -> {
                return module.getModuleLocation();
            })
            .toArray();

        return new SwerveDriveKinematics(positions);
    }
}
