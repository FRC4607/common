package org.frc4607.common.swerve;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;

public class SwerveDriveModule {
    private final SwerveMotorBase mDriveMotor;
    private final SwerveMotorBase mTurnMotor;
    private final Translation2d mModuleLocation;

    public SwerveDriveModule(SwerveMotorBase drive, SwerveMotorBase turn, Translation2d moduleLocation) {
        mDriveMotor = drive;
        mTurnMotor = turn;
        mModuleLocation = moduleLocation;
    }

    public void set(SwerveModuleState state) {
        SwerveModuleState newState = SwerveModuleState.optimize(state, mTurnMotor.getRotation2d());
        mDriveMotor.setTarget(newState.speedMetersPerSecond);
        mTurnMotor.setTarget(newState.angle.getDegrees());
    }

    public double getDriveMotorPosition() {
        return mDriveMotor.getEncoderPosition();
    }
    public double getDriveMotorVelocity() {
        return mDriveMotor.getEncoderVelocity();
    }
    public double getTurnMotorPosition() {
        return mTurnMotor.getEncoderPosition();
    }
    public double getTurnMotorVelocity() {
        return mTurnMotor.getEncoderVelocity();
    }

    public Translation2d getModuleLocation() {
        return mModuleLocation;
    }

    public boolean isDriveMotorConnected() {
        return mDriveMotor.isConnected();
    }
    public boolean isTurnMotorConnected() {
        return mTurnMotor.isConnected();
    }
}