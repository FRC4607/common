package org.frc4607.common.swerve;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.frc4607.common.util.Zip;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;

public class SwerveDrive {

    private static final Translation2d mCenter = new Translation2d(0, 0);

    private List<SwerveDriveModule> mActiveModules;
    private SwerveDriveKinematics mKinematics;


    public SwerveDrive(SwerveDriveModule... modules) {
        mActiveModules = List.of(modules);
        mKinematics = reconstructKinematics(mActiveModules.stream());
    }

    public void update(ChassisSpeeds speeds, Translation2d centerRotation) {
        List<SwerveDriveModule> validModules = validate(mActiveModules);

        SwerveModuleState[] states = mKinematics.toSwerveModuleStates(speeds, centerRotation);

        Zip.zip(validModules, List.of(states)).stream().forEach((pair) -> {
            pair.getKey().set(pair.getValue());
        });
    }

    public void update(ChassisSpeeds speeds) {
        update(speeds, mCenter);
    }

    private List<SwerveDriveModule> validate(List<SwerveDriveModule> modules) {
        // Based on https://stackoverflow.com/a/14832470
        Stream<SwerveDriveModule> validModules = modules.stream()
            .filter((module) -> {
                return module.isDriveMotorConnected() && module.isTurnMotorConnected();
            });
        if (validModules.count() < modules.size()) {
            mKinematics = reconstructKinematics(validModules);
        }
        return validModules.collect(Collectors.toList());
    }

    private SwerveDriveKinematics reconstructKinematics(Stream<SwerveDriveModule> modules) {
        Translation2d[] positions = (Translation2d[]) modules
            .map((module) -> {
                return module.getModuleLocation();
            })
            .toArray();

        return new SwerveDriveKinematics(positions);
    }
}
