package org.frc4607.common.swerve;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revrobotics.CANSparkMax;

import org.frc4607.common.util.Zip;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.SwerveDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;

public class SwerveDrive {

    public class ModuleInfo {
        public SwerveDriveModule mModule;
        public Translation2d mLocation;

        public ModuleInfo(CANSparkMax drive, CANSparkMax turn, Translation2d location) {
            mModule = new SwerveDriveModule(drive, turn);
            mLocation = location;
        }
    }

    private final int mModuleGracePeriod;
    private List<Integer> mModuleErrorCount;

    private List<ModuleInfo> mActiveModules;
    private SwerveDriveKinematics mKinematics;


    public SwerveDrive(int gracePeriod, ModuleInfo... modules) {
        mActiveModules = List.of(modules);
        mModuleGracePeriod = gracePeriod;
        mModuleErrorCount = new ArrayList<Integer>() {};

        List<Translation2d> translation2ds = new ArrayList<Translation2d>();
        for (ModuleInfo moduleInfo : mActiveModules) {
            translation2ds.add(moduleInfo.mLocation);
        }

        mKinematics = new SwerveDriveKinematics((Translation2d[]) translation2ds.toArray());
    }

    public void update(ChassisSpeeds speeds) {
        SwerveModuleState[] states = mKinematics.toSwerveModuleStates(speeds);

        List<Map.Entry<ModuleInfo, SwerveModuleState>> validStates = validate(Zip.zip(mActiveModules, List.of(states)));

        for (Map.Entry<ModuleInfo, SwerveModuleState> pair : validStates ) {
            pair.getKey().mModule.set(pair.getValue());
        }
    }

    private List<Map.Entry<ModuleInfo, SwerveModuleState>> validate(List<Map.Entry<ModuleInfo, SwerveModuleState>> targetStates) {
        /* for (Map.Entry<ModuleInfo, SwerveModuleState> pair : targetStates ) {
            ModuleInfo module = pair.getKey();
            SwerveModuleState state = pair.getValue();


        }*/
        return targetStates;
    }
}
